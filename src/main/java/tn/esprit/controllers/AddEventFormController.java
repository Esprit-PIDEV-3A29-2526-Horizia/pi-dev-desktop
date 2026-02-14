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

    private ServiceEvent serviceEvent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();
        categorieCombo.getItems().addAll("Concert", "Spectacle", "Conférence", "Festival", "Sport", "Autre");
        categorieCombo.setValue("Concert");
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
            // Parse price
            float prix = Float.parseFloat(prixField.getText());

            // Parse dates
            LocalDate debutDate = dateDebutPicker.getValue();
            LocalTime debutTime = LocalTime.of(0, 0); // Default time
            LocalDateTime debutDateTime = LocalDateTime.of(debutDate, debutTime);
            Timestamp dateDebut = Timestamp.valueOf(debutDateTime);

            Timestamp dateFin = null;
            if (dateFinPicker.getValue() != null) {
                LocalDate finDate = dateFinPicker.getValue();
                LocalDateTime finDateTime = LocalDateTime.of(finDate, LocalTime.of(23, 59));
                dateFin = Timestamp.valueOf(finDateTime);
            }

            // Create event
            Events event = new Events();
            event.setTitre(titreField.getText());
            event.setDescription(descriptionField.getText());
            event.setCategorie(categorieCombo.getValue());
            event.setLocation(locationField.getText());
            event.setDateDebut(dateDebut);
            event.setDateFin(dateFin);
            event.setPrix(prix);
            event.setCapaciteMax(capaciteSpinner.getValue());
            event.setPlacesRestantes(capaciteSpinner.getValue()); // Initially same as capacity
            event.setImage_url(imageUrlField.getText());
            event.setStatut("Actif");
            event.setId_createur(1); // Default admin ID for now

            // Save to database
            serviceEvent.ajouter(event);

            showAlert("Succès", "Événement ajouté avec succès!");

            // Close window
            ((Stage) titreField.getScene().getWindow()).close();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le prix doit être un nombre valide");
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