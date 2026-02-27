package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.example.services.Dashboardservice;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Contrôleur du Dashboard Horizia
 * CORRIGÉ : noms des méthodes Dashboardservice (getNombreLocationsActives,
 *            getNombreVehiculesDisponibles, getNombreVehiculesLoues,
 *            getModeleLesPlusLoue → Map<String,Object>,
 *            getTop5ModelesLoues, getLocationsParStatut)
 */
public class Dashboardcontroller implements Initializable {

    // ─── Labels statistiques ──────────────────────────────────────
    @FXML private Label lblModeleTopNom;
    @FXML private Label lblModeleTopCount;
    @FXML private Label lblLocationsActives;
    @FXML private Label lblVehiculesDisponibles;
    @FXML private Label lblVehiculesLoues;

    // ─── Graphiques ───────────────────────────────────────────────
    @FXML private BarChart<String, Number> barChartTopModeles;
    @FXML private PieChart pieChartStatuts;

    // ─── Cards de navigation ──────────────────────────────────────
    @FXML private HBox cardGestionLocations;
    @FXML private HBox cardGestionVehicules;
    @FXML private HBox cardGestionMarques;
    @FXML private HBox cardDocuments;
    @FXML private HBox cardPlanning;

    private Dashboardservice dashboardService;

    // ─────────────────────────────────────────────────────────────
    // INITIALISATION
    // ─────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dashboardService = new Dashboardservice();
        chargerStatistiques();
        configurerCardsNavigation();
    }

    private void chargerStatistiques() {
        // FIX : getNombreVehiculesDisponibles() (pas getNbVehiculesDisponibles())
        int disponibles = dashboardService.getNombreVehiculesDisponibles();
        // FIX : getNombreVehiculesLoues()
        int loues       = dashboardService.getNombreVehiculesLoues();
        // FIX : getNombreLocationsActives()
        int actives     = dashboardService.getNombreLocationsActives();

        if (lblVehiculesDisponibles != null) lblVehiculesDisponibles.setText(String.valueOf(disponibles));
        if (lblVehiculesLoues       != null) lblVehiculesLoues.setText(String.valueOf(loues));
        if (lblLocationsActives     != null) lblLocationsActives.setText(String.valueOf(actives));

        // FIX : getModeleLesPlusLoue() retourne Map<String, Object> avec clés "nomMarque", "nomModele", "nombreLocations"
        Map<String, Object> topModele = dashboardService.getModeleLesPlusLoue();
        if (topModele != null && !topModele.isEmpty()) {
            String nomMarque  = (String) topModele.getOrDefault("nomMarque",  "—");
            String nomModele  = (String) topModele.getOrDefault("nomModele",  "");
            Object nbLoc      = topModele.getOrDefault("nombreLocations", 0);
            if (lblModeleTopNom   != null) lblModeleTopNom.setText(nomMarque + " " + nomModele);
            if (lblModeleTopCount != null) lblModeleTopCount.setText(nbLoc + " location(s)");
        }

        chargerBarChart();
        chargerPieChart();
    }

    private void chargerBarChart() {
        if (barChartTopModeles == null) return;
        barChartTopModeles.getData().clear();

        // FIX : getTop5ModelesLoues() (pas getTop5ModeleLoues())
        Map<String, Integer> topModeles = dashboardService.getTop5ModelesLoues();
        if (topModeles == null || topModeles.isEmpty()) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Locations");
        for (Map.Entry<String, Integer> entry : topModeles.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        barChartTopModeles.getData().add(series);
    }

    private void chargerPieChart() {
        if (pieChartStatuts == null) return;
        pieChartStatuts.getData().clear();

        // FIX : getLocationsParStatut() (pas getLocationsByStatut())
        Map<String, Integer> statuts = dashboardService.getLocationsParStatut();
        if (statuts == null || statuts.isEmpty()) return;

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : statuts.entrySet()) {
            if (entry.getValue() > 0) {
                data.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }
        pieChartStatuts.setData(data);
    }

    // ─────────────────────────────────────────────────────────────
    // CONFIGURATION DES CARDS (hover effects)
    // ─────────────────────────────────────────────────────────────

    private void configurerCardsNavigation() {
        if (cardDocuments != null) {
            cardDocuments.setOnMouseEntered(e ->
                    cardDocuments.setStyle(cardDocuments.getStyle().replace("0.2", "0.4")));
            cardDocuments.setOnMouseExited(e ->
                    cardDocuments.setStyle(cardDocuments.getStyle().replace("0.4", "0.2")));
        }
        if (cardPlanning != null) {
            cardPlanning.setOnMouseEntered(e ->
                    cardPlanning.setStyle(cardPlanning.getStyle().replace("0.2", "0.4")));
            cardPlanning.setOnMouseExited(e ->
                    cardPlanning.setStyle(cardPlanning.getStyle().replace("0.4", "0.2")));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────

    @FXML private void handleCardGestionLocationsClick(MouseEvent event) { naviguerVers("/views/GestionLocationsView.fxml"); }
    @FXML private void handleCardGestionVehiculesClick(MouseEvent event) { naviguerVers("/views/GestionVehiculesView.fxml"); }
    @FXML private void handleCardGestionMarquesClick(MouseEvent event)   { naviguerVers("/views/GestionMarquesView.fxml"); }
    @FXML private void ouvrirDocuments(MouseEvent event)                 { naviguerVers("/views/DocumentsView.fxml"); }
    @FXML private void ouvrirPlanning(MouseEvent event)                  { naviguerVers("/views/PlanningView.fxml"); }

    @FXML
    private void rafraichirDashboard() {
        chargerStatistiques();
    }

    private void naviguerVers(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            StackPane contentArea = (StackPane) barChartTopModeles.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            } else {
                barChartTopModeles.getScene().setRoot(root);
            }
        } catch (Exception e) {
            System.err.println("[Dashboardcontroller] Erreur navigation vers " + fxmlPath + ": " + e.getMessage());
        }
    }
}