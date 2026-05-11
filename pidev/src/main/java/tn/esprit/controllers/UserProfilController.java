package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import tn.esprit.entities.User;
import tn.esprit.services.AuthService;
import tn.esprit.services.UserService;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

public class UserProfilController {

    @FXML
    private HBox userBox;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userIconLabel;

    @FXML
    private Label fullNameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label profileInitials;
    @FXML
    private Circle profileImageCircle;
    @FXML
    private Label totalReservationsLabel;
    @FXML
    private Label memberSinceLabel;

    @FXML
    private TextField prenomField;
    @FXML
    private TextField nomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField adresseField;

    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button btnSauvegarder;
    @FXML
    private Button btnAnnuler;
    @FXML
    private Button btnModifierPhoto;

    @FXML
    private Button btnAccueil;
    @FXML
    private Button btnNosLogements;
    @FXML
    private Button btnMesReservations;

    private User currentUser;
    private AuthService authService;
    private UserService userService;

    @FXML
    public void initialize() {
        // Initialiser les services
        authService = new AuthService();
        userService = new UserService();  // à créer si nécessaire, ou utiliser AuthService

        // Récupérer l'utilisateur connecté
        currentUser = SessionManager.getCurrentUser();

        // Configurer la navigation
        setupNavigation();

        // Charger les données de l'utilisateur
        chargerDonneesUtilisateur();

        // Configurer les actions des boutons
        setupActions();

        System.out.println("ProfileController initialisé pour: " +
                (currentUser != null ? currentUser.getEmail() : "non connecté"));
    }

    private void setupNavigation() {
        if (btnAccueil != null) {
            btnAccueil.setOnAction(e -> NavigationManager.loadView("/fxml/accueil.fxml", "Catalogue"));
        }

        if (btnNosLogements != null) {
            btnNosLogements.setOnAction(e -> NavigationManager.loadView("/fxml/accueil.fxml", "Catalogue"));
        }

        if (btnMesReservations != null) {
            btnMesReservations.setOnAction(e -> NavigationManager.loadView("/fxml/mesreservations.fxml", "Catalogue"));
        }

        if (SessionManager.isLoggedIn() && currentUser != null) {
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            }
            if (userBox != null) {
                userBox.setCursor(javafx.scene.Cursor.HAND);
                userBox.setOnMouseClicked(e -> chargerDonneesUtilisateur());
            }
        }
    }

    private void chargerDonneesUtilisateur() {
        if (currentUser != null) {
            fullNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            emailLabel.setText(currentUser.getEmail());

            String initials = "";
            if (currentUser.getPrenom() != null && !currentUser.getPrenom().isEmpty()) {
                initials += currentUser.getPrenom().charAt(0);
            }
            if (currentUser.getNom() != null && !currentUser.getNom().isEmpty()) {
                initials += currentUser.getNom().charAt(0);
            }
            profileInitials.setText(initials.toUpperCase());

            prenomField.setText(currentUser.getPrenom());
            nomField.setText(currentUser.getNom());
            emailField.setText(currentUser.getEmail());
            telephoneField.setText(currentUser.getTelephone());
            adresseField.setText(currentUser.getAddresse());

            // Exemple de stats – à remplacer par les vraies données
            totalReservationsLabel.setText("12");
            memberSinceLabel.setText("Janvier 2024");
        }
    }

    private void setupActions() {
        btnSauvegarder.setOnAction(e -> sauvegarderModifications());
        btnAnnuler.setOnAction(e -> annulerModifications());
        btnModifierPhoto.setOnAction(e -> modifierPhoto());
    }

    private void sauvegarderModifications() {
        if (currentUser == null) return;

        // Validation
        if (prenomField.getText().isEmpty() || nomField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les champs prénom, nom et email sont obligatoires");
            return;
        }

        // Mise à jour des champs dans l'objet
        currentUser.setPrenom(prenomField.getText());
        currentUser.setNom(nomField.getText());
        currentUser.setEmail(emailField.getText());
        currentUser.setTelephone(telephoneField.getText());
        currentUser.setAddresse(adresseField.getText());

        // Persistance en base
        boolean updated = authService.updateUserProfile(currentUser);
        if (!updated) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour le profil. Vérifiez la base de données.");
            return;
        }

        // Gestion du changement de mot de passe si demandé
        if (!newPasswordField.getText().isEmpty()) {
            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Les nouveaux mots de passe ne correspondent pas");
                return;
            }
            if (currentPasswordField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer votre mot de passe actuel");
                return;
            }
            boolean passwordChanged = authService.changePassword(
                    currentUser.getId(),
                    currentPasswordField.getText(),
                    newPasswordField.getText()
            );
            if (!passwordChanged) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Mot de passe actuel incorrect ou échec du changement");
                return;
            }
        }

        // Mettre à jour la session
        SessionManager.setCurrentUser(currentUser);

        // Rafraîchir l'affichage
        chargerDonneesUtilisateur();

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Vos informations ont été mises à jour");

        // Effacer les champs de mot de passe
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void annulerModifications() {
        chargerDonneesUtilisateur();
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void modifierPhoto() {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fonctionnalité à venir");
    }

    @FXML
    private void retourAccueil() {
        NavigationManager.loadView("/fxml/accueil.fxml", "Catalogue");
    }

    @FXML
    private void voirMesReservations() {
        NavigationManager.loadView("/fxml/mesreservations.fxml", "Catalogue");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}