package controllers;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.example.services.Dashboardservice;
import org.example.services.LocationService;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur du Dashboard Horizia
 * Version complète avec mise à jour automatique des statuts
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

    // ─── Boutons ──────────────────────────────────────────────────
    @FXML private Button btnMettreAJourStatuts;

    private Dashboardservice dashboardService;
    private LocationService locationService;

    // ─────────────────────────────────────────────────────────────
    // INITIALISATION
    // ─────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dashboardService = new Dashboardservice();
        locationService = new LocationService();

        chargerStatistiques();
        configurerCardsNavigation();
        configurerBoutons();
    }

    private void configurerBoutons() {
        if (btnMettreAJourStatuts != null) {
            // Effet hover
            btnMettreAJourStatuts.setOnMouseEntered(e ->
                    btnMettreAJourStatuts.setStyle(
                            "-fx-background-color: #2ecc71; -fx-text-fill: white;" +
                                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 18;" +
                                    "-fx-background-radius: 10; -fx-cursor: hand;" +
                                    "-fx-effect: dropshadow(gaussian, #2ecc7180, 12, 0, 0, 4);"
                    )
            );
            btnMettreAJourStatuts.setOnMouseExited(e ->
                    btnMettreAJourStatuts.setStyle(
                            "-fx-background-color: #27ae60; -fx-text-fill: white;" +
                                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 18;" +
                                    "-fx-background-radius: 10; -fx-cursor: hand;" +
                                    "-fx-effect: dropshadow(gaussian, #27ae6040, 8, 0, 0, 3);"
                    )
            );
        }
    }

    private void chargerStatistiques() {
        try {
            // Statistiques véhicules
            int disponibles = dashboardService.getNombreVehiculesDisponibles();
            int loues       = dashboardService.getNombreVehiculesLoues();
            int actives     = dashboardService.getNombreLocationsActives();

            if (lblVehiculesDisponibles != null) lblVehiculesDisponibles.setText(String.valueOf(disponibles));
            if (lblVehiculesLoues       != null) lblVehiculesLoues.setText(String.valueOf(loues));
            if (lblLocationsActives     != null) lblLocationsActives.setText(String.valueOf(actives));

            // Modèle le plus loué
            Map<String, Object> topModele = dashboardService.getModeleLesPlusLoue();
            if (topModele != null && !topModele.isEmpty()) {
                String nomMarque  = (String) topModele.getOrDefault("nomMarque",  "—");
                String nomModele  = (String) topModele.getOrDefault("nomModele",  "");
                Object nbLoc      = topModele.getOrDefault("nombreLocations", 0);
                if (lblModeleTopNom   != null) lblModeleTopNom.setText((nomMarque + " " + nomModele).trim());
                if (lblModeleTopCount != null) lblModeleTopCount.setText(nbLoc + " location(s)");
            }

            // Graphiques
            chargerBarChart();
            chargerPieChart();

            System.out.println("✓ Dashboard mis à jour avec succès");

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerBarChart() {
        if (barChartTopModeles == null) return;

        try {
            barChartTopModeles.getData().clear();

            Map<String, Integer> topModeles = dashboardService.getTop5ModelesLoues();
            if (topModeles == null || topModeles.isEmpty()) {
                // Afficher un message si pas de données
                XYChart.Series<String, Number> serieVide = new XYChart.Series<>();
                serieVide.getData().add(new XYChart.Data<>("Aucune donnée", 0));
                barChartTopModeles.getData().add(serieVide);
                return;
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Locations");

            for (Map.Entry<String, Integer> entry : topModeles.entrySet()) {
                if (entry.getValue() > 0) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
            }

            if (!series.getData().isEmpty()) {
                barChartTopModeles.getData().add(series);
            }

        } catch (Exception e) {
            System.err.println("Erreur chargement barChart : " + e.getMessage());
        }
    }

    private void chargerPieChart() {
        if (pieChartStatuts == null) return;

        try {
            pieChartStatuts.getData().clear();

            Map<String, Integer> statuts = dashboardService.getLocationsParStatut();
            if (statuts == null || statuts.isEmpty()) {
                // Afficher un message si pas de données
                ObservableList<PieChart.Data> dataVide = FXCollections.observableArrayList();
                dataVide.add(new PieChart.Data("Aucune location", 1));
                pieChartStatuts.setData(dataVide);
                return;
            }

            ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : statuts.entrySet()) {
                if (entry.getValue() > 0) {
                    data.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                }
            }

            if (!data.isEmpty()) {
                pieChartStatuts.setData(data);
            }

        } catch (Exception e) {
            System.err.println("Erreur chargement pieChart : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CONFIGURATION DES CARDS (hover effects)
    // ─────────────────────────────────────────────────────────────

    private void configurerCardsNavigation() {
        configurerHoverCard(cardGestionLocations);
        configurerHoverCard(cardGestionVehicules);
        configurerHoverCard(cardGestionMarques);
        configurerHoverCard(cardDocuments);
        configurerHoverCard(cardPlanning);
    }

    private void configurerHoverCard(HBox card) {
        if (card == null) return;

        card.setOnMouseEntered(e ->
                card.setStyle(card.getStyle() + "-fx-scale-x: 1.02; -fx-scale-y: 1.02; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 5);")
        );

        card.setOnMouseExited(e ->
                card.setStyle(card.getStyle().replace("-fx-scale-x: 1.02; -fx-scale-y: 1.02; ", "")
                        .replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 15, 0, 0, 5);",
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);"))
        );
    }

    // ─────────────────────────────────────────────────────────────
    // ACTIONS PRINCIPALES
    // ─────────────────────────────────────────────────────────────

    @FXML
    private void rafraichirDashboard() {
        try {
            // Animation simple sur le bouton
            Button source = (Button) barChartTopModeles.getScene().lookup("#rafraichir");
            if (source != null) {
                source.setStyle(source.getStyle() + "-fx-rotate: 180;");
                PauseTransition pause = new PauseTransition(Duration.seconds(0.3));
                pause.setOnFinished(e -> source.setStyle(source.getStyle().replace("-fx-rotate: 180;", "")));
                pause.play();
            }

            chargerStatistiques();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de rafraîchir le dashboard : " + e.getMessage());
        }
    }

    @FXML
    private void mettreAJourStatuts() {
        try {
            // Confirmation
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Mise à jour des statuts");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous lancer la mise à jour automatique des statuts ?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {

                // Animation sur le bouton
                if (btnMettreAJourStatuts != null) {
                    btnMettreAJourStatuts.setDisable(true);
                    btnMettreAJourStatuts.setText("⏳ Mise à jour en cours...");
                }

                // Exécuter la mise à jour dans un thread séparé
                new Thread(() -> {
                    try {
                        int nbMAJ = locationService.mettreAJourStatutsAutomatique();

                        javafx.application.Platform.runLater(() -> {
                            // Réactiver le bouton
                            if (btnMettreAJourStatuts != null) {
                                btnMettreAJourStatuts.setDisable(false);
                                btnMettreAJourStatuts.setText("⚡ Mettre à jour statuts");
                            }

                            // Afficher le résultat
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Mise à jour terminée");
                            alert.setHeaderText(null);
                            alert.setContentText(nbMAJ + " location(s) ont été mises à jour automatiquement.");
                            alert.showAndWait();

                            // Rafraîchir le dashboard
                            rafraichirDashboard();
                        });

                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            if (btnMettreAJourStatuts != null) {
                                btnMettreAJourStatuts.setDisable(false);
                                btnMettreAJourStatuts.setText("⚡ Mettre à jour statuts");
                            }
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour : " + e.getMessage());
                        });
                    }
                }).start();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de lancer la mise à jour : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────

    @FXML private void handleCardGestionLocationsClick(MouseEvent event) {
        naviguerVers("/views/GestionLocationsView.fxml");
    }

    @FXML private void handleCardGestionVehiculesClick(MouseEvent event) {
        naviguerVers("/views/GestionVehiculesView.fxml");
    }

    @FXML private void handleCardGestionMarquesClick(MouseEvent event)   {
        naviguerVers("/views/GestionMarquesView.fxml");
    }

    @FXML private void ouvrirDocuments(MouseEvent event) {
        naviguerVers("/views/DocumentsView.fxml");
    }

    @FXML private void ouvrirPlanning(MouseEvent event) {
        naviguerVers("/views/PlanningView.fxml");
    }

    private void naviguerVers(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

            // Chercher le contentArea dans MainLayout si présent
            StackPane contentArea = null;
            if (barChartTopModeles != null && barChartTopModeles.getScene() != null) {
                contentArea = (StackPane) barChartTopModeles.getScene().lookup("#contentArea");
            }

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            } else {
                // Sinon remplacer toute la scène
                barChartTopModeles.getScene().setRoot(root);
            }

            System.out.println("✓ Navigation vers : " + fxmlPath);

        } catch (Exception e) {
            System.err.println("[Dashboardcontroller] Erreur navigation vers " + fxmlPath + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation",
                    "Impossible d'ouvrir la page : " + fxmlPath + "\n" + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}