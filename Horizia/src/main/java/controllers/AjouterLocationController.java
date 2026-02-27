package controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Location;
import org.example.entities.Vehicule;
import org.example.entities.Pays;
import org.example.services.LocationService;
import org.example.services.VehiculeService;
import org.example.services.OCRService;
import org.example.services.GeolocationService;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AjouterLocationController {

    // Champs existants
    @FXML private ComboBox<Vehicule> comboVehicule;
    @FXML private Label lblInfoVehicule;
    @FXML private TextField txtNomClient;
    @FXML private ComboBox<Pays> comboPays;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtCIN;

    // 🆕 NOUVEAUX CHAMPS - Géolocalisation
    @FXML private TextField txtAdresse;
    @FXML private TextField txtVille;
    @FXML private TextField txtCodePostal;
    @FXML private Button btnScannerCIN;
    @FXML private Button btnGeocoderAdresse;
    @FXML private Label lblCoordonnees;

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

    // 🆕 NOUVEAUX SERVICES
    private OCRService ocrService;
    private GeolocationService geoService;

    // Coordonnées GPS stockées temporairement
    private Double latitudeClient = null;
    private Double longitudeClient = null;

    // Patterns de validation
    private static final Pattern PHONE_TUNISIA = Pattern.compile("^(\\+216)?[2459]\\d{7}$");
    private static final Pattern CIN_TUNISIA = Pattern.compile("^[0-9]{8}$");

    @FXML
    public void initialize() {
        vehiculeService = new VehiculeService();
        locationService = new LocationService();
        ocrService = new OCRService();
        geoService = new GeolocationService();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Ajouter Location - Chargée (avec OCR)");
        System.out.println("═══════════════════════════════════════════════");

        initialiserComboBoxes();
        chargerVehiculesDisponibles();
        configurerCalculAutomatique();
        configurerValidationEnTempsReel();
        configurerGeolocalisation();
    }

    private void initialiserComboBoxes() {
        comboStatut.getItems().addAll("réservée", "en_cours", "terminée", "annulée", "no_show");
        comboStatut.setValue("réservée");

        comboPays.getItems().addAll(Pays.getPaysSupportes());
        comboPays.setValue(comboPays.getItems().get(0)); // Tunisie par défaut

        comboPays.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtTelephone.setPromptText("Ex: " + newVal.getFormatExemple());
            }
        });
    }

    private void chargerVehiculesDisponibles() {
        List<Vehicule> vehicules = vehiculeService.getAllVehicules().stream()
                .filter(v -> "disponible".equals(v.getEtat()))
                .toList();

        comboVehicule.getItems().addAll(vehicules);
        System.out.println("✓ " + vehicules.size() + " véhicule(s) disponible(s)");
    }

    private void configurerCalculAutomatique() {
        comboVehicule.valueProperty().addListener((obs, old, nouv) -> {
            if (nouv != null) {
                txtPrixParJour.setText(String.format("%.3f", nouv.getPrixParJour()));
                lblInfoVehicule.setText(String.format("%s - %.3f TND/jour",
                        nouv.getImmatriculation(), nouv.getPrixParJour()));
                calculerMontantTotal();
            }
        });

        dateDebut.valueProperty().addListener((obs, old, nouv) -> calculerNombreJours());
        dateFinPrevue.valueProperty().addListener((obs, old, nouv) -> calculerNombreJours());
        txtPrixParJour.textProperty().addListener((obs, old, nouv) -> calculerMontantTotal());
        txtNombreJours.textProperty().addListener((obs, old, nouv) -> calculerMontantTotal());
    }

    private void calculerNombreJours() {
        if (dateDebut.getValue() != null && dateFinPrevue.getValue() != null) {
            long jours = ChronoUnit.DAYS.between(dateDebut.getValue(), dateFinPrevue.getValue());
            if (jours < 0) {
                afficherErreur("La date de fin doit être après la date de début !");
                txtNombreJours.clear();
            } else {
                txtNombreJours.setText(String.valueOf(jours > 0 ? jours : 1));
            }
        }
    }

    private void calculerMontantTotal() {
        try {
            if (!txtPrixParJour.getText().isEmpty() && !txtNombreJours.getText().isEmpty()) {
                double prix = Double.parseDouble(txtPrixParJour.getText());
                int jours = Integer.parseInt(txtNombreJours.getText());
                double total = prix * jours;
                txtMontantTotal.setText(String.format("%.3f", total));
            }
        } catch (NumberFormatException e) {
            // Ignorer les erreurs de parsing pendant la saisie
        }
    }

    private void configurerValidationEnTempsReel() {
        txtTelephone.textProperty().addListener((obs, old, nouv) -> {
            if (!nouv.isEmpty() && !PHONE_TUNISIA.matcher(nouv).matches()) {
                txtTelephone.setStyle("-fx-border-color: orange;");
            } else {
                txtTelephone.setStyle("");
            }
        });

        txtCIN.textProperty().addListener((obs, old, nouv) -> {
            if (!nouv.isEmpty() && !CIN_TUNISIA.matcher(nouv).matches()) {
                txtCIN.setStyle("-fx-border-color: orange;");
            } else {
                txtCIN.setStyle("");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    // 🆕 GÉOLOCALISATION AUTOMATIQUE
    // ═══════════════════════════════════════════════════════════════

    private void configurerGeolocalisation() {
        // Géocoder automatiquement quand l'adresse ou la ville change
        txtAdresse.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused && !txtAdresse.getText().trim().isEmpty()) {
                geocoderAdresseAuto();
            }
        });

        txtVille.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused && !txtVille.getText().trim().isEmpty()) {
                geocoderAdresseAuto();
            }
        });
    }

    /**
     * 🆕 Géocoder automatiquement l'adresse entrée
     */
    private void geocoderAdresseAuto() {
        String adresse = txtAdresse.getText().trim();
        String ville = txtVille.getText().trim();

        if (adresse.isEmpty() && ville.isEmpty()) {
            return;
        }

        // Construire l'adresse complète
        String adresseComplete;
        if (!ville.isEmpty()) {
            adresseComplete = adresse.isEmpty() ? ville : adresse + ", " + ville + ", Tunisie";
        } else {
            adresseComplete = adresse;
        }

        System.out.println("→ Géocodage automatique : " + adresseComplete);

        // Géocoder en arrière-plan
        new Thread(() -> {
            Map<String, String> resultats = geoService.geocoderAdresse(adresseComplete);

            javafx.application.Platform.runLater(() -> {
                if (resultats.containsKey("latitude") && resultats.containsKey("longitude")) {
                    latitudeClient = Double.parseDouble(resultats.get("latitude"));
                    longitudeClient = Double.parseDouble(resultats.get("longitude"));

                    // Calculer la distance depuis l'agence
                    double distance = geoService.calculerDistanceDepuisAgence(latitudeClient, longitudeClient);
                    String distanceFormatee = geoService.formaterDistance(distance);

                    lblCoordonnees.setText("📍 " + distanceFormatee + " de l'agence");
                    lblCoordonnees.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

                    // Auto-remplir la ville si vide
                    if (txtVille.getText().trim().isEmpty() && resultats.containsKey("ville")) {
                        txtVille.setText(resultats.get("ville"));
                    }

                    // Auto-remplir le code postal si vide
                    if (txtCodePostal.getText().trim().isEmpty() && resultats.containsKey("code_postal")) {
                        txtCodePostal.setText(resultats.get("code_postal"));
                    }

                    System.out.println("✓ Géocodage réussi : " + distanceFormatee);
                } else {
                    lblCoordonnees.setText("⚠ Adresse non trouvée");
                    lblCoordonnees.setStyle("-fx-text-fill: #e67e22;");
                }
            });
        }).start();
    }

    /**
     * 🆕 Bouton manuel pour géocoder
     */
    @FXML
    private void geocoderAdresseManuel() {
        geocoderAdresseAuto();
    }

    // ═══════════════════════════════════════════════════════════════
    // 🆕 SCANNER CIN AVEC OCR
    // ═══════════════════════════════════════════════════════════════

    /**
     * 🆕 Scanner une CIN avec OCR
     */
    @FXML
    private void scannerCIN() {
        System.out.println("→ Ouverture du sélecteur de fichier...");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo de CIN");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        Stage stage = (Stage) btnScannerCIN.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile == null) {
            System.out.println("✗ Aucun fichier sélectionné");
            return;
        }

        System.out.println("✓ Fichier sélectionné : " + selectedFile.getName());
        afficherInfo("⏳ Scan en cours... Veuillez patienter.");

        // Désactiver le bouton pendant le scan
        btnScannerCIN.setDisable(true);

        // Scanner en arrière-plan
        new Thread(() -> {
            Map<String, String> resultats = ocrService.scannerCIN(selectedFile);

            // Mettre à jour l'interface sur le thread JavaFX
            javafx.application.Platform.runLater(() -> {
                btnScannerCIN.setDisable(false);

                if (resultats.containsKey("erreur")) {
                    afficherErreur("❌ " + resultats.get("erreur"));
                    return;
                }

                // Remplir automatiquement les champs
                if (resultats.containsKey("nom_complet")) {
                    txtNomClient.setText(resultats.get("nom_complet"));
                }

                if (resultats.containsKey("cin")) {
                    txtCIN.setText(resultats.get("cin"));
                }

                if (resultats.containsKey("adresse")) {
                    txtAdresse.setText(resultats.get("adresse"));
                }

                if (resultats.containsKey("ville")) {
                    txtVille.setText(resultats.get("ville"));
                }

                if (resultats.containsKey("code_postal")) {
                    txtCodePostal.setText(resultats.get("code_postal"));
                }

                // Géocoder l'adresse extraite
                if (resultats.containsKey("adresse")) {
                    geocoderAdresseAuto();
                }

                afficherSucces("✓ CIN scannée avec succès !");
                System.out.println("✓ Scan OCR terminé avec succès");
            });
        }).start();
    }

    // ═══════════════════════════════════════════════════════════════
    // AJOUTER LA LOCATION
    // ═══════════════════════════════════════════════════════════════

    @FXML
    private void ajouterLocation() {
        // Validations
        if (!validerFormulaire()) {
            return;
        }

        try {
            // Créer l'objet Location
            Location nouvelleLocation = new Location();
            nouvelleLocation.setIdVehicule(comboVehicule.getValue().getIdVehicule());
            nouvelleLocation.setClientNomComplet(txtNomClient.getText().trim());

            // Formater le téléphone avec indicatif
            Pays paysSel = comboPays.getValue();
            String telephone = paysSel.formaterNumero(txtTelephone.getText().trim());
            nouvelleLocation.setClientTelephone(telephone);

            nouvelleLocation.setClientCin(txtCIN.getText().trim());

            // 🆕 Ajouter les champs de géolocalisation
            nouvelleLocation.setClientAdresse(txtAdresse.getText().trim());
            nouvelleLocation.setClientVille(txtVille.getText().trim());
            nouvelleLocation.setClientCodePostal(txtCodePostal.getText().trim());
            nouvelleLocation.setClientLatitude(latitudeClient);
            nouvelleLocation.setClientLongitude(longitudeClient);

            nouvelleLocation.setDateDebut(Timestamp.valueOf(dateDebut.getValue().atStartOfDay()));
            nouvelleLocation.setDateFinPrev(Timestamp.valueOf(dateFinPrevue.getValue().atTime(23, 59)));
            nouvelleLocation.setKilometrageDebut(Integer.parseInt(txtKilometrageDebut.getText()));
            nouvelleLocation.setPrixParJour(Double.parseDouble(txtPrixParJour.getText()));
            nouvelleLocation.setMontantTotal(Double.parseDouble(txtMontantTotal.getText()));
            nouvelleLocation.setAvance(txtAvance.getText().isEmpty() ? 0 : Double.parseDouble(txtAvance.getText()));
            nouvelleLocation.setStatut(comboStatut.getValue());
            nouvelleLocation.setNotes(txtNotes.getText());

            // Sauvegarder
            boolean succes = locationService.ajouterLocation(nouvelleLocation);

            if (succes) {
                afficherSucces("✓ Location ajoutée avec succès !\nID: " + nouvelleLocation.getIdLocation());

                // Réinitialiser après 2 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(this::reinitialiser);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                afficherErreur("✗ Erreur lors de l'ajout de la location !");
            }

        } catch (Exception e) {
            afficherErreur("✗ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validerFormulaire() {
        if (comboVehicule.getValue() == null) {
            afficherErreur("⚠ Veuillez sélectionner un véhicule !");
            return false;
        }

        if (txtNomClient.getText().trim().isEmpty()) {
            afficherErreur("⚠ Le nom du client est obligatoire !");
            txtNomClient.requestFocus();
            return false;
        }

        if (txtTelephone.getText().trim().isEmpty()) {
            afficherErreur("⚠ Le téléphone est obligatoire !");
            txtTelephone.requestFocus();
            return false;
        }

        if (dateDebut.getValue() == null || dateFinPrevue.getValue() == null) {
            afficherErreur("⚠ Les dates sont obligatoires !");
            return false;
        }

        if (dateFinPrevue.getValue().isBefore(dateDebut.getValue())) {
            afficherErreur("⚠ La date de fin doit être après la date de début !");
            return false;
        }

        return true;
    }

    @FXML
    private void reinitialiser() {
        comboVehicule.setValue(null);
        lblInfoVehicule.setText("Sélectionnez un véhicule");
        txtNomClient.clear();
        txtTelephone.clear();
        txtCIN.clear();
        txtAdresse.clear();
        txtVille.clear();
        txtCodePostal.clear();
        dateDebut.setValue(LocalDate.now());
        dateFinPrevue.setValue(LocalDate.now().plusDays(1));
        txtKilometrageDebut.clear();
        txtPrixParJour.clear();
        txtNombreJours.clear();
        txtMontantTotal.clear();
        txtAvance.clear();
        comboStatut.setValue("réservée");
        txtNotes.clear();
        cacherMessage();
        lblCoordonnees.setText("");
        latitudeClient = null;
        longitudeClient = null;
        chargerVehiculesDisponibles();
    }

    @FXML
    private void annuler() {
        Stage stage = (Stage) btnAjouter.getScene().getWindow();
        stage.close();
    }

    // Messages
    private void afficherSucces(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #27ae60; -fx-background-color: #d5f4e6; " +
                "-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #27ae60; " +
                "-fx-border-width: 2; -fx-border-radius: 5;");
        lblMessage.setVisible(true);

        FadeTransition fade = new FadeTransition(Duration.millis(300), lblMessage);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void afficherErreur(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fadbd8; " +
                "-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #e74c3c; " +
                "-fx-border-width: 2; -fx-border-radius: 5;");
        lblMessage.setVisible(true);

        FadeTransition fade = new FadeTransition(Duration.millis(300), lblMessage);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void afficherInfo(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #3498db; -fx-background-color: #d6eaf8; " +
                "-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #3498db; " +
                "-fx-border-width: 2; -fx-border-radius: 5;");
        lblMessage.setVisible(true);
    }

    private void cacherMessage() {
        lblMessage.setVisible(false);
    }
}