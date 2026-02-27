package tn.esprit.controllers;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import javafx.application.Platform;
import tn.esprit.entities.User;
import tn.esprit.services.AuthService;
import tn.esprit.utils.NavigationManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;

public class GoogleOAuthController {

    // 🔴 REMPLACE AVEC TES NOUVEAUX IDENTIFIANTS

    private static final String CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID");
    private static final String password = System.getenv("GOOGLE_CLIENT_SECRET");
    private static final int PORT = 8081;
    private static final String REDIRECT_URI = "http://localhost:8081/callback";
    private final AuthService authService = new AuthService();
    private HttpServer server;
    private GoogleAuthorizationCodeFlow flow;

    public void startOAuthFlow() {
        try {
            // 1. Créer le flux d'autorisation
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    CLIENT_ID,
                    password,
                    Arrays.asList("email", "profile"))
                    .setAccessType("offline")
                    .build();

            // 2. Démarrer le serveur local pour le callback
            startLocalServer();

            // 3. Générer l'URL d'autorisation
            String authorizationUrl = flow.newAuthorizationUrl()
                    .setRedirectUri(REDIRECT_URI)
                    .build();

            // 4. Ouvrir le navigateur par défaut
            java.awt.Desktop.getDesktop().browse(URI.create(authorizationUrl));

            System.out.println("🌐 Navigateur ouvert pour l'authentification Google");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage du flux OAuth: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("🔥 CALLBACK RECU !!!");
            System.out.println("URI: " + exchange.getRequestURI());
            System.out.println("Méthode: " + exchange.getRequestMethod());

            String query = exchange.getRequestURI().getQuery();
            System.out.println("Query: " + query);

            String response = "";

            if (query != null && query.contains("code=")) {
                // Extraire le code d'autorisation
                String code = query.split("code=")[1].split("&")[0];

                // Répondre à la requête HTTP
                response = "<html><body><h2>✅ Authentification réussie !</h2>"
                        + "<p>Vous pouvez fermer cette fenêtre et retourner à l'application.</p></body></html>";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

                // Traiter le code dans un thread séparé
                System.out.println("🟢 Code extrait: " + code.substring(0, Math.min(20, code.length())) + "...");
                new Thread(() -> {
                    try {
                        System.out.println("🟢 AVANT appel de handleAuthorizationCode");
                        handleAuthorizationCode(code);
                        System.out.println("✅ APRÈS appel de handleAuthorizationCode (sans erreur)");
                    } catch (Exception e) {
                        System.err.println("❌ EXCEPTION CATCHÉE: " + e.getMessage());
                        e.printStackTrace();
                    } catch (Error er) {
                        System.err.println("❌ ERROR CATCHÉ: " + er.getMessage());
                        er.printStackTrace();
                    }
                }).start();


            } else {
                response = "<html><body><h2>❌ Erreur d'authentification</h2></body></html>";
                exchange.sendResponseHeaders(400, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }



    private void handleAuthorizationCode(String code) {
        System.out.println("🔥🔥🔥 handleAuthorizationCode EXÉCUTÉE !!!");
        System.out.println("🔄 Code reçu: " + code.substring(0, Math.min(20, code.length())) + "...");
        try {
            // ... reste du codeI
            // 5. Échanger le code contre un token
            TokenResponse tokenResponse = flow.newTokenRequest(code)
                    .setRedirectUri(REDIRECT_URI)
                    .execute();
            System.out.println("✅ Token reçu !");

            // 6. Créer les credentials
            Credential credential = flow.createAndStoreCredential(tokenResponse, null);
            System.out.println("✅ Credential créé");

            // 7. Récupérer les infos utilisateur
            Oauth2 oauth2 = new Oauth2.Builder(new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName("PidevConnexionGoogle")
                    .build();

            Userinfo userInfo = oauth2.userinfo().get().execute();
            System.out.println("✅ Infos utilisateur reçues");

            String email = userInfo.getEmail();
            String nom = userInfo.getFamilyName();
            String prenom = userInfo.getGivenName();
            String nomComplet = userInfo.getName();

            System.out.println("📧 Email: " + email);
            System.out.println("👤 Nom: " + (nom != null ? nom : nomComplet));
            System.out.println("👤 Prénom: " + (prenom != null ? prenom : ""));

            // 8. Connecter l'utilisateur
            Platform.runLater(() -> {
                connectUserWithGoogle(email, nom, prenom, nomComplet);
            });

        } catch (Exception e) {
            System.err.println("❌ Erreur détaillée: " + e.getMessage());
            e.printStackTrace();
            }
        }
    // 🔴 AJOUTE CETTE MÉTHODE DANS TA CLASSE (à côté des autres méthodes)
    private void stopServer() {
        if (server != null) {
            try {
                server.stop(0);
                System.out.println("🛑 Serveur de callback arrêté");
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de l'arrêt du serveur: " + e.getMessage());
            }
        }
    }
    private void startLocalServer() throws IOException {
        // Forcer la libération du port avant de démarrer
        forcePortRelease();

        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/callback", new CallbackHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("🖥️ Serveur de callback démarré sur http://localhost:" + PORT + "/callback");
    }
    private void forcePortRelease() {
        try {
            String portStr = String.valueOf(PORT);
            Process process = Runtime.getRuntime().exec("cmd /c netstat -ano | findstr :" + portStr);
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("LISTENING")) {
                    String[] parts = line.trim().split("\\s+");
                    String pid = parts[parts.length - 1];
                    System.out.println("🛑 Processus trouvé sur le port " + PORT + " (PID: " + pid + ")");
                    Runtime.getRuntime().exec("cmd /c taskkill /F /PID " + pid);
                    System.out.println("✅ Processus tué");

                    // 🔴 ATTENDRE 2 SECONDES QUE LE PORT SE LIBÈRE
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la libération du port: " + e.getMessage());
        }
    }

    private void connectUserWithGoogle(String email, String nom, String prenom, String nomComplet) {
        try {
            // Vérifier si l'utilisateur existe déjà
            User user = authService.getUserByEmail(email);

            if (user == null) {
                // Créer un nouvel utilisateur avec les infos Google
                user = new User();
                user.setEmail(email);
                user.setNom(nom != null ? nom : nomComplet);
                user.setPrenom(prenom != null ? prenom : "");
                user.setPassword(""); // Pas de mot de passe

                // Sauvegarder dans la base
                boolean created = authService.createUserFromGoogle(user);
                if (!created) {
                    System.err.println("❌ Impossible de créer l'utilisateur");
                    return;
                }

                // Récupérer l'utilisateur avec son ID
                user = authService.getUserByEmail(email);
            }

            if (user != null) {
                // Rediriger vers l'accueil
                NavigationManager.loadView("/fxml/accueil.fxml");
                System.out.println("✅ Utilisateur connecté via Google: " + email);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }




}