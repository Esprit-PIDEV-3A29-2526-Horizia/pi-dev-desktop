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

public class ModifierPublicationController implements Initializable {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private ImageView currentImageView;
    @FXML private TextField imageField;
    @FXML private Button browseButton;
    @FXML private ImageView previewImage;
    @FXML private Button annulerButton;
    @FXML private Button enregistrerButton;

    private PublicationService publicationService = new PublicationService();
    private Publication publication;
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "src/main/resources/images/";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Récupérer la publication sélectionnée
        this.publication = Dashboard.getSelectedPublication();

        if (publication == null) {
            showAlert("Erreur", "Aucune publication sélectionnée !");
            Dashboard.loadView("/Publications.fxml");
            return;
        }

        // Remplir les champs avec les données actuelles
        remplirChamps();

        // Actions des boutons
        browseButton.setOnAction(e -> choisirImage());
        enregistrerButton.setOnAction(e -> enregistrer());
        annulerButton.setOnAction(e -> Dashboard.loadView("/Publications.fxml"));
    }

    private void remplirChamps() {
        titreField.setText(publication.getTitre());
        descriptionField.setText(publication.getDescription());

        // Afficher l'image actuelle
        if (publication.getImage() != null && !publication.getImage().isEmpty()) {
            try {
                Image image = new Image(getClass().getResource(publication.getImage()).toExternalForm());
                currentImageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Image actuelle non trouvée: " + publication.getImage());
            }
        }
    }

    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une nouvelle image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imageField.setText(file.getName());

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

        // Mettre à jour les données
        publication.setTitre(titreField.getText().trim());
        publication.setDescription(descriptionField.getText().trim());

        // Nouvelle image sélectionnée ?
        if (selectedImageFile != null) {
            String imagePath = saveImage();
            if (imagePath != null) {
                publication.setImage(imagePath);
            }
        }

        // Sauvegarder dans la base
        publicationService.modifier(publication);

        // Message de succès
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Succès");
        success.setHeaderText(null);
        success.setContentText("Publication modifiée avec succès !");
        success.showAndWait();

        // Retour à la liste
        Dashboard.loadView("/Publications.fxml");
    }

    private String saveImage() {
        try {
            // Créer le dossier s'il n'existe pas
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Nom unique pour l'image
            String fileName = UUID.randomUUID().toString() + "_" + selectedImageFile.getName();
            Path targetPath = uploadPath.resolve(fileName);

            // Copier le fichier
            Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/images/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}