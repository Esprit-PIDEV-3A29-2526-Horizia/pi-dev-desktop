package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import tn.esprit.entities.Profil;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceProfil;
import tn.esprit.services.Serviceuser;

import java.sql.SQLException;
import java.util.*;

public class StatisticsController {

    @FXML private Label totalMembersLabel;
    @FXML private Label activeMembersLabel;
    @FXML private Label blockedMembersLabel;
    @FXML private Label totalProfilsLabel;
    @FXML private Label membersEvolutionLabel;
    @FXML private Label activePercentageLabel;
    @FXML private Label blockedPercentageLabel;

    @FXML private PieChart typePieChart;
    @FXML private PieChart statutPieChart;
    @FXML private BarChart<String, Number> distributionBarChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private TableView<TypeStatistic> summaryTable;
    @FXML private TableColumn<TypeStatistic, String> colType;
    @FXML private TableColumn<TypeStatistic, Integer> colActif;
    @FXML private TableColumn<TypeStatistic, Integer> colBloque;
    @FXML private TableColumn<TypeStatistic, Integer> colInactif;
    @FXML private TableColumn<TypeStatistic, Integer> colTotal;

    @FXML private Label lblMessage;

    // ScrollPanes pour rendre responsive
    @FXML private ScrollPane mainScrollPane;
    @FXML private ScrollPane chartsScrollPane;
    @FXML private ScrollPane barChartScrollPane;
    @FXML private ScrollPane tableScrollPane;

    private final Serviceuser serviceUser = new Serviceuser();
    private final ServiceProfil serviceProfil = new ServiceProfil();

    private List<User> users = new ArrayList<>();
    private List<Profil> profils = new ArrayList<>();

    private AdminDashboardController dashboardController;

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation StatisticsController ===");

