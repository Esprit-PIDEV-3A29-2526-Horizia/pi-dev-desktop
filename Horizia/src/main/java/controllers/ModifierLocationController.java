package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entities.Location;
import org.example.services.LocationService;
import org.example.services.OCRService;
import org.example.services.GeolocationService;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class ModifierLocationController {

    @FXML private TextField txtRechercheClient;
    @FXML private Label lblMessageRecherche;
    @FXML private VBox vboxFormulaire;
    @FXML private Label lblLocationInfo;

    // NOUVEAUX CHAMPS - Géolocalisation
    @FXML private TextField txtAdresse;
    @FXML private TextField txtVille;
    @FXML private TextField txtCodePostal;
    @FXML private Button btnScannerCIN;
    @FXML private Label lblCoordonnees;

    @FXML private DatePicker dateFinReelle;
    @FXML private TextField txtKilometrageRetour;
    @FXML private TextField txtAvance;
    @FXML private ComboBox<String> comboStatut;
    @FXML private TextArea txtNotes;
    @FXML private Label lblMessage;

    private LocationService locationService;
    private OCRService ocrService;
    private GeolocationService geoService;
    private Location locationCourante;
    private Double latitudeClient = null;
    private Double longitudeClient = null;

    @FXML
    public void initialize() {
        locationService = new LocationService();
        ocrService = new OCRService();
        geoService = new GeolocationService();

        comboStatut.getItems().addAll("réservée", "en_cours", "terminée", "annulée", "no_show");
    }

    @FXML
    private void rechercherLocation() {
        String client = txtRechercheClient.getText();
        if (client == null || client.trim().isEmpty()) {
            return;
        }

        List<Location> locations = locationService.rechercherParClient(client.trim());
        if (locations.isEmpty()) {
            vboxFormulaire.setVisible(false);
            return;
        }

        locationCourante = locations.get(0);
        preremplirFormulaire();
        vboxFormulaire.setVisible(true);
    }

    private void preremplirFormulaire() {
        if (locationCourante.getClientAdresse() != null) {
            txtAdresse.setText(locationCourante.getClientAdresse());
        }
        if (locationCourante.getClientVille() != null) {
            txtVille.setText(locationCourante.getClientVille());
        }
        if (locationCourante.getClientCodePostal() != null) {
            txtCodePostal.setText(locationCourante.getClientCodePostal());
        }

        txtAvance.setText(String.valueOf(locationCourante.getAvance()));
        comboStatut.setValue(locationCourante.getStatut());
        txtNotes.setText(locationCourante.getNotes());
    }

    @FXML
    private void scannerCIN() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo de CIN");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) btnScannerCIN.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile == null) return;

        btnScannerCIN.setDisable(true);

        new Thread(() -> {
            Map<String, String> resultats = ocrService.scannerCIN(selectedFile);

            javafx.application.Platform.runLater(() -> {
                btnScannerCIN.setDisable(false);

                if (resultats.containsKey("adresse")) {
                    txtAdresse.setText(resultats.get("adresse"));
                }
                if (resultats.containsKey("ville")) {
                    txtVille.setText(resultats.get("ville"));
                }
                if (resultats.containsKey("code_postal")) {
                    txtCodePostal.setText(resultats.get("code_postal"));
                }
            });
        }).start();
    }

    @FXML
    private void enregistrerModifications() {
        if (locationCourante == null) return;

        try {
            if (dateFinReelle.getValue() != null) {
                locationCourante.setDateFinReelle(
                        Timestamp.valueOf(dateFinReelle.getValue().atTime(23, 59))
                );
            }

            locationCourante.setAvance(Double.parseDouble(txtAvance.getText()));
            locationCourante.setStatut(comboStatut.getValue());
            locationCourante.setNotes(txtNotes.getText());

            locationCourante.setClientAdresse(txtAdresse.getText().trim());
            locationCourante.setClientVille(txtVille.getText().trim());
            locationCourante.setClientCodePostal(txtCodePostal.getText().trim());
            locationCourante.setClientLatitude(latitudeClient);
            locationCourante.setClientLongitude(longitudeClient);

            locationService.modifierLocation(locationCourante);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void annuler() {
        Stage stage = (Stage) lblMessage.getScene().getWindow();
        stage.close();
    }
}