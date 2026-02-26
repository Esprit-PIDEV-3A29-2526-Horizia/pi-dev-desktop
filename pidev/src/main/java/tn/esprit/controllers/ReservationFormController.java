package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import tn.esprit.entities.logement;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.Status;
import tn.esprit.entities.User;
import tn.esprit.services.Servicereservationlog;
import tn.esprit.services.Servicelogement;
import tn.esprit.utils.*;

import javax.mail.MessagingException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

public class ReservationFormController {

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
    @FXML private Label nuitsLabel;

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

    private int currentNuits = 1;

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

        int capaciteMax = selectedLogement.getCapacite();
        adultesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, capaciteMax, 1));
        enfantsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, capaciteMax - 1, 0));

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
        calculerTotal();
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
            currentNuits = 1;
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
            try {
                enregistrerReservation(reservationToEdit.getStatus(), modalite, null);
                NavigationManager.loadView("/fxml/mesreservations.fxml");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la modification.");
            }
            return;
        }

        if ("Sur place".equals(modalite)) {
            try {
                int reservationId = enregistrerReservation(Status.confirmée, modalite, null);
                // Envoyer email de confirmation pour réservation sur place
                envoyerEmailConfirmation(reservationId, modalite, Status.confirmée);
                NavigationManager.loadView("/fxml/mesreservations.fxml");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'enregistrement.");
            }
        } else {
            gererPaiementEnLigne(modalite);
        }
    }

    private void gererPaiementEnLigne(String modalite) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Paiement en ligne");
        dialog.setHeaderText("Choisissez votre option de paiement");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/paiementstyle.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");

        Label content = new Label("Voulez-vous payer maintenant ou plus tard ?");
        content.setStyle("-fx-font-size: 14px; -fx-text-fill: #1A3C5A;");
        dialogPane.setContent(content);

        ButtonType payerMaintenant = new ButtonType("Payer maintenant (Stripe)", ButtonBar.ButtonData.OK_DONE);
        ButtonType payerPlusTard = new ButtonType("Payer plus tard (24h)", ButtonBar.ButtonData.OTHER);
        ButtonType annuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(payerMaintenant, payerPlusTard, annuler);

        dialogPane.lookupButton(payerMaintenant).getStyleClass().add("confirm-button");
        dialogPane.lookupButton(payerPlusTard).getStyleClass().add("info-button");
        dialogPane.lookupButton(annuler).getStyleClass().add("cancel-button");

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent()) {
            if (result.get() == payerMaintenant) {
                try {
                    double montant = currentNuits * selectedLogement.getTarif_nuit();
                    long montantCentimes = Math.round(montant * 100);
                    String currency = "eur";

                    String successUrl = "https://example.com/success";
                    String cancelUrl = "https://example.com/cancel";

                    String checkoutUrl = StripeService.createCheckoutSession(montantCentimes, currency, successUrl, cancelUrl);
                    ouvrirPagePaiement(checkoutUrl, modalite);

                } catch (Exception e) {
                    e.printStackTrace();
                    showStyledAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de lancer le paiement : " + e.getMessage());
                }

            }else if (result.get() == payerPlusTard) {
                Date dateLimite = Date.from(LocalDateTime.now().plusHours(24).atZone(ZoneId.systemDefault()).toInstant());
                try {
                    int reservationId = enregistrerReservation(Status.en_attente, modalite, dateLimite);

                    // Créer une session Stripe pour ce montant (valable 24h)
                    double montant = currentNuits * selectedLogement.getTarif_nuit();
                    long montantCentimes = Math.round(montant * 100);
                    String currency = "eur"; // ou autre devise supportée
                    String successUrl = "https://example.com/success"; // À remplacer par une vraie URL de retour
                    String cancelUrl = "https://example.com/cancel";
                    String checkoutUrl = StripeService.createCheckoutSession(montantCentimes, currency, successUrl, cancelUrl);

                    // Préparer les infos pour l'email
                    String dates = dateArriveePicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + " au " + dateDepartPicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    // Envoyer l'email avec le lien de paiement
                    try {
                        EmailService.sendPaymentDeferredEmail(
                                currentUser.getEmail(),
                                currentUser.getPrenom(),
                                selectedLogement.getNom(),

                                dates,
                                montant,
                                checkoutUrl
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Ne pas bloquer
                    }

                    // Planifier les rappels (2h, 1h, 30min)
                    PaymentReminderService.scheduleReminders(
                            reservationId,
                            dateLimite,
                            currentUser.getEmail(),
                            currentUser.getPrenom(),
                            selectedLogement.getNom(),
                            currentUser.getNom()
                    );

                    showStyledAlert(Alert.AlertType.INFORMATION, "Paiement différé", null,
                            "⏳ Vous avez 24h pour finaliser votre paiement. \n Un email avec un lien de paiement vous a été envoyé.");
                    NavigationManager.loadView("/fxml/mesreservations.fxml");
                } catch (Exception e) {
                    e.printStackTrace();
                    showStyledAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors du traitement.");
                }
            }
        }
    }

    /**
     * Ouvre une fenêtre avec WebView pour le paiement Stripe.
     */
    private void ouvrirPagePaiement(String checkoutUrl, String modalite) {
        Stage stage = new Stage();
        stage.setTitle("Paiement sécurisé");

        HBox titleBar = new HBox(10);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle("-fx-background-color: #1A3C5A; -fx-padding: 10 15;");
        Label titleLabel = new Label("💳 Paiement par carte bancaire");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        closeButton.setOnAction(e -> stage.close());
        titleBar.getChildren().addAll(titleLabel, spacer, closeButton);

        Region greenLine = new Region();
        greenLine.setPrefHeight(4);
        greenLine.setStyle("-fx-background-color: #2ECC71;");

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        engine.locationProperty().addListener((obs, oldUrl, newUrl) -> {
            if (newUrl.startsWith("https://example.com/success")) {
                stage.close();
                try {
                    int reservationId = enregistrerReservation(Status.confirmée, modalite, null);
                    // Envoyer email de confirmation après paiement réussi
                    envoyerEmailConfirmation(reservationId, modalite, Status.confirmée);
                    showAlert("Succès", "✅ Paiement accepté. Réservation confirmée !");
                    NavigationManager.loadView("/fxml/mesreservations.fxml");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de l'enregistrement.");
                }
            } else if (newUrl.startsWith("https://example.com/cancel")) {
                stage.close();
                try {
                    enregistrerReservation(Status.annulée, modalite, null);
                    showAlert("Paiement annulé", "❌ Vous avez annulé le paiement. Réservation annulée.");
                    NavigationManager.loadView("/fxml/mesreservations.fxml");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de l'enregistrement.");
                }
            }
        });

        engine.load(checkoutUrl);

        VBox root = new VBox(titleBar, greenLine, webView);
        VBox.setVgrow(webView, Priority.ALWAYS);

        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Envoie un email de confirmation avec PDF contenant les détails et le QR code.
     */
    private void envoyerEmailConfirmation(int reservationId, String modalite, Status statut) {
        try {
            String qrContent = "🏠 Logement: " + selectedLogement.getNom() + "\n" +
                    "📅 Arrivée: " + dateArriveePicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                    "📅 Départ: " + dateDepartPicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                    "💰 Montant: " + totalLabel.getText() + "\n" +
                    "💳 Modalité: " + modalite + "\n" +
                    (modalite.equals("En ligne") ? "✅ Paiement: Effectué" : "🏧 Paiement: À régler sur place");

            String subject = "Confirmation de réservation";

            String logementAdresse = selectedLogement.getAdresse(); // si votre entité logement a une adresse
            EmailService.sendReservationEmailWithPDF(
                    currentUser.getEmail(),
                    "Confirmation de réservation " ,
                    currentUser.getNom(),
                    currentUser.getPrenom(),
                    selectedLogement.getNom(),
                    logementAdresse,
                    dateArriveePicker.getValue(),
                    dateDepartPicker.getValue(),
                    currentNuits * selectedLogement.getTarif_nuit(),
                    modalite,
                    statut.toString(),
                    qrContent,
                    "reservation_logement.pdf"
            );
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "La réservation a été enregistrée mais l'envoi de l'email a échoué : " + e.getMessage());
        }
    }

    private void showStyledAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/paiementstyle.css").toExternalForm());
        alert.showAndWait();
    }

    /**
     * Enregistre ou modifie une réservation et retourne son ID.
     */
    private int enregistrerReservation(Status statut, String modalite, Date dateLimitePaiement) throws SQLException {
        LocalDate arrivee = dateArriveePicker.getValue();
        LocalDate depart = dateDepartPicker.getValue();
        float montant = currentNuits * selectedLogement.getTarif_nuit();

        if (reservationToEdit == null) {
            reservationlog reservation = new reservationlog();
            reservation.setId_l(selectedLogement.getId());
            reservation.setIdc(currentUser.getId());
            reservation.setDate_debut(Date.from(arrivee.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            reservation.setDate_fin(Date.from(depart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            reservation.setMontant(montant);
            reservation.setStatus(statut);
            reservation.setModalite(modalite);
            // Si vous avez un champ date_limite_paiement, ajoutez-le ici

            serviceReservation.ajouter(reservation);
            return reservation.getId();
        } else {
            reservationToEdit.setDate_debut(Date.from(arrivee.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            reservationToEdit.setDate_fin(Date.from(depart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            reservationToEdit.setMontant(montant);
            reservationToEdit.setStatus(statut);
            reservationToEdit.setModalite(modalite);
            serviceReservation.modifier(reservationToEdit);
            return reservationToEdit.getId();
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