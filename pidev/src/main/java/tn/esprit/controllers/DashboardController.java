package tn.esprit.controllers;


import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
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
import tn.esprit.services.Dashboardservice;
import tn.esprit.services.LocationService;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // ─── Labels statistiques ──────────────────────────────────────
    @FXML private Label lblModeleTopNom;
    @FXML private Label lblModeleTopCount;
    @FXML private Label lblLocationsActives;
    @FXML private Label lblVehiculesDisponibles;
    @FXML private Label lblVehiculesLoues;

    // ─── Graphiques ───────────────────────────────────────────────
    @FXML private BarChart<String, Number> barChartTopModeles;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private PieChart pieChartStatuts;

    // ─── Cards de navigation ──────────────────────────────────────
    @FXML private HBox cardGestionLocations;
    @FXML private HBox cardGestionVehicules;
    @FXML private HBox cardGestionMarques;
    @FXML private HBox cardDocuments;
    @FXML private HBox cardPlanning;

    // ─── Boutons ──────────────────────────────────────────────────
    @FXML private Button btnRafraichir;
    @FXML private Button btnMettreAJourStatuts;

    private Dashboardservice dashboardService;
    private LocationService locationService;

    private MainLayoutController mainLayoutController;

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dashboardService = new Dashboardservice();
        locationService = new LocationService();

        // Configuration initiale des graphiques
        configurerGraphiques();

        chargerStatistiques();
        configurerBoutons();
        configurerCardsNavigation();
    }

    /**
     * Configure les propriétés des graphiques pour un meilleur affichage
     */
    private void configurerGraphiques() {
        // Configuration du BarChart
        if (barChartTopModeles != null) {
            barChartTopModeles.setAnimated(false);
            barChartTopModeles.setLegendVisible(false);
            barChartTopModeles.setTitle("Top 5 Modèles Loués");
            barChartTopModeles.setTitleSide(Side.TOP);

            // Configuration de l'axe X
            if (xAxis != null) {
                xAxis.setLabel("Modèle");
                xAxis.setTickLabelRotation(0); // Pas de rotation
                xAxis.setTickLabelGap(5);
                xAxis.setTickMarkVisible(true);
            }

            // Configuration de l'axe Y
            if (yAxis != null) {
                yAxis.setLabel("Nombre de locations");
                yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
                    @Override
                    public String toString(Number object) {
                        return String.format("%d", object.intValue());
                    }
                });
            }

            // Style pour rendre les labels visibles
            barChartTopModeles.setStyle("-fx-bar-fill: #23779C;");
        }

        // Configuration du PieChart
        if (pieChartStatuts != null) {
            pieChartStatuts.setAnimated(false);
            pieChartStatuts.setTitle("Répartition par Statut");
            pieChartStatuts.setTitleSide(Side.TOP);
            pieChartStatuts.setLegendVisible(true);
            pieChartStatuts.setLabelsVisible(true);
            pieChartStatuts.setLabelLineLength(10);

            // Position de la légende à droite
            pieChartStatuts.setLegendSide(Side.RIGHT);

            // Style pour les labels
            pieChartStatuts.setStyle("-fx-pie-label-visible: true; -fx-font-size: 11px;");
        }
    }

    private void configurerBoutons() {
        if (btnMettreAJourStatuts != null) {
            btnMettreAJourStatuts.setOnMouseEntered(e ->
                    btnMettreAJourStatuts.setStyle(
                            "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 18; -fx-cursor: hand; -fx-font-weight: bold;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(46,204,113,0.5), 10, 0, 0, 2);"
                    )
            );
            btnMettreAJourStatuts.setOnMouseExited(e ->
                    btnMettreAJourStatuts.setStyle(
                            "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 18; -fx-cursor: hand; -fx-font-weight: bold;"
                    )
            );
        }
    }

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

    private void chargerStatistiques() {
        try {
            // Statistiques de base
            int disponibles = dashboardService.getNombreVehiculesDisponibles();
            int loues = dashboardService.getNombreVehiculesLoues();
            int actives = dashboardService.getNombreLocationsActives();

            if (lblVehiculesDisponibles != null) lblVehiculesDisponibles.setText(String.valueOf(disponibles));
            if (lblVehiculesLoues != null) lblVehiculesLoues.setText(String.valueOf(loues));
            if (lblLocationsActives != null) lblLocationsActives.setText(String.valueOf(actives));

            // Modèle le plus loué
            Map<String, Object> topModele = dashboardService.getModeleLesPlusLoue();
            if (topModele != null && !topModele.isEmpty()) {
                String nomMarque = (String) topModele.getOrDefault("nomMarque", "—");
                String nomModele = (String) topModele.getOrDefault("nomModele", "");
                Object nbLoc = topModele.getOrDefault("nombreLocations", 0);
                if (lblModeleTopNom != null) lblModeleTopNom.setText((nomMarque + " " + nomModele).trim());
                if (lblModeleTopCount != null) lblModeleTopCount.setText(nbLoc + " location(s)");
            }

            // Charger les graphiques
            chargerTopModelesBarChart();
            chargerStatutsPieChart();

            System.out.println("✓ Dashboard mis à jour avec succès");

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Charge le graphique en barres avec le top 5 des modèles loués
     */
    private void chargerTopModelesBarChart() {
        if (barChartTopModeles == null) return;

        try {
            barChartTopModeles.getData().clear();
            barChartTopModeles.setTitle("Top 5 Modèles Loués");

            Map<String, Integer> topModeles = dashboardService.getTop5ModelesLoues();

            System.out.println("=== TOP 5 MODÈLES ===");
            for (Map.Entry<String, Integer> entry : topModeles.entrySet()) {
                System.out.println("  " + entry.getKey() + " : " + entry.getValue());
            }
            System.out.println("====================");

            if (topModeles == null || topModeles.isEmpty()) {
                // Afficher un message si pas de données
                XYChart.Series<String, Number> serieVide = new XYChart.Series<>();
                serieVide.getData().add(new XYChart.Data<>("Aucune donnée", 0));
                barChartTopModeles.getData().add(serieVide);
                return;
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Locations");

            int count = 0;
            for (Map.Entry<String, Integer> entry : topModeles.entrySet()) {
                if (entry.getValue() > 0 && count < 5) {
                    // Tronquer les noms trop longs
                    String label = entry.getKey();
                    if (label.length() > 15) {
                        label = label.substring(0, 12) + "...";
                    }
                    series.getData().add(new XYChart.Data<>(label, entry.getValue()));
                    count++;
                }
            }

            if (!series.getData().isEmpty()) {
                barChartTopModeles.getData().add(series);

                // Appliquer un style aux barres
                for (XYChart.Data<String, Number> data : series.getData()) {
                    data.getNode().setStyle("-fx-bar-fill: #23779C;");
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur chargement barChart : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Charge le graphique en camembert avec la répartition par statut
     */
    private void chargerStatutsPieChart() {
        if (pieChartStatuts == null) return;

        try {
            pieChartStatuts.getData().clear();
            pieChartStatuts.setTitle("Répartition par Statut");

            Map<String, Integer> statuts = dashboardService.getLocationsParStatut();

            System.out.println("=== RÉPARTITION PAR STATUT ===");
            for (Map.Entry<String, Integer> entry : statuts.entrySet()) {
                System.out.println("  " + entry.getKey() + " : " + entry.getValue());
            }
            System.out.println("==============================");

            if (statuts == null || statuts.isEmpty()) {
                ObservableList<PieChart.Data> dataVide = FXCollections.observableArrayList();
                dataVide.add(new PieChart.Data("Aucune location", 1));
                pieChartStatuts.setData(dataVide);
                return;
            }

            ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : statuts.entrySet()) {
                if (entry.getValue() > 0) {
                    String label = entry.getKey();

                    // Formater les labels pour l'affichage
                    if (label.equals("en_cours")) label = "En cours";
                    else if (label.equals("réservée")) label = "Réservée";
                    else if (label.equals("terminée")) label = "Terminée";
                    else if (label.equals("annulée")) label = "Annulée";
                    else if (label.equals("no_show")) label = "No show";

                    // Ajouter le nombre dans le label
                    data.add(new PieChart.Data(label + " (" + entry.getValue() + ")", entry.getValue()));
                }
            }

            if (!data.isEmpty()) {
                pieChartStatuts.setData(data);

                // Définir des couleurs personnalisées
                for (PieChart.Data slice : data) {
                    String name = slice.getName();
                    if (name.contains("Réservée"))
                        slice.getNode().setStyle("-fx-pie-color: #3498db;");
                    else if (name.contains("En cours"))
                        slice.getNode().setStyle("-fx-pie-color: #27ae60;");
                    else if (name.contains("Terminée"))
                        slice.getNode().setStyle("-fx-pie-color: #95a5a6;");
                    else if (name.contains("Annulée"))
                        slice.getNode().setStyle("-fx-pie-color: #e74c3c;");
                    else if (name.contains("No show"))
                        slice.getNode().setStyle("-fx-pie-color: #e67e22;");
                }

                // Forcer l'affichage des labels
                pieChartStatuts.setLabelsVisible(true);
            }

        } catch (Exception e) {
            System.err.println("Erreur chargement pieChart : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void rafraichirDashboard() {
        try {
            if (btnRafraichir != null) {
                btnRafraichir.setStyle(btnRafraichir.getStyle() + "-fx-rotate: 180;");
                PauseTransition pause = new PauseTransition(Duration.seconds(0.3));
                pause.setOnFinished(e -> btnRafraichir.setStyle(btnRafraichir.getStyle().replace("-fx-rotate: 180;", "")));
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
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Mise à jour des statuts");
            confirm.setHeaderText(null);
            confirm.setContentText("Voulez-vous lancer la mise à jour automatique des statuts ?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {

                if (btnMettreAJourStatuts != null) {
                    btnMettreAJourStatuts.setDisable(true);
                    btnMettreAJourStatuts.setText("⏳ Mise à jour...");
                }

                new Thread(() -> {
                    try {
                        int nbMAJ = locationService.mettreAJourStatutsAutomatique();

                        javafx.application.Platform.runLater(() -> {
                            if (btnMettreAJourStatuts != null) {
                                btnMettreAJourStatuts.setDisable(false);
                                btnMettreAJourStatuts.setText("⚡ Mettre à jour statuts");
                            }

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Mise à jour terminée");
                            alert.setHeaderText(null);
                            alert.setContentText(nbMAJ + " location(s) ont été mises à jour automatiquement.");
                            alert.showAndWait();

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

    // ─── Méthodes de navigation ──────────────────────────────────
    @FXML
    private void handleCardGestionLocationsClick(MouseEvent event) {
        naviguerVers("/views/GestionLocationsView.fxml");
    }

    @FXML
    private void handleCardGestionVehiculesClick(MouseEvent event) {
        naviguerVers("/views/GestionVehiculesView.fxml");
    }

    @FXML
    private void handleCardGestionMarquesClick(MouseEvent event) {
        naviguerVers("/views/GestionMarquesView.fxml");
    }

    @FXML
    private void ouvrirDocuments(MouseEvent event) {
        naviguerVers("/views/DocumentsView.fxml");
    }

    @FXML
    private void ouvrirPlanning(MouseEvent event) {
        naviguerVers("/views/PlanningView.fxml");
    }

    private void naviguerVers(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));

            if (barChartTopModeles != null && barChartTopModeles.getScene() != null) {
                StackPane contentArea = (StackPane) barChartTopModeles.getScene().lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().setAll(root);
                    return;
                }
            }

            barChartTopModeles.getScene().setRoot(root);

        } catch (IOException e) {
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