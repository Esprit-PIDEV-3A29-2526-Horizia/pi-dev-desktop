package tn.esprit.frontend.user;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import tn.esprit.entities.User;
import tn.esprit.services.Serviceuser;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserProfilController implements Initializable {

    @FXML private ImageView profileImage;
    @FXML private TextField prenomField;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button btnChangerPhoto;
    @FXML private Button btnAnnuler;
    @FXML private Button btnEnregistrer;

    private User currentUser;
    private Serviceuser utilisateurService = new Serviceuser();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!SessionManager.isLoggedIn()) return;

        currentUser = SessionManager.getCurrentUser();
        remplirChamps();

        btnAnnuler.setOnAction(e -> annuler());
        btnEnregistrer.setOnAction(e -> enregistrer());
    }

    private void remplirChamps() {
        if (currentUser == null) return;
        prenomField.setText(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
        nomField.setText(currentUser.getNom() != null ? currentUser.getNom() : "");
        emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        telephoneField.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        adresseField.setText(currentUser.getAddresse() != null ? currentUser.getAddresse() : ""); // Attention : getAddresse() vs getAdresse()

        // Pour l'image, à adapter selon le champ dans User
        // Si User a un champ imageProfil (String), on peut le gérer


    }


    private void annuler() {
        remplirChamps();
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void enregistrer() {
        if (prenomField == null || nomField == null || emailField == null) {
            showAlert("Erreur", "Problème d'initialisation des champs");
            return;
        }

        currentUser.setPrenom(prenomField.getText().trim());
        currentUser.setNom(nomField.getText().trim());
        currentUser.setEmail(emailField.getText().trim());
        currentUser.setTelephone(telephoneField.getText().trim());
        currentUser.setAddresse(adresseField.getText().trim());

        // Gestion du mot de passe
        if (!newPasswordField.getText().isEmpty()) {
            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showAlert("Erreur", "Les mots de passe ne correspondent pas");
                return;
            }
            // Vérifier l'ancien mot de passe
            if (!currentPasswordField.getText().equals(currentUser.getPassword())) {
                showAlert("Erreur", "Mot de passe actuel incorrect");
                return;
            }
            currentUser.setPassword(newPasswordField.getText());
        }

        try {
            utilisateurService.modifier(currentUser);
            // Mettre à jour la session si nécessaire (si l'objet courant a changé, il est déjà le même)
            // On peut éventuellement rafraîchir la session, mais ce n'est pas obligatoire.
            showAlert("Succès", "Profil mis à jour !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(
                title.equals("Succès") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}