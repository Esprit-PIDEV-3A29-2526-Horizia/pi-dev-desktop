package tn.esprit.frontend.common;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.backend.entities.Categorie;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.utils.ApiClient;
import tn.esprit.backend.utils.Session;
import tn.esprit.frontend.admin.AdminDashboardController;
import tn.esprit.frontend.user.UserMainController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Pattern;

public class AjouterPublicationController implements Initializable {  // ✅ CORRIGÉ (sans 's')

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
    @FXML private VBox imagePlaceholder;
    @FXML private ImageView imagePreview;
    @FXML private Button parcourirBtn;
    @FXML private Label imagePathLabel;
    @FXML private CheckBox publierMaintenantCheck;
    @FXML private CheckBox autoriserCommentairesCheck;
    @FXML private CheckBox visiblePubliqueCheck;
    @FXML private TextField dateField;
    @FXML private Button annulerBtn;
    @FXML private Button enregistrerBtn;
    @FXML private Button btnGenererIA;
    @FXML private Button btnTraduire;

    private PublicationService publicationService = new PublicationService();  // ✅ CORRIGÉ (minuscule)
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "src/main/resources/images/";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Pattern TITRE_PATTERN = Pattern.compile("^[a-zA-Z0-9À-ÿ\\s\\-'.,!?]+$");
    private static final int TITRE_MIN = 3;
    private static final int TITRE_MAX = 100;
    private static final int DESC_MAX = 1000;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for (Categorie cat : Categorie.values()) {
            if (cat != Categorie.TOUS) {
                categorieCombo.getItems().add(cat.getLabel());
            }
        }
        categorieCombo.getSelectionModel().selectFirst();

        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        setupValidation();

