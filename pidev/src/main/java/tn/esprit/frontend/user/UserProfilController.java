package tn.esprit.frontend.user;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import tn.esprit.backend.entities.Utilisateur;
import tn.esprit.backend.services.UtilisateurService;
import tn.esprit.backend.utils.Session;

import java.io.File;
import java.net.URL;
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

    private Utilisateur currentUser;
    private UtilisateurService utilisateurService = new UtilisateurService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.estConnecte()) return;

        currentUser = Session.getUtilisateur();
        remplirChamps();

        btnChangerPhoto.setOnAction(e -> changerPhoto());
        btnAnnuler.setOnAction(e -> annuler());
        btnEnregistrer.setOnAction(e -> enregistrer());
    }

    private void remplirChamps() {
        if (currentUser == null) return;
        prenomField.setText(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
        nomField.setText(currentUser.getNom() != null ? currentUser.getNom() : "");
        emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        telephoneField.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        adresseField.setText(currentUser.getAdresse() != null ? currentUser.getAdresse() : "");

        if (currentUser.getImageProfil() != null && !currentUser.getImageProfil().isEmpty()) {
            try {
                Image img = new Image("file:" + currentUser.getImageProfil());
                profileImage.setImage(img);
            } catch (Exception e) {
                // Ignoré
            }
        }
    }

    private void changerPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(btnChangerPhoto.getScene().getWindow());
        if (file != null) {
            try {
                Image img = new Image(file.toURI().toString());
                profileImage.setImage(img);
                currentUser.setImageProfil(file.getAbsolutePath());
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de charger l'image");
            }
        }
    }

    private void annuler() {
        remplirChamps();
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void enregistrer() {
        // Sécurité : si les champs sont null (problème d'injection), on ne fait rien
        if (prenomField == null || nomField == null || emailField == null) {
            showAlert("Erreur", "Problème d'initialisation des champs");
            return;
        }

        currentUser.setPrenom(prenomField.getText().trim());
        currentUser.setNom(nomField.getText().trim());
        currentUser.setEmail(emailField.getText().trim());
        currentUser.setTelephone(telephoneField.getText().trim());
        currentUser.setAdresse(adresseField.getText().trim());

        if (!newPasswordField.getText().isEmpty()) {
            if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                showAlert("Erreur", "Les mots de passe ne correspondent pas");
                return;
            }
            if (!currentPasswordField.getText().equals(currentUser.getMotDePasse())) {
                showAlert("Erreur", "Mot de passe actuel incorrect");
                return;
            }
            currentUser.setMotDePasse(newPasswordField.getText());
        }

        utilisateurService.modifier(currentUser);
        Session.connecter(currentUser);
        showAlert("Succès", "Profil mis à jour !");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(
                title.equals("Succès") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}