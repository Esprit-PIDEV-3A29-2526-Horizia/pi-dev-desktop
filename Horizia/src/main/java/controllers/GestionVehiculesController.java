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

public class GestionVehiculesController {

    @FXML private VBox cardAjouter;
    @FXML private VBox cardAfficher;
    @FXML private VBox cardModifier;
    @FXML private VBox cardSupprimer;
    @FXML private Button btnRetour;

    @FXML
    public void initialize() {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Gestion Véhicules - Chargée");
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
     * Ouvre l'interface Ajouter Véhicule
     */
    @FXML
    private void ouvrirAjouterVehicule(MouseEvent event) {
        System.out.println("→ Navigation : Ajouter Véhicule");
        chargerInterface("/views/AjouterVehiculeView.fxml", "Ajouter un Véhicule");
    }

    /**
     * Ouvre l'interface Afficher Véhicules
     */
    @FXML
    private void ouvrirAfficherVehicules(MouseEvent event) {
        System.out.println("→ Navigation : Afficher Véhicules");
        chargerInterface("/views/AfficherVehiculesView.fxml", "Catalogue des Véhicules");
    }

    /**
     * Ouvre l'interface Modifier Véhicule
     */
    @FXML
    private void ouvrirModifierVehicule(MouseEvent event) {
        System.out.println("→ Navigation : Modifier Véhicule");
        chargerInterface("/views/ModifierVehiculeView.fxml", "Modifier un Véhicule");
    }

    /**
     * Ouvre l'interface Supprimer Véhicule
     */
    @FXML
    private void ouvrirSupprimerVehicule(MouseEvent event) {
        System.out.println("→ Navigation : Supprimer Véhicule");
        chargerInterface("/views/SupprimerVehiculeView.fxml", "Supprimer un Véhicule");
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
        try {
            // Récupérer la fenêtre actuelle
            Stage currentStage = (Stage) cardAjouter.getScene().getWindow();

            // Charger le Dashboard dans le MainLayout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DashboardView.fxml"));
            Parent dashboard = loader.load();

            // Récupérer le contrôleur du MainLayout (via la scène parente si elle existe)
            // Si on est dans la fenêtre principale, recharger le Dashboard
            currentStage.getScene().setRoot(dashboard);

            System.out.println("✓ Retour au Dashboard effectué");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors du retour au Dashboard : " + e.getMessage());
            e.printStackTrace();
        }
    }
}