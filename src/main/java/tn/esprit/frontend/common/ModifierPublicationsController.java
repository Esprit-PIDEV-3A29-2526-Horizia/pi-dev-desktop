package tn.esprit.frontend.common;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.backend.entities.Categorie;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.backend.utils.Session;
import tn.esprit.frontend.admin.AdminDashboardController;
import tn.esprit.frontend.user.UserMainController;

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

    // Flag pour savoir s'il faut retourner à "Mes publications" (true) ou à l'admin (false)
    private static boolean returnToUser = false;

    public static void setReturnToUser(boolean val) {
        returnToUser = val;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.publication = SelectedItem.getCurrentPublication();

        if (publication == null) {
            showAlert("Erreur", "Aucune publication sélectionnée");
            retour();
            return;
        }

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

    // ========== MÉTHODE RETOUR CORRIGÉE ==========
    private void retour() {
        Stage stage = (Stage) annulerButton.getScene().getWindow();

        // 1. Priorité à la vue utilisateur si le flag est actif
        if (returnToUser && UserMainController.getInstance() != null) {
            UserMainController.getInstance().showPublications();
            returnToUser = false;   // réinitialisation
            return;
        }

        // 2. Sinon, mode admin
        if (Session.estAdmin() && AdminDashboardController.getInstance() != null) {
            AdminDashboardController.getInstance().showPublications();
            return;
        }

        // 3. Utilisateur simple (non admin) mais flag non positionné
        if (Session.estConnecte() && UserMainController.getInstance() != null) {
            UserMainController.getInstance().showPublications();
            return;
        }

        // 4. Fallback : fermeture
        stage.hide();
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