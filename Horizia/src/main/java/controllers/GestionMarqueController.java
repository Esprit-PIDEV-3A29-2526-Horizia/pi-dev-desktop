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

public class GestionMarqueController {

    @FXML private VBox cardAjouterMarque;
    @FXML private VBox cardAfficherMarques;
    @FXML private VBox cardAjouterModele;
    @FXML private VBox cardAfficherModeles;
    @FXML private Button btnRetour;

    @FXML
    public void initialize() {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Gestion Marques et Modèles - Chargée");
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
        ajouterEffetHover(cardAjouterMarque);
        ajouterEffetHover(cardAfficherMarques);
        ajouterEffetHover(cardAjouterModele);
        ajouterEffetHover(cardAfficherModeles);
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
     * Ouvre l'interface Ajouter Marque
     */
    @FXML
    private void ouvrirAjouterMarque(MouseEvent event) {
        System.out.println("→ Navigation : Ajouter Marque");
        chargerInterface("/views/AjouterMarqueView.fxml", "Ajouter une Marque");
    }

    /**
     * Ouvre l'interface Afficher Marques
     */
    @FXML
    private void ouvrirAfficherMarques(MouseEvent event) {
        System.out.println("→ Navigation : Afficher Marques");
        chargerInterface("/views/AfficherMarquesView.fxml", "Catalogue des Marques");
    }

    /**
     * Ouvre l'interface Ajouter Modèle
     */
    @FXML
    private void ouvrirAjouterModele(MouseEvent event) {
        System.out.println("→ Navigation : Ajouter Modèle");
        chargerInterface("/views/AjouterModeleView.fxml", "Ajouter un Modèle");
    }

    /**
     * Ouvre l'interface Afficher Modèles
     */
    @FXML
    private void ouvrirAfficherModeles(MouseEvent event) {
        System.out.println("→ Navigation : Afficher Modèles");
        chargerInterface("/views/AfficherModelesView.fxml", "Catalogue des Modèles");
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
        Stage currentStage = (Stage) cardAjouterMarque.getScene().getWindow();
        currentStage.close();
        System.out.println("✓ Fenêtre fermée - retour au Dashboard");
    }
}