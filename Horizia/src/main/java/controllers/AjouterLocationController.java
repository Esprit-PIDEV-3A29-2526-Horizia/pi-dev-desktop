package controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Location;
import org.example.entities.Vehicule;
import org.example.entities.Pays;
import org.example.services.LocationService;
import org.example.services.VehiculeService;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

public class AjouterLocationController {

    @FXML private ComboBox<Vehicule> comboVehicule;
    @FXML private Label lblInfoVehicule;
    @FXML private TextField txtNomClient;
    @FXML private ComboBox<Pays> comboPays;
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

    // Patterns de validation pour différents pays
    private static final Pattern PHONE_TUNISIA = Pattern.compile("^(\\+216)?[2459]\\d{7}$");
    private static final Pattern PHONE_FRANCE = Pattern.compile("^(\\+33|0)[1-9]\\d{8}$");
    private static final Pattern PHONE_MAROC = Pattern.compile("^(\\+212|0)[5-7]\\d{8}$");
    private static final Pattern PHONE_ALGERIE = Pattern.compile("^(\\+213|0)[5-7]\\d{8}$");
    private static final Pattern PHONE_INTERNATIONAL = Pattern.compile("^\\+?[1-9]\\d{7,14}$");

    // Patterns pour CIN/Passport
    private static final Pattern CIN_TUNISIA = Pattern.compile("^[0-9]{8}$");
    private static final Pattern PASSPORT_INTERNATIONAL = Pattern.compile("^[A-Z0-9]{6,12}$");

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
        configurerValidationEnTempsReel();
    }

    private void initialiserComboBoxes() {
        // Initialiser les statuts
        comboStatut.getItems().addAll("réservée", "en_cours", "terminée", "annulée", "no_show");
        comboStatut.setValue("réservée");

        // Initialiser les pays avec la Tunisie par défaut
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

    /**
     * Configure la validation en temps réel des champs
     */
    private void configurerValidationEnTempsReel() {
        // Validation téléphone en temps réel
        txtTelephone.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty() && comboPays.getValue() != null) {
                if (validerTelephoneAvecPays(comboPays.getValue(), newVal)) {
                    txtTelephone.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; -fx-font-size: 14px; -fx-padding: 10;");
                } else {
                    txtTelephone.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-font-size: 14px; -fx-padding: 10;");
                }
            } else {
                txtTelephone.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
            }
        });

        // Revalider le téléphone quand le pays change
        comboPays.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (txtTelephone.getText() != null && !txtTelephone.getText().trim().isEmpty()) {
                // Déclencher la validation
                txtTelephone.setText(txtTelephone.getText());
            }
        });

        // Validation CIN en temps réel
        txtCIN.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                if (validerCinOuPassport(newVal)) {
                    txtCIN.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; -fx-font-size: 14px; -fx-padding: 10;");
                } else {
                    txtCIN.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-font-size: 14px; -fx-padding: 10;");
                }
            } else {
                txtCIN.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
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

        // Nettoyer les espaces et tirets
        String telClean = telephone.replaceAll("[\\s-]", "");

        // Si "Autre" est sélectionné, validation générique
        if (pays.getNom().equals("Autre")) {
            return PHONE_INTERNATIONAL.matcher(telClean).matches();
        }

        // Vérifier selon le pays
        switch (pays.getIndicatif()) {
            case "+216": // Tunisie
                return PHONE_TUNISIA.matcher(telClean).matches();
            case "+33": // France
                return PHONE_FRANCE.matcher(telClean).matches();
            case "+212": // Maroc
                return PHONE_MAROC.matcher(telClean).matches();
            case "+213": // Algérie
                return PHONE_ALGERIE.matcher(telClean).matches();
            default:
                // Pour les autres pays, validation générique
                return PHONE_INTERNATIONAL.matcher(telClean).matches();
        }
    }

    /**
     * Retourne un message d'aide pour le format téléphone
     */
    private String getFormatTelephoneHelp() {
        if (comboPays.getValue() != null) {
            return "Format attendu pour " + comboPays.getValue().getNom() + " :\n" +
                    "Exemple : " + comboPays.getValue().getFormatExemple() + "\n" +
                    "Indicatif : " + comboPays.getValue().getIndicatif();
        }
        return "Veuillez sélectionner un pays d'abord.";
    }

    /**
     * Valide le format CIN tunisien ou Passport international
     */
    private boolean validerCinOuPassport(String document) {
        if (document == null || document.trim().isEmpty()) {
            return true; // Optionnel
        }

        String docClean = document.trim().toUpperCase().replaceAll("[\\s-]", "");

        // CIN Tunisien : 8 chiffres
        if (CIN_TUNISIA.matcher(docClean).matches()) {
            return true;
        }

        // Passport international : 6 à 12 caractères alphanumériques
        if (PASSPORT_INTERNATIONAL.matcher(docClean).matches()) {
            return true;
        }

        return false;
    }

    /**
     * Vérifie si le CIN/Passport existe déjà dans la base de données
     */
    private boolean cinDejaExiste(String cin) {
        if (cin == null || cin.trim().isEmpty()) {
            return false; // Pas de vérification si vide (optionnel)
        }

        String cinClean = cin.trim().toUpperCase().replaceAll("[\\s-]", "");

        // Récupérer toutes les locations et vérifier si le CIN existe
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
        // ═══════════════════════════════════════════════════════
        // VALIDATIONS OBLIGATOIRES
        // ═══════════════════════════════════════════════════════

        if (comboVehicule.getValue() == null) {
            afficherErreur("⚠️ Veuillez sélectionner un véhicule !");
            return;
        }

        if (txtNomClient.getText() == null || txtNomClient.getText().trim().isEmpty()) {
            afficherErreur("⚠️ Le nom du client est obligatoire !");
            txtNomClient.requestFocus();
            return;
        }

        // ═══════════════════════════════════════════════════════
        // VALIDATION TÉLÉPHONE
        // ═══════════════════════════════════════════════════════

        if (comboPays.getValue() == null) {
            afficherErreur("⚠️ Veuillez sélectionner le pays du client !");
            comboPays.requestFocus();
            return;
        }

        if (txtTelephone.getText() == null || txtTelephone.getText().trim().isEmpty()) {
            afficherErreur("⚠️ Le téléphone du client est obligatoire !");
            txtTelephone.requestFocus();
            return;
        }

        if (!validerTelephoneAvecPays(comboPays.getValue(), txtTelephone.getText())) {
            afficherErreur("❌ Format de téléphone invalide pour " + comboPays.getValue().getNom() + " !\n\n" +
                    "Format attendu : " + comboPays.getValue().getFormatExemple() + "\n" +
                    "Indicatif : " + comboPays.getValue().getIndicatif());
            txtTelephone.requestFocus();
            return;
        }

        // Formater le numéro avec l'indicatif du pays
        String telephoneComplet = comboPays.getValue().formaterNumero(txtTelephone.getText().trim());

        // ═══════════════════════════════════════════════════════
        // VALIDATION CIN/PASSPORT
        // ═══════════════════════════════════════════════════════

        String cin = txtCIN.getText();

        // Vérifier le format si CIN est fourni
        if (cin != null && !cin.trim().isEmpty()) {
            if (!validerCinOuPassport(cin)) {
                afficherErreur("❌ Format de CIN/Passport invalide !\n\n" +
                        "Formats acceptés :\n" +
                        "• CIN Tunisien : 8 chiffres (ex: 12345678)\n" +
                        "• Passport : 6 à 12 caractères alphanumériques (ex: AB123456)");
                txtCIN.requestFocus();
                return;
            }

            // Vérifier l'unicité du CIN/Passport
            if (cinDejaExiste(cin)) {
                Alert confirmAlert = new Alert(Alert.AlertType.WARNING);
                confirmAlert.setTitle("⚠️ CIN/Passport déjà enregistré");
                confirmAlert.setHeaderText("Ce CIN/Passport existe déjà dans la base de données");
                confirmAlert.setContentText(
                        "Un client avec ce CIN/Passport a déjà effectué une location.\n\n" +
                                "Voulez-vous continuer quand même ?\n" +
                                "(Il est possible qu'un même client effectue plusieurs locations)"
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
                    return;
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // VALIDATION DATES
        // ═══════════════════════════════════════════════════════

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

        // ═══════════════════════════════════════════════════════
        // VALIDATION KILOMÉTRAGE
        // ═══════════════════════════════════════════════════════

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

        // ═══════════════════════════════════════════════════════
        // VALIDATION AVANCE
        // ═══════════════════════════════════════════════════════

        double avance = 0.0;
        if (txtAvance.getText() != null && !txtAvance.getText().trim().isEmpty()) {
            try {
                avance = Double.parseDouble(txtAvance.getText().trim());
                if (avance < 0) {
                    afficherErreur("⚠️ L'avance ne peut pas être négative !");
                    return;
                }
            } catch (NumberFormatException e) {
                afficherErreur("⚠️ L'avance doit être un nombre valide !");
                return;
            }
        }

        // ═══════════════════════════════════════════════════════
        // CRÉATION DE LA LOCATION
        // ═══════════════════════════════════════════════════════

        Timestamp tsDebut = Timestamp.valueOf(dateDebut.getValue().atStartOfDay());
        Timestamp tsFin = Timestamp.valueOf(dateFinPrevue.getValue().atTime(23, 59));

        double prixJour = comboVehicule.getValue().getPrixParJour();
        long nbJours = ChronoUnit.DAYS.between(dateDebut.getValue(), dateFinPrevue.getValue());
        if (nbJours == 0) nbJours = 1;
        double montantTotal = nbJours * prixJour;

        Location nouvelleLocation = new Location(
                comboVehicule.getValue().getIdVehicule(),
                txtNomClient.getText().trim(),
                telephoneComplet,  // Utiliser le numéro formaté avec indicatif
                tsDebut,
                tsFin,
                kmDebut,
                prixJour,
                montantTotal,
                comboStatut.getValue()
        );

        // Normaliser le CIN/Passport avant de l'enregistrer
        if (cin != null && !cin.trim().isEmpty()) {
            nouvelleLocation.setClientCin(cin.trim().toUpperCase().replaceAll("[\\s-]", ""));
        }

        nouvelleLocation.setAvance(avance);
        nouvelleLocation.setNotes(txtNotes.getText() != null ? txtNotes.getText().trim() : null);

        System.out.println("→ Ajout de la location pour " + nouvelleLocation.getClientNomComplet());

        boolean succes = locationService.ajouterLocation(nouvelleLocation);

        if (succes) {
            System.out.println("✓ Location ajoutée avec succès (ID: " + nouvelleLocation.getIdLocation() + ")");
            afficherSucces("✓ Location enregistrée avec succès !\n" +
                    "Client : " + nouvelleLocation.getClientNomComplet() + "\n" +
                    "Téléphone : " + nouvelleLocation.getClientTelephone() + "\n" +
                    "Montant total : " + String.format("%.3f TND", montantTotal));

            new Thread(() -> {
                try {
                    Thread.sleep(2500);
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
        comboPays.setValue(comboPays.getItems().get(0)); // Réinitialiser à Tunisie
        txtTelephone.clear();
        txtTelephone.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        txtCIN.clear();
        txtCIN.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
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