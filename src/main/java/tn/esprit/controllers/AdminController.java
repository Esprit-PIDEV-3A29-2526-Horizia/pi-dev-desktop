package tn.esprit.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.Events;
import tn.esprit.entities.Participation;
import tn.esprit.services.NotificationService;
import tn.esprit.services.NotificationService.Notification;
import tn.esprit.services.NotificationService.NotificationType;
import tn.esprit.services.ServiceEvent;
import tn.esprit.services.ServiceParticipation;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AdminController implements Initializable {

    // Dashboard Stats
    @FXML private Label totalEventsLabel;
    @FXML private Label totalPlacesLabel;
    @FXML private Label totalParticipationsLabel;
    @FXML private Label fillRateLabel;
    @FXML private FlowPane recentEventsFlow;

    // Dashboard Charts
    @FXML private LineChart<String, Number> evolutionChart;
    @FXML private PieChart categoriesChart;
    @FXML private BarChart<String, Number> topEventsChart;
    @FXML private HBox eventsContainer;

    // Events Management
    @FXML private TextField adminSearchField;
    @FXML private ComboBox<String> adminSortCombo;
    @FXML private FlowPane adminFlowEvents;

    // Participations
    @FXML private TextField participationSearchField;
    @FXML private FlowPane participationsFlow;

    // Navigation
    @FXML private Button btnDashboard;
    @FXML private Button btnEvents;
    @FXML private Button btnParticipations;
    @FXML private Button btnCalendar;
    @FXML private TabPane tabPane;

    // Notifications
    @FXML private Button notificationBtn;
    @FXML private Label notificationBadge;
    @FXML private VBox notificationsContainer;

    private ServiceEvent serviceEvent;
    private ServiceParticipation serviceParticipation;
    private List<Events> allEvents;
    private List<Participation> allParticipations;
    private NotificationService notificationService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();
        serviceParticipation = new ServiceParticipation();
        notificationService = NotificationService.getInstance();

        // S'abonner aux notifications
        notificationService.addListener(this::refreshNotifications);
        setupToastListener();

        setupNavigation();
        if (btnCalendar != null) {
            btnCalendar.setOnAction(e -> openCalendarView());
        }

        setupSortCombo();
        loadData();
        setupNotificationButton();
        refreshNotifications();

        // Search functionality
        if (adminSearchField != null) {
            adminSearchField.textProperty().addListener((obs, old, newVal) -> {
                if (newVal.isEmpty()) {
                    displayEvents(allEvents);
                } else {
                    filterEvents(newVal);
                }
            });
        }

        if (participationSearchField != null) {
            participationSearchField.textProperty().addListener((obs, old, newVal) -> {
                if (newVal.isEmpty()) {
                    displayParticipations(allParticipations);
                } else {
                    filterParticipations(newVal);
                }
            });
        }

        if (adminSortCombo != null) {
            adminSortCombo.setOnAction(e -> sortEvents());
        }
    }

    private void setupNavigation() {
        if (btnDashboard != null) btnDashboard.setOnAction(e -> tabPane.getSelectionModel().select(0));
        if (btnEvents != null) btnEvents.setOnAction(e -> tabPane.getSelectionModel().select(1));
        if (btnParticipations != null) btnParticipations.setOnAction(e -> tabPane.getSelectionModel().select(2));

        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, old, newVal) -> {
            updateButtonStyles(newVal.intValue());
        });
    }

    private void updateButtonStyles(int selectedIndex) {
        Button[] buttons = {btnDashboard, btnEvents, btnParticipations};

        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == null) continue;
            if (i == selectedIndex) {
                buttons[i].setStyle("-fx-background-color: #2d9cdb; -fx-text-fill: white; -fx-font-size:13; -fx-font-weight:800; -fx-background-radius:10; -fx-padding:12 14; -fx-cursor:hand;");
            } else {
                buttons[i].setStyle("-fx-background-color: transparent; -fx-text-fill: #E5E7EB; -fx-font-size:13; -fx-font-weight:800; -fx-background-radius:10; -fx-padding:12 14; -fx-cursor:hand; -fx-border-color:#3a4758; -fx-border-radius:10; -fx-border-width:1;");
            }
        }
    }

    private void setupSortCombo() {
        if (adminSortCombo != null) {
            adminSortCombo.getItems().addAll("Titre", "Prix", "Date", "Places", "Capacité");
        }
    }

    public void loadData() {
        try {
            allEvents = serviceEvent.afficher();
            allParticipations = serviceParticipation.afficher();

            System.out.println("✅ Données chargées: " + allEvents.size() + " événements, " + allParticipations.size() + " participations");

            updateStatistics();
            updateDashboardCharts();
            displayEvents(allEvents);
            displayParticipations(allParticipations);
            displayRecentEvents();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        int totalEvents = allEvents.size();
        int totalPlaces = 0;
        int totalBooked = 0;
        double totalRevenue = 0;

        for (Events event : allEvents) {
            totalPlaces += event.getCapaciteMax();
            int booked = event.getCapaciteMax() - event.getPlacesRestantes();
            totalBooked += booked;
            totalRevenue += booked * event.getPrix();
        }

        if (totalEventsLabel != null) totalEventsLabel.setText(String.valueOf(totalEvents));
        if (totalPlacesLabel != null) totalPlacesLabel.setText(String.valueOf(totalPlaces));
        if (totalParticipationsLabel != null) totalParticipationsLabel.setText(String.valueOf(allParticipations.size()));

        int fillRate = totalPlaces > 0 ? (totalBooked * 100 / totalPlaces) : 0;
        if (fillRateLabel != null) fillRateLabel.setText(fillRate + "%");
    }

    private void updateDashboardCharts() {
        updateEvolutionChart();
        updateCategoriesChart();
        updateTopEventsChart();
        updateEventsList();
    }

    private void updateEvolutionChart() {
        if (evolutionChart == null) return;

        evolutionChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations");

        Map<String, Long> reservationsParJour = allParticipations.stream()
                .collect(Collectors.groupingBy(
                        p -> new SimpleDateFormat("dd/MM").format(p.getDateParticipation()),
                        Collectors.counting()
                ));

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
        if (categoriesChart == null) return;

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
        if (topEventsChart == null) return;

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
        if (eventsContainer == null) return;

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

    private void displayRecentEvents() {
        if (recentEventsFlow == null) return;

        recentEventsFlow.getChildren().clear();
        List<Events> recent = allEvents.stream().limit(3).toList();
        for (Events event : recent) {
            recentEventsFlow.getChildren().add(createMiniEventCard(event));
        }
    }

    private VBox createMiniEventCard(Events event) {
        VBox card = new VBox();
        card.setPrefWidth(280);
        card.setSpacing(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        Label title = new Label(event.getTitre());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #23779C;");

        Label details = new Label(String.format("%.0f DT | %d/%d places",
                event.getPrix(), event.getPlacesRestantes(), event.getCapaciteMax()));
        details.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        card.getChildren().addAll(title, details);
        return card;
    }

    private void displayEvents(List<Events> events) {
        if (adminFlowEvents == null) return;

        adminFlowEvents.getChildren().clear();
        for (Events event : events) {
            adminFlowEvents.getChildren().add(createAdminEventCard(event));
        }
    }

    /*private VBox createAdminEventCard(Events event) {
        VBox card = new VBox();
        card.setPrefWidth(280);
        card.setSpacing(12);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-border-color: #DACEB6; -fx-border-radius: 15; -fx-border-width: 1;");

        Label title = new Label(event.getTitre());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #23779C;");
        title.setWrapText(true);

        Label category = new Label(event.getCategorie());
        category.setStyle("-fx-background-color: #DACEB6; -fx-text-fill: #23779C; " +
                "-fx-background-radius: 12; -fx-padding: 3 10; -fx-font-size: 12px;");

        Label details = new Label(String.format("%.0f DT | %d/%d places",
                event.getPrix(), event.getPlacesRestantes(), event.getCapaciteMax()));
        details.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");

        Label location = new Label("📍 " + (event.getLocation() != null ? event.getLocation() : "N/A"));
        location.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateText = "";
        if (event.getDateDebut() != null) {
            dateText = "📅 " + sdf.format(event.getDateDebut());
        }
        Label dates = new Label(dateText);
        dates.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button editBtn = new Button("✏️ Modifier");
        editBtn.setStyle("-fx-background-color: #3D94CA; -fx-text-fill: white; " +
                "-fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand;");
        editBtn.setOnAction(e -> openEditEventForm(event));

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteEvent(event));

        buttonBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(title, category, location, dates, details, buttonBox);
        return card;
    }*/

    private VBox createAdminEventCard(Events event) {
        VBox card = new VBox();
        card.setPrefWidth(280);
        card.setSpacing(12);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-border-color: #DACEB6; -fx-border-radius: 15; -fx-border-width: 1;");

        // Stocker l'ID pour les notifications
        card.getProperties().put("id", event.getId_event());

        Label title = new Label(event.getTitre());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #23779C;");
        title.setWrapText(true);

        Label category = new Label(event.getCategorie());
        category.setStyle("-fx-background-color: #DACEB6; -fx-text-fill: #23779C; " +
                "-fx-background-radius: 12; -fx-padding: 3 10; -fx-font-size: 12px;");

        Label details = new Label(String.format("%.0f DT | %d/%d places",
                event.getPrix(), event.getPlacesRestantes(), event.getCapaciteMax()));
        details.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");

        Label location = new Label("📍 " + (event.getLocation() != null ? event.getLocation() : "N/A"));
        location.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateText = "";
        if (event.getDateDebut() != null) {
            dateText = "📅 " + sdf.format(event.getDateDebut());
        }
        Label dates = new Label(dateText);
        dates.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // ===== BOUTONS AVEC LE NOUVEAU DESIGN =====
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        // Bouton Modifier - Style exact comme dans le CSS que tu as montré
        Button editBtn = new Button("Modifier");
        editBtn.setStyle(
                "-fx-background-color: #fff0f0; " +
                        "-fx-text-fill: #382b22; " +
                        "-fx-font-weight: 600; " +
                        "-fx-padding: 8 15; " +
                        "-fx-border-color: #b18597; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 0.75em; " +
                        "-fx-background-radius: 0.75em; " +
                        "-fx-cursor: hand;"
        );

        // Effet hover
        editBtn.setOnMouseEntered(e ->
                editBtn.setStyle(
                        "-fx-background-color: #ffe9e9; " +
                                "-fx-text-fill: #382b22; " +
                                "-fx-font-weight: 600; " +
                                "-fx-padding: 8 15; " +
                                "-fx-border-color: #b18597; " +
                                "-fx-border-width: 2px; " +
                                "-fx-border-radius: 0.75em; " +
                                "-fx-background-radius: 0.75em; " +
                                "-fx-cursor: hand; " +
                                "-fx-translate-y: -2px;"
                )
        );

        editBtn.setOnMouseExited(e ->
                editBtn.setStyle(
                        "-fx-background-color: #fff0f0; " +
                                "-fx-text-fill: #382b22; " +
                                "-fx-font-weight: 600; " +
                                "-fx-padding: 8 15; " +
                                "-fx-border-color: #b18597; " +
                                "-fx-border-width: 2px; " +
                                "-fx-border-radius: 0.75em; " +
                                "-fx-background-radius: 0.75em; " +
                                "-fx-cursor: hand;"
                )
        );

        editBtn.setOnAction(e -> openEditEventForm(event));

        // Bouton Supprimer - Style adapté pour la suppression
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle(
                "-fx-background-color: #ffe3e3; " +
                        "-fx-text-fill: #8b2c2c; " +
                        "-fx-font-weight: 600; " +
                        "-fx-padding: 8 15; " +
                        "-fx-border-color: #c44b4b; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 0.75em; " +
                        "-fx-background-radius: 0.75em; " +
                        "-fx-cursor: hand;"
        );

        // Effet hover pour suppression
        deleteBtn.setOnMouseEntered(e ->
                deleteBtn.setStyle(
                        "-fx-background-color: #ffd6d6; " +
                                "-fx-text-fill: #8b2c2c; " +
                                "-fx-font-weight: 600; " +
                                "-fx-padding: 8 15; " +
                                "-fx-border-color: #c44b4b; " +
                                "-fx-border-width: 2px; " +
                                "-fx-border-radius: 0.75em; " +
                                "-fx-background-radius: 0.75em; " +
                                "-fx-cursor: hand; " +
                                "-fx-translate-y: -2px;"
                )
        );

        deleteBtn.setOnMouseExited(e ->
                deleteBtn.setStyle(
                        "-fx-background-color: #ffe3e3; " +
                                "-fx-text-fill: #8b2c2c; " +
                                "-fx-font-weight: 600; " +
                                "-fx-padding: 8 15; " +
                                "-fx-border-color: #c44b4b; " +
                                "-fx-border-width: 2px; " +
                                "-fx-border-radius: 0.75em; " +
                                "-fx-background-radius: 0.75em; " +
                                "-fx-cursor: hand;"
                )
        );

        deleteBtn.setOnAction(e -> deleteEvent(event));

        buttonBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(title, category, location, dates, details, buttonBox);
        return card;
    }

    private void displayParticipations(List<Participation> participations) {
        if (participationsFlow == null) return;

        participationsFlow.getChildren().clear();
        for (Participation p : participations) {
            participationsFlow.getChildren().add(createParticipationCard(p));
        }
    }

    private VBox createParticipationCard(Participation p) {
        VBox card = new VBox();
        card.setPrefWidth(300);
        card.setSpacing(12);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-border-color: #3D94CA; -fx-border-radius: 15; -fx-border-width: 1;");

        String eventTitle = "Événement #" + p.getId_event();
        for (Events e : allEvents) {
            if (e.getId_event() == p.getId_event()) {
                eventTitle = e.getTitre();
                break;
            }
        }

        Label eventLabel = new Label("🎫 " + eventTitle);
        eventLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #23779C;");
        eventLabel.setWrapText(true);

        Label placesLabel = new Label("📋 " + p.getNombrePlaces() + " place(s)");
        placesLabel.setStyle("-fx-text-fill: #666;");

        Label totalLabel = new Label(String.format("💰 %.0f DT", p.getMontantTotal()));
        totalLabel.setStyle("-fx-text-fill: #81AE8D; -fx-font-size: 18px; -fx-font-weight: bold;");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label("📅 " + sdf.format(p.getDateParticipation()));
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        Button detailsBtn = new Button("👤 Détails participant");
        detailsBtn.setStyle("-fx-background-color: #3D94CA; -fx-text-fill: white; " +
                "-fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 12px;");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setOnAction(e -> showParticipantDetails(p));

        card.getChildren().addAll(eventLabel, placesLabel, totalLabel, dateLabel, detailsBtn);
        return card;
    }

    private void showParticipantDetails(Participation p) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails du participant");
        dialog.setHeaderText("Informations de réservation");

        ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        String eventTitle = "Événement #" + p.getId_event();
        for (Events e : allEvents) {
            if (e.getId_event() == p.getId_event()) {
                eventTitle = e.getTitre();
                break;
            }
        }

        Label eventInfo = new Label("📌 " + eventTitle);
        eventInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #23779C; -fx-font-size: 16px;");

        Label placesInfo = new Label("📋 Places réservées: " + p.getNombrePlaces());
        placesInfo.setStyle("-fx-text-fill: #666;");

        Label totalInfo = new Label("💰 Montant total: " + String.format("%.0f DT", p.getMontantTotal()));
        totalInfo.setStyle("-fx-text-fill: #81AE8D; -fx-font-size: 16px; -fx-font-weight: bold;");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Label dateInfo = new Label("📅 Date: " + sdf.format(p.getDateParticipation()));
        dateInfo.setStyle("-fx-text-fill: #666;");

        Label noteLabel = new Label("ℹ️ Les détails utilisateur seront disponibles après intégration avec le module Utilisateurs");
        noteLabel.setStyle("-fx-text-fill: #E8B156; -fx-font-size: 11px; -fx-font-style: italic; -fx-wrap-text: true;");

        content.getChildren().addAll(eventInfo, placesInfo, totalInfo, dateInfo, noteLabel);

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void filterEvents(String keyword) {
        try {
            List<Events> filtered = serviceEvent.rechercher(keyword);
            displayEvents(filtered);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterParticipations(String keyword) {
        try {
            List<Participation> filtered = serviceParticipation.rechercher(keyword);
            displayParticipations(filtered);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sortEvents() {
        String selected = adminSortCombo.getValue();
        if (selected == null || allEvents == null) return;

        String column = switch (selected) {
            case "Titre" -> "titre";
            case "Prix" -> "prix";
            case "Date" -> "date_debut";
            case "Places" -> "places_restantes";
            case "Capacité" -> "capacite_max";
            default -> "id_event";
        };

        try {
            List<Events> sorted = serviceEvent.trier(column, "ASC");
            displayEvents(sorted);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdminSearch() {
        String keyword = adminSearchField.getText();
        if (!keyword.isEmpty()) {
            filterEvents(keyword);
        } else {
            displayEvents(allEvents);
        }
    }

    @FXML
    private void handleParticipationSearch() {
        String keyword = participationSearchField.getText();
        if (!keyword.isEmpty()) {
            filterParticipations(keyword);
        } else {
            displayParticipations(allParticipations);
        }
    }

    @FXML
    private void openAddEventForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddEventForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un événement");
            stage.setScene(new Scene(root, 500, 600));
            stage.showAndWait();

            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void openEditEventForm(Events event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditEventForm.fxml"));
            Parent root = loader.load();

            EditEventFormController controller = loader.getController();
            controller.setEvent(event);

            Stage stage = new Stage();
            stage.setTitle("Modifier événement");
            stage.setScene(new Scene(root, 500, 600));
            stage.showAndWait();

            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification");
        }
    }

    private void deleteEvent(Events event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'événement");
        confirm.setContentText("Voulez-vous vraiment supprimer " + event.getTitre() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceEvent.supprimer(event.getId_event());
                loadData();
                showAlert("Succès", "Événement supprimé avec succès");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer l'événement");
            }
        }
    }

    private void openCalendarView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CalendarView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Calendrier des événements - EventHub");
            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/LOGO.png"));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("Logo non trouvé");
            }
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le calendrier");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void refreshDashboard() {
        loadData();
        showAlert("Succès", "Dashboard rafraîchi avec succès !");
    }

    @FXML
    private void reindexer() {
        // Méthode pour réindexer si besoin
        showAlert("Info", "Réindexation effectuée");
    }

    // ==================== NOTIFICATIONS ====================

    private void setupNotificationButton() {
        if (notificationBtn != null) {
            notificationBtn.setOnAction(e -> {
                notificationService.markAllAsRead();
                refreshNotifications();
            });
        }
    }

    private void refreshNotifications() {
        Platform.runLater(() -> {
            // Mettre à jour le badge
            int unread = notificationService.getUnreadCount();
            if (notificationBadge != null) {
                notificationBadge.setText(String.valueOf(unread));
                notificationBadge.setVisible(unread > 0);
            }

            // Mettre à jour la liste
            if (notificationsContainer != null) {
                notificationsContainer.getChildren().clear();

                List<Notification> notifs = notificationService.getNotifications();
                if (notifs.isEmpty()) {
                    Label emptyLabel = new Label("Aucune notification");
                    emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 20;");
                    notificationsContainer.getChildren().add(emptyLabel);
                    return;
                }

                for (Notification notif : notifs) {
                    VBox notifCard = createNotificationCard(notif);
                    notificationsContainer.getChildren().add(notifCard);
                }
            }
        });
    }

    private VBox createNotificationCard(Notification notif) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: " + (notif.isRead() ? "#f5f5f5" : "white") + "; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #DACEB6; -fx-border-radius: 8; -fx-cursor: hand;");
        card.setPrefWidth(280);

        String typeColor = switch (notif.getType()) {
            case SUCCESS -> "#81AE8D";
            case INFO -> "#23779C";
            case WARNING -> "#E8B156";
            case ERROR -> "#e74c3c";
        };

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label typeIndicator = new Label("●");
        typeIndicator.setStyle("-fx-text-fill: " + typeColor + "; -fx-font-size: 14px;");

        Label titleLabel = new Label(notif.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #23779C;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label(notif.getFormattedTime());
        timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 10px;");

        header.getChildren().addAll(typeIndicator, titleLabel, spacer, timeLabel);

        Label messageLabel = new Label(notif.getMessage());
        messageLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        messageLabel.setWrapText(true);

        card.getChildren().addAll(header, messageLabel);

        // Action au clic : focus sur l'élément concerné
        card.setOnMouseClicked(e -> {
            if (!notif.isRead()) {
                notificationService.markAsRead(notif.getId());
            }

            // Basculer vers le bon onglet
            if ("event".equals(notif.getRelatedType())) {
                tabPane.getSelectionModel().select(1); // Onglet Gestion Événements

                // Chercher la carte de l'événement
                Platform.runLater(() -> {
                    for (Node node : adminFlowEvents.getChildren()) {
                        if (node instanceof VBox && node.getProperties().containsKey("id")) {
                            int id = (int) node.getProperties().get("id");
                            if (id == notif.getRelatedId()) {
                                animateFocus(node);
                                break;
                            }
                        }
                    }
                });
            }
        });

        return card;
    }

    private void animateFocus(Node node) {
        // Animation de zoom
        ScaleTransition st = new ScaleTransition(Duration.millis(300), node);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setAutoReverse(true);
        st.setCycleCount(2);

        // Effet de highlight
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(0.2);
        node.setEffect(colorAdjust);

        st.setOnFinished(e -> node.setEffect(null));
        st.play();

        // Scroll jusqu'à l'élément
        ScrollPane scrollPane = (ScrollPane) adminFlowEvents.getParent().getParent();
        double targetY = node.getBoundsInParent().getMinY();
        double height = scrollPane.getViewportBounds().getHeight();

        Timeline scroll = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(scrollPane.vvalueProperty(), targetY / (adminFlowEvents.getHeight() - height))
                )
        );
        scroll.play();
    }

    @FXML
    private void markAllNotificationsRead() {
        notificationService.markAllAsRead();
    }
    private void setupToastListener() {
        notificationService.addToastListener(notif -> {
            Platform.runLater(() -> showNotificationToast(notif));
        });
    }

    private void showNotificationToast(NotificationService.Notification notif) {
        // Créer le toast
        Popup toast = new Popup();
        toast.setAutoHide(true);

        // Couleur selon le type
        String bgColor = switch (notif.getType()) {
            case SUCCESS -> "#81AE8D";
            case INFO -> "#23779C";
            case WARNING -> "#E8B156";
            case ERROR -> "#e74c3c";
        };

        String icon = switch (notif.getType()) {
            case SUCCESS -> "✅";
            case INFO -> "ℹ️";
            case WARNING -> "⚠️";
            case ERROR -> "❌";
        };

        // Contenu du toast
        HBox content = new HBox(10);
        content.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPrefWidth(320);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");

        VBox textBox = new VBox(3);
        Label titleLabel = new Label(notif.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label messageLabel = new Label(notif.getMessage());
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        messageLabel.setWrapText(true);
        textBox.getChildren().addAll(titleLabel, messageLabel);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> toast.hide());

        content.getChildren().addAll(iconLabel, textBox, closeBtn);
        toast.getContent().add(content);

        // Position sous la cloche
        toast.show(notificationBtn.getScene().getWindow());

        double x = notificationBtn.localToScreen(notificationBtn.getBoundsInLocal()).getMinX();
        double y = notificationBtn.localToScreen(notificationBtn.getBoundsInLocal()).getMaxY() + 5;
        toast.setX(x);
        toast.setY(y);

        // Animation d'entrée
        content.setTranslateY(-20);
        content.setOpacity(0);

        Timeline showAnimation = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(content.translateYProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(content.opacityProperty(), 1, Interpolator.EASE_BOTH)
                )
        );
        showAnimation.play();

        // Disparition après 5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> {
                    Timeline hideAnimation = new Timeline(
                            new KeyFrame(Duration.millis(200),
                                    new KeyValue(content.translateYProperty(), -20, Interpolator.EASE_BOTH),
                                    new KeyValue(content.opacityProperty(), 0, Interpolator.EASE_BOTH)
                            )
                    );
                    hideAnimation.setOnFinished(e -> toast.hide());
                    hideAnimation.play();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}