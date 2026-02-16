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
import java.util.regex.Pattern;

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
    private String ancienTitre; // Pour vérifier les doublons

    // Constantes de validation
    private static final int TITRE_MIN_LENGTH = 3;
    private static final int TITRE_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MIN_LENGTH = 10;
    private static final int DESCRIPTION_MAX_LENGTH = 1000;
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Pattern TITRE_PATTERN = Pattern.compile("^[a-zA-Z0-9À-ÿ\\s\\-'.,!?]+$");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.publication = Dashboard.getSelectedPublication();

        if (publication == null) {
            showAlert("Erreur", "Aucune publication sélectionnée !");
            Dashboard.loadView("/Publications.fxml");
            return;
        }

        ancienTitre = publication.getTitre();
        remplirChamps();
        addRealTimeValidation();

        browseButton.setOnAction(e -> choisirImage());
        enregistrerButton.setOnAction(e -> enregistrer());
        annulerButton.setOnAction(e -> Dashboard.loadView("/Publications.fxml"));
    }

    private void addRealTimeValidation() {
        titreField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > TITRE_MAX_LENGTH) {
                titreField.setText(newVal.substring(0, TITRE_MAX_LENGTH));
                showAlert("Information", "Le titre ne peut pas dépasser " + TITRE_MAX_LENGTH + " caractères.");
            }
        });

        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > DESCRIPTION_MAX_LENGTH) {
                descriptionField.setText(newVal.substring(0, DESCRIPTION_MAX_LENGTH));
                showAlert("Information", "La description ne peut pas dépasser " + DESCRIPTION_MAX_LENGTH + " caractères.");
            }
        });
    }

    private void remplirChamps() {
        titreField.setText(publication.getTitre());
        descriptionField.setText(publication.getDescription());

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
            if (file.length() > MAX_IMAGE_SIZE) {
                showAlert("Erreur", "L'image ne doit pas dépasser 5 Mo !");
                return;
            }

            selectedImageFile = file;
            imageField.setText(file.getName());

            Image image = new Image(file.toURI().toString());
            previewImage.setImage(image);
        }
    }

    private boolean validerChamps() {
        StringBuilder erreurs = new StringBuilder();

        String titre = titreField.getText().trim();
        if (titre.isEmpty()) {
            erreurs.append("• Le titre est obligatoire.\n");
        } else if (titre.length() < TITRE_MIN_LENGTH) {
            erreurs.append("• Le titre doit contenir au moins ").append(TITRE_MIN_LENGTH).append(" caractères.\n");
        } else if (!TITRE_PATTERN.matcher(titre).matches()) {
            erreurs.append("• Le titre contient des caractères invalides.\n");
        }

        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            erreurs.append("• La description est obligatoire.\n");
        } else if (description.length() < DESCRIPTION_MIN_LENGTH) {
            erreurs.append("• La description doit contenir au moins ").append(DESCRIPTION_MIN_LENGTH).append(" caractères.\n");
        }

        if (erreurs.length() > 0) {
            showAlert("Erreurs de validation", erreurs.toString());
            return false;
        }

        return true;
    }

    private void enregistrer() {
        if (!validerChamps()) {
            return;
        }

        String nouveauTitre = titreField.getText().trim();

        // Vérifier si le titre a changé et s'il existe déjà
        if (!nouveauTitre.equalsIgnoreCase(ancienTitre) && publicationExiste(nouveauTitre)) {
            showAlert("Erreur", "Une publication avec ce titre existe déjà !");
            return;
        }

        publication.setTitre(nouveauTitre);
        publication.setDescription(descriptionField.getText().trim());

        if (selectedImageFile != null) {
            String imagePath = saveImage();
            if (imagePath != null) {
                publication.setImage(imagePath);
            }
        }

        publicationService.modifier(publication);

        showAlert("Succès", "Publication modifiée avec succès !");
        Dashboard.loadView("/Publications.fxml");
    }

    private boolean publicationExiste(String titre) {
        return publicationService.getAll().stream()
                .anyMatch(p -> p.getTitre().equalsIgnoreCase(titre) && p.getId() != publication.getId());
    }

    private String saveImage() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID().toString() + "_" + selectedImageFile.getName();
            Path targetPath = uploadPath.resolve(fileName);

            Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/images/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showAlert(String title, String content) {
        Alert.AlertType type = title.equals("Succès") ? Alert.AlertType.INFORMATION :
                title.equals("Information") ? Alert.AlertType.WARNING : Alert.AlertType.ERROR;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}