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
import tn.esprit.backend.utils.Session;
import tn.esprit.frontend.admin.AdminDashboardController;
import tn.esprit.frontend.user.UserMainController;
import tn.esprit.frontend.utils.Navigator;
import tn.esprit.backend.utils.ApiClient;

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

    // Nouveaux boutons IA
    @FXML private Button btnGenererIA;
    @FXML private Button btnTraduire;

    private PublicationService publicationService = new PublicationService();
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "src/main/resources/images/";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Pattern TITRE_PATTERN = Pattern.compile("^[a-zA-Z0-9À-ÿ\\s\\-'.,!?]+$");
    private static final int TITRE_MIN = 3;
    private static final int TITRE_MAX = 100;
    private static final int DESC_MAX = 1000;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les catégories
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

        // Actions des boutons IA
        btnGenererIA.setOnAction(e -> genererDescriptionIA());
        btnTraduire.setOnAction(e -> traduireDescription());
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
        if (publicationService.titreExiste(titre)) {
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
            publicationService.ajouter(p);
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

    // ---------- Fonctionnalités IA ----------
    private void genererDescriptionIA() {
        String titre = titreField.getText().trim();
        String categorie = categorieCombo.getValue();

        if (titre.isEmpty()) {
            showAlert("Erreur", "Veuillez d'abord saisir un titre.");
            return;
        }
        if (categorie == null) {
            showAlert("Erreur", "Veuillez sélectionner une catégorie.");
            return;
        }

        btnGenererIA.setDisable(true);
        btnGenererIA.setText("Génération en cours...");

        new Thread(() -> {
            try {
                String description = ApiClient.genererDescriptionIA(titre, categorie);
                javafx.application.Platform.runLater(() -> {
                    descriptionArea.setText(description);
                    btnGenererIA.setDisable(false);
                    btnGenererIA.setText("🤖 Générer description IA");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showAlert("Erreur API", "Impossible de générer la description : " + ex.getMessage());
                    btnGenererIA.setDisable(false);
                    btnGenererIA.setText("🤖 Générer description IA");
                });
            }
        }).start();
    }

    private void traduireDescription() {
        String description = descriptionArea.getText().trim();
        if (description.isEmpty()) {
            showAlert("Erreur", "Il n'y a rien à traduire.");
            return;
        }

        btnTraduire.setDisable(true);
        btnTraduire.setText("Traduction en cours...");

        new Thread(() -> {
            try {
                // Traduire du français vers l'anglais (vous pouvez changer les codes)
                String traduit = ApiClient.traduire(description, "fr", "en");
                javafx.application.Platform.runLater(() -> {
                    descriptionArea.setText(traduit);
                    btnTraduire.setDisable(false);
                    btnTraduire.setText("🌐 Traduire en anglais");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showAlert("Erreur API", "Traduction échouée : " + ex.getMessage());
                    btnTraduire.setDisable(false);
                    btnTraduire.setText("🌐 Traduire en anglais");
                });
            }
        }).start();
    }

    // ---------- Fin IA ----------

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
            Navigator.loadView("/views/admin/GestionPublications.fxml");
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.out.println("→ Recherche contentPane");
            StackPane contentPane = (StackPane) retourBtn.getScene().lookup("#contentPane");
            if (contentPane != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/GestionPublications.fxml"));
                Node view = loader.load();
                contentPane.getChildren().setAll(view);
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            System.out.println("→ Rechargement complet du dashboard");
            Parent root = FXMLLoader.load(getClass().getResource("/views/admin/admin_dashboard.fxml"));
            Stage stage = (Stage) retourBtn.getScene().getWindow();
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