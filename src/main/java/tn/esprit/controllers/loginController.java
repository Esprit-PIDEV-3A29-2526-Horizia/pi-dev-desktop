package tn.esprit.controllers;

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
            if (faceServiceAvailable) {
                proposeFaceRegistration(user);
            } else {
                redirectToAccueilClient(user);
            }
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
        boolean emailExists = authService.checkEmailExists(email);

        if (emailExists) {
            showMessage("❌ Mot de passe incorrect !", "error");
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


}