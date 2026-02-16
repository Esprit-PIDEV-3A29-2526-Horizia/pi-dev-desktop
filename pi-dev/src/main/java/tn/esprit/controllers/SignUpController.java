package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.entities.Profil;
import tn.esprit.services.ServiceUser;
import tn.esprit.services.ServiceProfil;
import tn.esprit.utils.ValidationUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class SignUpController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnSignUp;
    @FXML private Label lblMessage;

    private ServiceUser serviceUser = new ServiceUser();
    private ServiceProfil serviceProfil = new ServiceProfil();

    @FXML
    public void initialize() {
        txtConfirmPassword.setOnAction(event -> handleSignUp());
        btnSignUp.setDefaultButton(true);
        setupValidationListeners();
    }

    private void setupValidationListeners() {
        // Email
        txtEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtEmail.getText().isEmpty()) {
                if (!ValidationUtil.isValidEmail(txtEmail.getText())) {
                    showTemporaryMessage("⚠️ Format email invalide", "warning");
                    ValidationUtil.setFieldStyle(txtEmail, false);
                } else {
                    ValidationUtil.setFieldStyle(txtEmail, true);
                }
            }
        });

        // Téléphone
        txtTelephone.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtTelephone.getText().isEmpty()) {
                if (!ValidationUtil.isValidPhone(txtTelephone.getText())) {
                    showTemporaryMessage("⚠️ Téléphone: 8 chiffres requis", "warning");
                    ValidationUtil.setFieldStyle(txtTelephone, false);
                } else {
                    ValidationUtil.setFieldStyle(txtTelephone, true);
                }
            }
        });

        // Vérification mot de passe
        txtConfirmPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !txtPassword.getText().isEmpty()) {
                if (!newVal.equals(txtPassword.getText())) {
                    ValidationUtil.setFieldStyle(txtConfirmPassword, false);
                } else {
                    ValidationUtil.setFieldStyle(txtConfirmPassword, true);
                }
            }
        });
    }

    @FXML
    private void handleSignUp() {
        resetFieldStyles();

        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String telephone = txtTelephone.getText().trim();
        String adresse = txtAdresse.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        // VALIDATIONS
        if (nom.isEmpty()) {
            showMessage("❌ Le nom est obligatoire", "error");
            ValidationUtil.setFieldStyle(txtNom, false);
            txtNom.requestFocus();
            return;
        }

        if (!ValidationUtil.isValidName(nom)) {
            showMessage("❌ Nom invalide (min 2 caractères)", "error");
            ValidationUtil.setFieldStyle(txtNom, false);
            txtNom.requestFocus();
            return;
        }

        if (prenom.isEmpty()) {
            showMessage("❌ Le prénom est obligatoire", "error");
            ValidationUtil.setFieldStyle(txtPrenom, false);
            txtPrenom.requestFocus();
            return;
        }

        if (!ValidationUtil.isValidName(prenom)) {
            showMessage("❌ Prénom invalide (min 2 caractères)", "error");
            ValidationUtil.setFieldStyle(txtPrenom, false);
            txtPrenom.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            showMessage("❌ L'email est obligatoire", "error");
            ValidationUtil.setFieldStyle(txtEmail, false);
            txtEmail.requestFocus();
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            showMessage("❌ Format email invalide", "error");
            ValidationUtil.setFieldStyle(txtEmail, false);
            txtEmail.requestFocus();
            return;
        }

        if (telephone.isEmpty()) {
            showMessage("❌ Le téléphone est obligatoire", "error");
            ValidationUtil.setFieldStyle(txtTelephone, false);
            txtTelephone.requestFocus();
            return;
        }

        if (!ValidationUtil.isValidPhone(telephone)) {
            showMessage("❌ Téléphone: 8 chiffres requis", "error");
            ValidationUtil.setFieldStyle(txtTelephone, false);
            txtTelephone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showMessage("❌ Le mot de passe est obligatoire", "error");
            ValidationUtil.setFieldStyle(txtPassword, false);
            txtPassword.requestFocus();
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            showMessage("❌ Mot de passe: min 6 caractères", "error");
            ValidationUtil.setFieldStyle(txtPassword, false);
            txtPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            showMessage("❌ Confirmez votre mot de passe", "error");
            ValidationUtil.setFieldStyle(txtConfirmPassword, false);
            txtConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("❌ Les mots de passe ne correspondent pas", "error");
            ValidationUtil.setFieldStyle(txtPassword, false);
            ValidationUtil.setFieldStyle(txtConfirmPassword, false);
            txtPassword.requestFocus();
            return;
        }

        // Vérifier si l'email existe déjà
        try {
            if (serviceUser.emailExiste(email)) {
                showMessage("❌ Cet email est déjà utilisé", "error");
                ValidationUtil.setFieldStyle(txtEmail, false);
                txtEmail.requestFocus();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("❌ Erreur de vérification", "error");
            return;
        }

        btnSignUp.setDisable(true);
        btnSignUp.setText("Création du compte...");

        try {
            // Récupérer le profil CLIENT par défaut
            Profil profilClient = getProfilClient();

            if (profilClient == null) {
                showMessage("❌ Erreur: Profil CLIENT non trouvé", "error");
                btnSignUp.setDisable(false);
                btnSignUp.setText("S'inscrire");
                return;
            }

            // Créer l'utilisateur
            User newUser = new User();
            newUser.setNom(nom);
            newUser.setPrenom(prenom);
            newUser.setEmail(email);
            newUser.setPassword(password);
            newUser.setTelephone(telephone);
            newUser.setAddresse(adresse);
            newUser.setProfil(profilClient);  // Associer le profil CLIENT

            serviceUser.ajouter(newUser);

            showMessage("✅ Compte créé avec succès!", "success");

            // Rediriger vers login après délai
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        goToLogin();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("❌ Erreur: " + e.getMessage(), "error");
            btnSignUp.setDisable(false);
            btnSignUp.setText("S'inscrire");
        }
    }

    /**
     * Récupère le profil CLIENT (le crée si nécessaire)
     */
    private Profil getProfilClient() throws SQLException {
        // Chercher un profil CLIENT existant
        List<Profil> profils = serviceProfil.rechercherParType("CLIENT");
        if (profils != null && !profils.isEmpty()) {
            return profils.get(0);
        }

        // Si pas de profil CLIENT, en créer un
        Profil nouveauProfil = new Profil("CLIENT", "ACTIF");
        serviceProfil.ajouter(nouveauProfil);

        // Récupérer le profil créé
        profils = serviceProfil.rechercherParType("CLIENT");
        if (profils != null && !profils.isEmpty()) {
            return profils.get(0);
        }

        return null;
    }

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetFieldStyles() {
        ValidationUtil.setFieldStyle(txtNom, true);
        ValidationUtil.setFieldStyle(txtPrenom, true);
        ValidationUtil.setFieldStyle(txtEmail, true);
        ValidationUtil.setFieldStyle(txtTelephone, true);
        ValidationUtil.setFieldStyle(txtPassword, true);
        ValidationUtil.setFieldStyle(txtConfirmPassword, true);
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        if ("error".equals(type)) {
            lblMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        } else if ("warning".equals(type)) {
            lblMessage.setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
        } else {
            lblMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        }
    }

    private void showTemporaryMessage(String message, String type) {
        String originalMessage = lblMessage.getText();
        String originalStyle = lblMessage.getStyle();

        showMessage(message, type);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    lblMessage.setText(originalMessage);
                    lblMessage.setStyle(originalStyle);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}