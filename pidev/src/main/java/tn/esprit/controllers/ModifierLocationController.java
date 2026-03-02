package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.Location;
import tn.esprit.services.LocationService;
import tn.esprit.services.OCRService;
import tn.esprit.services.GeolocationService;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class ModifierLocationController {

    @FXML private TextField txtRechercheClient;
    @FXML private Label     lblMessageRecherche;
    @FXML private VBox      vboxFormulaire;
    @FXML private Label     lblLocationInfo;

    @FXML private TextField txtNomClient;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtCIN;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtVille;
    @FXML private TextField txtCodePostal;
    @FXML private Button    btnScannerCIN;
    @FXML private Label     lblCoordonnees;

    @FXML private DatePicker         dateFinReelle;
    @FXML private TextField          txtKilometrageRetour;
    @FXML private TextField          txtAvance;
    @FXML private ComboBox<String>   comboStatut;
    @FXML private TextArea           txtNotes;
    @FXML private Label              lblMessage;

    private LocationService    locationService;
    private OCRService         ocrService;
    private GeolocationService geoService;

    private Location locationCourante;
    private Double   latitudeClient  = null;
    private Double   longitudeClient = null;

    @FXML
    public void initialize() {
        locationService = new LocationService();
        ocrService      = new OCRService();
        geoService      = new GeolocationService();

        comboStatut.getItems().addAll("réservée", "en_cours", "terminée", "annulée", "no_show");
        configurerGeolocalisation();
    }

    // ── GÉOLOCALISATION ─────────────────────────────────────────────

    private void configurerGeolocalisation() {
        if (txtAdresse != null)
            txtAdresse.focusedProperty().addListener((obs, was, is) -> {
                if (was && !is && !txtAdresse.getText().trim().isEmpty()) geocoderAdresseAuto();
            });
        if (txtVille != null)
            txtVille.focusedProperty().addListener((obs, was, is) -> {
                if (was && !is && !txtVille.getText().trim().isEmpty()) geocoderAdresseAuto();
            });
    }

    @FXML
    private void geocoderAdresseManuel() { geocoderAdresseAuto(); }

    private void geocoderAdresseAuto() {
        String adresse = txtAdresse != null ? txtAdresse.getText().trim() : "";
        String ville   = txtVille   != null ? txtVille.getText().trim()   : "";
        if (adresse.isEmpty() && ville.isEmpty()) return;

        String adresseComplete = (!adresse.isEmpty() && !ville.isEmpty())
                ? adresse + ", " + ville + ", Tunisie"
                : (!adresse.isEmpty() ? adresse : ville);

        new Thread(() -> {
            Map<String, String> r = geoService.geocoderAdresse(adresseComplete);
            javafx.application.Platform.runLater(() -> {
                if (r.containsKey("latitude") && r.containsKey("longitude")) {
                    latitudeClient  = Double.parseDouble(r.get("latitude"));
                    longitudeClient = Double.parseDouble(r.get("longitude"));
                    double dist = geoService.calculerDistanceDepuisAgence(latitudeClient, longitudeClient);
                    if (lblCoordonnees != null) {
                        lblCoordonnees.setText("📍 " + geoService.formaterDistance(dist) + " de l'agence");
                        lblCoordonnees.setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;");
                    }
                    if (txtVille != null && txtVille.getText().trim().isEmpty() && r.containsKey("ville"))
                        txtVille.setText(r.get("ville"));
                    if (txtCodePostal != null && txtCodePostal.getText().trim().isEmpty() && r.containsKey("code_postal"))
                        txtCodePostal.setText(r.get("code_postal"));
                } else if (lblCoordonnees != null) {
                    lblCoordonnees.setText("⚠ Adresse non trouvée");
                    lblCoordonnees.setStyle("-fx-text-fill:#e67e22;");
                }
            });
        }).start();
    }

    // ── RECHERCHE ────────────────────────────────────────────────────

    @FXML
    private void rechercherLocation() {
        String client = txtRechercheClient.getText();
        if (client == null || client.trim().isEmpty()) {
            afficherMessageRecherche("⚠ Entrez un nom de client.", false);
            return;
        }

        List<Location> locations = locationService.rechercherParClient(client.trim());
        if (locations.isEmpty()) {
            afficherMessageRecherche("✗ Aucune location trouvée pour : " + client, false);
            vboxFormulaire.setVisible(false);
            vboxFormulaire.setManaged(false);
            return;
        }

        locationCourante = locations.get(0);
        preremplirFormulaire();
        vboxFormulaire.setVisible(true);
        vboxFormulaire.setManaged(true);
        afficherMessageRecherche("✓ Location trouvée !", true);
    }

    private void preremplirFormulaire() {
        if (locationCourante == null) return;

        if (txtNomClient != null && locationCourante.getClientNomComplet() != null)
            txtNomClient.setText(locationCourante.getClientNomComplet());
        if (txtTelephone != null && locationCourante.getClientTelephone() != null)
            txtTelephone.setText(locationCourante.getClientTelephone());
        if (txtCIN != null && locationCourante.getClientCin() != null)
            txtCIN.setText(locationCourante.getClientCin());
        if (txtAdresse != null && locationCourante.getClientAdresse() != null)
            txtAdresse.setText(locationCourante.getClientAdresse());
        if (txtVille != null && locationCourante.getClientVille() != null)
            txtVille.setText(locationCourante.getClientVille());
        if (txtCodePostal != null && locationCourante.getClientCodePostal() != null)
            txtCodePostal.setText(locationCourante.getClientCodePostal());

        if (locationCourante.getClientLatitude() != null && locationCourante.getClientLongitude() != null) {
            latitudeClient  = locationCourante.getClientLatitude();
            longitudeClient = locationCourante.getClientLongitude();
            double dist = geoService.calculerDistanceDepuisAgence(latitudeClient, longitudeClient);
            if (lblCoordonnees != null) {
                lblCoordonnees.setText("📍 " + geoService.formaterDistance(dist) + " de l'agence");
                lblCoordonnees.setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;");
            }
        }

        if (txtAvance != null)   txtAvance.setText(String.valueOf(locationCourante.getAvance()));
        if (comboStatut != null) comboStatut.setValue(locationCourante.getStatut());
        if (txtNotes != null)    txtNotes.setText(locationCourante.getNotes());

        if (lblLocationInfo != null)
            lblLocationInfo.setText(String.format(
                    "Location #%d – Client: %s – Véhicule ID: %d – Statut: %s",
                    locationCourante.getIdLocation(),
                    locationCourante.getClientNomComplet(),
                    locationCourante.getIdVehicule(),
                    locationCourante.getStatut()));
    }

    // ── SCAN CIN ────────────────────────────────────────────────────

    /**
     * Scan simplifié : sélectionner l'image → extrait le CIN (8 chiffres) uniquement
     */
    @FXML
    private void scannerCIN() {
        File fichier = choisirImage("Sélectionnez l'image de la CIN");
        if (fichier == null) return;

        afficherInfo("⏳ Lecture du CIN en cours…");
        btnScannerCIN.setDisable(true);

        new Thread(() -> {
            Map<String, String> res = ocrService.scannerCINRecto(fichier);

            javafx.application.Platform.runLater(() -> {
                btnScannerCIN.setDisable(false);

                if (res.containsKey("erreur")) {
                    afficherErreur("❌ " + res.get("erreur"));
                    return;
                }

                if (res.containsKey("cin")) {
                    if (txtCIN != null) txtCIN.setText(res.get("cin"));
                    afficherSucces("✓ CIN détecté : " + res.get("cin") + " – Complétez le nom et l'adresse manuellement.");
                } else {
                    afficherErreur("⚠ CIN non détecté – vérifiez la qualité de l'image et réessayez.");
                }
            });
        }).start();
    }

    private File choisirImage(String titre) {
        FileChooser fc = new FileChooser();
        fc.setTitle(titre);
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp"),
                new FileChooser.ExtensionFilter("Tous", "*.*")
        );
        Stage stage = (Stage) btnScannerCIN.getScene().getWindow();
        return fc.showOpenDialog(stage);
    }

    // ── ENREGISTRER ──────────────────────────────────────────────────

    @FXML
    private void enregistrerModifications() {
        if (locationCourante == null) {
            afficherErreur("⚠ Aucune location sélectionnée !");
            return;
        }
        try {
            if (txtNomClient != null && !txtNomClient.getText().trim().isEmpty())
                locationCourante.setClientNomComplet(txtNomClient.getText().trim());
            if (txtTelephone != null && !txtTelephone.getText().trim().isEmpty())
                locationCourante.setClientTelephone(txtTelephone.getText().trim());
            if (txtCIN != null && !txtCIN.getText().trim().isEmpty())
                locationCourante.setClientCin(txtCIN.getText().trim());

            if (txtAdresse != null)    locationCourante.setClientAdresse(txtAdresse.getText().trim());
            if (txtVille != null)      locationCourante.setClientVille(txtVille.getText().trim());
            if (txtCodePostal != null) locationCourante.setClientCodePostal(txtCodePostal.getText().trim());
            locationCourante.setClientLatitude(latitudeClient);
            locationCourante.setClientLongitude(longitudeClient);

            if (dateFinReelle != null && dateFinReelle.getValue() != null)
                locationCourante.setDateFinReelle(
                        Timestamp.valueOf(dateFinReelle.getValue().atTime(23, 59)));

            if (txtKilometrageRetour != null && !txtKilometrageRetour.getText().trim().isEmpty())
                locationCourante.setKilometrageRetour(
                        Integer.parseInt(txtKilometrageRetour.getText().trim()));

            if (txtAvance != null && !txtAvance.getText().trim().isEmpty())
                locationCourante.setAvance(Double.parseDouble(txtAvance.getText().trim()));
            if (comboStatut != null && comboStatut.getValue() != null)
                locationCourante.setStatut(comboStatut.getValue());
            if (txtNotes != null)
                locationCourante.setNotes(txtNotes.getText());

            boolean ok = locationService.modifierLocation(locationCourante);
            if (ok)
                afficherSucces("✓ Location #" + locationCourante.getIdLocation() + " mise à jour !");
            else
                afficherErreur("✗ Erreur lors de la mise à jour !");

        } catch (Exception e) {
            afficherErreur("✗ " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── MESSAGES ────────────────────────────────────────────────────

    private void afficherSucces(String msg) { afficherMessage(msg,
            "-fx-text-fill:#27ae60;-fx-background-color:#d5f4e6;-fx-border-color:#27ae60;"); }

    private void afficherErreur(String msg) { afficherMessage(msg,
            "-fx-text-fill:#e74c3c;-fx-background-color:#fadbd8;-fx-border-color:#e74c3c;"); }

    private void afficherInfo(String msg) { afficherMessage(msg,
            "-fx-text-fill:#3498db;-fx-background-color:#d6eaf8;-fx-border-color:#3498db;"); }

    private void afficherMessage(String msg, String style) {
        if (lblMessage == null) return;
        lblMessage.setText(msg);
        lblMessage.setStyle(style + "-fx-padding:10;-fx-background-radius:5;-fx-border-width:2;-fx-border-radius:5;");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), lblMessage);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void afficherMessageRecherche(String msg, boolean ok) {
        if (lblMessageRecherche == null) return;
        lblMessageRecherche.setText(msg);
        lblMessageRecherche.setStyle(ok
                ? "-fx-text-fill:#27ae60;-fx-padding:8;"
                : "-fx-text-fill:#e74c3c;-fx-padding:8;");
        lblMessageRecherche.setVisible(true);
        lblMessageRecherche.setManaged(true);
    }

    @FXML
    private void annuler() {
        Stage stage = (Stage) (lblMessage != null
                ? lblMessage.getScene().getWindow()
                : btnScannerCIN.getScene().getWindow());
        stage.close();
    }
}