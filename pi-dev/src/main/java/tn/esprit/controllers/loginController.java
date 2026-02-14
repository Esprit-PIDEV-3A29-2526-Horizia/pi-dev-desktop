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

        // Valeurs de test pour le développement (à retirer en production)
        // txtEmail.setText("admin@admin.com");
        // txtPassword.setText("admin123");
    }

    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Veuillez remplir tous les champs", "error");
            return;
        }

        // Désactiver le bouton pendant la tentative
        btnLogin.setDisable(true);
        btnLogin.setText("Connexion en cours...");

        try {
            // Tentative de connexion
            User user = authService.login(email, password);

            if (user != null) {
                // Vérifier si c'est bien un administrateur
                if ("ADMIN".equals(user.getType())) {
                    showMessage("Connexion réussie ! Bienvenue " + user.getNom(), "success");

                    // Rediriger vers le dashboard admin
                    redirectToAdminDashboard(user);

                } else {
                    showMessage("Accès réservé aux administrateurs", "error");
                    btnLogin.setDisable(false);
                    btnLogin.setText("Se connecter");
                }
            } else {
                showMessage("Email ou mot de passe incorrect", "error");
                btnLogin.setDisable(false);
                btnLogin.setText("Se connecter");
            }

        } catch (Exception e) {
            showMessage("Erreur de connexion: " + e.getMessage(), "error");
            e.printStackTrace();
            btnLogin.setDisable(false);
            btnLogin.setText("Se connecter");
        }
    }

    private void redirectToAdminDashboard(User user) {
        try {
            // Charger le dashboard admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur connecté au contrôleur du dashboard
            AdminDashboardController controller = loader.getController();
            controller.setCurrentUser(user);

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Tableau de bord Administrateur");
            stage.setMaximized(true);  // Ouvrir en plein écran
            stage.centerOnScreen();

        } catch (IOException e) {
            showMessage("Erreur de redirection vers le dashboard", "error");
            e.printStackTrace();
        }
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        if ("error".equals(type)) {
            lblMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        } else {
            lblMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        }
    }
}