package controllers.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.entities.Location;
import org.example.entities.Modele;
import org.example.entities.Pays;
import org.example.entities.Vehicule;
import org.example.services.LocationService;
import org.example.services.ModeleService;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

public class ReservationVoitureController {

    @FXML private ImageView imgVoiture;
    @FXML private Label lblModele;
    @FXML private Label lblCaracteristiques;
    @FXML private Label lblPrixJour;

    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private Spinner<Integer> spHeureDebut;
    @FXML private Spinner<Integer> spHeureFin;

    @FXML private TextField txtNomComplet;
    @FXML private ComboBox<Pays> comboPays;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtCIN;
    @FXML private TextArea txtNotes;

    @FXML private Label lblNbJours;
    @FXML private Label lblPrixTotal;
    @FXML private Label lblAvance;
    @FXML private Label lblResteAPayer;

    @FXML private CheckBox cbAccepteConditions;
    @FXML private Button btnConfirmer;
    @FXML private Button btnAnnuler;

    private Vehicule voiture;
    private LocationService locationService;
    private ModeleService modeleService;

    private static final Pattern PHONE_TUNISIA       = Pattern.compile("^(\\+216)?[2459]\\d{7}$");
    private static final Pattern PHONE_FRANCE        = Pattern.compile("^(\\+33|0)[1-9]\\d{8}$");
    private static final Pattern PHONE_MAROC         = Pattern.compile("^(\\+212|0)[5-7]\\d{8}$");
    private static final Pattern PHONE_ALGERIE       = Pattern.compile("^(\\+213|0)[5-7]\\d{8}$");
    private static final Pattern PHONE_INTERNATIONAL = Pattern.compile("^\\+?[1-9]\\d{7,14}$");
    private static final Pattern CIN_TUNISIA         = Pattern.compile("^[0-9]{8}$");
    private static final Pattern PASSPORT_INTERNATIONAL = Pattern.compile("^[A-Z0-9]{6,12}$");

    public ReservationVoitureController() {
        this.locationService = new LocationService();
        this.modeleService   = new ModeleService();
    }

    @FXML
    public void initialize() {
        configurerSpinners();
        configurerCalculAuto();
        initialiserPays();
        configurerValidationEnTempsReel();
    }

