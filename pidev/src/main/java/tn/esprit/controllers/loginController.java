package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.AuthService;

import java.io.IOException;

public class loginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblMessage;

    private AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Permettre la connexion avec la touche Entrée
        txtPassword.setOnAction(event -> handleLogin());
        btnLogin.setDefaultButton(true);
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
                if ("ADMIN".equals(user.getType())) {
                    showMessage("Connexion réussie ! Bienvenue " + user.getNom(), "success");
                    redirectToAdminDashboard(user);

                } else if ("CLIENT".equals(user.getType())) {
                    showMessage("Connexion réussie ! Bienvenue " + user.getNom(), "success");
                    // MODIFICATION ICI : Rediriger vers accueil.fxml au lieu de UserFrontEnd.fxml
                    redirectToAccueilClient(user);

                } else {
                    showMessage("Type d'utilisateur inconnu", "error");
                    btnLogin.setDisable(false);
                    btnLogin.setText("Se connecter");
                }

            } else {
                // Si l'utilisateur n'existe pas → rediriger vers SignUp
                showMessage("Utilisateur inexistant. Création de compte...", "info");
                goToSignUp();
            }

        } catch (Exception e) {
            showMessage("Erreur de connexion: " + e.getMessage(), "error");
            e.printStackTrace();
            btnLogin.setDisable(false);
            btnLogin.setText("Se connecter");
        }
    }

    /**
     * NOUVELLE MÉTHODE : Redirige vers le nouvel accueil client moderne
     */
    private void redirectToAccueilClient(User user) {
        try {
            System.out.println("Redirection vers l'accueil client moderne pour: " + user.getEmail());

            // Charger le nouveau fichier accueil.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/accueil.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur au contrôleur de l'accueil
            AccueilController accueilController = loader.getController();
            if (accueilController != null) {
                accueilController.setCurrentUser(user);
            }

            // Changer la scène
            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil - Trouvez votre logement idéal");
            stage.setMaximized(true);
            stage.centerOnScreen();
            stage.show();

            System.out.println("Redirection réussie vers l'accueil client");

        } catch (IOException e) {
            showMessage("Erreur de redirection vers l'accueil: " + e.getMessage(), "error");
            e.printStackTrace();

            // Fallback : essayer l'ancien UserFrontEnd si le nouveau n'existe pas
            tryFallbackToUserFrontEnd(user);
        }
    }

    /**
     * Méthode de secours si accueil.fxml n'existe pas
     */
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

    /**
     * Méthode existante pour le dashboard admin
     */
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

    /**
     * Méthode existante pour l'inscription
     */
    @FXML
    private void goToSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignUp.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Erreur lors du chargement de la page d'inscription", "error");
        }
    }

    /**
     * Affiche les messages à l'utilisateur
     */
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

        // Faire disparaître le message après 5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> lblMessage.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}