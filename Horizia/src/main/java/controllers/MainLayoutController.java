package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainLayoutController {

    @FXML private StackPane contentArea;

    // Boutons de navigation
    @FXML private Button btnDashboard;
    @FXML private Button btnVoyages;
    @FXML private Button btnReservations;
    @FXML private Button btnLocations;
    @FXML private Button btnLogements;
    @FXML private Button btnConseil;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        // Charger le Dashboard des Locations par défaut au démarrage
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Initialisation de la Sidebar - HORIZIA");
        System.out.println("═══════════════════════════════════════════════");
        showGestionLocations();
    }

    /**
     * Affiche le Dashboard de Gestion des Locations (VOTRE PARTIE)
     */
    @FXML
    private void showGestionLocations() {
        loadView("/views/DashboardView.fxml");
        setActiveButton(btnLocations);
        System.out.println("→ Navigation : Gestion des Locations (Dashboard)");
    }

    /**
     * Charge une vue FXML dans la zone de contenu
     */
    private void loadView(String fxmlPath) {
        try {
            System.out.println("   Chargement de la vue : " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Vider la zone de contenu et ajouter la nouvelle vue
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            System.out.println("   ✓ Vue chargée avec succès");

        } catch (IOException e) {
            System.err.println("   ✗ ERREUR lors du chargement de la vue : " + fxmlPath);
            System.err.println("   Message : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("   ✗ ERREUR inattendue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Définit le bouton actif dans la sidebar
     */
    private void setActiveButton(Button activeButton) {
        // Retirer la classe active de tous les boutons
        btnDashboard.getStyleClass().removeAll("sidebar-button-active");
        btnVoyages.getStyleClass().removeAll("sidebar-button-active");
        btnReservations.getStyleClass().removeAll("sidebar-button-active");
        btnLocations.getStyleClass().removeAll("sidebar-button-active");
        btnLogements.getStyleClass().removeAll("sidebar-button-active");
        btnConseil.getStyleClass().removeAll("sidebar-button-active");

        // Ajouter la classe active au bouton cliqué
        if (!activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    // ═══════════════════════════════════════════════════════
    // MÉTHODES POUR LES AUTRES SECTIONS (Non implémentées)
    // Ces méthodes seront activées lors de l'intégration future
    // ═══════════════════════════════════════════════════════

    /*
    @FXML
    private void showDashboard() {
        loadView("/views/DashboardGlobal.fxml");
        setActiveButton(btnDashboard);
        System.out.println("→ Navigation : Dashboard Global");
    }

    @FXML
    private void showVoyages() {
        loadView("/views/VoyagesView.fxml");
        setActiveButton(btnVoyages);
        System.out.println("→ Navigation : Voyages");
    }

    @FXML
    private void showReservations() {
        loadView("/views/ReservationsView.fxml");
        setActiveButton(btnReservations);
        System.out.println("→ Navigation : Réservations");
    }

    @FXML
    private void showLogements() {
        loadView("/views/LogementsView.fxml");
        setActiveButton(btnLogements);
        System.out.println("→ Navigation : Logements");
    }

    @FXML
    private void showConseil() {
        loadView("/views/ConseilView.fxml");
        setActiveButton(btnConseil);
        System.out.println("→ Navigation : Conseil");
    }
    */
}