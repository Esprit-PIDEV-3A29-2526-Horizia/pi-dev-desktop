package controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Location;
import org.example.entities.Vehicule;
import org.example.services.LocationService;
import org.example.services.VehiculeService;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AjouterLocationController {

    @FXML private ComboBox<Vehicule> comboVehicule;
    @FXML private Label lblInfoVehicule;
    @FXML private TextField txtNomClient;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtCIN;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFinPrevue;
    @FXML private TextField txtKilometrageDebut;
    @FXML private TextField txtPrixParJour;
    @FXML private TextField txtNombreJours;
    @FXML private TextField txtMontantTotal;
    @FXML private TextField txtAvance;
    @FXML private ComboBox<String> comboStatut;
    @FXML private TextArea txtNotes;
    @FXML private Label lblMessage;
    @FXML private Button btnAjouter;

    private VehiculeService vehiculeService;
    private LocationService locationService;

    @FXML
    public void initialize() {
        vehiculeService = new VehiculeService();
        locationService = new LocationService();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Ajouter Location - Chargée");
        System.out.println("═══════════════════════════════════════════════");

        initialiserComboBoxes();
        chargerVehiculesDisponibles();
        configurerCalculAutomatique();
    }

    private void initialiserComboBoxes() {
        comboStatut.getItems().addAll("réservée", "en_cours", "terminée", "annulée", "no_show");
        comboStatut.setValue("réservée");
    }

    private void chargerVehiculesDisponibles() {
        List<Vehicule> vehicules = vehiculeService.getAllVehicules().stream()
                .filter(v -> "disponible".equals(v.getEtat()))
                .toList();

        comboVehicule.getItems().addAll(vehicules);
        System.out.println("✓ " + vehicules.size() + " véhicule(s) disponible(s) chargé(s)");
    }

    private void configurerCalculAutomatique() {
        comboVehicule.valueProperty().addListener((obs, old, nouv) -> {
            if (nouv != null) {
                txtPrixParJour.setText(String.format("%.3f TND", nouv.getPrixParJour()));
                txtKilometrageDebut.setText(String.valueOf(nouv.getKilometrage()));
                lblInfoVehicule.setText("✓ " + nouv.getImmatriculation() + " - " + nouv.getPrixParJour() + " TND/jour");
                lblInfoVehicule.setVisible(true);
                calculerMontant();
            }
        });

        dateDebut.valueProperty().addListener((obs, old, nouv) -> calculerMontant());
        dateFinPrevue.valueProperty().addListener((obs, old, nouv) -> calculerMontant());
    }

    private void calculerMontant() {
        if (comboVehicule.getValue() == null || dateDebut.getValue() == null || dateFinPrevue.getValue() == null) {
            return;
        }

        LocalDate debut = dateDebut.getValue();
        LocalDate fin = dateFinPrevue.getValue();

        if (fin.isBefore(debut)) {
            txtNombreJours.setText("0");
            txtMontantTotal.setText("0.000 TND");
            return;
        }

        long nbJours = ChronoUnit.DAYS.between(debut, fin);
        if (nbJours == 0) nbJours = 1;

        double prixJour = comboVehicule.getValue().getPrixParJour();
        double total = nbJours * prixJour;

        txtNombreJours.setText(String.valueOf(nbJours));
        txtMontantTotal.setText(String.format("%.3f TND", total));

        System.out.println("💰 Calcul : " + nbJours + " jours × " + prixJour + " = " + total + " TND");
    }

    @FXML
    private void ajouterLocation() {
        // Validations
        if (comboVehicule.getValue() == null) {
            afficherErreur("⚠️ Veuillez sélectionner un véhicule !");
            return;
        }

        if (txtNomClient.getText() == null || txtNomClient.getText().trim().isEmpty()) {
            afficherErreur("⚠️ Le nom du client est obligatoire !");
            txtNomClient.requestFocus();
            return;
        }

        if (txtTelephone.getText() == null || txtTelephone.getText().trim().isEmpty()) {
            afficherErreur("⚠️ Le téléphone du client est obligatoire !");
            txtTelephone.requestFocus();
            return;
        }

        if (dateDebut.getValue() == null) {
            afficherErreur("⚠️ La date de début est obligatoire !");
            return;
        }

        if (dateFinPrevue.getValue() == null) {
            afficherErreur("⚠️ La date de fin prévue est obligatoire !");
            return;
        }

        if (dateFinPrevue.getValue().isBefore(dateDebut.getValue())) {
            afficherErreur("⚠️ La date de fin doit être après la date de début !");
            return;
        }

        if (txtKilometrageDebut.getText() == null || txtKilometrageDebut.getText().trim().isEmpty()) {
            afficherErreur("⚠️ Le kilométrage de départ est obligatoire !");
            txtKilometrageDebut.requestFocus();
            return;
        }

        int kmDebut;
        try {
            kmDebut = Integer.parseInt(txtKilometrageDebut.getText().trim());
            if (kmDebut < 0) {
                afficherErreur("⚠️ Le kilométrage ne peut pas être négatif !");
                return;
            }
        } catch (NumberFormatException e) {
            afficherErreur("⚠️ Le kilométrage doit être un nombre valide !");
            return;
        }

        double avance = 0.0;
        if (txtAvance.getText() != null && !txtAvance.getText().trim().isEmpty()) {
            try {
                avance = Double.parseDouble(txtAvance.getText().trim());
            } catch (NumberFormatException e) {
                afficherErreur("⚠️ L'avance doit être un nombre valide !");
                return;
            }
        }

        // Création de la location
        Timestamp tsDebut = Timestamp.valueOf(dateDebut.getValue().atStartOfDay());
        Timestamp tsFin = Timestamp.valueOf(dateFinPrevue.getValue().atTime(23, 59));

        double prixJour = comboVehicule.getValue().getPrixParJour();
        long nbJours = ChronoUnit.DAYS.between(dateDebut.getValue(), dateFinPrevue.getValue());
        if (nbJours == 0) nbJours = 1;
        double montantTotal = nbJours * prixJour;

        Location nouvelleLocation = new Location(
                comboVehicule.getValue().getIdVehicule(),
                txtNomClient.getText().trim(),
                txtTelephone.getText().trim(),
                tsDebut,
                tsFin,
                kmDebut,
                prixJour,
                montantTotal,
                comboStatut.getValue()
        );

        nouvelleLocation.setClientCin(txtCIN.getText() != null ? txtCIN.getText().trim() : null);
        nouvelleLocation.setAvance(avance);
        nouvelleLocation.setNotes(txtNotes.getText() != null ? txtNotes.getText().trim() : null);

        System.out.println("→ Ajout de la location pour " + nouvelleLocation.getClientNomComplet());

        boolean succes = locationService.ajouterLocation(nouvelleLocation);

        if (succes) {
            System.out.println("✓ Location ajoutée avec succès (ID: " + nouvelleLocation.getIdLocation() + ")");
            afficherSucces("✓ Location enregistrée avec succès !\nMontant total : " +
                    String.format("%.3f TND", montantTotal));

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::reinitialiser);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            System.err.println("✗ Échec de l'ajout de la location");
            afficherErreur("✗ Erreur lors de l'enregistrement de la location !");
        }
    }

    @FXML
    private void reinitialiser() {
        comboVehicule.setValue(null);
        lblInfoVehicule.setVisible(false);
        txtNomClient.clear();
        txtTelephone.clear();
        txtCIN.clear();
        dateDebut.setValue(null);
        dateFinPrevue.setValue(null);
        txtKilometrageDebut.clear();
        txtPrixParJour.clear();
        txtNombreJours.clear();
        txtMontantTotal.clear();
        txtAvance.clear();
        comboStatut.setValue("réservée");
        txtNotes.clear();
        cacherMessage();
        System.out.println("🔄 Formulaire réinitialisé");
    }

    @FXML
    private void annuler() {
        System.out.println("✖ Annulation de l'ajout de location");
        Stage stage = (Stage) txtNomClient.getScene().getWindow();
        stage.close();
    }

    private void afficherSucces(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #27ae60; -fx-background-color: #d5f4e6; -fx-font-weight: bold; -fx-border-color: #27ae60; -fx-border-width: 2; -fx-border-radius: 8;");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(500), lblMessage);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void afficherErreur(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fadbd8; -fx-font-weight: bold; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void cacherMessage() {
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }
}