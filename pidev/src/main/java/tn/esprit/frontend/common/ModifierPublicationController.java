package tn.esprit.frontend.common;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.frontend.admin.AdminDashboardController;

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

    private Publication publication;
    private final PublicationService service = new PublicationService();
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "src/main/resources/images/";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.publication = SelectedItem.getCurrentPublication();
        if (publication == null) {
            showAlert("Erreur", "Aucune publication sélectionnée");
            retour();
            return;
        }
        populateFields();
        browseButton.setOnAction(e -> choisirImage());
        annulerButton.setOnAction(e -> retour());
        enregistrerButton.setOnAction(e -> enregistrer());
    }

    private void populateFields() {
        titreField.setText(publication.getTitre());
        descriptionField.setText(publication.getDescription());
        if (publication.getImage() != null && !publication.getImage().isEmpty()) {
            try {
                Image img = new Image(getClass().getResource(publication.getImage()).toExternalForm());
                currentImageView.setImage(img);
            } catch (Exception e) {
                System.out.println("Image non trouvée");
            }
        }
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
            imageField.setText(file.getName());
            previewImage.setImage(new Image(file.toURI().toString()));
        }
    }

    private void enregistrer() {
        try {
            String titre = titreField.getText().trim();
            String description = descriptionField.getText().trim();
            if (titre.isEmpty() || description.isEmpty()) {
                showAlert("Erreur", "Titre et description obligatoires");
                return;
            }
            publication.setTitre(titre);
            publication.setDescription(description);
            if (selectedImageFile != null) {
                String newPath = saveImage();
                if (newPath != null) publication.setImage(newPath);
            }
            service.modifier(publication);
            showAlert("Succès", "Publication modifiée !");
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
            String ext = selectedImageFile.getName().substring(selectedImageFile.getName().lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + ext;
            Path target = uploadPath.resolve(fileName);
            Files.copy(selectedImageFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/images/" + fileName;
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde image: " + e.getMessage());
            return null;
        }
    }

    private void retour() {
        if (AdminDashboardController.getInstance() != null) {
            AdminDashboardController.getInstance().showPublications();
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