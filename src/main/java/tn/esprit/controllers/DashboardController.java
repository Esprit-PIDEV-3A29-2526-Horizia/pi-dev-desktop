package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Events;
import tn.esprit.entities.Participation;
import tn.esprit.services.ServiceEvent;
import tn.esprit.services.ServiceParticipation;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    @FXML private Label totalEventsLabel;
    @FXML private Label totalPlacesLabel;
    @FXML private Label fillRateLabel;
    @FXML private Label totalRevenueLabel;

    @FXML private LineChart<String, Number> evolutionChart;
    @FXML private PieChart categoriesChart;
    @FXML private BarChart<String, Number> topEventsChart;
    @FXML private HBox eventsContainer;


    private ServiceEvent serviceEvent;
    private ServiceParticipation serviceParticipation;
    private List<Events> allEvents;
    private List<Participation> allParticipations;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();
        serviceParticipation = new ServiceParticipation();

        loadData();
    }

    private void loadData() {
        try {
            allEvents = serviceEvent.afficher();
            allParticipations = serviceParticipation.afficher();

            updateKPIs();
            updateEvolutionChart();
            updateCategoriesChart();
            updateTopEventsChart();
            updateEventsList();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateKPIs() {
        // Total événements
        totalEventsLabel.setText(String.valueOf(allEvents.size()));

        // Calculs
        int totalPlaces = 0;
        int totalBooked = 0;
        double totalRevenue = 0;

        for (Events event : allEvents) {
            totalPlaces += event.getCapaciteMax();
            int booked = event.getCapaciteMax() - event.getPlacesRestantes();
            totalBooked += booked;
            totalRevenue += booked * event.getPrix();
        }

        totalPlacesLabel.setText(String.valueOf(totalPlaces));

        int fillRate = totalPlaces > 0 ? (totalBooked * 100 / totalPlaces) : 0;
        fillRateLabel.setText(fillRate + "%");

        totalRevenueLabel.setText(String.format("%.0f DT", totalRevenue));
    }

    private void updateEvolutionChart() {
        evolutionChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations");

        // Grouper par date (7 derniers jours)
        Map<String, Long> reservationsParJour = allParticipations.stream()
                .collect(Collectors.groupingBy(
                        p -> new SimpleDateFormat("dd/MM").format(p.getDateParticipation()),
                        Collectors.counting()
                ));

        // Trier et limiter
        List<String> dates = reservationsParJour.keySet().stream()
                .sorted(Comparator.comparing(d -> {
                    try {
                        return new SimpleDateFormat("dd/MM").parse(d);
                    } catch (Exception e) {
                        return new Date();
                    }
                }))
                .limit(7)
                .collect(Collectors.toList());

        for (String date : dates) {
            series.getData().add(new XYChart.Data<>(date, reservationsParJour.getOrDefault(date, 0L)));
        }

        evolutionChart.getData().add(series);
    }

    private void updateCategoriesChart() {
        categoriesChart.getData().clear();

        Map<String, Long> countParCategorie = allEvents.stream()
                .collect(Collectors.groupingBy(Events::getCategorie, Collectors.counting()));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map.Entry<String, Long> entry : countParCategorie.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        categoriesChart.setData(pieData);
    }

    private void updateTopEventsChart() {
        topEventsChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Taux de remplissage");

        List<Events> topEvents = allEvents.stream()
                .sorted((e1, e2) -> Integer.compare(
                        e2.getCapaciteMax() - e2.getPlacesRestantes(),
                        e1.getCapaciteMax() - e1.getPlacesRestantes()
                ))
                .limit(5)
                .collect(Collectors.toList());

        for (Events event : topEvents) {
            double taux = (event.getCapaciteMax() - event.getPlacesRestantes()) * 100.0 / event.getCapaciteMax();
            series.getData().add(new XYChart.Data<>(event.getTitre(), taux));
        }

        topEventsChart.getData().add(series);
    }

    private void updateEventsList() {
        eventsContainer.getChildren().clear();

        List<Events> topEvents = allEvents.stream()
                .sorted((e1, e2) -> Integer.compare(
                        e2.getCapaciteMax() - e2.getPlacesRestantes(),
                        e1.getCapaciteMax() - e1.getPlacesRestantes()
                ))
                .limit(3)
                .collect(Collectors.toList());

        for (Events event : topEvents) {
            double taux = (event.getCapaciteMax() - event.getPlacesRestantes()) * 100.0 / event.getCapaciteMax();
            VBox card = createEventCard(event, taux);
            eventsContainer.getChildren().add(card);
        }
    }

    private VBox createEventCard(Events event, double taux) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 10; -fx-border-color: #DACEB6; -fx-border-radius: 10;");
        card.setPrefWidth(200);

        Label title = new Label(event.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #23779C; -fx-wrap-text: true;");

        Label progress = new Label(String.format("Taux: %.1f%%", taux));
        progress.setStyle("-fx-text-fill: " + (taux > 80 ? "#e74c3c" : "#27ae60") + ";");

        Label places = new Label(event.getPlacesRestantes() + "/" + event.getCapaciteMax() + " places");
        places.setStyle("-fx-text-fill: #666;");

        ProgressBar progressBar = new ProgressBar(taux / 100);
        progressBar.setPrefWidth(180);
        progressBar.setStyle("-fx-accent: " + (taux > 80 ? "#e74c3c" : "#81AE8D") + ";");

        card.getChildren().addAll(title, progress, places, progressBar);
        return card;
    }

    @FXML
    private void refreshDashboard(){
        loadData();
    }
}