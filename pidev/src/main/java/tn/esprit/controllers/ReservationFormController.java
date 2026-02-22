package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import tn.esprit.entities.logement;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.Status;
import tn.esprit.entities.User;
import tn.esprit.services.Servicereservationlog;
import tn.esprit.services.Servicelogement;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

public class ReservationFormController {
    @FXML private Label prixParNuitLabel;
    @FXML private ImageView imageLogement;
    @FXML private Label nomLabel;
    @FXML private Label typeLabel;
    @FXML private Label adresseLabel;
    @FXML private Label capaciteLabel;
    @FXML private Label prixLabel;
    @FXML private Label equipementLabel;
    @FXML private Label disponibiliteLabel;
    @FXML private DatePicker dateArriveePicker;
    @FXML private DatePicker dateDepartPicker;
    @FXML private Spinner<Integer> adultesSpinner;
    @FXML private Spinner<Integer> enfantsSpinner;
    @FXML private RadioButton enLigneRadio;
    @FXML private RadioButton surPlaceRadio;
    @FXML private ToggleGroup modaliteGroup;
    @FXML private Label totalLabel;
    @FXML private Label infoPaiementLabel;
    @FXML private Button confirmerBtn;
    @FXML private Button accueilBtn;
    @FXML private Button annulerBtn;
    @FXML private HBox userBox;
    @FXML private Label userNameLabel;
    @FXML private Label totalNuitLabel;
    @FXML private Label nuitsLabel; // Nouveau label pour afficher le nombre de nuits

    // Labels d'erreur
    @FXML private Label arriveeErrorLabel;
    @FXML private Label departErrorLabel;
    @FXML private Label dureeErrorLabel;
    @FXML private Label modaliteErrorLabel;
    @FXML private Label capaciteErrorLabel;

    private logement selectedLogement;
    private reservationlog reservationToEdit;
    private User currentUser;
    private Servicelogement serviceLogement = new Servicelogement();
    private Servicereservationlog serviceReservation = new Servicereservationlog();

