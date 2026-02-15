package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.entities.logement;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.Status;
import tn.esprit.services.Servicereservationlog;
import tn.esprit.services.Servicelogement;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ReservationFormController {

    @FXML
    private Label nomLabel;
    @FXML
    private Label adresseLabel;
    @FXML
    private Label prixLabel;
    @FXML
    private Label equipementLabel;
    @FXML
    private Label disponibiliteLabel;
    @FXML
    private DatePicker dateArriveePicker;
    @FXML
    private DatePicker dateDepartPicker;
    @FXML
    private Spinner<Integer> dureeSpinner;

    @FXML
    private ComboBox<Status> statusCombo;
    @FXML
    private TextField modaliteField;
    @FXML
    private Label totalLabel;
    @FXML
    private Button confirmerBtn;
    @FXML
    private Button accueilBtn;

    private logement selectedLogement;
    private Servicelogement serviceLogement = new Servicelogement();
    private Servicereservationlog serviceReservation = new Servicereservationlog();

    @FXML
    public void initialize() {
        // Récupérer le logement sélectionné
        selectedLogement = SessionManager.getSelectedLogement();
        if (selectedLogement == null) {
            showAlert("Erreur", "Aucun logement sélectionné.");
            NavigationManager.loadView("/accueil.fxml");
            return;
        }

        // Charger les détails du logement
        nomLabel.setText(selectedLogement.getNom());
        adresseLabel.setText(selectedLogement.getAdresse());
        prixLabel.setText(selectedLogement.getTarif_nuit() + " DT / nuit");
        equipementLabel.setText("Équipement: " + selectedLogement.getEquipement());
        disponibiliteLabel.setText(selectedLogement.isDisponibilite() ? "Disponible" : "Non disponible");

        // Initialiser le ComboBox pour status
        statusCombo.getItems().addAll(Status.confirmée, Status.annulée, Status.en_attente, Status.terminée);
        statusCombo.setValue(Status.en_attente);  // Valeur par défaut

        // Valeur par défaut pour modalite
        modaliteField.setText("En ligne");

        // Calculer le total en temps réel
        dureeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> calculerTotal());
        dateArriveePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculerDuree());
        dateDepartPicker.valueProperty().addListener((obs, oldVal, newVal) -> calculerDuree());

        // Action du bouton Confirmer
        confirmerBtn.setOnAction(e -> confirmerReservation());

        // Action du bouton Accueil
        accueilBtn.setOnAction(e -> NavigationManager.loadView("/accueil.fxml"));
    }

    private void calculerDuree() {
        LocalDate arrivee = dateArriveePicker.getValue();
        LocalDate depart = dateDepartPicker.getValue();
        if (arrivee != null && depart != null && depart.isAfter(arrivee)) {
            long nuits = java.time.temporal.ChronoUnit.DAYS.between(arrivee, depart);
            dureeSpinner.getValueFactory().setValue((int) nuits);
        }
        calculerTotal();
    }

    private void calculerTotal() {
        int nuits = dureeSpinner.getValue();
        double prixParNuit = selectedLogement.getTarif_nuit();
        double total = nuits * prixParNuit;
        totalLabel.setText(total + " DT");
    }

    private void confirmerReservation() {
        // Vérifications
        if (!selectedLogement.isDisponibilite()) {
            showAlert("Erreur", "Le logement n'est pas disponible.");
            return;
        }
        LocalDate arrivee = dateArriveePicker.getValue();
        LocalDate depart = dateDepartPicker.getValue();
        Status status = statusCombo.getValue();
        String modalite = modaliteField.getText().trim();
        if (arrivee == null || depart == null || !depart.isAfter(arrivee)) {
            showAlert("Erreur", "Veuillez sélectionner des dates valides.");
            return;
        }
        if (status == null) {
            showAlert("Erreur", "Veuillez sélectionner un statut.");
            return;
        }
        if (modalite.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir une modalité.");
            return;
        }

        // Calculer le montant
        int nuits = dureeSpinner.getValue();
        float montant = nuits * selectedLogement.getTarif_nuit();

        // Créer la réservation
        reservationlog reservation = new reservationlog();
        reservation.setId_l(selectedLogement.getId());
        // ID client statique en attendant l'intégration
        reservation.setIdc(14); // ← ID fixe, à adapter
        reservation.setDate_debut(Date.from(arrivee.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        reservation.setDate_fin(Date.from(depart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        reservation.setMontant(montant);
        reservation.setStatus(status);
        reservation.setModalite(modalite);

        // Sauvegarder
        try {
            serviceReservation.ajouter(reservation);
            showAlert("Succès", "Réservation confirmée pour " + selectedLogement.getNom() + " ! Montant : " + montant + " DT, Statut : " + status + ", Modalité : " + modalite);
            NavigationManager.loadView("/accueil.fxml");
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de sauvegarder la réservation : " + e.getMessage());
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}