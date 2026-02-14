package tn.esprit.controllers;

import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import tn.esprit.entities.Publication;
import tn.esprit.services.ServicePublication;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;

public class AjoutPublicationController {

    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField titreField, imageField, lieuField, descriptionField, tarifField;
    @FXML private CheckBox actifCheckBox;
    @FXML private Button ajouterBtn, annulerBtn, titreMicroBtn, imageMicroBtn, lieuMicroBtn, descriptionMicroBtn, tarifMicroBtn;

    private ServicePublication servicePublication = new ServicePublication();

    @FXML
    public void initialize() {
        // Micro boutons
        titreMicroBtn.setOnAction(e -> handleMicro(titreField, titreMicroBtn));
        imageMicroBtn.setOnAction(e -> handleMicro(imageField, imageMicroBtn));
        lieuMicroBtn.setOnAction(e -> handleMicro(lieuField, lieuMicroBtn));
        descriptionMicroBtn.setOnAction(e -> handleMicro(descriptionField, descriptionMicroBtn));
        tarifMicroBtn.setOnAction(e -> handleMicro(tarifField, tarifMicroBtn));

        // Actions boutons
        ajouterBtn.setOnAction(e -> ajouterPublication());
        annulerBtn.setOnAction(e -> retourListe());
    }

    private void handleMicro(TextField field, Button microBtn) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(2), microBtn);
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.play();

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                String recognizedText = "Texte reconnu";
                javafx.application.Platform.runLater(() -> {
                    field.setText(recognizedText.toUpperCase());
                    rotate.stop();
                    microBtn.setRotate(0);
                });
            } catch (InterruptedException ex) { ex.printStackTrace(); }
        }).start();
    }

    private void ajouterPublication() {
        StringBuilder erreurs = new StringBuilder();
        String titre = titreField.getText().trim();
        String lieu = lieuField.getText().trim();
        String image = imageField.getText().trim();
        String description = descriptionField.getText().trim();
        String type = typeComboBox.getValue();
        String tarifStr = tarifField.getText().trim();
        float tarif = 0;

        if (titre.isEmpty()) erreurs.append("- Le titre est obligatoire\n");
        if (lieu.isEmpty()) erreurs.append("- Le lieu est obligatoire\n");
        if (image.isEmpty() || !isValidImagePath(image)) erreurs.append("- Image invalide\n");
        if (description.length() > 255) erreurs.append("- Description trop longue\n");
        if (type == null) erreurs.append("- Sélectionner un type\n");
        if (tarifStr.isEmpty()) erreurs.append("- Tarif obligatoire\n");
        else {
            try {
                tarif = Float.parseFloat(tarifStr);
                if (tarif <= 0) erreurs.append("- Tarif doit être positif\n");
            } catch (NumberFormatException e) {
                erreurs.append("- Tarif invalide\n");
            }
        }

        if (erreurs.length() > 0) {
            showAlert("Erreur", erreurs.toString());
            return;
        }

        try {
            Publication p = new Publication();
            p.setTitre(titre.toUpperCase());
            p.setLieu(lieu.toUpperCase());
            p.setImage(image);
            p.setContenu(description.toUpperCase());
            p.setTarif(tarif);
            p.setActif(actifCheckBox.isSelected());
            p.setType(type);
            p.setDatePublication(LocalDate.now());

            servicePublication.ajouter(p);
            showAlert("Succès", "Publication ajoutée !");
            retourListe();
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private boolean isValidImagePath(String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            try { new URL(path); return true; }
            catch (MalformedURLException e) { return false; }
        } else return path.toLowerCase().matches(".*\\.(jpg|png|jpeg|gif)$");
    }

    private void retourListe() {
        Dashboard.loadView("/Publications.fxml");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
