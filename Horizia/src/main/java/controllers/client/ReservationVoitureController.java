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
import org.example.entities.Vehicule;
import org.example.entities.Pays;
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

    // Patterns de validation pour différents pays
    private static final Pattern PHONE_TUNISIA = Pattern.compile("^(\\+216)?[2459]\\d{7}$");
    private static final Pattern PHONE_FRANCE = Pattern.compile("^(\\+33|0)[1-9]\\d{8}$");
    private static final Pattern PHONE_MAROC = Pattern.compile("^(\\+212|0)[5-7]\\d{8}$");
    private static final Pattern PHONE_ALGERIE = Pattern.compile("^(\\+213|0)[5-7]\\d{8}$");
    private static final Pattern PHONE_INTERNATIONAL = Pattern.compile("^\\+?[1-9]\\d{7,14}$");

    // Patterns pour CIN/Passport
    private static final Pattern CIN_TUNISIA = Pattern.compile("^[0-9]{8}$");
    private static final Pattern PASSPORT_INTERNATIONAL = Pattern.compile("^[A-Z0-9]{6,12}$");

    public ReservationVoitureController() {
        this.locationService = new LocationService();
        this.modeleService = new ModeleService();
    }

    @FXML
    public void initialize() {
        configurerSpinners();
        configurerCalculAuto();
        initialiserPays();
        configurerValidationEnTempsReel();
    }

    /**
     * Initialise le ComboBox des pays
     */
    private void initialiserPays() {
        comboPays.getItems().addAll(Pays.getPaysSupportes());
        comboPays.setValue(comboPays.getItems().get(0)); // Tunisie par défaut

        // Mettre à jour le placeholder du téléphone selon le pays sélectionné
        comboPays.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtTelephone.setPromptText("Ex: " + newVal.getFormatExemple());
                System.out.println("📱 Pays sélectionné : " + newVal.getNom() + " " + newVal.getIndicatif());
            }
        });
    }

    /**
     * Configure la validation en temps réel des champs
     */
    private void configurerValidationEnTempsReel() {
        // Validation téléphone en temps réel
        txtTelephone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty() && comboPays.getValue() != null) {
                if (validerTelephoneAvecPays(comboPays.getValue(), newVal)) {
                    txtTelephone.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; -fx-font-size: 14px; -fx-background-radius: 8;");
                } else {
                    txtTelephone.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-font-size: 14px; -fx-background-radius: 8;");
                }
            } else {
                txtTelephone.setStyle("-fx-font-size: 14px; -fx-background-radius: 8;");
            }
        });

        // Revalider le téléphone quand le pays change
        comboPays.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (txtTelephone.getText() != null && !txtTelephone.getText().trim().isEmpty()) {
                txtTelephone.setText(txtTelephone.getText());
            }
        });

        // Validation CIN en temps réel
        txtCIN.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                if (validerCinOuPassport(newVal)) {
                    txtCIN.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; -fx-font-size: 14px; -fx-background-radius: 8;");
                } else {
                    txtCIN.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-font-size: 14px; -fx-background-radius: 8;");
                }
            } else {
                txtCIN.setStyle("-fx-font-size: 14px; -fx-background-radius: 8;");
            }
        });
    }

    /**
     * Valide le format du numéro de téléphone selon le pays sélectionné
     */
    private boolean validerTelephoneAvecPays(Pays pays, String telephone) {
        if (pays == null || telephone == null || telephone.trim().isEmpty()) {
            return false;
        }

        String telClean = telephone.replaceAll("[\\s-]", "");

        if (pays.getNom().equals("Autre")) {
            return PHONE_INTERNATIONAL.matcher(telClean).matches();
        }

        switch (pays.getIndicatif()) {
            case "+216": return PHONE_TUNISIA.matcher(telClean).matches();
            case "+33": return PHONE_FRANCE.matcher(telClean).matches();
            case "+212": return PHONE_MAROC.matcher(telClean).matches();
            case "+213": return PHONE_ALGERIE.matcher(telClean).matches();
            default: return PHONE_INTERNATIONAL.matcher(telClean).matches();
        }
    }

    /**
     * Valide le format CIN tunisien ou Passport international
     */
    private boolean validerCinOuPassport(String document) {
        if (document == null || document.trim().isEmpty()) {
            return false; // Obligatoire pour la réservation
        }

        String docClean = document.trim().toUpperCase().replaceAll("[\\s-]", "");
        return CIN_TUNISIA.matcher(docClean).matches() ||
                PASSPORT_INTERNATIONAL.matcher(docClean).matches();
    }

    /**
     * Vérifie si le CIN/Passport existe déjà dans la base de données
     */
    private boolean cinDejaExiste(String cin) {
        if (cin == null || cin.trim().isEmpty()) {
            return false;
        }

        String cinClean = cin.trim().toUpperCase().replaceAll("[\\s-]", "");
        List<Location> locations = locationService.getAllLocations();

        for (Location loc : locations) {
            if (loc.getClientCin() != null) {
                String existingCin = loc.getClientCin().trim().toUpperCase().replaceAll("[\\s-]", "");
                if (existingCin.equals(cinClean)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Initialise la réservation (appelé depuis le catalogue)
     */
    public void initialiserReservation(Vehicule voiture, LocalDate dateDebut, LocalDate dateFin) {
        this.voiture = voiture;

        afficherInfosVoiture();

        // Pré-remplir les dates si fournies
        if (dateDebut != null) {
            dpDateDebut.setValue(dateDebut);
        } else {
            dpDateDebut.setValue(LocalDate.now().plusDays(1));
        }

        if (dateFin != null) {
            dpDateFin.setValue(dateFin);
        } else {
            dpDateFin.setValue(LocalDate.now().plusDays(3));
        }

        calculerMontants();
    }

    /**
     * Affiche les informations de la voiture
     */
    private void afficherInfosVoiture() {
        // Image
        String imageUrl = (voiture.getPhoto() == null || voiture.getPhoto().trim().isEmpty())
                ? "https://via.placeholder.com/500x300/3498db/ffffff?text=Voiture"
                : voiture.getPhoto();

        try {
            imgVoiture.setImage(new Image(imageUrl, true));
        } catch (Exception e) {
            imgVoiture.setImage(new Image("https://via.placeholder.com/500x300/3498db/ffffff?text=Voiture", true));
        }

        // Modèle
        Modele modele = modeleService.getModeleById(voiture.getIdModele());
        String nomModele = (modele != null) ? modele.getNomModele() : "Modèle inconnu";
        lblModele.setText(nomModele);

        // Caractéristiques
        lblCaracteristiques.setText(
                voiture.getAnnee() + " • " +
                        voiture.getCarburant() + " • " +
                        voiture.getCouleur() + " • " +
                        String.format("%,d km", voiture.getKilometrage())
        );

        // Prix
        lblPrixJour.setText(String.format("%.2f TND/jour", voiture.getPrixParJour()));
    }

    /**
     * Configure les spinners d'heures
     */
    private void configurerSpinners() {
        spHeureDebut.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        spHeureFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18));
    }

    /**
     * Configure le calcul automatique des montants
     */
    private void configurerCalculAuto() {
        dpDateDebut.valueProperty().addListener((obs, old, newVal) -> calculerMontants());
        dpDateFin.valueProperty().addListener((obs, old, newVal) -> calculerMontants());
        spHeureDebut.valueProperty().addListener((obs, old, newVal) -> calculerMontants());
        spHeureFin.valueProperty().addListener((obs, old, newVal) -> calculerMontants());
    }

    /**
     * Calcule le montant total et l'avance
     */
    private void calculerMontants() {
        if (dpDateDebut.getValue() == null || dpDateFin.getValue() == null) {
            lblNbJours.setText("--");
            lblPrixTotal.setText("-- TND");
            lblAvance.setText("-- TND");
            lblResteAPayer.setText("-- TND");
            return;
        }

        LocalDateTime debut = dpDateDebut.getValue().atTime(spHeureDebut.getValue(), 0);
        LocalDateTime fin = dpDateFin.getValue().atTime(spHeureFin.getValue(), 0);

        long jours = ChronoUnit.DAYS.between(debut, fin);
        if (jours < 0) {
            lblNbJours.setText("Dates invalides");
            return;
        }

        if (jours == 0) jours = 1; // Minimum 1 jour

        double prixJour = voiture.getPrixParJour();
        double montantTotal = jours * prixJour;
        double avance = montantTotal * 0.30; // 30% d'avance
        double reste = montantTotal - avance;

        lblNbJours.setText(jours + " jour(s)");
        lblPrixTotal.setText(String.format("%.3f TND", montantTotal));
        lblAvance.setText(String.format("%.3f TND (30%%)", avance));
        lblResteAPayer.setText(String.format("%.3f TND", reste));
    }

    /**
     * Valide et confirme la réservation
     */
    @FXML
    private void confirmerReservation() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            // Formater le numéro avec l'indicatif du pays
            String telephoneComplet = comboPays.getValue().formaterNumero(txtTelephone.getText().trim());

            // Créer la location
            Location location = new Location();

            location.setIdVehicule(voiture.getIdVehicule());
            location.setClientNomComplet(txtNomComplet.getText().trim());
            location.setClientTelephone(telephoneComplet);

            // Normaliser le CIN/Passport
            String cinNormalise = txtCIN.getText().trim().toUpperCase().replaceAll("[\\s-]", "");
            location.setClientCin(cinNormalise);

            LocalDateTime debut = dpDateDebut.getValue().atTime(spHeureDebut.getValue(), 0);
            LocalDateTime fin = dpDateFin.getValue().atTime(spHeureFin.getValue(), 0);

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
            location.setNotes(txtNotes.getText().trim());

            // Enregistrer dans la base
            boolean succes = locationService.ajouterLocation(location);

            if (succes) {
                afficherConfirmation(location);
            } else {
                afficherAlerte("Erreur", "Impossible d'enregistrer la réservation.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("Erreur réservation : " + e.getMessage());
            e.printStackTrace();
            afficherAlerte("Erreur", "Une erreur est survenue.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Valide le formulaire
     */
    private boolean validerFormulaire() {
        // Validation nom
        if (txtNomComplet.getText().trim().isEmpty()) {
            afficherAlerte("Champ requis", "Veuillez saisir votre nom complet.", Alert.AlertType.WARNING);
            txtNomComplet.requestFocus();
            return false;
        }

        // Validation pays
        if (comboPays.getValue() == null) {
            afficherAlerte("Champ requis", "Veuillez sélectionner votre pays.", Alert.AlertType.WARNING);
            comboPays.requestFocus();
            return false;
        }

        // Validation téléphone
        if (txtTelephone.getText().trim().isEmpty()) {
            afficherAlerte("Champ requis", "Veuillez saisir votre numéro de téléphone.", Alert.AlertType.WARNING);
            txtTelephone.requestFocus();
            return false;
        }

        if (!validerTelephoneAvecPays(comboPays.getValue(), txtTelephone.getText())) {
            afficherAlerte("Format invalide",
                    "Format de téléphone invalide pour " + comboPays.getValue().getNom() + " !\n\n" +
                            "Format attendu : " + comboPays.getValue().getFormatExemple() + "\n" +
                            "Indicatif : " + comboPays.getValue().getIndicatif(),
                    Alert.AlertType.WARNING);
            txtTelephone.requestFocus();
            return false;
        }

        // Validation CIN
        if (txtCIN.getText().trim().isEmpty()) {
            afficherAlerte("Champ requis", "Veuillez saisir votre CIN/Passport.", Alert.AlertType.WARNING);
            txtCIN.requestFocus();
            return false;
        }

        if (!validerCinOuPassport(txtCIN.getText())) {
            afficherAlerte("Format invalide",
                    "Format de CIN/Passport invalide !\n\n" +
                            "Formats acceptés :\n" +
                            "• CIN Tunisien : 8 chiffres (ex: 12345678)\n" +
                            "• Passport : 6 à 12 caractères alphanumériques (ex: AB123456)",
                    Alert.AlertType.WARNING);
            txtCIN.requestFocus();
            return false;
        }

        // Vérification unicité CIN
        if (cinDejaExiste(txtCIN.getText())) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("⚠️ CIN/Passport déjà enregistré");
            confirmAlert.setHeaderText("Ce CIN/Passport existe déjà dans nos enregistrements");
            confirmAlert.setContentText(
                    "Un client avec ce CIN/Passport a déjà effectué une réservation.\n\n" +
                            "Voulez-vous continuer quand même ?\n" +
                            "(Il est possible qu'un même client effectue plusieurs réservations)"
            );

            ButtonType btnContinuer = new ButtonType("Continuer");
            ButtonType btnAnnuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmAlert.getButtonTypes().setAll(btnContinuer, btnAnnuler);

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response != btnContinuer) {
                    txtCIN.requestFocus();
                }
            });

            if (confirmAlert.getResult() != btnContinuer) {
                return false;
            }
        }

        // Validation dates
        if (dpDateDebut.getValue() == null || dpDateFin.getValue() == null) {
            afficherAlerte("Dates requises", "Veuillez sélectionner les dates.", Alert.AlertType.WARNING);
            return false;
        }

        if (dpDateFin.getValue().isBefore(dpDateDebut.getValue())) {
            afficherAlerte("Dates invalides", "La date de fin doit être après la date de début.", Alert.AlertType.WARNING);
            return false;
        }

        // Validation conditions
        if (!cbAccepteConditions.isSelected()) {
            afficherAlerte("Conditions", "Veuillez accepter les conditions générales.", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    /**
     * Affiche la confirmation de réservation
     */
    private void afficherConfirmation(Location location) {
        Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
        confirmation.setTitle("Réservation confirmée !");
        confirmation.setHeaderText("✅ Votre réservation a été enregistrée");
        confirmation.setContentText(
                "Numéro de réservation : #" + location.getIdLocation() + "\n\n" +
                        "Client : " + location.getClientNomComplet() + "\n" +
                        "Téléphone : " + location.getClientTelephone() + "\n" +
                        "CIN/Passport : " + location.getClientCin() + "\n\n" +
                        "Montant total : " + String.format("%.3f TND", location.getMontantTotal()) + "\n" +
                        "Avance à payer : " + String.format("%.3f TND", location.getAvance()) + "\n\n" +
                        "Nous vous contacterons bientôt pour finaliser votre réservation."
        );

        confirmation.showAndWait();
        retourCatalogue();
    }

    /**
     * Annule la réservation
     */
    @FXML
    private void annulerReservation() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Annuler la réservation");
        confirmation.setHeaderText("Êtes-vous sûr ?");
        confirmation.setContentText("Les données saisies seront perdues.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                retourCatalogue();
            }
        });
    }

    /**
     * Retour au catalogue
     */
    private void retourCatalogue() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client/CatalogueVoitures.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 800);
            stage.setScene(scene);
            stage.setTitle("Horizia - Catalogue");

        } catch (IOException e) {
            System.err.println("Erreur retour catalogue : " + e.getMessage());
        }
    }

    /**
     * Affiche une alerte
     */
    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}