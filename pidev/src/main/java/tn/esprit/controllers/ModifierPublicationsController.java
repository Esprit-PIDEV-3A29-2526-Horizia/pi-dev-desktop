package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import tn.esprit.entities.Categorie;
import tn.esprit.entities.Publication;
import tn.esprit.services.PublicationService;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SelectedItem;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.UUID;

public class ModifierPublicationsController implements Initializable {

    // Navbar partagée
    @FXML private NavbarController navbarController;

    @FXML private TextField titreField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private TextArea descriptionField;
    @FXML private ImageView currentImageView;
    @FXML private TextField imageField;
    @FXML private Button browseButton;
    @FXML private ImageView previewImage;
    @FXML private Button annulerButton;
    @FXML private Button enregistrerButton;

    private Publication publication;
    private final PublicationService service = new PublicationService();
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "src/main/resources/images/";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Mettre à jour la navbar
        if (navbarController != null) {
            navbarController.updateUserInfo();
            navbarController.setActivePublications();
        }

        this.publication = SelectedItem.getCurrentPublication();

        if (publication == null) {
            showAlert("Erreur", "Aucune publication sélectionnée");
            retour();
            return;
        }

        // Remplir les catégories
        for (Categorie cat : Categorie.values()) {
            if (cat != Categorie.TOUS) {
                categorieCombo.getItems().add(cat.getLabel());
            }
        }

        populateFields();
        browseButton.setOnAction(e -> choisirImage());
        annulerButton.setOnAction(e -> retour());
        enregistrerButton.setOnAction(e -> enregistrer());
    }

    private void populateFields() {
        titreField.setText(publication.getTitre());
        descriptionField.setText(publication.getDescription());

        String currentCategorie = publication.getCategorie().getLabel();
        categorieCombo.setValue(currentCategorie);

        if (publication.getImage() != null && !publication.getImage().isEmpty()) {
            try {
                String imagePath = publication.getImage();
                if (!imagePath.startsWith("/")) {
                    imagePath = "/" + imagePath;
                }
                Image img = new Image(getClass().getResourceAsStream(imagePath));
                if (img != null && !img.isError()) {
                    currentImageView.setImage(img);
                }
            } catch (Exception e) {
                // Ignorer
            }
        }
    }

    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        File file = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imageField.setText(file.getName());
            try {
                Image img = new Image(file.toURI().toString());
                previewImage.setImage(img);
                previewImage.setVisible(true);
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de charger l'image");
            }
        }
    }

    private void enregistrer() {
        try {
            String titre = titreField.getText().trim();
            String description = descriptionField.getText().trim();
            String categorieLabel = categorieCombo.getValue();

            if (titre.isEmpty() || description.isEmpty()) {
                showAlert("Erreur", "Titre et description obligatoires");
                return;
            }
            if (categorieLabel == null || categorieLabel.isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner une catégorie");
                return;
            }
            if (service.titreExiste(titre, publication.getId())) {
                showAlert("Erreur", "Une publication avec ce titre existe déjà !");
                return;
            }

            publication.setTitre(titre);
            publication.setDescription(description);

            for (Categorie cat : Categorie.values()) {
                if (cat.getLabel().equals(categorieLabel)) {
                    publication.setCategorie(cat);
                    break;
                }
            }

            publication.setDateCreation(LocalDateTime.now());

            if (selectedImageFile != null) {
                String newPath = saveImage();
                if (newPath != null) publication.setImage(newPath);
            }

            service.modifier(publication);
            showAlert("Succès", "Publication modifiée avec succès !");
            retour();

        } catch (Exception e) {
            showAlert("Erreur", "Modification échouée: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String saveImage() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String fileName = selectedImageFile.getName();
            String ext = "";
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot > 0) {
                ext = fileName.substring(lastDot);
            }
            String uniqueName = UUID.randomUUID().toString() + ext;
            Path target = uploadPath.resolve(uniqueName);
            Files.copy(selectedImageFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/images/" + uniqueName;
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde image: " + e.getMessage());
            return null;
        }
    }

    private void retour() {
        // Retour vers Mes publications
        NavigationManager.loadView("/fxml/UserPublications.fxml", "Mes publications");
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