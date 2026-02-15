package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Events;
import tn.esprit.services.ServiceEvent;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class AddEventFormController implements Initializable {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private TextField locationField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField prixField;
    @FXML private Spinner<Integer> capaciteSpinner;
    @FXML private TextField imageUrlField;

    // Labels d'erreur
    @FXML private Label titreError;
    @FXML private Label descriptionError;
    @FXML private Label categorieError;
    @FXML private Label locationError;
    @FXML private Label dateDebutError;
    @FXML private Label dateFinError;
    @FXML private Label prixError;
    @FXML private Label capaciteError;
    @FXML private Label imageError;

    private ServiceEvent serviceEvent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();

        // Initialisation de la combo catégorie
        categorieCombo.getItems().addAll("Concert", "Spectacle", "Conférence", "Festival", "Sport", "Autre");
        categorieCombo.setValue("Concert");

        // Initialisation du spinner
        capaciteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 100));

        // Mettre en place toutes les validations
        setupValidation();
    }

    private void setupValidation() {
        // ===== TITRE =====
        titreField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) {
                titreError.setText("❌ Le titre est obligatoire");
                titreError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else if (newVal.length() < 3) {
                titreError.setText("❌ Minimum 3 caractères");
                titreError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else if (newVal.length() > 100) {
                titreError.setText("❌ Maximum 100 caractères");
                titreError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else {
                titreError.setText("✓ Valide");
                titreError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
            }
        });

        // ===== DESCRIPTION =====
        descriptionField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.length() > 500) {
                descriptionError.setText("❌ Maximum 500 caractères");
                descriptionError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else if (newVal.isEmpty()) {
                descriptionError.setText("ℹ️ Optionnel");
                descriptionError.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
            } else {
                descriptionError.setText("✓ Valide");
                descriptionError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
            }
        });

        // ===== CATÉGORIE =====
        categorieCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                categorieError.setText("❌ Sélectionnez une catégorie");
                categorieError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else {
                categorieError.setText("✓ Valide");
                categorieError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
            }
        });

        // ===== LIEU =====
        locationField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) {
                locationError.setText("❌ Le lieu est obligatoire");
                locationError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else if (newVal.length() < 3) {
                locationError.setText("❌ Minimum 3 caractères");
                locationError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else {
                locationError.setText("✓ Valide");
                locationError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
            }
        });

        // ===== DATES =====
        dateDebutPicker.valueProperty().addListener((obs, old, newVal) -> validateDates());
        dateFinPicker.valueProperty().addListener((obs, old, newVal) -> validateDates());

        // ===== PRIX =====
        prixField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) {
                prixError.setText("❌ Le prix est obligatoire");
                prixError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
                return;
            }

            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                prixError.setText("❌ Chiffres uniquement");
                prixError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
                return;
            }

            try {
                float prix = Float.parseFloat(newVal);
                if (prix <= 0) {
                    prixError.setText("❌ Prix doit être > 0");
                    prixError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
                } else if (prix > 100000) {
                    prixError.setText("❌ Prix maximum 100000 DT");
                    prixError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
                } else {
                    prixError.setText("✓ Valide");
                    prixError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
                }
            } catch (NumberFormatException e) {
                prixError.setText("❌ Format invalide");
                prixError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            }
        });

        // ===== CAPACITÉ =====
        capaciteSpinner.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal <= 0) {
                capaciteError.setText("❌ Capacité invalide");
                capaciteError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else {
                capaciteError.setText("✓ Valide");
                capaciteError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
            }
        });

        // ===== IMAGE URL =====
        imageUrlField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) {
                imageError.setText("ℹ️ Optionnel");
                imageError.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
            } else if (!newVal.matches("^(http|https)://.*\\.(jpg|jpeg|png|gif|webp|bmp|svg).*$")) {
                imageError.setText("❌ URL d'image invalide");
                imageError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else {
                imageError.setText("✓ Valide");
                imageError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
            }
        });
    }

    private void validateDates() {
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin = dateFinPicker.getValue();

        // Validation date début
        if (debut == null) {
            dateDebutError.setText("❌ Date début obligatoire");
            dateDebutError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        } else if (debut.isBefore(LocalDate.now())) {
            dateDebutError.setText("❌ Doit être dans le futur");
            dateDebutError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        } else {
            dateDebutError.setText("✓ Valide");
            dateDebutError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
        }

        // Validation date fin
        if (fin != null) {
            if (debut != null && fin.isBefore(debut)) {
                dateFinError.setText("❌ Après date début");
                dateFinError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else {
                dateFinError.setText("✓ Valide");
                dateFinError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
            }
        } else {
            dateFinError.setText("ℹ️ Optionnel");
            dateFinError.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
        }
    }

    private boolean validateAll() {
        boolean isValid = true;

        // Titre
        if (titreField.getText().isEmpty() || titreField.getText().length() < 3) {
            titreError.setText("❌ Titre invalide");
            titreError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            isValid = false;
        }

        // Catégorie
        if (categorieCombo.getValue() == null) {
            categorieError.setText("❌ Catégorie requise");
            categorieError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            isValid = false;
        }

        // Lieu
        if (locationField.getText().isEmpty() || locationField.getText().length() < 3) {
            locationError.setText("❌ Lieu invalide");
            locationError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            isValid = false;
        }

        // Date début
        if (dateDebutPicker.getValue() == null || dateDebutPicker.getValue().isBefore(LocalDate.now())) {
            dateDebutError.setText("❌ Date début invalide");
            dateDebutError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            isValid = false;
        }

        // Prix
        try {
            float prix = Float.parseFloat(prixField.getText());
            if (prix <= 0 || prix > 100000) {
                prixError.setText("❌ Prix invalide");
                prixError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
                isValid = false;
            }
        } catch (Exception e) {
            prixError.setText("❌ Prix invalide");
            prixError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void handleSave() {
        if (!validateAll()) {
            showAlert("Erreur de validation", "Veuillez corriger les erreurs dans le formulaire");
            return;
        }

        try {
            float prix = Float.parseFloat(prixField.getText());

            LocalDate debutDate = dateDebutPicker.getValue();
            LocalDateTime debutDateTime = debutDate.atStartOfDay();
            Timestamp dateDebut = Timestamp.valueOf(debutDateTime);

            Timestamp dateFin = null;
            if (dateFinPicker.getValue() != null) {
                LocalDate finDate = dateFinPicker.getValue();
                LocalDateTime finDateTime = finDate.atTime(23, 59);
                dateFin = Timestamp.valueOf(finDateTime);
            }

            Events event = new Events();
            event.setTitre(titreField.getText());
            event.setDescription(descriptionField.getText());
            event.setCategorie(categorieCombo.getValue());
            event.setLocation(locationField.getText());
            event.setDateDebut(dateDebut);
            event.setDateFin(dateFin);
            event.setPrix(prix);
            event.setCapaciteMax(capaciteSpinner.getValue());
            event.setPlacesRestantes(capaciteSpinner.getValue());
            event.setImage_url(imageUrlField.getText());
            event.setStatut("Actif");
            event.setId_createur(1);

            serviceEvent.ajouter(event);
            showAlert("Succès", "Événement ajouté avec succès!");
            ((Stage) titreField.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) titreField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}