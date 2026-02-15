package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Marque;
import org.example.entities.Modele;
import org.example.entities.Vehicule;
import org.example.services.MarqueService;
import org.example.services.ModeleService;
import org.example.services.VehiculeService;

import java.util.List;

public class AjouterVehiculeController {

    // Champs d'identification
    @FXML private TextField txtImmatriculation;
    @FXML private ComboBox<Marque> comboMarque;
    @FXML private ComboBox<Modele> comboModele;
    @FXML private TextField txtAnnee;

    // Caractéristiques techniques
    @FXML private ComboBox<String> comboCarburant;
    @FXML private TextField txtCouleur;
    @FXML private TextField txtKilometrage;
    @FXML private ComboBox<String> comboEtat;

    // Tarification et média
    @FXML private TextField txtPrixParJour;
    @FXML private TextField txtPhoto;

    // Autres
    @FXML private Label lblMessage;
    @FXML private Button btnAjouter;

    private MarqueService marqueService;
    private ModeleService modeleService;
    private VehiculeService vehiculeService;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        marqueService = new MarqueService();
        modeleService = new ModeleService();
        vehiculeService = new VehiculeService();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Ajouter Véhicule - Chargée");
        System.out.println("═══════════════════════════════════════════════");

        // Initialiser les ComboBox avec les valeurs fixes
        initialiserComboBoxes();

        // Charger les marques
        chargerMarques();

        // Listener pour sélection de marque → charge les modèles
        comboMarque.valueProperty().addListener((obs, old, nouv) -> {
            if (nouv != null) {
                chargerModelesParMarque(nouv.getIdMarque());
            } else {
                comboModele.getItems().clear();
                comboModele.setDisable(true);
            }
        });

        // Validation des champs numériques en temps réel
        ajouterValidationNumerique(txtAnnee);
        ajouterValidationNumerique(txtKilometrage);
        ajouterValidationDecimale(txtPrixParJour);