    private int currentNuits = 1; // valeur par défaut

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Erreur", "Vous devez être connecté pour faire une réservation");
            NavigationManager.loadView("/fxml/Login.fxml");
            return;
        }

        setupUserBox();

        reservationToEdit = SessionManager.getEditingReservation();
        if (reservationToEdit != null) {
            try {
                selectedLogement = serviceLogement.rechercherParId(reservationToEdit.getId_l());
                if (selectedLogement == null) {
                    showAlert("Erreur", "Logement introuvable.");
                    NavigationManager.loadView("/fxml/mesreservations.fxml");
                    return;
                }
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de charger le logement.");
                NavigationManager.loadView("/fxml/mesreservations.fxml");
                return;
            }
        } else {
            selectedLogement = SessionManager.getSelectedLogement();
            if (selectedLogement == null) {
                showAlert("Erreur", "Aucun logement sélectionné.");
                NavigationManager.loadView("/fxml/accueil.fxml");
                return;
            }
        }

        afficherInfosLogement();

        // Initialisation des spinners avec la capacité du logement
        int capaciteMax = selectedLogement.getCapacite();
        adultesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, capaciteMax, 1));
        enfantsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, capaciteMax - 1, 0));

        // Valeur par défaut pour la modalité
        enLigneRadio.setSelected(true);

        if (reservationToEdit != null) {
            preRemplirChamps();
        }

        setupListeners();

        confirmerBtn.setOnAction(e -> confirmerReservation());
        accueilBtn.setOnAction(e -> NavigationManager.loadView("/fxml/accueil.fxml"));
        annulerBtn.setOnAction(e -> {
            SessionManager.clearEditingReservation();
            NavigationManager.loadView("/fxml/mesreservations.fxml");
        });

        resetErrorLabels();
        calculerTotal(); // initialise l'affichage
    }

    private void setupUserBox() {
        if (SessionManager.isLoggedIn() && currentUser != null) {
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            }
            if (userBox != null) {
                userBox.setCursor(javafx.scene.Cursor.HAND);
                userBox.setOnMouseClicked(e -> showUserProfile());
                userBox.setOnMouseEntered(e -> onUserBoxHover());
                userBox.setOnMouseExited(e -> onUserBoxExit());
            }
        } else {
            if (userNameLabel != null) {
                userNameLabel.setText("Connexion");
            }
            if (userBox != null) {
                userBox.setCursor(javafx.scene.Cursor.HAND);
                userBox.setOnMouseClicked(e -> NavigationManager.loadView("/fxml/Login.fxml"));
            }
        }
    }

    private void onUserBoxHover() {
        if (userBox != null) {
            userBox.setStyle("-fx-background-color: #2C7AA0; -fx-background-radius: 25; -fx-padding: 8 20; -fx-cursor: hand; " +
                    "-fx-scale-x: 1.05; -fx-scale-y: 1.05; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");
        }
    }

    private void onUserBoxExit() {
        if (userBox != null) {
            userBox.setStyle("-fx-background-color: #3D94CA; -fx-background-radius: 25; -fx-padding: 8 20; -fx-cursor: hand; " +
                    "-fx-scale-x: 1.0; -fx-scale-y: 1.0; -fx-effect: null;");
        }
    }

    private void showUserProfile() {
        if (SessionManager.isLoggedIn() && currentUser != null) {
            NavigationManager.loadView("/fxml/UserProfil.fxml");
        } else {
            NavigationManager.loadView("/fxml/Login.fxml");
        }
    }

    private void resetErrorLabels() {
        if (arriveeErrorLabel != null) {
            arriveeErrorLabel.setVisible(false);
            arriveeErrorLabel.setManaged(false);
        }
        if (departErrorLabel != null) {
            departErrorLabel.setVisible(false);
            departErrorLabel.setManaged(false);
        }
        if (dureeErrorLabel != null) {
            dureeErrorLabel.setVisible(false);
            dureeErrorLabel.setManaged(false);
        }
        if (modaliteErrorLabel != null) {
            modaliteErrorLabel.setVisible(false);
            modaliteErrorLabel.setManaged(false);
        }
        if (capaciteErrorLabel != null) {
            capaciteErrorLabel.setVisible(false);
            capaciteErrorLabel.setManaged(false);
        }
    }

    private void afficherInfosLogement() {
        prixParNuitLabel.setText(String.format("%.0f DT", selectedLogement.getTarif_nuit()));
        String imagePath = selectedLogement.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                if (imagePath.startsWith("http")) {
                    imageLogement.setImage(new Image(imagePath, true));
                } else {
                    InputStream is = getClass().getResourceAsStream(imagePath);
                    if (is != null) {
                        imageLogement.setImage(new Image(is));
                    } else {
                        java.io.File file = new java.io.File("uploads/" + imagePath);
                        if (file.exists()) {
                            imageLogement.setImage(new Image(file.toURI().toString()));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            imageLogement.setImage(null);
        }

        nomLabel.setText(selectedLogement.getNom());
        typeLabel.setText("Type: " + selectedLogement.getType());
        adresseLabel.setText("📍 " + selectedLogement.getAdresse());
        capaciteLabel.setText("👥 Capacité: " + selectedLogement.getCapacite() + " personnes");
        prixLabel.setText(String.format("%.0f DT", selectedLogement.getTarif_nuit()));
        equipementLabel.setText("⚙️ " + selectedLogement.getEquipement());
        disponibiliteLabel.setText(selectedLogement.isDisponibilite() ? "Disponible" : "Non disponible");
    }

    private void preRemplirChamps() {
        LocalDate arrivee = reservationToEdit.getDate_debut().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate depart = reservationToEdit.getDate_fin().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        dateArriveePicker.setValue(arrivee);
        dateDepartPicker.setValue(depart);

        String modalite = reservationToEdit.getModalite();
        if ("En ligne".equals(modalite)) {
            enLigneRadio.setSelected(true);
        } else if ("Cash".equals(modalite) || "Sur place".equals(modalite)) {
            surPlaceRadio.setSelected(true);
        } else {
            enLigneRadio.setSelected(true);
        }

        // Recalculer la durée après avoir rempli les dates
        calculerDuree();
    }

    private void setupListeners() {
        dateArriveePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            calculerDuree();
            if (arriveeErrorLabel != null) {
                arriveeErrorLabel.setVisible(false);
                arriveeErrorLabel.setManaged(false);
            }
        });
        dateDepartPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            calculerDuree();
            if (departErrorLabel != null) {
                departErrorLabel.setVisible(false);
                departErrorLabel.setManaged(false);
            }
        });
        modaliteGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (modaliteErrorLabel != null) {
                modaliteErrorLabel.setVisible(false);
                modaliteErrorLabel.setManaged(false);
            }
        });
        adultesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> verifierCapacite());
        enfantsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> verifierCapacite());
    }

    private void verifierCapacite() {
        int adultes = adultesSpinner.getValue();
        int enfants = enfantsSpinner.getValue();
        if (adultes + enfants > selectedLogement.getCapacite()) {
            int maxEnfants = selectedLogement.getCapacite() - adultes;
            if (maxEnfants < 0) {
                adultesSpinner.getValueFactory().setValue(selectedLogement.getCapacite());
                enfantsSpinner.getValueFactory().setValue(0);
            } else {
                enfantsSpinner.getValueFactory().setValue(maxEnfants);
            }
            if (capaciteErrorLabel != null) {
                capaciteErrorLabel.setText("Capacité maximale dépassée !");
                capaciteErrorLabel.setVisible(true);
                capaciteErrorLabel.setManaged(true);
            }
        } else {
            if (capaciteErrorLabel != null) {
                capaciteErrorLabel.setVisible(false);
                capaciteErrorLabel.setManaged(false);
            }
        }
    }

    private void calculerDuree() {
        LocalDate arrivee = dateArriveePicker.getValue();
        LocalDate depart = dateDepartPicker.getValue();
        if (arrivee != null && depart != null && depart.isAfter(arrivee)) {
            currentNuits = (int) ChronoUnit.DAYS.between(arrivee, depart);
        } else {
            currentNuits = 1; // valeur par défaut
        }
        nuitsLabel.setText(String.valueOf(currentNuits));
        calculerTotal();
    }

    private void calculerTotal() {
        double prixParNuit = selectedLogement.getTarif_nuit();
        double total = currentNuits * prixParNuit;
        String totalText = String.format("%.0f DT", total);
        totalNuitLabel.setText(totalText);
        totalLabel.setText(totalText);
    }

    private boolean validerSaisie() {
        boolean isValid = true;
        resetErrorLabels();

        if (!selectedLogement.isDisponibilite()) {
            showAlert("Erreur", "Le logement n'est pas disponible.");
            return false;
        }

        LocalDate arrivee = dateArriveePicker.getValue();
        LocalDate depart = dateDepartPicker.getValue();

        if (arrivee == null) {
            arriveeErrorLabel.setText("La date d'arrivée est requise.");
            arriveeErrorLabel.setVisible(true);
            arriveeErrorLabel.setManaged(true);
            isValid = false;
        } else if (arrivee.isBefore(LocalDate.now())) {
            arriveeErrorLabel.setText("La date d'arrivée ne peut pas être dans le passé.");
            arriveeErrorLabel.setVisible(true);
            arriveeErrorLabel.setManaged(true);
            isValid = false;
        }

        if (depart == null) {
            departErrorLabel.setText("La date de départ est requise.");
            departErrorLabel.setVisible(true);
            departErrorLabel.setManaged(true);
            isValid = false;
        } else if (arrivee != null && !depart.isAfter(arrivee)) {
            departErrorLabel.setText("La date de départ doit être postérieure à l'arrivée.");
            departErrorLabel.setVisible(true);
            departErrorLabel.setManaged(true);
            isValid = false;
        }

        if (modaliteGroup.getSelectedToggle() == null) {
            modaliteErrorLabel.setText("Veuillez choisir une modalité.");
            modaliteErrorLabel.setVisible(true);
            modaliteErrorLabel.setManaged(true);
            isValid = false;
        }

        int adultes = adultesSpinner.getValue();
        int enfants = enfantsSpinner.getValue();
        if (adultes + enfants > selectedLogement.getCapacite()) {
            capaciteErrorLabel.setText("Le nombre de personnes dépasse la capacité (" + selectedLogement.getCapacite() + ").");
            capaciteErrorLabel.setVisible(true);
            capaciteErrorLabel.setManaged(true);
            isValid = false;
        }

        if (currentNuits <= 0) {
            dureeErrorLabel.setText("La durée doit être d'au moins 1 nuit.");
            dureeErrorLabel.setVisible(true);
            dureeErrorLabel.setManaged(true);
            isValid = false;
        }

        return isValid;
    }

    private void confirmerReservation() {
        if (!validerSaisie()) return;

        String modalite = enLigneRadio.isSelected() ? "En ligne" : "Sur place";

        if (reservationToEdit != null) {
            enregistrerReservation(reservationToEdit.getStatus(), modalite, null);
            return;
        }

        if ("Sur place".equals(modalite)) {
            enregistrerReservation(Status.en_attente, modalite, null);
        } else {
            gererPaiementEnLigne(modalite);
        }
    }

    private void gererPaiementEnLigne(String modalite) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Paiement en ligne");
        alert.setHeaderText("Choisissez votre option de paiement");
        alert.setContentText("Voulez-vous payer maintenant ou plus tard ?");

        ButtonType payerMaintenant = new ButtonType("Payer maintenant");
        ButtonType payerPlusTard = new ButtonType("Payer plus tard (24h)");
        ButtonType annuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(payerMaintenant, payerPlusTard, annuler);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == payerMaintenant) {
                boolean paiementReussi = simulerPaiement();
                if (paiementReussi) {
                    enregistrerReservation(Status.confirmée, modalite, null);
                    showAlert("Succès", "Paiement accepté. Réservation confirmée !");
                } else {
                    enregistrerReservation(Status.annulée, modalite, null);
                    showAlert("Paiement échoué", "Le paiement a échoué. Réservation annulée.");
                }
            } else if (result.get() == payerPlusTard) {
                Date dateLimite = Date.from(LocalDateTime.now().plusHours(24)
                        .atZone(ZoneId.systemDefault()).toInstant());
                enregistrerReservation(Status.en_attente, modalite, dateLimite);
                showAlert("Paiement différé",
                        "Vous avez 24h pour finaliser votre paiement. Passé ce délai, la réservation sera automatiquement annulée.");
            }
        }
    }

    private boolean simulerPaiement() {
        Alert choix = new Alert(Alert.AlertType.CONFIRMATION);
        choix.setTitle("Simulation de paiement");
        choix.setHeaderText("Choisissez l'issue du paiement");
        choix.setContentText("Simuler un paiement réussi ou échoué ?");

        ButtonType succes = new ButtonType("Succès");
        ButtonType echec = new ButtonType("Échec");
        choix.getButtonTypes().setAll(succes, echec);

        Optional<ButtonType> result = choix.showAndWait();
        return result.isPresent() && result.get() == succes;
    }

    private void enregistrerReservation(Status statut, String modalite, Date dateLimitePaiement) {
        LocalDate arrivee = dateArriveePicker.getValue();
        LocalDate depart = dateDepartPicker.getValue();
        float montant = currentNuits * selectedLogement.getTarif_nuit();

        try {
            if (reservationToEdit == null) {
                reservationlog reservation = new reservationlog();
                reservation.setId_l(selectedLogement.getId());
                reservation.setIdc(currentUser.getId());
                reservation.setDate_debut(Date.from(arrivee.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                reservation.setDate_fin(Date.from(depart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                reservation.setMontant(montant);
                reservation.setStatus(statut);
                reservation.setModalite(modalite);

                serviceReservation.ajouter(reservation);
            } else {
                reservationToEdit.setDate_debut(Date.from(arrivee.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                reservationToEdit.setDate_fin(Date.from(depart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                reservationToEdit.setMontant(montant);
                reservationToEdit.setStatus(statut);
                reservationToEdit.setModalite(modalite);

                serviceReservation.modifier(reservationToEdit);
            }

            SessionManager.clearSelectedLogement();
            SessionManager.clearEditingReservation();
            NavigationManager.loadView("/fxml/mesreservations.fxml");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "❌ Erreur lors de l'enregistrement : " + e.getMessage());
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