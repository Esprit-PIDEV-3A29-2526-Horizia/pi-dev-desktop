package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.Publication;
import tn.esprit.services.PublicationService;

import java.io.File;

public class AjouterPublicationController {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField imageField;
    @FXML private Button annulerBtn;
    @FXML private Button enregistrerBtn;
    @FXML private Button parcourirBtn;  // Ajoutez ce champ si vous utilisez fx:id

    private PublicationService publicationService = new PublicationService();

    @FXML
    public void initialize() {
        // Lier les actions aux boutons
        annulerBtn.setOnAction(event -> fermerFenetre());
        enregistrerBtn.setOnAction(event -> enregistrerPublication());

        // Si vous utilisez fx:id="parcourirBtn", décommentez la ligne suivante
        // parcourirBtn.setOnAction(event -> parcourirImage());
    }

    // Méthode appelée si le bouton utilise onAction="#parcourirImage"
    @FXML
    private void parcourirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) annulerBtn.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            imageField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) annulerBtn.getScene().getWindow();
        stage.close();
    }

    private void enregistrerPublication() {
        String titre = titreField.getText().trim();
        String description = descriptionArea.getText().trim();
        String imagePath = imageField.getText().trim();

        if (titre.isEmpty() || description.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs manquants");
            alert.setContentText("Le titre et la description sont obligatoires.");
            alert.showAndWait();
            return;
        }

        // Utilisation du constructeur par défaut et des setters
        Publication publication = new Publication();
        publication.setTitre(titre);
        publication.setDescription(description);
        publication.setImage(imagePath);  // Adaptez le nom du setter selon votre champ (setImage, setImagePath, etc.)

        publicationService.ajouter(publication);
        fermerFenetre();
    }
}