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
import org.example.services.MarqueService;
import org.example.services.ModeleService;

import java.util.List;

public class AjouterModeleController {

    @FXML private ComboBox<Marque> comboMarque;
    @FXML private TextField txtNomModele;
    @FXML private Label lblMessage;
    @FXML private Label lblApercu;
    @FXML private Label lblNombreMarques;
    @FXML private Button btnAjouter;

    private MarqueService marqueService;
    private ModeleService modeleService;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        marqueService = new MarqueService();
        modeleService = new ModeleService();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Ajouter Modèle - Chargée");
        System.out.println("═══════════════════════════════════════════════");

        // Charger les marques dans le ComboBox
        chargerMarques();

        // Désactiver le bouton Ajouter par défaut
        btnAjouter.setDisable(true);

        // Listeners pour validation et aperçu en temps réel
        comboMarque.valueProperty().addListener((obs, old, nouv) -> {
            cacherMessage();
            mettreAJourApercu();
            validerFormulaire();
        });

        txtNomModele.textProperty().addListener((obs, old, nouv) -> {
            cacherMessage();
            mettreAJourApercu();
            validerFormulaire();
        });

        // Focus sur le ComboBox
        comboMarque.requestFocus();
    }

    /**
     * Charge toutes les marques dans le ComboBox
     */
    private void chargerMarques() {
        System.out.println("→ Chargement des marques...");

        List<Marque> marques = marqueService.getAllMarquesAlphabetique();

        comboMarque.getItems().clear();
        comboMarque.getItems().addAll(marques);

        // Mettre à jour le label du nombre de marques
        String texte = marques.size() + " marque" + (marques.size() > 1 ? "s" : "") + " disponible" + (marques.size() > 1 ? "s" : "");
        lblNombreMarques.setText(texte);

        System.out.println("✓ " + marques.size() + " marque(s) chargée(s)");

        if (marques.isEmpty()) {
            afficherErreur("⚠️ Aucune marque disponible !\nVeuillez d'abord ajouter des marques avant d'ajouter des modèles.");
            btnAjouter.setDisable(true);
        }
    }

    /**
     * Met à jour l'aperçu du modèle complet
     */
    private void mettreAJourApercu() {
        Marque marqueSelectionnee = comboMarque.getValue();
        String nomModele = txtNomModele.getText().trim();

        if (marqueSelectionnee != null && !nomModele.isEmpty()) {
            lblApercu.setText(marqueSelectionnee.getNomMarque() + " " + nomModele);
            lblApercu.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        } else if (marqueSelectionnee != null) {
            lblApercu.setText(marqueSelectionnee.getNomMarque() + " ...");
            lblApercu.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #95a5a6;");
        } else if (!nomModele.isEmpty()) {
            lblApercu.setText("??? " + nomModele);
            lblApercu.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #95a5a6;");
        } else {
            lblApercu.setText("Marque + Modèle");
            lblApercu.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2980b9;");
        }
    }

    /**
     * Valide le formulaire et active/désactive le bouton Ajouter
     */
    private void validerFormulaire() {
        Marque marqueSelectionnee = comboMarque.getValue();
        String nomModele = txtNomModele.getText();

        boolean valide = marqueSelectionnee != null &&
                nomModele != null &&
                !nomModele.trim().isEmpty() &&
                nomModele.trim().length() >= 2;

        btnAjouter.setDisable(!valide);
    }

    /**
     * Ajoute un nouveau modèle
     */
    @FXML
    private void ajouterModele() {
        Marque marqueSelectionnee = comboMarque.getValue();
        String nomModele = txtNomModele.getText();

        // ═══════════════════════════════════════════════════════
        // VALIDATION 1 : Vérifier qu'une marque est sélectionnée
        // ═══════════════════════════════════════════════════════
        if (marqueSelectionnee == null) {
            afficherErreur("⚠️ Veuillez sélectionner une marque !");
            comboMarque.requestFocus();
            return;
        }

        // ═══════════════════════════════════════════════════════
        // VALIDATION 2 : Vérifier que le nom n'est pas vide
        // ═══════════════════════════════════════════════════════
        if (nomModele == null || nomModele.trim().isEmpty()) {
            afficherErreur("⚠️ Le nom du modèle ne peut pas être vide !");
            txtNomModele.requestFocus();
            return;
        }

        // ═══════════════════════════════════════════════════════
        // VALIDATION 3 : Vérifier la longueur minimale
        // ═══════════════════════════════════════════════════════
        if (nomModele.trim().length() < 2) {
            afficherErreur("⚠️ Le nom doit contenir au moins 2 caractères !");
            txtNomModele.requestFocus();
            return;
        }

        // ═══════════════════════════════════════════════════════
        // VALIDATION 4 : Vérifier la longueur maximale
        // ═══════════════════════════════════════════════════════
        if (nomModele.trim().length() > 80) {
            afficherErreur("⚠️ Le nom ne peut pas dépasser 80 caractères !");
            txtNomModele.requestFocus();
            return;
        }

        // ═══════════════════════════════════════════════════════
        // VALIDATION 5 : Vérifier caractères spéciaux
        // ═══════════════════════════════════════════════════════
        if (!nomModele.matches("^[a-zA-ZÀ-ÿ0-9\\s\\-]+$")) {
            afficherErreur("⚠️ Le nom contient des caractères non autorisés !\n" +
                    "Autorisés : lettres, chiffres, espaces et tirets");
            txtNomModele.requestFocus();
            return;
        }

        // ═══════════════════════════════════════════════════════
        // AJOUT DU MODÈLE
        // ═══════════════════════════════════════════════════════
        Modele nouveauModele = new Modele(
                marqueSelectionnee.getIdMarque(),
                nomModele.trim()
        );

        System.out.println("→ Tentative d'ajout du modèle : " + marqueSelectionnee.getNomMarque() +
                " " + nomModele.trim());

        boolean succes = modeleService.ajouterModele(nouveauModele);

        if (succes) {
            System.out.println("✓ Modèle ajouté avec succès : " +
                    marqueSelectionnee.getNomMarque() + " " + nouveauModele.getNomModele() +
                    " (ID: " + nouveauModele.getIdModele() + ")");

            afficherSucces("✓ Le modèle \"" + marqueSelectionnee.getNomMarque() + " " +
                    nouveauModele.getNomModele() + "\" a été ajouté avec succès !");

            // Animation de succès sur le bouton
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
            System.err.println("✗ Échec de l'ajout - Le modèle existe probablement déjà pour cette marque");
            afficherErreur("✗ Erreur : Ce modèle existe déjà pour la marque \"" +
                    marqueSelectionnee.getNomMarque() + "\" !\n" +
                    "Veuillez vérifier l'orthographe ou choisir un autre nom.");
            txtNomModele.selectAll();
            txtNomModele.requestFocus();
        }
    }

    /**
     * Réinitialise le formulaire
     */
    @FXML
    private void reinitialiser() {
        comboMarque.setValue(null);
        txtNomModele.clear();
        cacherMessage();
        mettreAJourApercu();
        comboMarque.requestFocus();
        btnAjouter.setDisable(true);

        System.out.println("🔄 Formulaire réinitialisé");
    }

    /**
     * Ferme la fenêtre (annulation)
     */
    @FXML
    private void annuler() {
        System.out.println("✖ Annulation de l'ajout de modèle");
        Stage stage = (Stage) txtNomModele.getScene().getWindow();
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

        // Animation d'apparition
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

        // Animation de secousse
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
     * Animation de succès sur le bouton
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