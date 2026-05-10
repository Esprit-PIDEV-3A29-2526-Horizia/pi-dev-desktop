package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import tn.esprit.entities.User;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

public class UserProfilController {

    // Navbar partagée
    @FXML private NavbarController navbarController;

    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label profileInitials;
    @FXML private Circle profileImageCircle;
    @FXML private Label totalReservationsLabel;
    @FXML private Label memberSinceLabel;

    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Button btnSauvegarder;
    @FXML private Button btnAnnuler;
    @FXML private Button btnModifierPhoto;

    private User currentUser;

    @FXML
    public void initialize() {
        // Récupérer l'utilisateur connecté
        currentUser = SessionManager.getCurrentUser();

        // Mettre à jour la navbar
        if (navbarController != null) {
            navbarController.updateUserInfo();
        }

        // Charger les données de l'utilisateur
        chargerDonneesUtilisateur();

        // Configurer les actions des boutons
        setupActions();

        System.out.println("UserProfilController initialisé pour: " +
                (currentUser != null ? currentUser.getEmail() : "non connecté"));
    }

    private void chargerDonneesUtilisateur() {
        if (currentUser != null) {
            // Mettre à jour les labels
            fullNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            emailLabel.setText(currentUser.getEmail());

            // Initiales pour le cercle
            String initials = "";
            if (currentUser.getPrenom() != null && !currentUser.getPrenom().isEmpty()) {
                initials += currentUser.getPrenom().charAt(0);
            }
            if (currentUser.getNom() != null && !currentUser.getNom().isEmpty()) {
                initials += currentUser.getNom().charAt(0);
            }
            profileInitials.setText(initials.toUpperCase());

            // Remplir les champs du formulaire
            prenomField.setText(currentUser.getPrenom());
            nomField.setText(currentUser.getNom());
            emailField.setText(currentUser.getEmail());
            telephoneField.setText(currentUser.getTelephone());
            adresseField.setText(currentUser.getAddresse());

            // Statistiques (à adapter selon votre logique métier)
            totalReservationsLabel.setText("0");
            memberSinceLabel.setText("2024");
        }
    }

    private void setupActions() {
        btnSauvegarder.setOnAction(e -> sauvegarderModifications());
        btnAnnuler.setOnAction(e -> annulerModifications());
        btnModifierPhoto.setOnAction(e -> modifierPhoto());
    }

    private void sauvegarderModifications() {
        if (currentUser == null) return;

        // Validation des champs
        if (prenomField.getText().isEmpty() || nomField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Erreur", "Les champs prénom, nom et email sont obligatoires");
            return;
        }

        // Vérification du mot de passe si modifié
        if (!newPasswordField.getText().isEmpty()) {
            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showAlert("Erreur", "Les nouveaux mots de passe ne correspondent pas");
                return;
            }
        }

        // Mettre à jour l'objet User
        currentUser.setPrenom(prenomField.getText());
        currentUser.setNom(nomField.getText());
        currentUser.setEmail(emailField.getText());
        currentUser.setTelephone(telephoneField.getText());
        currentUser.setAddresse(adresseField.getText());

        // Mettre à jour la session
        SessionManager.setCurrentUser(currentUser);

        // Rafraîchir la navbar
        if (navbarController != null) {
            navbarController.updateUserInfo();
        }

        // Mettre à jour l'affichage
        chargerDonneesUtilisateur();

        showAlert("Succès", "Vos informations ont été mises à jour");

        // Effacer les champs de mot de passe
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void annulerModifications() {
        // Recharger les données originales
        chargerDonneesUtilisateur();
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void modifierPhoto() {
        showAlert("Info", "Fonctionnalité à venir");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}