        // Focus sur l'immatriculation
        txtImmatriculation.requestFocus();
    }

    /**
     * Initialise les ComboBox avec les valeurs fixes
     */
    private void initialiserComboBoxes() {
        // Types de carburant
        comboCarburant.getItems().addAll("Essence", "Diesel", "Hybride", "Electrique");
        comboCarburant.setValue("Essence");

        // États du véhicule
        comboEtat.getItems().addAll("disponible", "louee", "en_maintenance", "indisponible");
        comboEtat.setValue("disponible");
    }

    /**
     * Charge les marques dans le ComboBox
     */
    private void chargerMarques() {
        List<Marque> marques = marqueService.getAllMarquesAlphabetique();
        comboMarque.getItems().clear();
        comboMarque.getItems().addAll(marques);
        System.out.println("✓ " + marques.size() + " marque(s) chargée(s)");
    }

    /**
     * Charge les modèles pour une marque donnée
     */
    private void chargerModelesParMarque(int idMarque) {
        List<Modele> modeles = modeleService.getAllModeles().stream()
                .filter(m -> m.getIdMarque() == idMarque)
                .toList();

        comboModele.getItems().clear();
        comboModele.getItems().addAll(modeles);
        comboModele.setDisable(false);
        comboModele.setPromptText("Sélectionnez un modèle...");

        System.out.println("✓ " + modeles.size() + " modèle(s) chargé(s) pour la marque");
    }

    /**
     * Ajoute validation numérique à un TextField
     */
    private void ajouterValidationNumerique(TextField field) {
        field.textProperty().addListener((obs, old, nouv) -> {
            if (!nouv.matches("\\d*")) {
                field.setText(nouv.replaceAll("[^\\d]", ""));
            }
        });
    }

    /**
     * Ajoute validation décimale à un TextField
     */
    private void ajouterValidationDecimale(TextField field) {
        field.textProperty().addListener((obs, old, nouv) -> {
            if (!nouv.matches("\\d*\\.?\\d*")) {
                field.setText(old);
            }
        });
    }

    /**
     * Ajoute un nouveau véhicule
     */
    @FXML
    private void ajouterVehicule() {
        // ═══════════════════════════════════════════════════════
        // VALIDATION DES CHAMPS OBLIGATOIRES
        // ═══════════════════════════════════════════════════════

        // Immatriculation
        if (txtImmatriculation.getText() == null || txtImmatriculation.getText().trim().isEmpty()) {
            afficherErreur("⚠️ L'immatriculation est obligatoire !");
            txtImmatriculation.requestFocus();
            return;
        }

        // Marque
        if (comboMarque.getValue() == null) {
            afficherErreur("⚠️ Veuillez sélectionner une marque !");
            comboMarque.requestFocus();
            return;
        }

        // Modèle
        if (comboModele.getValue() == null) {
            afficherErreur("⚠️ Veuillez sélectionner un modèle !");
            comboModele.requestFocus();
            return;
        }

        // Année
        if (txtAnnee.getText() == null || txtAnnee.getText().trim().isEmpty()) {
            afficherErreur("⚠️ L'année de fabrication est obligatoire !");
            txtAnnee.requestFocus();
            return;
        }

        int annee;
        try {
            annee = Integer.parseInt(txtAnnee.getText().trim());
            if (annee < 1900 || annee > 2050) {
                afficherErreur("⚠️ L'année doit être entre 1900 et 2050 !");
                txtAnnee.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            afficherErreur("⚠️ L'année doit être un nombre valide !");
            txtAnnee.requestFocus();
            return;
        }

        // Carburant
        if (comboCarburant.getValue() == null) {
            afficherErreur("⚠️ Veuillez sélectionner un type de carburant !");
            comboCarburant.requestFocus();
            return;
        }

        // Kilométrage
        if (txtKilometrage.getText() == null || txtKilometrage.getText().trim().isEmpty()) {
            afficherErreur("⚠️ Le kilométrage est obligatoire !");
            txtKilometrage.requestFocus();
            return;
        }

        int kilometrage;
        try {
            kilometrage = Integer.parseInt(txtKilometrage.getText().trim());
            if (kilometrage < 0) {
                afficherErreur("⚠️ Le kilométrage ne peut pas être négatif !");
                txtKilometrage.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            afficherErreur("⚠️ Le kilométrage doit être un nombre valide !");
            txtKilometrage.requestFocus();
            return;
        }

        // État
        if (comboEtat.getValue() == null) {
            afficherErreur("⚠️ Veuillez sélectionner un état !");
            comboEtat.requestFocus();
            return;
        }

        // Prix par jour
        if (txtPrixParJour.getText() == null || txtPrixParJour.getText().trim().isEmpty()) {
            afficherErreur("⚠️ Le prix par jour est obligatoire !");
            txtPrixParJour.requestFocus();
            return;
        }

        double prixParJour;
        try {
            prixParJour = Double.parseDouble(txtPrixParJour.getText().trim());
            if (prixParJour <= 0) {
                afficherErreur("⚠️ Le prix doit être supérieur à 0 !");
                txtPrixParJour.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            afficherErreur("⚠️ Le prix doit être un nombre valide !");
            txtPrixParJour.requestFocus();
            return;
        }

        // ═══════════════════════════════════════════════════════
        // CRÉATION DU VÉHICULE
        // ═══════════════════════════════════════════════════════
        Vehicule nouveauVehicule = new Vehicule(
                txtImmatriculation.getText().trim(),
                comboModele.getValue().getIdModele(),
                annee,
                comboCarburant.getValue(),
                txtCouleur.getText() != null ? txtCouleur.getText().trim() : null,
                kilometrage,
                comboEtat.getValue(),
                prixParJour,
                txtPhoto.getText() != null ? txtPhoto.getText().trim() : null
        );

        System.out.println("→ Tentative d'ajout du véhicule : " + txtImmatriculation.getText());

        boolean succes = vehiculeService.ajouterVehicule(nouveauVehicule);

        if (succes) {
            System.out.println("✓ Véhicule ajouté avec succès : " +
                    nouveauVehicule.getImmatriculation() +
                    " (ID: " + nouveauVehicule.getIdVehicule() + ")");

            afficherSucces("✓ Le véhicule \"" + nouveauVehicule.getImmatriculation() +
                    "\" a été ajouté avec succès !");

            animerSuccesBouton();

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
            System.err.println("✗ Échec de l'ajout du véhicule");
            afficherErreur("✗ Erreur : Impossible d'ajouter le véhicule !\n" +
                    "Vérifiez que l'immatriculation n'existe pas déjà.");
            txtImmatriculation.selectAll();
            txtImmatriculation.requestFocus();
        }
    }

    /**
     * Réinitialise le formulaire
     */
    @FXML
    private void reinitialiser() {
        txtImmatriculation.clear();
        comboMarque.setValue(null);
        comboModele.getItems().clear();
        comboModele.setDisable(true);
        txtAnnee.clear();
        comboCarburant.setValue("Essence");
        txtCouleur.clear();
        txtKilometrage.clear();
        comboEtat.setValue("disponible");
        txtPrixParJour.clear();
        txtPhoto.clear();
        cacherMessage();
        txtImmatriculation.requestFocus();

        System.out.println("🔄 Formulaire réinitialisé");
    }

    /**
     * Ferme la fenêtre
     */
    @FXML
    private void annuler() {
        System.out.println("✖ Annulation de l'ajout de véhicule");
        Stage stage = (Stage) txtImmatriculation.getScene().getWindow();
        stage.close();
    }

    /**
     * Affiche un message de succès
     */
    private void afficherSucces(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #27ae60; " +
                "-fx-background-color: #d5f4e6; " +
                "-fx-padding: 12; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #27ae60; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 8;");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(500), lblMessage);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Affiche un message d'erreur
     */
    private void afficherErreur(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #e74c3c; " +
                "-fx-background-color: #fadbd8; " +
                "-fx-padding: 12; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #e74c3c; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 8;");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);

        ScaleTransition shake = new ScaleTransition(Duration.millis(100), lblMessage);
        shake.setFromX(1.0);
        shake.setToX(1.05);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }

    /**
     * Cache le message
     */
    private void cacherMessage() {
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }

    /**
     * Animation de succès
     */
    private void animerSuccesBouton() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), btnAjouter);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.1);
        scale.setToY(1.1);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();
    }
}