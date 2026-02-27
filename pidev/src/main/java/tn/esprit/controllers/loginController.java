package tn.esprit.controllers;

import com.github.sarxos.webcam.Webcam;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.User;
import tn.esprit.services.AuthService;
import tn.esprit.services.FaceRecognitionService;
import tn.esprit.utils.EmailService;
import tn.esprit.utils.NavigationManager;

import java.io.IOException;
import java.util.Optional;

public class loginController {

    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnLogin;
    @FXML
    private Label lblMessage;
    @FXML
    private Hyperlink forgotPasswordLink;
    @FXML
    private Button btnFaceLogin;

    private AuthService authService = new AuthService();
    private FaceRecognitionService faceService;
    private boolean faceServiceAvailable = false;
    // 🔴 NOUVELLES VARIABLES
    private int failedAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private Webcam securityWebcam;  // Webcam pour la capture de sécurité

    @FXML
    public void initialize() {
        txtPassword.setOnAction(event -> handleLogin());
        btnLogin.setDefaultButton(true);
        initFaceRecognitionService();
    }

    private void initFaceRecognitionService() {
        try {
            faceService = new FaceRecognitionService();
            faceServiceAvailable = true;
            System.out.println("✅ Service de reconnaissance faciale initialisé");
        } catch (Exception e) {
            faceServiceAvailable = false;
            System.err.println("❌ Service de reconnaissance faciale non disponible: " + e.getMessage());
            if (btnFaceLogin != null) {
                btnFaceLogin.setDisable(true);
                btnFaceLogin.setText("📷 Face ID (non disponible)");
                btnFaceLogin.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666;");
            }
        }
    }

    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Veuillez remplir tous les champs", "error");
            return;
        }

        btnLogin.setDisable(true);
        btnLogin.setText("Connexion en cours...");

        try {
            User user = authService.login(email, password);
            if (user != null) {
                handleSuccessfulLogin(user);
            } else {
                handleFailedLogin(email);
            }
        } catch (Exception e) {
            showMessage("Erreur de connexion: " + e.getMessage(), "error");
            e.printStackTrace();
            btnLogin.setDisable(false);
            btnLogin.setText("Se connecter");
        }
    }

    @FXML
    private void handleFaceLogin() {
        if (!faceServiceAvailable) {
            showMessage("Service de reconnaissance faciale non disponible", "error");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face-login.fxml"));
            Parent root = loader.load();
            FaceLoginController faceController = loader.getController();
            faceController.setLoginController(this);

            Stage faceStage = new Stage();
            faceStage.setTitle("Connexion par reconnaissance faciale");
            faceStage.setScene(new Scene(root));
            faceStage.setResizable(false);
            faceStage.centerOnScreen();
            faceStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Erreur lors de l'ouverture de la reconnaissance faciale", "error");
        }
    }

    public void onFaceRecognized(String email) {
        try {
            User user = authService.getUserByEmail(email);
            if (user != null) {
                Platform.runLater(() -> {
                    showMessage("✅ Reconnaissance faciale réussie! Connexion...", "success");
                    handleSuccessfulLogin(user);
                });
            } else {
                Platform.runLater(() -> {
                    showMessage("❌ Utilisateur non trouvé", "error");
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                showMessage("Erreur lors de la récupération de l'utilisateur", "error");
            });
        }
    }

    private void handleSuccessfulLogin(User user) {
        String userType = user.getType();

        if ("ADMIN".equals(userType)) {
            showMessage("Connexion réussie ! Bienvenue " + user.getNom(), "success");
            redirectToAdminDashboard(user);
        } else if ("CLIENT".equals(userType)) {
            showMessage("Connexion réussie ! Bienvenue " + user.getNom(), "success");

                redirectToAccueilClient(user);

        } else {
            showMessage("Type d'utilisateur inconnu", "error");
            btnLogin.setDisable(false);
            btnLogin.setText("Se connecter");
        }
    }

    private void proposeFaceRegistration(User user) {
        boolean hasFace = authService.hasFaceRegistered(user.getId());

        if (hasFace) {
            redirectToAccueilClient(user);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reconnaissance faciale");
        alert.setHeaderText("Voulez-vous activer la connexion par reconnaissance faciale ?");
        alert.setContentText("Cela vous permettra de vous connecter plus rapidement la prochaine fois en utilisant votre visage.");

        ButtonType buttonYes = new ButtonType("Oui, enregistrer");
        ButtonType buttonNo = new ButtonType("Non, pas maintenant");
        ButtonType buttonLater = new ButtonType("Me le demander plus tard", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonLater);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonYes) {
            openFaceRegistration(user);
        } else {
            redirectToAccueilClient(user);
        }
    }

    private void openFaceRegistration(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face-register.fxml"));
            Parent root = loader.load();

            FaceRegisterController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Enregistrement facial");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

            Stage loginStage = (Stage) txtEmail.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            redirectToAccueilClient(user);
        }
    }

    private void handleFailedLogin(String email) {
        failedAttempts++;
        System.out.println("⚠️ Tentative échouée #" + failedAttempts + " pour: " + email);

        // 🔴 SI 3 ÉCHECS, DÉCLENCHER LA CAPTURE
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            captureIntruder(email);
            // Réinitialiser le compteur après capture
            failedAttempts = 0;
        }

        boolean emailExists = authService.checkEmailExists(email);

        if (emailExists) {
            showMessage("❌ Mot de passe incorrect ! Tentative " + failedAttempts + "/" + MAX_FAILED_ATTEMPTS, "error");
            btnLogin.setDisable(false);
            btnLogin.setText("Se connecter");
        } else {
            showMessage("❌ Aucun compte trouvé avec cet email. Redirection vers l'inscription...", "error");
            btnLogin.setDisable(true);
            btnLogin.setText("Redirection...");

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> goToSignUp());
            pause.play();
        }
    }

    private void captureIntruder(String email) {
        System.out.println("📸 3 échecs - Capture de l'intrus!");

        new Thread(() -> {
            try {
                // Initialiser la webcam
                securityWebcam = Webcam.getDefault();
                if (securityWebcam == null) {
                    System.err.println("❌ Impossible d'initialiser la webcam de sécurité");
                    return;
                }

                securityWebcam.open();

                // Attendre que la webcam s'initialise
                Thread.sleep(1000);

                // Capturer l'image
                java.awt.image.BufferedImage bufferedImage = securityWebcam.getImage();

                if (bufferedImage != null) {
                    // Sauvegarder l'image
                    String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                    String filename = "intruder_" + email.replace("@", "_") + "_" + timestamp + ".jpg";
                    String filepath = "data/intruders/" + filename;

                    // Créer le dossier si nécessaire
                    java.nio.file.Files.createDirectories(java.nio.file.Paths.get("data/intruders"));

                    // Sauvegarder l'image
                    javax.imageio.ImageIO.write(bufferedImage, "jpg", new java.io.File(filepath));
                    System.out.println("✅ Image sauvegardée: " + filepath);

                    // Envoyer l'email à l'admin
                    sendIntruderAlert(email, filepath);
                }

                // Fermer la webcam
                securityWebcam.close();

            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la capture de sécurité: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    private void sendIntruderAlert(String email, String imagePath) {
        try {
            String adminEmail = "khalilbenlahmer@gmail.com";
            String subject = "🚨 ALERTE SÉCURITÉ - Tentatives de connexion suspectes";

            // Corps du message pour l'admin
            StringBuilder adminBody = new StringBuilder();
            adminBody.append("<h2>🚨 Alerte de sécurité</h2>");
            adminBody.append("<p><strong>3 tentatives de connexion échouées</strong> ont été détectées.</p>");
            adminBody.append("<p><strong>Email concerné :</strong> ").append(email).append("</p>");
            adminBody.append("<p><strong>Date :</strong> ").append(new java.util.Date()).append("</p>");
            adminBody.append("<p><strong>IP/Machine :</strong> ").append(java.net.InetAddress.getLocalHost().getHostName()).append("</p>");
            adminBody.append("<p>Une capture de la personne a été jointe à cet email.</p>");

            // Envoyer l'email à l'admin avec la photo
            EmailService.sendEmail(adminEmail, subject + " - ADMIN", adminBody.toString(), imagePath);

            // 🔴 VÉRIFIER SI L'EMAIL EXISTE DANS LA BASE
            boolean emailExists = authService.checkEmailExists(email);

            if (emailExists) {
                // Envoyer une alerte à l'utilisateur concerné
                String userSubject = "🔐 ALERTE - Tentatives de connexion sur votre compte";
                String userBody = "🔐 ALERTE DE SÉCURITÉ\n" +
                        "=====================\n\n" +
                        "3 tentatives de connexion échouées ont été détectées sur votre compte.\n\n" +
                        "Détails :\n" +
                        "- Date : " + new java.util.Date() + "\n" +
                        "- IP/Machine : " + java.net.InetAddress.getLocalHost().getHostName() + "\n\n" +
                        "Si ce n'était pas vous, nous vous recommandons de :\n" +
                        "- Changer immédiatement votre mot de passe\n" +
                        "- Activer la reconnaissance faciale\n" +
                        "- Contacter l'administrateur\n\n" +
                        "Si vous êtes à l'origine de ces tentatives, ignorez cet email.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe de sécurité";
                // Envoyer sans photo à l'utilisateur (pour ne pas l'effrayer avec sa propre photo 😅)
                EmailService.sendEmail(email, userSubject, userBody.toString(), null);

                System.out.println("✅ Alertes envoyées à l'admin et à l'utilisateur: " + email);
            } else {
                System.out.println("ℹ️ Email " + email + " n'existe pas dans la base - alerte uniquement à l'admin");
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void redirectToAccueilClient(User user) {
        try {
            System.out.println("Redirection vers l'accueil client pour: " + user.getEmail());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/accueil.fxml"));
            Parent root = loader.load();

            AccueilController accueilController = loader.getController();
            if (accueilController != null) {
                accueilController.setCurrentUser(user);
            }

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            showMessage("Erreur de redirection vers l'accueil: " + e.getMessage(), "error");
            e.printStackTrace();
            tryFallbackToUserFrontEnd(user);
        }
    }

    private void tryFallbackToUserFrontEnd(User user) {
        try {
            System.out.println("Fallback vers UserFrontEnd.fxml");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserFrontEnd.fxml"));
            Parent root = loader.load();

            UserFrontEndController controller = loader.getController();
            controller.setCurrentUser(user);

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Interface Utilisateur");
            stage.setMaximized(true);
            stage.centerOnScreen();

        } catch (IOException ex) {
            showMessage("Erreur critique: impossible de charger l'interface", "error");
            ex.printStackTrace();
            btnLogin.setDisable(false);
            btnLogin.setText("Se connecter");
        }
    }

    private void redirectToAdminDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setCurrentUser(user);

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Tableau de bord Administrateur");
            stage.setMaximized(true);
            stage.centerOnScreen();

        } catch (IOException e) {
            showMessage("Erreur de redirection vers le dashboard", "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSignUp() {
        try {
            FXMLLoader loader = NavigationManager.loadViewWithController("/fxml/SignUp.fxml");
            Parent root = loader.load();

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            System.out.println("✅ Redirection vers la page d'inscription");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Erreur lors du chargement de la page d'inscription", "error");
        }
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        switch (type) {
            case "error":
                lblMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                break;
            case "success":
                lblMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                break;
            default:
                lblMessage.setStyle("-fx-text-fill: #3D94CA; -fx-font-weight: bold;");
                break;
        }
        lblMessage.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> lblMessage.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleForgotPassword() {
        System.out.println("🔐 Redirection vers la page de réinitialisation de mot de passe");

        // Vérifier si le fichier existe
        java.net.URL url = getClass().getResource("/fxml/ForgotPassword.fxml");
        System.out.println("URL du fichier: " + url);

        if (url == null) {
            showMessage("Fichier ForgotPassword.fxml non trouvé!", "error");
            return;
        }

        NavigationManager.loadView("/fxml/ForgotPassword.fxml");
    }

    @FXML
    private void handleGoogleLogin() {
        new Thread(() -> {
            GoogleOAuthController googleAuth = new GoogleOAuthController();
            googleAuth.startOAuthFlow();
        }).start();
    }
}