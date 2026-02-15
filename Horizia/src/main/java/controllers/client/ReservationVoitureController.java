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
import org.example.services.LocationService;
import org.example.services.ModeleService;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

    public ReservationVoitureController() {
        this.locationService = new LocationService();
        this.modeleService = new ModeleService();
    }

    @FXML
    public void initialize() {
        configurerSpinners();
        configurerCalculAuto();
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
            // Créer la location
            Location location = new Location();

            location.setIdVehicule(voiture.getIdVehicule());
            location.setClientNomComplet(txtNomComplet.getText().trim());
            location.setClientTelephone(txtTelephone.getText().trim());
            location.setClientCin(txtCIN.getText().trim());

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
        if (txtNomComplet.getText().trim().isEmpty()) {
            afficherAlerte("Champ requis", "Veuillez saisir votre nom complet.", Alert.AlertType.WARNING);
            txtNomComplet.requestFocus();
            return false;
        }

        if (txtTelephone.getText().trim().isEmpty()) {
            afficherAlerte("Champ requis", "Veuillez saisir votre numéro de téléphone.", Alert.AlertType.WARNING);
            txtTelephone.requestFocus();
            return false;
        }

        if (txtCIN.getText().trim().isEmpty()) {
            afficherAlerte("Champ requis", "Veuillez saisir votre CIN.", Alert.AlertType.WARNING);
            txtCIN.requestFocus();
            return false;
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
                        "Téléphone : " + location.getClientTelephone() + "\n\n" +
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