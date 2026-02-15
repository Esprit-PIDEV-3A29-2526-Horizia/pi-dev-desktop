package controllers;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class GestionLocationsController {

    @FXML private VBox cardAjouter;
    @FXML private VBox cardAfficher;
    @FXML private VBox cardModifier;
    @FXML private VBox cardSupprimer;
    @FXML private Button btnRetour;

    @FXML
    public void initialize() {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Gestion Locations - Chargée");
        System.out.println("═══════════════════════════════════════════════");



        // Ajouter effets hover sur les cards
        configurerEffetsHover();

        // Ajouter effet hover au bouton retour
        configurerBoutonRetour();
    }

    /**
     * Configure l'effet hover du bouton retour
     */
    private void configurerBoutonRetour() {
        String styleNormal = "-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";
        String styleHover = "-fx-background-color: #d1d5db; -fx-text-fill: #1f2937; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";

        btnRetour.setOnMouseEntered(e -> btnRetour.setStyle(styleHover));
        btnRetour.setOnMouseExited(e -> btnRetour.setStyle(styleNormal));
    }

    /**
     * Configure les effets de survol sur toutes les cards
     */
    private void configurerEffetsHover() {
        ajouterEffetHover(cardAjouter);
        ajouterEffetHover(cardAfficher);
        ajouterEffetHover(cardModifier);
        ajouterEffetHover(cardSupprimer);
    }

    /**
     * Ajoute un effet hover (zoom + ombre) à une card
     */
    private void ajouterEffetHover(VBox card) {
        card.setOnMouseEntered(event -> {
            // Animation de zoom
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();

            // Augmenter l'ombre
            card.setStyle(card.getStyle() +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 18, 0, 0, 6);");
        });

        card.setOnMouseExited(event -> {
            // Retour à la taille normale
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();

            // Réduire l'ombre
            card.setStyle(card.getStyle().replace(
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 18, 0, 0, 6);",
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4);"
            ));
        });
    }

    /**
     * Ouvre l'interface Ajouter Location
     */
    @FXML
    private void ouvrirAjouterLocation(MouseEvent event) {
        System.out.println("→ Navigation : Ajouter Location");
        chargerInterface("/views/AjouterLocationView.fxml", "Nouvelle Location");
    }

    /**
     * Ouvre l'interface Afficher Locations (Historique des Locations)
     */
    @FXML
    private void ouvrirAfficherLocations(MouseEvent event) {
        System.out.println("→ Navigation : Afficher Locations");
        chargerInterface("/views/AfficherLocationsView.fxml", "Historique des Locations");
    }

    /**
     * Ouvre l'interface Modifier Location
     */
    @FXML
    private void ouvrirModifierLocation(MouseEvent event) {
        System.out.println("→ Navigation : Modifier Location");
        chargerInterface("/views/ModifierLocationView.fxml", "Modifier une Location");
    }

    /**
     * Ouvre l'interface Supprimer Location
     */
    @FXML
    private void ouvrirSupprimerLocation(MouseEvent event) {
        System.out.println("→ Navigation : Supprimer Location");
        chargerInterface("/views/SupprimerLocationView.fxml", "Annuler une Location");
    }

    /**
     * Charge une interface FXML dans une nouvelle fenêtre
     */
    private void chargerInterface(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(titre + " - Horizia");
            stage.setScene(new Scene(root));
            stage.show();
            stage.setMaximized(true);  // Plein écran
            stage.setResizable(true);  // Responsive

            System.out.println("✓ Interface chargée : " + titre);

        } catch (IOException e) {
            System.err.println("✗ Erreur chargement de l'interface " + fxmlPath);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retourne au Dashboard principal
     */
    @FXML
    private void retourDashboard() {
        System.out.println("← Retour au Dashboard");
        // Récupérer la fenêtre actuelle et la fermer
        Stage currentStage = (Stage) cardAjouter.getScene().getWindow();
        currentStage.close();
        System.out.println("✓ Fenêtre fermée - retour au Dashboard");
    }
}