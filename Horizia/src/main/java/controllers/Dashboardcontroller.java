package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.services.Dashboardservice;

import java.io.IOException;
import java.util.Map;

public class Dashboardcontroller {

    @FXML private Label lblModeleTopNom;
    @FXML private Label lblModeleTopCount;
    @FXML private Label lblLocationsActives;
    @FXML private Label lblVehiculesDisponibles;
    @FXML private Label lblVehiculesLoues;

    @FXML private BarChart<String, Number> barChartTopModeles;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private PieChart pieChartStatuts;

    @FXML private HBox cardGestionLocations;
    @FXML private HBox cardGestionVehicules;
    @FXML private HBox cardGestionMarques;

    private Dashboardservice dashboardservice;

    public Dashboardcontroller() {
        this.dashboardservice = new Dashboardservice();
    }

    @FXML
    public void initialize() {
        chargerStatistiques();
        chargerGraphiqueTopModeles();
        chargerGraphiqueStatuts();
        configurerCards();
    }

    /**
     * Charge les statistiques principales
     */
    private void chargerStatistiques() {
        // Modèle le plus loué
        Map<String, Object> topModele = dashboardservice.getModeleLesPlusLoue();
        String marque = (String) topModele.get("nomMarque");
        String modele = (String) topModele.get("nomModele");
        int nbLocations = (int) topModele.get("nombreLocations");

        lblModeleTopNom.setText(marque + " " + modele);
        lblModeleTopCount.setText(nbLocations + " location" + (nbLocations > 1 ? "s" : ""));

        // Locations actives
        int locationsActives = dashboardservice.getNombreLocationsActives();
        lblLocationsActives.setText(String.valueOf(locationsActives));

        // Véhicules disponibles
        int vehiculesDisponibles = dashboardservice.getNombreVehiculesDisponibles();
        lblVehiculesDisponibles.setText(String.valueOf(vehiculesDisponibles));

        // Véhicules loués
        int vehiculesLoues = dashboardservice.getNombreVehiculesLoues();
        lblVehiculesLoues.setText(String.valueOf(vehiculesLoues));
    }

    /**
     * Charge le graphique en barres des top 5 modèles
     */
    private void chargerGraphiqueTopModeles() {
        Map<String, Integer> topModeles = dashboardservice.getTop5ModelesLoues();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de locations");

        for (Map.Entry<String, Integer> entry : topModeles.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChartTopModeles.getData().clear();
        barChartTopModeles.getData().add(series);
        barChartTopModeles.setLegendVisible(false);
    }

    /**
     * Charge le graphique en camembert des statuts
     */
    private void chargerGraphiqueStatuts() {
        Map<String, Integer> statutsData = dashboardservice.getLocationsParStatut();

        pieChartStatuts.getData().clear();

        for (Map.Entry<String, Integer> entry : statutsData.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                    formatStatut(entry.getKey()) + " (" + entry.getValue() + ")",
                    entry.getValue()
            );
            pieChartStatuts.getData().add(slice);
        }

        pieChartStatuts.setLegendVisible(true);
    }

    /**
     * Formate les noms de statuts pour affichage
     */
    private String formatStatut(String statut) {
        switch (statut) {
            case "réservée": return "Réservée";
            case "en_cours": return "En cours";
            case "terminée": return "Terminée";
            case "annulée": return "Annulée";
            case "no_show": return "No show";
            default: return statut;
        }
    }

    /**
     * Configure les actions de clic sur les cards
     */
    private void configurerCards() {
        cardGestionLocations.setOnMouseClicked(event -> ouvrirGestionLocations());
        cardGestionVehicules.setOnMouseClicked(event -> ouvrirGestionVehicules());
        cardGestionMarques.setOnMouseClicked(event -> ouvrirGestionMarques());

        // Effet hover
        ajouterEffetHover(cardGestionLocations);
        ajouterEffetHover(cardGestionVehicules);
        ajouterEffetHover(cardGestionMarques);
    }

    /**
     * Ajoute un effet de survol aux cards
     */
    private void ajouterEffetHover(HBox card) {
        card.setOnMouseEntered(event -> {
            card.setStyle(card.getStyle() + "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);");
        });

        card.setOnMouseExited(event -> {
            card.setStyle(card.getStyle().replace("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);", ""));
        });
    }

    /**
     * Ouvre l'interface de gestion des locations
     */
    @FXML
    private void ouvrirGestionLocations() {
        chargerNouvelleInterface("/views/GestionLocationsView.fxml", "Gestion des Locations");
    }

    /**
     * Ouvre l'interface de gestion des véhicules
     */
    @FXML
    private void ouvrirGestionVehicules() {
        chargerNouvelleInterface("/views/GestionVehiculesView.fxml", "Gestion des Véhicules");
    }

    /**
     * Ouvre l'interface de gestion des marques
     */
    @FXML
    private void ouvrirGestionMarques() {
        chargerNouvelleInterface("/views/GestionMarquesView.fxml", "Gestion des Marques");
    }

    /**
     * Charge une nouvelle interface dans une nouvelle fenêtre
     */
    private void chargerNouvelleInterface(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur chargement de l'interface " + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Rafraîchit toutes les données du dashboard
     */
    @FXML
    private void rafraichirDashboard() {
        chargerStatistiques();
        chargerGraphiqueTopModeles();
        chargerGraphiqueStatuts();
        System.out.println("Dashboard rafraîchi !");
    }

    // Ouvrir l'interface Ajouter Marque
    @FXML
    private void ouvrirAjouterMarque() {
        chargerNouvelleInterface("/views/AjouterMarqueView.fxml", "Ajouter une Marque");
    }

    // Ouvrir l'interface Afficher Marques
    @FXML
    private void ouvrirAfficherMarques() {
        chargerNouvelleInterface("/views/AfficherMarquesView.fxml", "Catalogue des Marques");
    }
}