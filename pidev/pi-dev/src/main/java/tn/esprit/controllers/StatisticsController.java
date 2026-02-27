package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.Profil;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceProfil;
import tn.esprit.services.ServiceUser;

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

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceProfil serviceProfil = new ServiceProfil();

    private List<User> users = new ArrayList<>();
    private List<Profil> profils = new ArrayList<>();

    private AdminDashboardController dashboardController;

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation StatisticsController ===");

        setupTableColumns();
        loadData();
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

            // Évolution simulée (à remplacer par de vraies données historiques)
            membersEvolutionLabel.setText("+" + String.format("%.1f", Math.random() * 10) + "% vs mois dernier");
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
            if (user.getType() != null) {
                types.add(user.getType());
            }
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

            actifSeries.getData().add(new XYChart.Data<>(type, actif));
            bloqueSeries.getData().add(new XYChart.Data<>(type, bloque));
            inactifSeries.getData().add(new XYChart.Data<>(type, inactif));
        }

        // Ajouter les séries au graphique
        distributionBarChart.getData().addAll(actifSeries, bloqueSeries, inactifSeries);

        // Appliquer les styles APRÈS que les séries soient ajoutées
        applyBarChartStyles();
    }

    private void applyBarChartStyles() {
        // Appliquer les styles après un court délai pour que les nœuds soient créés
        javafx.application.Platform.runLater(() -> {
            try {
                if (!distributionBarChart.getData().isEmpty()) {
                    // Série Actif (index 0)
                    if (distributionBarChart.getData().size() > 0) {
                        XYChart.Series<String, Number> serie = distributionBarChart.getData().get(0);
                        if (serie.getNode() != null) {
                            serie.getNode().setStyle("-fx-bar-fill: #10B981;");
                        }
                    }

                    // Série Bloqué (index 1)
                    if (distributionBarChart.getData().size() > 1) {
                        XYChart.Series<String, Number> serie = distributionBarChart.getData().get(1);
                        if (serie.getNode() != null) {
                            serie.getNode().setStyle("-fx-bar-fill: #EF4444;");
                        }
                    }

                    // Série Inactif (index 2)
                    if (distributionBarChart.getData().size() > 2) {
                        XYChart.Series<String, Number> serie = distributionBarChart.getData().get(2);
                        if (serie.getNode() != null) {
                            serie.getNode().setStyle("-fx-bar-fill: #6B7280;");
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
            if (user.getType() != null) {
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

            statistics.add(new TypeStatistic(type, (int) actif, (int) bloque, (int) inactif, (int) total));
        }

        // Ajouter une ligne "TOTAL"
        int totalActif = statistics.stream().mapToInt(TypeStatistic::getActif).sum();
        int totalBloque = statistics.stream().mapToInt(TypeStatistic::getBloque).sum();
        int totalInactif = statistics.stream().mapToInt(TypeStatistic::getInactif).sum();
        int totalGlobal = statistics.stream().mapToInt(TypeStatistic::getTotal).sum();

        statistics.add(new TypeStatistic("TOTAL", totalActif, totalBloque, totalInactif, totalGlobal));

        summaryTable.setItems(statistics);
    }

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
        System.out.println("✅ DashboardController passé à StatisticsController");
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        if ("error".equals(type)) {
            lblMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        } else {
            lblMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
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