package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Events;
import tn.esprit.services.ServiceEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;


import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

public class EditEventFormController implements Initializable {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private TextField locationField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField prixField;
    @FXML private Spinner<Integer> capaciteSpinner;
    @FXML private TextField imageUrlField;
    @FXML private ComboBox<String> statutCombo;

    @FXML private Button pickLocationBtn;

    @FXML private Label titreError;
    @FXML private Label descriptionError;
    @FXML private Label categorieError;
    @FXML private Label locationError;
    @FXML private Label dateDebutError;
    @FXML private Label dateFinError;
    @FXML private Label prixError;
    @FXML private Label capaciteError;
    @FXML private Label imageError;
    @FXML private Label statutError;


    private ServiceEvent serviceEvent;
    private Events currentEvent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();

        categorieCombo.getItems().addAll("Concert", "Spectacle", "Conférence", "Festival", "Sport", "Autre");
        statutCombo.getItems().addAll("Actif", "Annulé", "Reporté", "Complet");

        capaciteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 100));

        setupValidation();
        //pickLocationBtn.setOnAction(e -> openMapPicker());
    }

    public void setEvent(Events event) {
        this.currentEvent = event;
        loadEventData();
    }

    private void loadEventData() {
        titreField.setText(currentEvent.getTitre());
        descriptionField.setText(currentEvent.getDescription());
        categorieCombo.setValue(currentEvent.getCategorie());
        locationField.setText(currentEvent.getLocation());

        if (currentEvent.getDateDebut() != null) {
            dateDebutPicker.setValue(currentEvent.getDateDebut().toLocalDateTime().toLocalDate());
        }
        if (currentEvent.getDateFin() != null) {
            dateFinPicker.setValue(currentEvent.getDateFin().toLocalDateTime().toLocalDate());
        }

        prixField.setText(String.valueOf(currentEvent.getPrix()));
        capaciteSpinner.getValueFactory().setValue(currentEvent.getCapaciteMax());
        imageUrlField.setText(currentEvent.getImage_url());
        statutCombo.setValue(currentEvent.getStatut());
    }

    private void setupValidation() {
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

        categorieCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                categorieError.setText("❌ Sélectionnez une catégorie");
                categorieError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else {
                categorieError.setText("✓ Valide");
                categorieError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
            }
        });

        statutCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                statutError.setText("❌ Sélectionnez un statut");
                statutError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else {
                statutError.setText("✓ Valide");
                statutError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
            }
        });

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

        dateDebutPicker.valueProperty().addListener((obs, old, newVal) -> validateDates());
        dateFinPicker.valueProperty().addListener((obs, old, newVal) -> validateDates());


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

        capaciteSpinner.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal <= 0) {
                capaciteError.setText("❌ Capacité invalide");
                capaciteError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            } else {
                capaciteError.setText("✓ Valide");
                capaciteError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
            }
        });

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

        if (debut == null) {
            dateDebutError.setText("❌ Date début obligatoire");
            dateDebutError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        } else {
            dateDebutError.setText("✓ Valide");
            dateDebutError.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
        }

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

        if (titreField.getText().isEmpty() || titreField.getText().length() < 3) {
            titreError.setText("❌ Titre invalide");
            titreError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            isValid = false;
        }

        if (categorieCombo.getValue() == null) {
            categorieError.setText("❌ Catégorie requise");
            categorieError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            isValid = false;
        }

        if (statutCombo.getValue() == null) {
            statutError.setText("❌ Statut requis");
            statutError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            isValid = false;
        }

        if (locationField.getText().isEmpty() || locationField.getText().length() < 3) {
            locationError.setText("❌ Lieu invalide");
            locationError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            isValid = false;
        }

        if (dateDebutPicker.getValue() == null) {
            dateDebutError.setText("❌ Date début requise");
            dateDebutError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            isValid = false;
        }

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
            currentEvent.setTitre(titreField.getText());
            currentEvent.setDescription(descriptionField.getText());
            currentEvent.setCategorie(categorieCombo.getValue());
            currentEvent.setLocation(locationField.getText());

            LocalDate debutDate = dateDebutPicker.getValue();
            LocalDateTime debutDateTime = debutDate.atStartOfDay();
            currentEvent.setDateDebut(Timestamp.valueOf(debutDateTime));

            if (dateFinPicker.getValue() != null) {
                LocalDate finDate = dateFinPicker.getValue();
                LocalDateTime finDateTime = finDate.atTime(23, 59);
                currentEvent.setDateFin(Timestamp.valueOf(finDateTime));
            }

            currentEvent.setPrix(Float.parseFloat(prixField.getText()));

            int oldCapacity = currentEvent.getCapaciteMax();
            int newCapacity = capaciteSpinner.getValue();
            if (newCapacity != oldCapacity) {
                int diff = newCapacity - oldCapacity;
                currentEvent.setPlacesRestantes(currentEvent.getPlacesRestantes() + diff);
                currentEvent.setCapaciteMax(newCapacity);
            }

            currentEvent.setImage_url(imageUrlField.getText());
            currentEvent.setStatut(statutCombo.getValue());

            serviceEvent.modifier(currentEvent);

            showAlert("Succès", "Événement modifié avec succès!");
            ((Stage) titreField.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
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

    private double selectedLat = 0;
    private double selectedLng = 0;

    /*private void openMapPicker() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MapPicker.fxml"));
            DialogPane dialogPane = loader.load();

            MapPickerController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Sélectionner un emplacement");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String address = controller.getSelectedAddress();
                selectedLat = controller.getSelectedLat();
                selectedLng = controller.getSelectedLng();

                if (!address.isEmpty()) {
                    locationField.setText(address);
                    // Vous pouvez stocker les coordonnées dans la base de données
                    // event.setLatitude(selectedLat);
                    // event.setLongitude(selectedLng);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la carte");
        }
    }*/
}