package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur du Layout Principal (Sidebar + Contenu)
 *
 * FIXES :
 *  1. showDashboard() ajouté → bouton Dashboard dans sidebar navigue vers Dashboard
 *  2. showGestionLocations() → navigue vers GestionLocationsView (pas Dashboard)
 *  3. chargerVueInterne() → charge dans contentArea (sidebar reste visible)
 *     Les contrôleurs enfants (Documents, Planning) utilisent naviguerVers()
 *     au lieu de remplacer stage.getScene().setRoot()
 */
public class MainLayoutController implements Initializable {

    // ─── FXML Components ─────────────────────────────────────────
    @FXML private StackPane contentArea;

    @FXML private Button btnDashboard;
    @FXML private Button btnLocations;
    @FXML private Button btnVoyages;
    @FXML private Button btnReservations;
    @FXML private Button btnLogements;
    @FXML private Button btnConseil;
    @FXML private Button btnDocuments;
    @FXML private Button btnPlanning;

    // ─────────────────────────────────────────────────────────────
    // INITIALISATION
    // ─────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Charger le Dashboard par défaut au lancement
        chargerVue("/views/Dashboardview.fxml");
        setActiveButton(btnDashboard);
    }

    // ─────────────────────────────────────────────────────────────
    // NAVIGATION — Sidebar
    // ─────────────────────────────────────────────────────────────

    /** ✅ FIX 1 : Dashboard → affiche le dashboard */
    @FXML
    private void showDashboard() {
        chargerVue("/views/Dashboardview.fxml");
        setActiveButton(btnDashboard);
    }

    /** ✅ FIX 2 : Locations → affiche GestionLocations (pas Dashboard) */
    @FXML
    private void showGestionLocations() {
        chargerVue("/views/GestionLocationsView.fxml");
        setActiveButton(btnLocations);
    }

    @FXML
    private void showDocuments() {
        chargerVue("/views/DocumentsView.fxml");
        setActiveButton(btnDocuments);
    }

    @FXML
    private void showPlanning() {
        chargerVue("/views/PlanningView.fxml");
        setActiveButton(btnPlanning);
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTHODE PUBLIQUE — appelée par les contrôleurs enfants
    // ─────────────────────────────────────────────────────────────

    /**
     * ✅ FIX 3 : Les boutons "Retour" des sous-pages appellent cette méthode
     * via le lookup du MainLayoutController dans la scène.
     * La sidebar reste visible car on change uniquement le contentArea.
     */
    public void naviguerVers(String fxmlPath) {
        chargerVue(fxmlPath);
        // Mettre à jour le bouton actif selon la destination
        if (fxmlPath.contains("Dashboard"))       setActiveButton(btnDashboard);
        else if (fxmlPath.contains("Locations"))  setActiveButton(btnLocations);
        else if (fxmlPath.contains("Documents"))  setActiveButton(btnDocuments);
        else if (fxmlPath.contains("Planning"))   setActiveButton(btnPlanning);
    }

    // ─────────────────────────────────────────────────────────────
    // UTILITAIRES
    // ─────────────────────────────────────────────────────────────

    private void chargerVue(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent vue = loader.load();

            // ✅ Injecter le MainLayoutController dans le contrôleur enfant
            Object ctrl = loader.getController();
            if (ctrl instanceof ControllerAvecLayout) {
                ((ControllerAvecLayout) ctrl).setMainLayoutController(this);
            }

            contentArea.getChildren().setAll(vue);
        } catch (Exception e) {
            System.err.println("[MainLayoutController] Erreur chargement " + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeBtn) {
        Button[] tous = {btnDashboard, btnLocations, btnDocuments, btnPlanning};
        for (Button btn : tous) {
            if (btn == null) continue;
            if (btn == activeBtn) {
                btn.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white;" +
                        " -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 8;" +
                        " -fx-font-weight: bold;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e0e0;" +
                        " -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 8;");
            }
        }
    }

    /**
     * Interface optionnelle que les contrôleurs enfants peuvent implémenter
     * pour recevoir une référence au MainLayoutController.
     */
    public interface ControllerAvecLayout {
        void setMainLayoutController(MainLayoutController controller);
    }
}