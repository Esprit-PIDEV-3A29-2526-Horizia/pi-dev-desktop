package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import tn.esprit.entities.User;
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

    @FXML
    public void initialize() {
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
        // Style du bouton actif (aucun sur cette page)

        if (btnAccueil != null) {
            btnAccueil.setOnAction(e -> NavigationManager.loadView("/fxml/accueil.fxml"));
        }

        if (btnNosLogements != null) {
            btnNosLogements.setOnAction(e -> NavigationManager.loadView("/fxml/accueil.fxml"));
        }

        if (btnMesReservations != null) {
            btnMesReservations.setOnAction(e -> NavigationManager.loadView("/fxml/mesreservations.fxml"));
        }

        // Gestion de l'affichage utilisateur
        if (SessionManager.isLoggedIn() && currentUser != null) {
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            }

            // Rendre userBox cliquable pour rester sur le profil (ou actualiser)
            if (userBox != null) {
                userBox.setCursor(javafx.scene.Cursor.HAND);
                userBox.setOnMouseClicked(e -> {
                    // Déjà sur le profil, on peut recharger si nécessaire
                    chargerDonneesUtilisateur();
                });
            }
        }
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
            totalReservationsLabel.setText("12"); // À remplacer par le vrai nombre
            memberSinceLabel.setText("Janvier 2024"); // À remplacer par la vraie date
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
            // Ici, vous appelleriez votre service pour changer le mot de passe
            // authService.changePassword(currentUser.getId(), currentPasswordField.getText(), newPasswordField.getText());
        }

        // Mettre à jour l'objet User
        currentUser.setPrenom(prenomField.getText());
        currentUser.setNom(nomField.getText());
        currentUser.setEmail(emailField.getText());
        currentUser.setTelephone(telephoneField.getText());
        currentUser.setAddresse(adresseField.getText());

        // Ici, vous appelleriez votre service pour sauvegarder en base de données
        // userService.update(currentUser);

        // Mettre à jour la session
        SessionManager.setCurrentUser(currentUser);

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
        // Logique pour changer la photo de profil
        showAlert("Info", "Fonctionnalité à venir");
    }

    @FXML
    private void retourAccueil() {
        NavigationManager.loadView("/fxml/accueil.fxml");
    }

    @FXML
    private void voirMesReservations() {
        NavigationManager.loadView("/fxml/mesreservations.fxml");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}