package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import tn.esprit.entities.Publication;
import tn.esprit.services.PublicationService;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.UUID;

public class AjouterPublicationController implements Initializable {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField imageField;
    @FXML private Button browseButton;
    @FXML private ImageView previewImage;
    @FXML private Button annulerButton;
    @FXML private Button enregistrerButton;

    private PublicationService publicationService = new PublicationService();
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "src/main/resources/images/";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Bouton Parcourir
        browseButton.setOnAction(e -> choisirImage());

        // Bouton Enregistrer
        enregistrerButton.setOnAction(e -> enregistrer());

        // Bouton Annuler
        annulerButton.setOnAction(e -> annuler());
    }

    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imageField.setText(file.getAbsolutePath());

            // Afficher la prévisualisation
            Image image = new Image(file.toURI().toString());
            previewImage.setImage(image);
        }
    }

    private void enregistrer() {
        // Validation
        if (titreField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le titre est obligatoire !");
            return;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            showAlert("Erreur", "La description est obligatoire !");
            return;
        }

        // Créer la publication
        Publication publication = new Publication();
        publication.setTitre(titreField.getText().trim());
        publication.setDescription(descriptionField.getText().trim());

        // Gérer l'image
        String imagePath = saveImage();
        if (imagePath != null) {
            publication.setImage(imagePath);
        }

        // Enregistrer
        publicationService.ajouter(publication);

        showAlert("Succès", "Publication ajoutée avec succès !");

        // Retour à la liste
        Dashboard.loadView("/Publications.fxml");
    }

    private String saveImage() {
        if (selectedImageFile == null) {
            return null;
        }

        try {
            // Créer le dossier images s'il n'existe pas
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Générer un nom unique
            String fileName = UUID.randomUUID().toString() + "_" + selectedImageFile.getName();
            Path targetPath = uploadPath.resolve(fileName);

            // Copier le fichier
            Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Retourner le chemin relatif pour la DB
            return "/images/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void annuler() {
        Dashboard.loadView("/Publications.fxml");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(title.equals("Erreur") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}