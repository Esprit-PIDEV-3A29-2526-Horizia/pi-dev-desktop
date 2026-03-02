package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.Location;
import tn.esprit.entities.Vehicule;
import tn.esprit.entities.Pays;
import tn.esprit.services.LocationService;
import tn.esprit.services.VehiculeService;
import tn.esprit.services.OCRService;
import tn.esprit.services.GeolocationService;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AjouterLocationController {

    @FXML private ComboBox<Vehicule> comboVehicule;
    @FXML private Label lblInfoVehicule;
    @FXML private TextField txtNomClient;
    @FXML private ComboBox<Pays> comboPays;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtCIN;

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
    private OCRService ocrService;
    private GeolocationService geoService;

    private Double latitudeClient = null;
    private Double longitudeClient = null;

    private static final Pattern PHONE_TUNISIA = Pattern.compile("^(\\+216)?[2459]\\d{7}$");
    private static final Pattern CIN_TUNISIA   = Pattern.compile("^[0-9]{8}$");

    @FXML
    public void initialize() {
        vehiculeService = new VehiculeService();
        locationService = new LocationService();
        ocrService      = new OCRService();
        geoService      = new GeolocationService();

        initialiserComboBoxes();
        chargerVehiculesDisponibles();
        configurerCalculAutomatique();
        configurerValidationEnTempsReel();
        configurerGeolocalisation();

        // Initialiser avec des dates par défaut
        dateDebut.setValue(LocalDate.now());
        dateFinPrevue.setValue(LocalDate.now().plusDays(1));
    }

    // ── INITIALISATION ──────────────────────────────────────────────

    private void initialiserComboBoxes() {
        comboStatut.getItems().addAll("réservée", "en_cours", "terminée", "annulée", "no_show");
        comboStatut.setValue("réservée");

        comboPays.getItems().addAll(Pays.getPaysSupportes());
        comboPays.setValue(comboPays.getItems().get(0));

        comboPays.valueProperty().addListener((obs, o, n) -> {
            if (n != null) txtTelephone.setPromptText("Ex: " + n.getFormatExemple());
        });
    }

    private void chargerVehiculesDisponibles() {
        List<Vehicule> vehicules = vehiculeService.getAllVehicules().stream()
                .filter(v -> "disponible".equals(v.getEtat())).toList();
        comboVehicule.getItems().setAll(vehicules);
        System.out.println("✓ " + vehicules.size() + " véhicule(s) disponible(s)");
    }

    private void configurerCalculAutomatique() {
        // Listener pour le véhicule sélectionné
        comboVehicule.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtPrixParJour.setText(String.format("%.3f", newVal.getPrixParJour()));
                lblInfoVehicule.setText(newVal.getImmatriculation() + " – " +
                        String.format("%.3f", newVal.getPrixParJour()) + " TND/jour");

                // Déclencher le calcul du montant total après avoir défini le prix
                calculerMontantTotal();
            } else {
                txtPrixParJour.clear();
                lblInfoVehicule.setText("Sélectionnez un véhicule");
            }
        });

        // Listener pour les dates
        dateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            calculerNombreJours();
            // Après avoir recalculé le nombre de jours, recalculer le montant
            calculerMontantTotal();
        });

        dateFinPrevue.valueProperty().addListener((obs, oldVal, newVal) -> {
            calculerNombreJours();
            calculerMontantTotal();
        });

        // Listener pour le prix par jour
        txtPrixParJour.textProperty().addListener((obs, oldVal, newVal) -> {
            calculerMontantTotal();
        });

        // Listener pour le nombre de jours
        txtNombreJours.textProperty().addListener((obs, oldVal, newVal) -> {
            calculerMontantTotal();
        });
    }

    private void calculerNombreJours() {
        if (dateDebut.getValue() != null && dateFinPrevue.getValue() != null) {
            long jours = ChronoUnit.DAYS.between(dateDebut.getValue(), dateFinPrevue.getValue());

            if (jours < 0) {
                afficherErreur("La date de fin doit être après la date de début !");
                txtNombreJours.clear();
            } else {
                // Si jours = 0 (même jour), compter comme 1 jour
                long joursEffectifs = (jours == 0) ? 1 : jours;
                txtNombreJours.setText(String.valueOf(joursEffectifs));
            }
        } else {
            txtNombreJours.clear();
        }
    }

    private void calculerMontantTotal() {
        try {
            // Vérifier que les deux champs nécessaires sont présents
            boolean prixPresent = txtPrixParJour.getText() != null && !txtPrixParJour.getText().trim().isEmpty();
            boolean joursPresent = txtNombreJours.getText() != null && !txtNombreJours.getText().trim().isEmpty();

            if (prixPresent && joursPresent) {
                double prix = Double.parseDouble(txtPrixParJour.getText().trim().replace(",", "."));
                int jours = Integer.parseInt(txtNombreJours.getText().trim());

                if (prix > 0 && jours > 0) {
                    double montant = prix * jours;

                    // ✅ IMPORTANT: S'assurer que c'est bien le txtMontantTotal qui est mis à jour
                    txtMontantTotal.setText(String.format("%.3f", montant).replace(".", ","));

                    // Ne PAS toucher à txtAvance ici !
                    System.out.println("✓ Montant calculé: " + prix + " x " + jours + " = " + montant);

                    // Vérification visuelle dans la console
                    System.out.println("   txtMontantTotal = " + txtMontantTotal.getText());
                    System.out.println("   txtAvance = " + txtAvance.getText());
                } else {
                    txtMontantTotal.clear();
                }
            } else {
                txtMontantTotal.clear();
            }
        } catch (NumberFormatException e) {
            System.err.println("⚠ Erreur calcul montant: " + e.getMessage());
            txtMontantTotal.clear();
        }
    }
    private void configurerValidationEnTempsReel() {
        txtTelephone.textProperty().addListener((obs, o, n) -> {
            if (n != null && !n.isEmpty()) {
                txtTelephone.setStyle(PHONE_TUNISIA.matcher(n).matches()
                        ? "" : "-fx-border-color: orange;");
            } else {
                txtTelephone.setStyle("");
            }
        });

        txtCIN.textProperty().addListener((obs, o, n) -> {
            if (n != null && !n.isEmpty()) {
                txtCIN.setStyle(CIN_TUNISIA.matcher(n).matches()
                        ? "" : "-fx-border-color: orange;");
            } else {
                txtCIN.setStyle("");
            }
        });
    }

    private void configurerGeolocalisation() {
        txtAdresse.focusedProperty().addListener((obs, was, is) -> {
            if (was && !is && !txtAdresse.getText().trim().isEmpty()) geocoderAdresseAuto();
        });
        txtVille.focusedProperty().addListener((obs, was, is) -> {
            if (was && !is && !txtVille.getText().trim().isEmpty()) geocoderAdresseAuto();
        });
    }

    // ── GÉOLOCALISATION ─────────────────────────────────────────────

    private void geocoderAdresseAuto() {
        String adresse = txtAdresse.getText().trim();
        String ville   = txtVille.getText().trim();
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
                    lblCoordonnees.setText("📍 " + geoService.formaterDistance(dist) + " de l'agence");
                    lblCoordonnees.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    if (txtVille.getText().trim().isEmpty() && r.containsKey("ville"))
                        txtVille.setText(r.get("ville"));
                    if (txtCodePostal.getText().trim().isEmpty() && r.containsKey("code_postal"))
                        txtCodePostal.setText(r.get("code_postal"));
                } else {
                    lblCoordonnees.setText("⚠ Adresse non trouvée");
                    lblCoordonnees.setStyle("-fx-text-fill: #e67e22;");
                }
            });
        }).start();
    }

    @FXML private void geocoderAdresseManuel() { geocoderAdresseAuto(); }

    // ── SCAN CIN ────────────────────────────────────────────────────

    @FXML
    private void scannerCIN() {
        File fichier = choisirImage("Sélectionnez l'image de la CIN (recto)");
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
                    txtCIN.setText(res.get("cin"));
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

    // ── AJOUTER LOCATION ────────────────────────────────────────────

    @FXML
    private void ajouterLocation() {
        if (!validerFormulaire()) return;

        try {
            Location loc = new Location();
            loc.setIdVehicule(comboVehicule.getValue().getIdVehicule());
            loc.setClientNomComplet(txtNomClient.getText().trim());

            Pays pays = comboPays.getValue();
            loc.setClientTelephone(pays.formaterNumero(txtTelephone.getText().trim()));
            loc.setClientCin(txtCIN.getText().trim());
            loc.setClientAdresse(txtAdresse.getText().trim());
            loc.setClientVille(txtVille.getText().trim());
            loc.setClientCodePostal(txtCodePostal.getText().trim());
            loc.setClientLatitude(latitudeClient);
            loc.setClientLongitude(longitudeClient);

            loc.setDateDebut(Timestamp.valueOf(dateDebut.getValue().atStartOfDay()));
            loc.setDateFinPrev(Timestamp.valueOf(dateFinPrevue.getValue().atTime(23, 59, 59)));

            if (!txtKilometrageDebut.getText().trim().isEmpty()) {
                loc.setKilometrageDebut(Integer.parseInt(txtKilometrageDebut.getText().trim()));
            }

            loc.setPrixParJour(Double.parseDouble(txtPrixParJour.getText().trim()));
            loc.setMontantTotal(Double.parseDouble(txtMontantTotal.getText().trim()));

            if (!txtAvance.getText().trim().isEmpty()) {
                loc.setAvance(Double.parseDouble(txtAvance.getText().trim()));
            } else {
                loc.setAvance(0);
            }

            loc.setStatut(comboStatut.getValue());
            loc.setNotes(txtNotes.getText().trim());

            boolean ok = locationService.ajouterLocation(loc);
            if (ok) {
                afficherSucces("✓ Location ajoutée ! ID : " + loc.getIdLocation());
                new Thread(() -> {
                    try { Thread.sleep(2000); }
                    catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(this::reinitialiser);
                }).start();
            } else {
                afficherErreur("✗ Erreur lors de l'ajout !");
            }
        } catch (NumberFormatException e) {
            afficherErreur("✗ Erreur de format numérique : " + e.getMessage());
        } catch (Exception e) {
            afficherErreur("✗ " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validerFormulaire() {
        if (comboVehicule.getValue() == null) {
            afficherErreur("⚠ Sélectionnez un véhicule !");
            return false;
        }

        if (txtNomClient.getText().trim().isEmpty()) {
            afficherErreur("⚠ Nom du client obligatoire !");
            txtNomClient.requestFocus();
            return false;
        }

        if (txtTelephone.getText().trim().isEmpty()) {
            afficherErreur("⚠ Téléphone obligatoire !");
            txtTelephone.requestFocus();
            return false;
        }

        if (!PHONE_TUNISIA.matcher(txtTelephone.getText().trim()).matches()) {
            afficherErreur("⚠ Format de téléphone invalide !");
            txtTelephone.requestFocus();
            return false;
        }

        if (!txtCIN.getText().trim().isEmpty() && !CIN_TUNISIA.matcher(txtCIN.getText().trim()).matches()) {
            afficherErreur("⚠ Le CIN doit contenir 8 chiffres !");
            txtCIN.requestFocus();
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

        if (txtKilometrageDebut.getText().trim().isEmpty()) {
            afficherErreur("⚠ Kilométrage de départ obligatoire !");
            txtKilometrageDebut.requestFocus();
            return false;
        }

        try {
            Integer.parseInt(txtKilometrageDebut.getText().trim());
        } catch (NumberFormatException e) {
            afficherErreur("⚠ Kilométrage doit être un nombre !");
            txtKilometrageDebut.requestFocus();
            return false;
        }

        if (txtPrixParJour.getText().trim().isEmpty()) {
            afficherErreur("⚠ Sélectionnez un véhicule avec un prix valide !");
            return false;
        }

        if (txtMontantTotal.getText().trim().isEmpty()) {
            afficherErreur("⚠ Le montant total n'a pas pu être calculé !");
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
        ((Stage) btnAjouter.getScene().getWindow()).close();
    }

    // ── MESSAGES ────────────────────────────────────────────────────

    private void afficherSucces(String msg) {
        afficherMessage(msg,
                "-fx-text-fill:#27ae60;-fx-background-color:#d5f4e6;-fx-border-color:#27ae60;");
    }

    private void afficherErreur(String msg) {
        afficherMessage(msg,
                "-fx-text-fill:#e74c3c;-fx-background-color:#fadbd8;-fx-border-color:#e74c3c;");
    }

    private void afficherInfo(String msg) {
        afficherMessage(msg,
                "-fx-text-fill:#3498db;-fx-background-color:#d6eaf8;-fx-border-color:#3498db;");
    }

    private void afficherMessage(String msg, String style) {
        lblMessage.setText(msg);
        lblMessage.setStyle(style + "-fx-padding:10;-fx-background-radius:5;-fx-border-width:2;-fx-border-radius:5;");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), lblMessage);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void cacherMessage() {
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }
}