        setupTableColumns();
        setupResponsiveScrollPanes();
        loadData();
    }

    private void setupResponsiveScrollPanes() {
        // Configurer le ScrollPane principal
        if (mainScrollPane != null) {
            mainScrollPane.setFitToWidth(true);
            mainScrollPane.setFitToHeight(true);
            mainScrollPane.setPannable(true); // Permettre le panning avec la souris
        }

        // Configurer le ScrollPane des graphiques
        if (chartsScrollPane != null) {
            chartsScrollPane.setFitToWidth(true);
            chartsScrollPane.setFitToHeight(true);
        }

        // Configurer le ScrollPane du graphique en barres
        if (barChartScrollPane != null) {
            barChartScrollPane.setFitToWidth(true);
            barChartScrollPane.setFitToHeight(true);
        }

        // Configurer le ScrollPane du tableau
        if (tableScrollPane != null) {
            tableScrollPane.setFitToWidth(true);
            tableScrollPane.setFitToHeight(true);
        }
    }

    private void setupTableColumns() {
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
        colBloque.setCellValueFactory(new PropertyValueFactory<>("bloque"));
        colInactif.setCellValueFactory(new PropertyValueFactory<>("inactif"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        colType.setStyle("-fx-font-weight: bold;");
        colActif.setStyle("-fx-text-fill: #10B981; -fx-alignment: CENTER;");
        colBloque.setStyle("-fx-text-fill: #EF4444; -fx-alignment: CENTER;");
        colInactif.setStyle("-fx-text-fill: #6B7280; -fx-alignment: CENTER;");
        colTotal.setStyle("-fx-font-weight: bold; -fx-alignment: CENTER;");

        // Rendre le tableau redimensionnable
        summaryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void refreshStats() {
        loadData();
        showMessage("Statistiques actualisées", "success");
    }

    private void loadData() {
        try {
            // Charger les données
            users = serviceUser.afficher();
            profils = serviceProfil.afficher();

            System.out.println("✅ " + users.size() + " utilisateurs trouvés");
            System.out.println("✅ " + profils.size() + " profils trouvés");

            // Mettre à jour les KPIs
            updateKPIs();

            // Mettre à jour les graphiques
            updateCharts();

            // Mettre à jour le tableau récapitulatif
            updateSummaryTable();

            System.out.println("✅ Statistiques chargées avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Erreur de chargement des données", "error");
        }
    }

    private void updateKPIs() {
        // Total membres
        int totalMembers = users.size();
        totalMembersLabel.setText(String.valueOf(totalMembers));

        // Membres actifs
        long activeMembers = users.stream().filter(u -> "ACTIF".equals(u.getStatut())).count();
        activeMembersLabel.setText(String.valueOf(activeMembers));

        // Membres bloqués
        long blockedMembers = users.stream().filter(u -> "BLOQUE".equals(u.getStatut())).count();
        blockedMembersLabel.setText(String.valueOf(blockedMembers));

        // Membres inactifs
        long inactiveMembers = users.stream()
                .filter(u -> u.getStatut() != null &&
                        !"ACTIF".equals(u.getStatut()) &&
                        !"BLOQUE".equals(u.getStatut()))
                .count();

        // Pourcentages
        if (totalMembers > 0) {
            double activePercentage = (activeMembers * 100.0) / totalMembers;
            double blockedPercentage = (blockedMembers * 100.0) / totalMembers;

            activePercentageLabel.setText(String.format("%.1f%% du total", activePercentage));
            blockedPercentageLabel.setText(String.format("%.1f%% du total", blockedPercentage));

            // Évolution simulée
            Random rand = new Random();
            double evolution = (rand.nextDouble() * 15) - 5; // Entre -5% et +10%
            String evolutionText = evolution >= 0 ?
                    String.format("+%.1f%% vs mois dernier", evolution) :
                    String.format("%.1f%% vs mois dernier", evolution);
            membersEvolutionLabel.setText(evolutionText);
            membersEvolutionLabel.setStyle(evolution >= 0 ?
                    "-fx-text-fill: #10B981; -fx-font-size: 14px;" :
                    "-fx-text-fill: #EF4444; -fx-font-size: 14px;");
        }

        // Total profils
        totalProfilsLabel.setText(String.valueOf(profils.size()));
    }

    private void updateCharts() {
        updateTypePieChart();
        updateStatutPieChart();
        updateBarChart();
    }

    private void updateTypePieChart() {
        ObservableList<PieChart.Data> typeData = FXCollections.observableArrayList();

        Map<String, Long> typeCount = new HashMap<>();
        for (User user : users) {
            String type = user.getType() != null ? user.getType() : "Non défini";
            typeCount.put(type, typeCount.getOrDefault(type, 0L) + 1);
        }

        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
            typeData.add(slice);
        }

        typePieChart.setData(typeData);
        typePieChart.setTitle("Répartition par Type");
        typePieChart.setLabelsVisible(true);
        typePieChart.setClockwise(true);

        // Ajouter des couleurs personnalisées
        applyPieChartColors(typePieChart);
    }

    private void updateStatutPieChart() {
        ObservableList<PieChart.Data> statutData = FXCollections.observableArrayList();

        Map<String, Long> statutCount = new HashMap<>();
        for (User user : users) {
            String statut = user.getStatut() != null ? user.getStatut() : "Non défini";
            statutCount.put(statut, statutCount.getOrDefault(statut, 0L) + 1);
        }

        for (Map.Entry<String, Long> entry : statutCount.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
            statutData.add(slice);
        }

        statutPieChart.setData(statutData);
        statutPieChart.setTitle("Répartition par Statut");
        statutPieChart.setLabelsVisible(true);
        statutPieChart.setClockwise(true);

        applyPieChartColors(statutPieChart);
    }

    private void applyPieChartColors(PieChart pieChart) {
        // Appliquer les couleurs après un court délai
        javafx.application.Platform.runLater(() -> {
            int index = 0;
            for (PieChart.Data data : pieChart.getData()) {
                String color;
                switch (index % 6) {
                    case 0: color = "#3B82F6"; break; // Bleu
                    case 1: color = "#10B981"; break; // Vert
                    case 2: color = "#F59E0B"; break; // Orange
                    case 3: color = "#EF4444"; break; // Rouge
                    case 4: color = "#8B5CF6"; break; // Violet
                    default: color = "#6B7280"; break; // Gris
                }
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
                index++;
            }
        });
    }

    private void updateBarChart() {
        // Vider les données existantes
        distributionBarChart.getData().clear();

        // Séries pour chaque statut
        XYChart.Series<String, Number> actifSeries = new XYChart.Series<>();
        actifSeries.setName("Actif");

        XYChart.Series<String, Number> bloqueSeries = new XYChart.Series<>();
        bloqueSeries.setName("Bloqué");

        XYChart.Series<String, Number> inactifSeries = new XYChart.Series<>();
        inactifSeries.setName("Inactif");

        // Obtenir tous les types uniques
        Set<String> types = new HashSet<>();
        for (User user : users) {
            if (user.getType() != null && !user.getType().isEmpty()) {
                types.add(user.getType());
            }
        }

        if (types.isEmpty()) {
            types.add("Aucun type");
        }

        // Trier les types pour un affichage cohérent
        List<String> sortedTypes = new ArrayList<>(types);
        Collections.sort(sortedTypes);

        // Pour chaque type, compter par statut
        for (String type : sortedTypes) {
            long actif = users.stream()
                    .filter(u -> type.equals(u.getType()) && "ACTIF".equals(u.getStatut()))
                    .count();

            long bloque = users.stream()
                    .filter(u -> type.equals(u.getType()) && "BLOQUE".equals(u.getStatut()))
                    .count();

            long inactif = users.stream()
                    .filter(u -> type.equals(u.getType()) &&
                            u.getStatut() != null &&
                            !"ACTIF".equals(u.getStatut()) &&
                            !"BLOQUE".equals(u.getStatut()))
                    .count();

            if (actif > 0 || bloque > 0 || inactif > 0) {
                actifSeries.getData().add(new XYChart.Data<>(type, actif));
                bloqueSeries.getData().add(new XYChart.Data<>(type, bloque));
                inactifSeries.getData().add(new XYChart.Data<>(type, inactif));
            }
        }

        // Ajouter les séries au graphique
        distributionBarChart.getData().addAll(actifSeries, bloqueSeries, inactifSeries);

        // Appliquer les styles
        applyBarChartStyles();
    }

    private void applyBarChartStyles() {
        javafx.application.Platform.runLater(() -> {
            try {
                // Appliquer les couleurs aux barres
                if (!distributionBarChart.getData().isEmpty()) {
                    for (int i = 0; i < distributionBarChart.getData().size(); i++) {
                        XYChart.Series<String, Number> series = distributionBarChart.getData().get(i);
                        String color;
                        switch (series.getName()) {
                            case "Actif":
                                color = "#10B981";
                                break;
                            case "Bloqué":
                                color = "#EF4444";
                                break;
                            default:
                                color = "#6B7280";
                                break;
                        }

                        for (XYChart.Data<String, Number> data : series.getData()) {
                            if (data.getNode() != null) {
                                data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors de l'application des styles: " + e.getMessage());
            }
        });
    }

    private void updateSummaryTable() {
        ObservableList<TypeStatistic> statistics = FXCollections.observableArrayList();

        // Obtenir tous les types uniques
        Set<String> types = new HashSet<>();
        for (User user : users) {
            if (user.getType() != null && !user.getType().isEmpty()) {
                types.add(user.getType());
            }
        }

        // Trier les types
        List<String> sortedTypes = new ArrayList<>(types);
        Collections.sort(sortedTypes);

        // Pour chaque type, créer une statistique
        for (String type : sortedTypes) {
            long actif = users.stream()
                    .filter(u -> type.equals(u.getType()) && "ACTIF".equals(u.getStatut()))
                    .count();

            long bloque = users.stream()
                    .filter(u -> type.equals(u.getType()) && "BLOQUE".equals(u.getStatut()))
                    .count();

            long inactif = users.stream()
                    .filter(u -> type.equals(u.getType()) &&
                            u.getStatut() != null &&
                            !"ACTIF".equals(u.getStatut()) &&
                            !"BLOQUE".equals(u.getStatut()))
                    .count();

            long total = users.stream()
                    .filter(u -> type.equals(u.getType()))
                    .count();

            if (total > 0) {
                statistics.add(new TypeStatistic(type, (int) actif, (int) bloque, (int) inactif, (int) total));
            }
        }

        // Ajouter une ligne "TOTAL"
        int totalActif = statistics.stream().mapToInt(TypeStatistic::getActif).sum();
        int totalBloque = statistics.stream().mapToInt(TypeStatistic::getBloque).sum();
        int totalInactif = statistics.stream().mapToInt(TypeStatistic::getInactif).sum();
        int totalGlobal = statistics.stream().mapToInt(TypeStatistic::getTotal).sum();

        statistics.add(new TypeStatistic("📊 TOTAL", totalActif, totalBloque, totalInactif, totalGlobal));

        summaryTable.setItems(statistics);
    }

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
        System.out.println("✅ DashboardController passé à StatisticsController");
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        if ("error".equals(type)) {
            lblMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else {
            lblMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 14px;");
        }

        // Faire disparaître le message après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() ->
                        lblMessage.setText("")
                );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Classe interne pour les statistiques par type
     */
    public static class TypeStatistic {
        private final String type;
        private final int actif;
        private final int bloque;
        private final int inactif;
        private final int total;

        public TypeStatistic(String type, int actif, int bloque, int inactif, int total) {
            this.type = type;
            this.actif = actif;
            this.bloque = bloque;
            this.inactif = inactif;
            this.total = total;
        }

        public String getType() { return type; }
        public int getActif() { return actif; }
        public int getBloque() { return bloque; }
        public int getInactif() { return inactif; }
        public int getTotal() { return total; }
    }
}