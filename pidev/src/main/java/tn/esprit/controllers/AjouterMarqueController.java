package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.Marque;
import tn.esprit.services.MarqueService;
public class AjouterMarqueController {

    @FXML private TextField txtNomMarque;
    @FXML private Label lblMessage;
    @FXML private Button btnAjouter;

    private MarqueService marqueService;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        marqueService = new MarqueService();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Ajouter Marque - Chargée");
        System.out.println("═══════════════════════════════════════════════");

        // Validation en temps réel pendant la saisie
        txtNomMarque.textProperty().addListener((observable, oldValue, newValue) -> {
            cacherMessage();

            // Activer/désactiver le bouton selon la saisie
            btnAjouter.setDisable(newValue == null || newValue.trim().isEmpty());
        });

        // Focus automatique sur le champ de saisie
        txtNomMarque.requestFocus();
    }

    /**
     * Ajoute une nouvelle marque
     */
    @FXML
    private void ajouterMarque() {
        String nomMarque = txtNomMarque.getText();

        // ═══════════════════════════════════════════════════════
        // VALIDATION 1 : Vérifier que le champ n'est pas vide
        // ═══════════════════════════════════════════════════════
        if (nomMarque == null || nomMarque.trim().isEmpty()) {
            afficherErreur("⚠️ Le nom de la marque ne peut pas être vide !");
            txtNomMarque.requestFocus();
            return;
        }

        // ═══════════════════════════════════════════════════════
        // VALIDATION 2 : Vérifier la longueur minimale
        // ═══════════════════════════════════════════════════════
        if (nomMarque.trim().length() < 2) {
            afficherErreur("⚠️ Le nom doit contenir au moins 2 caractères !");
            txtNomMarque.requestFocus();
            return;
        }

        // ═══════════════════════════════════════════════════════
        // VALIDATION 3 : Vérifier la longueur maximale
        // ═══════════════════════════════════════════════════════
        if (nomMarque.trim().length() > 50) {
            afficherErreur("⚠️ Le nom ne peut pas dépasser 50 caractères !");
            txtNomMarque.requestFocus();
            return;
        }

        // ═══════════════════════════════════════════════════════
        // VALIDATION 4 : Vérifier caractères spéciaux (optionnel)
        // ═══════════════════════════════════════════════════════
        if (!nomMarque.matches("^[a-zA-ZÀ-ÿ0-9\\s\\-]+$")) {
            afficherErreur("⚠️ Le nom contient des caractères non autorisés !\n" +
                    "Autorisés : lettres, chiffres, espaces et tirets");
            txtNomMarque.requestFocus();
            return;
        }

        // ═══════════════════════════════════════════════════════
        // AJOUT DE LA MARQUE
        // ═══════════════════════════════════════════════════════
        Marque nouvelleMarque = new Marque(nomMarque.trim());

        System.out.println("→ Tentative d'ajout de la marque : " + nomMarque.trim());

        boolean succes = marqueService.ajouterMarque(nouvelleMarque);

        if (succes) {
            System.out.println("✓ Marque ajoutée avec succès : " + nouvelleMarque.getNomMarque() +
                    " (ID: " + nouvelleMarque.getIdMarque() + ")");

            afficherSucces("✓ La marque \"" + nouvelleMarque.getNomMarque() + "\" a été ajoutée avec succès !");

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
            System.err.println("✗ Échec de l'ajout - La marque existe probablement déjà");
            afficherErreur("✗ Erreur : Cette marque existe déjà dans la base de données !\n" +
                    "Veuillez vérifier l'orthographe ou choisir un autre nom.");
            txtNomMarque.selectAll();
            txtNomMarque.requestFocus();
        }
    }

    /**
     * Réinitialise le formulaire
     */
    @FXML
    private void reinitialiser() {
        txtNomMarque.clear();
        cacherMessage();
        txtNomMarque.requestFocus();
        btnAjouter.setDisable(true);

        System.out.println("🔄 Formulaire réinitialisé");
    }

    /**
     * Ferme la fenêtre (annulation)
     */
    @FXML
    private void annuler() {
        System.out.println("✖ Annulation de l'ajout de marque");
        Stage stage = (Stage) txtNomMarque.getScene().getWindow();
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