    private void initialiserPays() {
        comboPays.getItems().addAll(Pays.getPaysSupportes());
        comboPays.setValue(comboPays.getItems().get(0));
        comboPays.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) txtTelephone.setPromptText("Ex: " + newVal.getFormatExemple());
        });
    }

    private void configurerValidationEnTempsReel() {
        txtTelephone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty() && comboPays.getValue() != null) {
                String style = "-fx-border-color: " +
                        (validerTelephoneAvecPays(comboPays.getValue(), newVal) ? "#27ae60" : "#e74c3c") +
                        "; -fx-border-width: 2; -fx-font-size: 14px; -fx-background-radius: 8;";
                txtTelephone.setStyle(style);
            } else {
                txtTelephone.setStyle("-fx-font-size: 14px; -fx-background-radius: 8;");
            }
        });
        comboPays.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (txtTelephone.getText() != null && !txtTelephone.getText().trim().isEmpty()) {
                txtTelephone.setText(txtTelephone.getText()); // re-trigger listener
            }
        });
        txtCIN.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                String style = "-fx-border-color: " +
                        (validerCinOuPassport(newVal) ? "#27ae60" : "#e74c3c") +
                        "; -fx-border-width: 2; -fx-font-size: 14px; -fx-background-radius: 8;";
                txtCIN.setStyle(style);
            } else {
                txtCIN.setStyle("-fx-font-size: 14px; -fx-background-radius: 8;");
            }
        });
    }

    private boolean validerTelephoneAvecPays(Pays pays, String telephone) {
        if (pays == null || telephone == null || telephone.trim().isEmpty()) return false;
        String telClean = telephone.replaceAll("[\\s-]", "");
        if (pays.getNom().equals("Autre")) return PHONE_INTERNATIONAL.matcher(telClean).matches();
        return switch (pays.getIndicatif()) {
            case "+216" -> PHONE_TUNISIA.matcher(telClean).matches();
            case "+33"  -> PHONE_FRANCE.matcher(telClean).matches();
            case "+212" -> PHONE_MAROC.matcher(telClean).matches();
            case "+213" -> PHONE_ALGERIE.matcher(telClean).matches();
            default     -> PHONE_INTERNATIONAL.matcher(telClean).matches();
        };
    }

    private boolean validerCinOuPassport(String document) {
        if (document == null || document.trim().isEmpty()) return false;
        String docClean = document.trim().toUpperCase().replaceAll("[\\s-]", "");
        return CIN_TUNISIA.matcher(docClean).matches() ||
                PASSPORT_INTERNATIONAL.matcher(docClean).matches();
    }

    private boolean cinDejaExiste(String cin) {
        if (cin == null || cin.trim().isEmpty()) return false;
        String cinClean = cin.trim().toUpperCase().replaceAll("[\\s-]", "");
        List<Location> locations = locationService.getAllLocations();
        for (Location loc : locations) {
            if (loc.getClientCin() != null) {
                if (loc.getClientCin().trim().toUpperCase().replaceAll("[\\s-]", "").equals(cinClean)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void initialiserReservation(Vehicule voiture, LocalDate dateDebut, LocalDate dateFin) {
        this.voiture = voiture;
        afficherInfosVoiture();

        dpDateDebut.setValue(dateDebut != null ? dateDebut : LocalDate.now().plusDays(1));
        dpDateFin.setValue(dateFin != null ? dateFin : LocalDate.now().plusDays(3));
        calculerMontants();
    }

    private void afficherInfosVoiture() {
        String imageUrl = (voiture.getPhoto() == null || voiture.getPhoto().trim().isEmpty())
                ? "https://via.placeholder.com/500x300/3498db/ffffff?text=Voiture"
                : voiture.getPhoto();
        try {
            imgVoiture.setImage(new Image(imageUrl, true));
        } catch (Exception e) {
            imgVoiture.setImage(new Image("https://via.placeholder.com/500x300/3498db/ffffff?text=Voiture", true));
        }
        Modele modele = modeleService.getModeleById(voiture.getIdModele());
        lblModele.setText(modele != null ? modele.getNomModele() : "Modèle inconnu");
        lblCaracteristiques.setText(voiture.getAnnee() + " • " + voiture.getCarburant() +
                " • " + voiture.getCouleur() +
                " • " + String.format("%,d km", voiture.getKilometrage()));
        lblPrixJour.setText(String.format("%.2f TND/jour", voiture.getPrixParJour()));
    }

    private void configurerSpinners() {
        spHeureDebut.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        spHeureFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18));
    }

    private void configurerCalculAuto() {
        dpDateDebut.valueProperty().addListener((obs, old, newVal) -> calculerMontants());
        dpDateFin.valueProperty().addListener((obs, old, newVal) -> calculerMontants());
        spHeureDebut.valueProperty().addListener((obs, old, newVal) -> calculerMontants());
        spHeureFin.valueProperty().addListener((obs, old, newVal) -> calculerMontants());
    }

    private void calculerMontants() {
        if (dpDateDebut.getValue() == null || dpDateFin.getValue() == null) {
            lblNbJours.setText("--");
            lblPrixTotal.setText("-- TND");
            lblAvance.setText("-- TND");
            lblResteAPayer.setText("-- TND");
            return;
        }
        LocalDateTime debut = dpDateDebut.getValue().atTime(spHeureDebut.getValue(), 0);
        LocalDateTime fin   = dpDateFin.getValue().atTime(spHeureFin.getValue(), 0);
        long jours = ChronoUnit.DAYS.between(debut, fin);
        if (jours < 0) {
            lblNbJours.setText("Dates invalides");
            return;
        }
        if (jours == 0) jours = 1;

        double montantTotal = jours * voiture.getPrixParJour();
        double avance = montantTotal * 0.30;
        double reste  = montantTotal - avance;

        lblNbJours.setText(jours + " jour(s)");
        lblPrixTotal.setText(String.format("%.3f TND", montantTotal));
        lblAvance.setText(String.format("%.3f TND (30%%)", avance));
        lblResteAPayer.setText(String.format("%.3f TND", reste));
    }

    @FXML
    private void confirmerReservation() {
        if (!validerFormulaire()) return;

        try {
            String telephoneComplet = comboPays.getValue().formaterNumero(txtTelephone.getText().trim());
            Location location = new Location();
            location.setIdVehicule(voiture.getIdVehicule());
            location.setClientNomComplet(txtNomComplet.getText().trim());
            location.setClientTelephone(telephoneComplet);
            location.setClientCin(txtCIN.getText().trim().toUpperCase().replaceAll("[\\s-]", ""));

            LocalDateTime debut = dpDateDebut.getValue().atTime(spHeureDebut.getValue(), 0);
            LocalDateTime fin   = dpDateFin.getValue().atTime(spHeureFin.getValue(), 0);
            location.setDateDebut(Timestamp.valueOf(debut));
            location.setDateFinPrev(Timestamp.valueOf(fin));
            location.setKilometrageDebut(voiture.getKilometrage());
            location.setPrixParJour(voiture.getPrixParJour());

            long jours = ChronoUnit.DAYS.between(debut, fin);
            if (jours == 0) jours = 1;
            double montantTotal = jours * voiture.getPrixParJour();
            double avance = montantTotal * 0.30;

            location.setMontantTotal(montantTotal);
            location.setAvance(avance);
            location.setStatut("réservée");
            location.setNotes(txtNotes.getText() != null ? txtNotes.getText().trim() : "");

            boolean succes = locationService.ajouterLocation(location);

            if (succes) {
                // ✅ CORRECTION PRINCIPALE : naviguer vers ConfirmationReservation.fxml
                naviguerVersConfirmation(location);
            } else {
                afficherAlerte("Erreur", "Impossible d'enregistrer la réservation.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("Erreur réservation : " + e.getMessage());
            e.printStackTrace();
            afficherAlerte("Erreur", "Une erreur est survenue : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * ✅ NOUVEAU : Navigate to ConfirmationReservation.fxml and inject the location.
     */
    private void naviguerVersConfirmation(Location location) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/client/ConfirmationReservation.fxml"));
            Parent root = loader.load();

            ConfirmationReservationController ctrl = loader.getController();
            ctrl.setLocation(location);

            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle("Horizia - Réservation Confirmée !");

        } catch (IOException e) {
            System.err.println("[Reservation] Erreur navigation confirmation : " + e.getMessage());
            e.printStackTrace();
            // Fallback : afficher une alerte simple
            afficherAlerte("Réservation confirmée !",
                    "Numéro : #" + location.getIdLocation() + "\nClient : " + location.getClientNomComplet(),
                    Alert.AlertType.INFORMATION);
            retourCatalogue();
        }
    }

    private boolean validerFormulaire() {
        if (txtNomComplet.getText().trim().isEmpty()) {
            afficherAlerte("Champ requis", "Veuillez saisir votre nom complet.", Alert.AlertType.WARNING);
            txtNomComplet.requestFocus();
            return false;
        }
        if (comboPays.getValue() == null) {
            afficherAlerte("Champ requis", "Veuillez sélectionner votre pays.", Alert.AlertType.WARNING);
            comboPays.requestFocus();
            return false;
        }
        if (txtTelephone.getText().trim().isEmpty()) {
            afficherAlerte("Champ requis", "Veuillez saisir votre numéro de téléphone.", Alert.AlertType.WARNING);
            txtTelephone.requestFocus();
            return false;
        }
        if (!validerTelephoneAvecPays(comboPays.getValue(), txtTelephone.getText())) {
            afficherAlerte("Format invalide",
                    "Format de téléphone invalide pour " + comboPays.getValue().getNom() + " !\n" +
                            "Format attendu : " + comboPays.getValue().getFormatExemple(),
                    Alert.AlertType.WARNING);
            txtTelephone.requestFocus();
            return false;
        }
        if (txtCIN.getText().trim().isEmpty()) {
            afficherAlerte("Champ requis", "Veuillez saisir votre CIN/Passport.", Alert.AlertType.WARNING);
            txtCIN.requestFocus();
            return false;
        }
        if (!validerCinOuPassport(txtCIN.getText())) {
            afficherAlerte("Format invalide",
                    "Format de CIN/Passport invalide !\n" +
                            "CIN Tunisien : 8 chiffres (ex: 12345678)\n" +
                            "Passport : 6-12 caractères alphanumériques (ex: AB123456)",
                    Alert.AlertType.WARNING);
            txtCIN.requestFocus();
            return false;
        }
        if (cinDejaExiste(txtCIN.getText())) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("⚠️ CIN/Passport déjà enregistré");
            confirmAlert.setHeaderText("Ce CIN/Passport existe déjà dans nos enregistrements");
            confirmAlert.setContentText(
                    "Un client avec ce CIN a déjà effectué une réservation.\n" +
                            "Voulez-vous continuer quand même ?");
            ButtonType btnContinuer = new ButtonType("Continuer");
            ButtonType btnAnnuler2  = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmAlert.getButtonTypes().setAll(btnContinuer, btnAnnuler2);
            confirmAlert.showAndWait();
            if (confirmAlert.getResult() != btnContinuer) {
                txtCIN.requestFocus();
                return false;
            }
        }
        if (dpDateDebut.getValue() == null || dpDateFin.getValue() == null) {
            afficherAlerte("Dates requises", "Veuillez sélectionner les dates.", Alert.AlertType.WARNING);
            return false;
        }
        if (dpDateFin.getValue().isBefore(dpDateDebut.getValue())) {
            afficherAlerte("Dates invalides", "La date de fin doit être après la date de début.", Alert.AlertType.WARNING);
            return false;
        }
        if (!cbAccepteConditions.isSelected()) {
            afficherAlerte("Conditions", "Veuillez accepter les conditions générales.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    @FXML
    private void annulerReservation() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Annuler la réservation");
        confirmation.setHeaderText("Êtes-vous sûr ?");
        confirmation.setContentText("Les données saisies seront perdues.");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) retourCatalogue();
        });
    }

    private void retourCatalogue() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client/CatalogueVoitures.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle("Horizia - Catalogue");
        } catch (IOException e) {
            System.err.println("Erreur retour catalogue : " + e.getMessage());
        }
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}