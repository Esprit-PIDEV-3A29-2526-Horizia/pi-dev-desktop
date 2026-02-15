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
        selectedLogement = SessionManager.getSelectedLogement();
        if (selectedLogement == null) {
            showAlert("Erreur", "Aucun logement sélectionné.");
            NavigationManager.loadView("/accueil.fxml");
            return;
        }

        nomLabel.setText(selectedLogement.getNom());
        adresseLabel.setText(selectedLogement.getAdresse());
        prixLabel.setText(selectedLogement.getTarif_nuit() + " DT / nuit");
        equipementLabel.setText("Équipement: " + selectedLogement.getEquipement());
        disponibiliteLabel.setText(selectedLogement.isDisponibilite() ? "Disponible" : "Non disponible");

        statusCombo.getItems().addAll(Status.confirmée, Status.annulée, Status.en_attente, Status.terminée);
        statusCombo.setValue(Status.en_attente);

        modaliteField.setText("En ligne");

        dureeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> calculerTotal());
        dateArriveePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculerDuree());
        dateDepartPicker.valueProperty().addListener((obs, oldVal, newVal) -> calculerDuree());

        confirmerBtn.setOnAction(e -> confirmerReservation());
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

    /**
     * Valide les données saisies avant confirmation.
     * @return true si toutes les validations passent, sinon false.
     */
    private boolean validerSaisie() {
        // Vérifier disponibilité du logement
        if (!selectedLogement.isDisponibilite()) {
            showAlert("Erreur", "Le logement n'est pas disponible.");
            return false;
        }

        LocalDate arrivee = dateArriveePicker.getValue();
        LocalDate depart = dateDepartPicker.getValue();

        // Vérifier que les dates sont sélectionnées
        if (arrivee == null) {
            showAlert("Erreur", "Veuillez sélectionner une date d'arrivée.");
            return false;
        }
        if (depart == null) {
            showAlert("Erreur", "Veuillez sélectionner une date de départ.");
            return false;
        }

        // Vérifier que la date d'arrivée n'est pas dans le passé (avant aujourd'hui)
        LocalDate aujourdHui = LocalDate.now();
        if (arrivee.isBefore(aujourdHui)) {
            showAlert("Erreur", "La date d'arrivée ne peut pas être dans le passé.");
            return false;
        }

        // Vérifier que la date de départ est après la date d'arrivée
        if (!depart.isAfter(arrivee)) {
            showAlert("Erreur", "La date de départ doit être postérieure à la date d'arrivée.");
            return false;
        }

        // Vérifier que le statut est sélectionné
        Status status = statusCombo.getValue();
        if (status == null) {
            showAlert("Erreur", "Veuillez sélectionner un statut.");
            return false;
        }

        // Vérifier que la modalité n'est pas vide
        String modalite = modaliteField.getText().trim();
        if (modalite.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir une modalité.");
            return false;
        }

        // (Optionnel) Vérifier que la durée est positive (normalement déjà assuré par le spinner)
        int nuits = dureeSpinner.getValue();
        if (nuits <= 0) {
            showAlert("Erreur", "La durée doit être d'au moins 1 nuit.");
            return false;
        }

        return true;
    }

    private void confirmerReservation() {
        if (!validerSaisie()) {
            return; // Arrêter si validation échoue
        }

        // Optionnel : demande de confirmation avant l'insertion
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Voulez-vous confirmer cette réservation ?");
        confirmation.setContentText("Vérifiez les informations avant de confirmer.");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        LocalDate arrivee = dateArriveePicker.getValue();
        LocalDate depart = dateDepartPicker.getValue();
        Status status = statusCombo.getValue();
        String modalite = modaliteField.getText().trim();

        int nuits = dureeSpinner.getValue();
        float montant = nuits * selectedLogement.getTarif_nuit();

        reservationlog reservation = new reservationlog();
        reservation.setId_l(selectedLogement.getId());
        reservation.setIdc(14); // ID statique
        reservation.setDate_debut(Date.from(arrivee.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        reservation.setDate_fin(Date.from(depart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        reservation.setMontant(montant);
        reservation.setStatus(status);
        reservation.setModalite(modalite);

        try {
            serviceReservation.ajouter(reservation);
            showAlert("Succès", "Réservation confirmée pour " + selectedLogement.getNom() + " !\n" +
                    "Montant : " + montant + " DT\n" +
                    "Statut : " + status + "\n" +
                    "Modalité : " + modalite);
            NavigationManager.loadView("/mesreservations.fxml");
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