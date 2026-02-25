package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import tn.esprit.entities.Publication;
import tn.esprit.services.PublicationService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Pattern;

public class AjouterPublicationController implements Initializable {

    @FXML private Button retourBtn;
    @FXML private TextField titreField;
    @FXML private Label titreError;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private TextField villeField;
    @FXML private TextField paysField;
    @FXML private TextArea descriptionArea;
    @FXML private Label descCount;
    @FXML private TextField tagsField;
    @FXML private StackPane imagePreviewContainer;
    @FXML private ImageView imagePreview;
    @FXML private Button parcourirBtn;
    @FXML private Label imagePathLabel;
    @FXML private CheckBox publierMaintenantCheck;
    @FXML private CheckBox autoriserCommentairesCheck;
    @FXML private CheckBox visiblePubliqueCheck;
    @FXML private Button annulerBtn;
    @FXML private Button enregistrerBtn;

    private PublicationService publicationService = new PublicationService();
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "src/main/resources/images/";

    // Validation
    private static final int TITRE_MIN = 3;
    private static final int TITRE_MAX = 100;
    private static final int DESC_MAX = 1000;
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Pattern TITRE_PATTERN = Pattern.compile("^[a-zA-Z0-9À-ÿ\\s\\-'.,!?]+$");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser catégories
        categorieCombo.getItems().addAll(
                "Voyage", "Hébergement", "Restaurant", "Activité",
                "Transport", "Conseil", "Autre"
        );

        // Validation en temps réel
        setupValidation();

        // Événements
        retourBtn.setOnAction(e -> Dashboard.loadView("/Publications.fxml"));
        annulerBtn.setOnAction(e -> Dashboard.loadView("/Publications.fxml"));
        parcourirBtn.setOnAction(e -> choisirImage());
        enregistrerBtn.setOnAction(e -> enregistrer());
    }

    private void setupValidation() {
        // Compteur de caractères description
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > DESC_MAX) {
                descriptionArea.setText(newVal.substring(0, DESC_MAX));
            }
            descCount.setText(newVal.length() + " / " + DESC_MAX);
            descCount.setStyle(newVal.length() > 900 ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #95a5a6;");
        });

        // Validation titre
        titreField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > TITRE_MAX) {
                titreField.setText(newVal.substring(0, TITRE_MAX));
            }
            boolean valid = newVal.length() >= TITRE_MIN && TITRE_PATTERN.matcher(newVal).matches();
            titreError.setVisible(!valid && !newVal.isEmpty());
            titreField.setStyle(valid || newVal.isEmpty() ? "" : "-fx-border-color: #e74c3c;");
        });
    }

    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(parcourirBtn.getScene().getWindow());
        if (file != null) {
            if (file.length() > MAX_IMAGE_SIZE) {
                showAlert("Erreur", "L'image ne doit pas dépasser 5 Mo !");
                return;
            }

            selectedImageFile = file;
            imagePathLabel.setText(file.getName());

            // Afficher preview
            Image image = new Image(file.toURI().toString());
            imagePreview.setImage(image);
            imagePreview.setVisible(true);

            // Masquer le placeholder
            ((VBox) imagePreviewContainer.getChildren().get(0)).setVisible(false);
        }
    }

    private void enregistrer() {
        // Validation
        String titre = titreField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (titre.length() < TITRE_MIN) {
            showAlert("Erreur", "Le titre doit contenir au moins " + TITRE_MIN + " caractères !");
            titreField.requestFocus();
            return;
        }

        if (!TITRE_PATTERN.matcher(titre).matches()) {
            showAlert("Erreur", "Le titre contient des caractères invalides !");
            return;
        }

        if (description.isEmpty()) {
            showAlert("Erreur", "La description est obligatoire !");
            descriptionArea.requestFocus();
            return;
        }

        if (categorieCombo.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une catégorie !");
            return;
        }

        // Vérifier doublon titre
        if (publicationService.titreExiste(titre)) {
            showAlert("Erreur", "Une publication avec ce titre existe déjà !");
            return;
        }

        // Créer la publication
        Publication p = new Publication();
        p.setTitre(titre);
        p.setDescription(description);
        p.setUtilisateurId(1); // TODO: Récupérer l'utilisateur connecté

        // Sauvegarder l'image si présente
        if (selectedImageFile != null) {
            String imagePath = saveImage();
            if (imagePath != null) {
                p.setImage(imagePath);
            }
        }

        // Sauvegarder
        publicationService.ajouter(p);

        showAlert("Succès", "Publication créée avec succès !");
        Dashboard.loadView("/views/Publications.fxml");
    }

    private String saveImage() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String ext = selectedImageFile.getName().substring(selectedImageFile.getName().lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + ext;
            Path targetPath = uploadPath.resolve(fileName);

            Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/images/" + fileName;

        } catch (IOException e) {
            System.err.println("Erreur sauvegarde image: " + e.getMessage());
            return null;
        }
    }

    private void showAlert(String title, String content) {
        Alert.AlertType type = title.equals("Succès") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}