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
    @FXML
    private Button annulerBtn; // Nouveau bouton

    private logement selectedLogement;
    private reservationlog reservationToEdit; // null si nouvelle réservation
    private Servicelogement serviceLogement = new Servicelogement();
    private Servicereservationlog serviceReservation = new Servicereservationlog();

    @FXML
    public void initialize() {
        // Vérifier si on est en mode édition
        reservationToEdit = SessionManager.getEditingReservation();
        if (reservationToEdit != null) {
            // Mode édition : récupérer le logement associé
            try {
                selectedLogement = serviceLogement.rechercherParId(reservationToEdit.getId_l());
                if (selectedLogement == null) {
                    showAlert("Erreur", "Logement introuvable pour cette réservation.");
                    NavigationManager.loadView("/fxml/mesreservations.fxml");
                    return;
                }
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de charger le logement : " + e.getMessage());
                NavigationManager.loadView("/fxml/mesreservations.fxml");
                return;
            }
        } else {
            // Mode création : récupérer le logement sélectionné depuis la session
            selectedLogement = SessionManager.getSelectedLogement();
            if (selectedLogement == null) {
                showAlert("Erreur", "Aucun logement sélectionné.");
                NavigationManager.loadView("/fxml/accueil.fxml");
                return;
            }
        }

        // Afficher les infos du logement
        nomLabel.setText(selectedLogement.getNom());
        adresseLabel.setText(selectedLogement.getAdresse());
        prixLabel.setText(selectedLogement.getTarif_nuit() + " DT / nuit");
        equipementLabel.setText("Équipement: " + selectedLogement.getEquipement());
        disponibiliteLabel.setText(selectedLogement.isDisponibilite() ? "Disponible" : "Non disponible");

        // Initialiser la combo statut
        statusCombo.getItems().addAll(Status.values());
        statusCombo.setValue(Status.en_attente);

        modaliteField.setText("En ligne");

        // Si édition, pré-remplir les champs
        if (reservationToEdit != null) {
            // Convertir java.util.Date en LocalDate
            LocalDate arrivee = reservationToEdit.getDate_debut().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate depart = reservationToEdit.getDate_fin().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            dateArriveePicker.setValue(arrivee);
            dateDepartPicker.setValue(depart);
            statusCombo.setValue(reservationToEdit.getStatus());
            modaliteField.setText(reservationToEdit.getModalite());
            // La durée sera calculée automatiquement par le listener
        }

        // Listeners
        dureeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> calculerTotal());
        dateArriveePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculerDuree());
        dateDepartPicker.valueProperty().addListener((obs, oldVal, newVal) -> calculerDuree());

        confirmerBtn.setOnAction(e -> confirmerReservation());
        accueilBtn.setOnAction(e -> NavigationManager.loadView("/fxml/accueil.fxml"));

        // Bouton Annuler : retour à la liste des réservations
        if (annulerBtn != null) {
            annulerBtn.setOnAction(e -> {
                SessionManager.clearEditingReservation();
                NavigationManager.loadView("/fxml/mesreservations.fxml");
            });
        }
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

    private boolean validerSaisie() {
        if (!selectedLogement.isDisponibilite()) {
            showAlert("Erreur", "Le logement n'est pas disponible.");
            return false;
        }
        LocalDate arrivee = dateArriveePicker.getValue();
        LocalDate depart = dateDepartPicker.getValue();
        if (arrivee == null) {
            showAlert("Erreur", "Veuillez sélectionner une date d'arrivée.");
            return false;
        }
        if (depart == null) {
            showAlert("Erreur", "Veuillez sélectionner une date de départ.");
            return false;
        }
        if (arrivee.isBefore(LocalDate.now())) {
            showAlert("Erreur", "La date d'arrivée ne peut pas être dans le passé.");
            return false;
        }
        if (!depart.isAfter(arrivee)) {
            showAlert("Erreur", "La date de départ doit être postérieure à la date d'arrivée.");
            return false;
        }
        Status status = statusCombo.getValue();
        if (status == null) {
            showAlert("Erreur", "Veuillez sélectionner un statut.");
            return false;
        }
        String modalite = modaliteField.getText().trim();
        if (modalite.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir une modalité.");
            return false;
        }
        int nuits = dureeSpinner.getValue();
        if (nuits <= 0) {
            showAlert("Erreur", "La durée doit être d'au moins 1 nuit.");
            return false;
        }
        return true;
    }

    private void confirmerReservation() {
        if (!validerSaisie()) return;

        LocalDate arrivee = dateArriveePicker.getValue();
        LocalDate depart = dateDepartPicker.getValue();
        Status status = statusCombo.getValue();
        String modalite = modaliteField.getText().trim();
        int nuits = dureeSpinner.getValue();
        float montant = nuits * selectedLogement.getTarif_nuit();

        if (reservationToEdit == null) {
            // Nouvelle réservation
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
                showAlert("Succès", "Réservation confirmée pour " + selectedLogement.getNom() + " !");
                SessionManager.clearEditingReservation();
                NavigationManager.loadView("/fxml/mesreservations.fxml");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de sauvegarder la réservation : " + e.getMessage());
            }
        } else {
            // Modification
            reservationToEdit.setDate_debut(Date.from(arrivee.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            reservationToEdit.setDate_fin(Date.from(depart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            reservationToEdit.setMontant(montant);
            reservationToEdit.setStatus(status);
            reservationToEdit.setModalite(modalite);
            // Note : id_l et idc ne changent pas

            try {
                serviceReservation.modifier(reservationToEdit);
                showAlert("Succès", "Réservation modifiée avec succès !");
                SessionManager.clearEditingReservation();
                NavigationManager.loadView("/fxml/mesreservations.fxml");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de modifier la réservation : " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}