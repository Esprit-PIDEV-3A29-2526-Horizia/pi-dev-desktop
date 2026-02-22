package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.entities.logement;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.Status;
import tn.esprit.entities.User;
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
    private Button annulerBtn;

    private logement selectedLogement;
    private reservationlog reservationToEdit;
    private User currentUser; // Ajout de l'utilisateur courant
    private Servicelogement serviceLogement = new Servicelogement();
    private Servicereservationlog serviceReservation = new Servicereservationlog();

    @FXML
    public void initialize() {
        // Récupérer l'utilisateur connecté
        currentUser = SessionManager.getCurrentUser();

        // Vérifier que l'utilisateur est connecté
        if (currentUser == null) {
            showAlert("Erreur", "Vous devez être connecté pour faire une réservation");
            NavigationManager.loadView("/fxml/Login.fxml");
            return;
        }

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
        afficherInfosLogement();

        // Initialiser la combo statut
        statusCombo.getItems().addAll(Status.values());
        statusCombo.setValue(Status.en_attente);

        modaliteField.setText("En ligne");

        // Si édition, pré-remplir les champs
        if (reservationToEdit != null) {
            preRemplirChamps();
        }

        // Listeners
        setupListeners();

        // Actions des boutons
        confirmerBtn.setOnAction(e -> confirmerReservation());
        accueilBtn.setOnAction(e -> NavigationManager.loadView("/fxml/accueil.fxml"));

        if (annulerBtn != null) {
            annulerBtn.setOnAction(e -> {
                SessionManager.clearEditingReservation();
                NavigationManager.loadView("/fxml/mesreservations.fxml");
            });
        }
    }

    private void afficherInfosLogement() {
        nomLabel.setText(selectedLogement.getNom());
        adresseLabel.setText(selectedLogement.getAdresse());
        prixLabel.setText(selectedLogement.getTarif_nuit() + " DT / nuit");
        equipementLabel.setText("Équipement: " + selectedLogement.getEquipement());
        disponibiliteLabel.setText(selectedLogement.isDisponibilite() ? "Disponible" : "Non disponible");
    }

    private void preRemplirChamps() {
        // Convertir java.util.Date en LocalDate
        LocalDate arrivee = reservationToEdit.getDate_debut().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate depart = reservationToEdit.getDate_fin().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        dateArriveePicker.setValue(arrivee);
        dateDepartPicker.setValue(depart);
        statusCombo.setValue(reservationToEdit.getStatus());
        modaliteField.setText(reservationToEdit.getModalite());
    }

    private void setupListeners() {
        dureeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> calculerTotal());
        dateArriveePicker.valueProperty().addListener((obs, oldVal, newVal) -> calculerDuree());
        dateDepartPicker.valueProperty().addListener((obs, oldVal, newVal) -> calculerDuree());
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

        // Vérifier que l'utilisateur est connecté
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Erreur", "Vous devez être connecté pour réserver");
            NavigationManager.loadView("/fxml/Login.fxml");
            return;
        }

        LocalDate arrivee = dateArriveePicker.getValue();
        LocalDate depart = dateDepartPicker.getValue();
        Status status = statusCombo.getValue();
        String modalite = modaliteField.getText().trim();
        int nuits = dureeSpinner.getValue();
        float montant = nuits * selectedLogement.getTarif_nuit();

        try {
            if (reservationToEdit == null) {
                // NOUVELLE RÉSERVATION
                reservationlog reservation = new reservationlog();
                reservation.setId_l(selectedLogement.getId());
                reservation.setIdc(currentUser.getId()); // ID DYNAMIQUE
                reservation.setDate_debut(Date.from(arrivee.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                reservation.setDate_fin(Date.from(depart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                reservation.setMontant(montant);
                reservation.setStatus(status);
                reservation.setModalite(modalite);

                System.out.println("Création réservation - ID Client: " + currentUser.getId());

                serviceReservation.ajouter(reservation);
                showAlert("Succès", "✅ Réservation confirmée !");

            } else {
                // MODIFICATION
                reservationToEdit.setDate_debut(Date.from(arrivee.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                reservationToEdit.setDate_fin(Date.from(depart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                reservationToEdit.setMontant(montant);
                reservationToEdit.setStatus(status);
                reservationToEdit.setModalite(modalite);

                serviceReservation.modifier(reservationToEdit);
                showAlert("Succès", "✅ Réservation modifiée !");
            }

            // NETTOYAGE
            SessionManager.clearSelectedLogement();  // Maintenant cette méthode existe
            SessionManager.clearEditingReservation();

            // REDIRECTION
            NavigationManager.loadView("/fxml/mesreservations.fxml");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "❌ " + e.getMessage());
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}