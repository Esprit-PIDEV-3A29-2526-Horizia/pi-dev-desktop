package tn.esprit.controllers;

import javafx.animation.RotateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.Publication;
import tn.esprit.services.ServicePublication;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AjoutPublicationController {

    // ============ FXML FIELDS ============

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextField titreField;

    @FXML
    private TextField lieuField;

    @FXML
    private TextField descriptionField;

    @FXML
    private TextField tarifField;

    @FXML
    private CheckBox actifCheckBox;

    @FXML
    private Button ajouterBtn;

    @FXML
    private Button annulerBtn;

    @FXML
    private Button imageSelectBtn;

    @FXML
    private Button titreMicroBtn;

    @FXML
    private Button lieuMicroBtn;

    @FXML
    private Button descriptionMicroBtn;

    @FXML
    private Button tarifMicroBtn;

    @FXML
    private Label errorLabel;

    @FXML
    private Label imagePathLabel;

    // ============ PRIVATE FIELDS ============

    private ServicePublication servicePublication;
    private String selectedImagePath;

    // ============ INITIALIZATION ============

    @FXML
    public void initialize() {
        // Initialiser le service
        this.servicePublication = new ServicePublication();

        // Initialiser ComboBox
        if (typeComboBox != null) {
            typeComboBox.getItems().addAll("Voyage", "Loisir", "Conseil", "Expérience", "Guide");
        }

        // Micro boutons
        if (titreMicroBtn != null) {
            titreMicroBtn.setOnAction(e -> handleMicro(titreField, titreMicroBtn));
        }
        if (lieuMicroBtn != null) {
            lieuMicroBtn.setOnAction(e -> handleMicro(lieuField, lieuMicroBtn));
        }
        if (descriptionMicroBtn != null) {
            descriptionMicroBtn.setOnAction(e -> handleMicro(descriptionField, descriptionMicroBtn));
        }
        if (tarifMicroBtn != null) {
            tarifMicroBtn.setOnAction(e -> handleMicro(tarifField, tarifMicroBtn));
        }

        // Actions principales
        if (ajouterBtn != null) {
            ajouterBtn.setOnAction(e -> ajouterPublication());
        }
        if (annulerBtn != null) {
            annulerBtn.setOnAction(e -> retourListe());
        }
        if (imageSelectBtn != null) {
            imageSelectBtn.setOnAction(e -> selectImage());
        }
    }

    // ============ PRIVATE METHODS ============

    private void handleMicro(TextField field, Button microBtn) {
        if (field == null || microBtn == null) return;

        RotateTransition rotate = new RotateTransition(Duration.seconds(2), microBtn);
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);

        Task<String> voiceTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                Thread.sleep(3000);
                return "Texte reconnu par voix";
            }
        };

        voiceTask.setOnRunning(e -> rotate.play());
        voiceTask.setOnSucceeded(e -> {
            field.setText(voiceTask.getValue().toUpperCase());
            rotate.stop();
            microBtn.setRotate(0);
        });
        voiceTask.setOnFailed(e -> {
            rotate.stop();
            microBtn.setRotate(0);
        });

        Thread thread = new Thread(voiceTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void selectImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) imageSelectBtn.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);

        if (file != null) {
            selectedImagePath = file.toURI().toString();
            if (imagePathLabel != null) {
                imagePathLabel.setText(file.getName());
            }
        }
    }

    private void ajouterPublication() {
        // Reset error
        if (errorLabel != null) {
            errorLabel.setText("");
        }

        StringBuilder erreurs = new StringBuilder();

        // Récupération des valeurs
        String titre = (titreField != null) ? titreField.getText().trim() : "";
        String lieu = (lieuField != null) ? lieuField.getText().trim() : "";
        String description = (descriptionField != null) ? descriptionField.getText().trim() : "";
        String type = (typeComboBox != null) ? typeComboBox.getValue() : null;
        String tarifStr = (tarifField != null) ? tarifField.getText().trim() : "";

        float tarif = 0.0f;

        // Validations
        if (titre.isEmpty()) {
            erreurs.append("• Titre obligatoire\n");
        }
        if (lieu.isEmpty()) {
            erreurs.append("• Lieu obligatoire\n");
        }
        if (selectedImagePath == null || selectedImagePath.isEmpty()) {
            erreurs.append("• Image obligatoire\n");
        }
        if (description.length() > 255) {
            erreurs.append("• Description trop longue (max 255)\n");
        }
        if (type == null) {
            erreurs.append("• Type obligatoire\n");
        }

        if (tarifStr.isEmpty()) {
            erreurs.append("• Tarif obligatoire\n");
        } else {
            try {
                tarif = Float.parseFloat(tarifStr);
                if (tarif <= 0) {
                    erreurs.append("• Tarif doit être positif\n");
                }
            } catch (NumberFormatException e) {
                erreurs.append("• Tarif invalide\n");
            }
        }

        // Si erreurs, afficher et arrêter
        if (erreurs.length() > 0) {
            showError(erreurs.toString());
            return;
        }

        // Création et sauvegarde
        try {
            Publication p = new Publication();
            p.setTitre(titre.toUpperCase());
            p.setLieu(lieu.toUpperCase());
            p.setImage(selectedImagePath);
            p.setContenu(description);
            p.setTarif(tarif);
            p.setActif(actifCheckBox != null && actifCheckBox.isSelected());
            p.setType(type);
            p.setDatePublication(LocalDateTime.now());
            p.setLikes(0);
            p.setEstPublie(true);

            servicePublication.ajouter(p);

            showAlert("Succès", "Publication ajoutée avec succès !\nID: " + p.getId());
            retourListe();

        } catch (SQLException e) {
            showError("Erreur base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void retourListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Publications.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) annulerBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showError("Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            errorLabel.setText(message);
        } else {
            System.err.println("ERROR: " + message);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}