        retourBtn.setOnAction(e -> goBack());
        annulerBtn.setOnAction(e -> goBack());
        parcourirBtn.setOnAction(e -> choisirImage());
        enregistrerBtn.setOnAction(e -> enregistrer());
        if (btnGenererIA != null) {
            btnGenererIA.setOnAction(e -> genererDescriptionIA());
        }
        if (btnTraduire != null) {
            btnTraduire.setOnAction(e -> traduireDescription());
        }
    }

    private void setupValidation() {
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > DESC_MAX) {
                descriptionArea.setText(newVal.substring(0, DESC_MAX));
            }
            descCount.setText(newVal.length() + " / " + DESC_MAX);
            descCount.setStyle(newVal.length() > 900 ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #95a5a6;");
        });

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
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(parcourirBtn.getScene().getWindow());
        if (file != null) {
            if (file.length() > MAX_IMAGE_SIZE) {
                showAlert("Erreur", "L'image ne doit pas dépasser 5 Mo !");
                return;
            }

            selectedImageFile = file;
            imagePathLabel.setText("Fichier sélectionné: " + file.getName());

            try {
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setVisible(true);
                imagePlaceholder.setVisible(false);
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de charger l'image");
            }
        }
    }

    private void enregistrer() {
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
        if (publicationService.titreExiste(titre)) {  // ✅ CORRIGÉ
            showAlert("Erreur", "Une publication avec ce titre existe déjà !");
            return;
        }

        Publication p = new Publication();
        p.setTitre(titre);
        p.setDescription(description);
        p.setUtilisateurId(Session.getUtilisateur() != null ? Session.getUtilisateur().getId() : 1);
        p.setAuteur(Session.getUtilisateur() != null ? Session.getUtilisateur().getNomComplet() : "Anonyme");

        String catLabel = categorieCombo.getValue();
        for (Categorie cat : Categorie.values()) {
            if (cat.getLabel().equals(catLabel)) {
                p.setCategorie(cat);
                break;
            }
        }

        if (!villeField.getText().trim().isEmpty() || !paysField.getText().trim().isEmpty()) {
            String location = "";
            if (!villeField.getText().trim().isEmpty()) location += villeField.getText().trim();
            if (!paysField.getText().trim().isEmpty()) {
                if (!location.isEmpty()) location += ", ";
                location += paysField.getText().trim();
            }
            p.setDescription("[📍 " + location + "]\n\n" + p.getDescription());
        }

        if (selectedImageFile != null) {
            String imagePath = saveImage();
            if (imagePath != null) p.setImage(imagePath);
        }

        try {
            publicationService.ajouter(p);  // ✅ CORRIGÉ
            showAlert("Succès", "Publication créée avec succès !");
            goBack();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la création: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String saveImage() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String ext = "";
            String fileName = selectedImageFile.getName();
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot > 0) ext = fileName.substring(lastDot);
            String uniqueName = UUID.randomUUID().toString() + ext;
            Path targetPath = uploadPath.resolve(uniqueName);
            Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/images/" + uniqueName;
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde image: " + e.getMessage());
            showAlert("Erreur", "Impossible de sauvegarder l'image");
            return null;
        }
    }

    private void genererDescriptionIA() {
        String titre = titreField.getText().trim();
        String categorie = categorieCombo.getValue();

        if (titre.isEmpty()) {
            showAlert("Attention", "Veuillez d'abord saisir un titre.");
            titreField.requestFocus();
            return;
        }
        if (categorie == null || categorie.isEmpty()) {
            showAlert("Attention", "Veuillez sélectionner une catégorie.");
            categorieCombo.requestFocus();
            return;
        }

        btnGenererIA.setDisable(true);
        btnGenererIA.setText("⏳ Génération...");

        new Thread(() -> {
            String description = null;
            try {
                description = ApiClient.genererDescriptionIA(titre, categorie);
            } catch (IOException e) {
                e.printStackTrace();
            }
            final String desc = description;
            javafx.application.Platform.runLater(() -> {
                btnGenererIA.setDisable(false);
                btnGenererIA.setText("🤖 Générer description");
                if (desc != null && !desc.isEmpty()) {
                    descriptionArea.setText(desc);
                    showAlert("Succès", "Description générée !");
                } else {
                    showAlert("Erreur", "Échec de la génération.");
                }
            });
        }).start();
    }

    private void traduireDescription() {
        String texte = descriptionArea.getText().trim();
        if (texte.isEmpty()) {
            showAlert("Attention", "Veuillez d'abord saisir une description.");
            descriptionArea.requestFocus();
            return;
        }

        btnTraduire.setDisable(true);
        btnTraduire.setText("⏳ Traduction...");

        new Thread(() -> {
            String traduit = null;
            try {
                traduit = ApiClient.traduire(texte, "fr", "en");
            } catch (IOException e) {
                e.printStackTrace();
            }
            final String result = traduit;
            javafx.application.Platform.runLater(() -> {
                btnTraduire.setDisable(false);
                btnTraduire.setText("🌐 Traduire");
                if (result != null && !result.isEmpty()) {
                    descriptionArea.setText(result);
                    showAlert("Succès", "Traduction effectuée.");
                } else {
                    showAlert("Erreur", "Échec de la traduction.");
                }
            });
        }).start();
    }

    private void goBack() {
        System.out.println("=== goBack() appelé ===");
        try {
            if (AdminDashboardController.getInstance() != null) {
                System.out.println("→ Retour vers admin");
                AdminDashboardController.getInstance().showPublications();
                return;
            }
            if (UserMainController.getInstance() != null) {
                System.out.println("→ Retour vers user");
                UserMainController.getInstance().showPublications();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.out.println("→ Tentative de retour via lookup #contentPane");
            StackPane contentPane = (StackPane) annulerBtn.getScene().lookup("#contentPane");
            if (contentPane != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/GestionPublications.fxml"));  // ✅ CORRIGÉ
                Node view = loader.load();
                contentPane.getChildren().setAll(view);
                return;
            } else {
                System.out.println("→ contentPane non trouvé");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            System.out.println("→ Rechargement complet du dashboard admin");
            Parent root = FXMLLoader.load(getClass().getResource("/views/admin/admin_dashboard.fxml"));
            Stage stage = (Stage) annulerBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Horizia - Administration");
            stage.setMaximized(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible de retourner.");
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