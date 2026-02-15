package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.Publication;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PublicationFormController {

    @FXML
    private TextField tfTitre, tfLieu;
    @FXML
    private TextArea tfDescription;
    @FXML
    private DatePicker dpDate;
    @FXML
    private Button btnSave, btnCancel;

    private Publication publicationCreated;

    // Pour pré-remplir lors de l'édition
    public void setPublicationToEdit(Publication p) {
        if (p != null) {
            tfTitre.setText(p.getTitre());
            tfLieu.setText(p.getLieu());
            tfDescription.setText(p.getContenu());
            // CORRECTION: Convertir LocalDateTime → LocalDate
            if (p.getDatePublication() != null) {
                dpDate.setValue(p.getDatePublication().toLocalDate());
            }
        } else {
            dpDate.setValue(LocalDate.now());
        }
    }

    public Publication getPublicationCreated() {
        return publicationCreated;
    }

    @FXML
    private void handleSave() {
        // Créer un objet Publication depuis le formulaire
        publicationCreated = new Publication();
        publicationCreated.setTitre(tfTitre.getText());
        publicationCreated.setLieu(tfLieu.getText());
        publicationCreated.setContenu(tfDescription.getText());

        // CORRECTION: Convertir LocalDate → LocalDateTime
        LocalDate date = dpDate.getValue();
        if (date != null) {
            publicationCreated.setDatePublication(date.atStartOfDay()); // ou .atTime(LocalTime.now())
        } else {
            publicationCreated.setDatePublication(LocalDateTime.now());
        }

        publicationCreated.setActif(true);

        ((Stage) tfTitre.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancel() {
        ((Stage) tfTitre.getScene().getWindow()).close();
    }
}