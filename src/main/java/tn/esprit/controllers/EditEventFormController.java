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
import java.time.ZoneId;
import java.util.ResourceBundle;

public class EditEventFormController implements Initializable {

    @FXML private Label idLabel; // Not shown, just for reference
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

    private ServiceEvent serviceEvent;
    private Events currentEvent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();
        categorieCombo.getItems().addAll("Concert", "Spectacle", "Conférence", "Festival", "Sport", "Autre");
        statutCombo.getItems().addAll("Actif", "Annulé", "Reporté", "Complet");
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
        capaciteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, currentEvent.getCapaciteMax()));
        imageUrlField.setText(currentEvent.getImage_url());
        statutCombo.setValue(currentEvent.getStatut());
    }

    @FXML
    private void handleSave() {
        // Validate fields
        if (titreField.getText().isEmpty() || locationField.getText().isEmpty() ||
                prixField.getText().isEmpty() || dateDebutPicker.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }

        try {
            // Update event
            currentEvent.setTitre(titreField.getText());
            currentEvent.setDescription(descriptionField.getText());
            currentEvent.setCategorie(categorieCombo.getValue());
            currentEvent.setLocation(locationField.getText());

            // Update dates
            LocalDate debutDate = dateDebutPicker.getValue();
            LocalDateTime debutDateTime = debutDate.atStartOfDay();
            currentEvent.setDateDebut(Timestamp.valueOf(debutDateTime));

            if (dateFinPicker.getValue() != null) {
                LocalDate finDate = dateFinPicker.getValue();
                LocalDateTime finDateTime = finDate.atTime(23, 59);
                currentEvent.setDateFin(Timestamp.valueOf(finDateTime));
            }

            currentEvent.setPrix(Float.parseFloat(prixField.getText()));

            // Update capacity if changed
            int oldCapacity = currentEvent.getCapaciteMax();
            int newCapacity = capaciteSpinner.getValue();
            if (newCapacity != oldCapacity) {
                int diff = newCapacity - oldCapacity;
                currentEvent.setPlacesRestantes(currentEvent.getPlacesRestantes() + diff);
                currentEvent.setCapaciteMax(newCapacity);
            }

            currentEvent.setImage_url(imageUrlField.getText());
            currentEvent.setStatut(statutCombo.getValue());

            // Save to database
            serviceEvent.modifier(currentEvent);

            showAlert("Succès", "Événement modifié avec succès!");

            // Close window
            ((Stage) titreField.getScene().getWindow()).close();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le prix doit être un nombre valide");
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
}