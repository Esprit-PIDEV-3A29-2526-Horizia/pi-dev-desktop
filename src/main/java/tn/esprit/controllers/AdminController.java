package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.entities.Events;
import tn.esprit.entities.Participation;
import tn.esprit.services.ServiceEvent;
import tn.esprit.services.ServiceParticipation;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;

public class AdminController implements Initializable {

    @FXML private Label totalEventsLabel;
    @FXML private Label totalPlacesLabel;
    @FXML private Label totalParticipationsLabel;
    @FXML private Label fillRateLabel;
    @FXML private FlowPane recentEventsFlow;

    @FXML private TextField adminSearchField;
    @FXML private ComboBox<String> adminSortCombo;
    @FXML private FlowPane adminFlowEvents;

    @FXML private TextField participationSearchField;
    @FXML private FlowPane participationsFlow;

    @FXML private Button btnDashboard;
    @FXML private Button btnEvents;
    @FXML private Button btnParticipations;
    @FXML private TabPane tabPane;

    private ServiceEvent serviceEvent;
    private ServiceParticipation serviceParticipation;
    private List<Events> allEvents;
    private List<Participation> allParticipations;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();
        serviceParticipation = new ServiceParticipation();

        setupNavigation();
        setupSortCombo();
        loadData();

        adminSearchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) {
                displayEvents(allEvents);
            } else {
                filterEvents(newVal);
            }
        });

        participationSearchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) {
                displayParticipations(allParticipations);
            } else {
                filterParticipations(newVal);
            }
        });

        adminSortCombo.setOnAction(e -> sortEvents());
    }

    private void setupNavigation() {
        btnDashboard.setOnAction(e -> tabPane.getSelectionModel().select(0));
        btnEvents.setOnAction(e -> tabPane.getSelectionModel().select(1));
        btnParticipations.setOnAction(e -> tabPane.getSelectionModel().select(2));

        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, old, newVal) -> {
            updateButtonStyles(newVal.intValue());
        });
    }

    private void updateButtonStyles(int selectedIndex) {
        Button[] buttons = {btnDashboard, btnEvents, btnParticipations};

        for (int i = 0; i < buttons.length; i++) {
            if (i == selectedIndex) {
                buttons[i].setStyle("-fx-background-color: #2d9cdb; -fx-text-fill: white; -fx-font-size:13; -fx-font-weight:800; -fx-background-radius:10; -fx-padding:12 14; -fx-cursor:hand;");
            } else {
                buttons[i].setStyle("-fx-background-color: transparent; -fx-text-fill: #E5E7EB; -fx-font-size:13; -fx-font-weight:800; -fx-background-radius:10; -fx-padding:12 14; -fx-cursor:hand; -fx-border-color:#3a4758; -fx-border-radius:10; -fx-border-width:1;");
            }
        }
    }

    private void setupSortCombo() {
        adminSortCombo.getItems().addAll("Titre", "Prix", "Date", "Places", "Capacité");
    }

    private void loadData() {
        try {
            allEvents = serviceEvent.afficher();
            allParticipations = serviceParticipation.afficher();

            System.out.println("✅ Données chargées: " + allEvents.size() + " événements, " + allParticipations.size() + " participations");

            updateStatistics();
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
        int totalRevenue = 0;

        for (Events event : allEvents) {
            totalPlaces += event.getCapaciteMax();
            int booked = event.getCapaciteMax() - event.getPlacesRestantes();
            totalBooked += booked;
            totalRevenue += booked * event.getPrix();
        }

        totalEventsLabel.setText(String.valueOf(totalEvents));
        totalPlacesLabel.setText(String.valueOf(totalPlaces));
        totalParticipationsLabel.setText(String.valueOf(allParticipations.size()));

        int fillRate = totalPlaces > 0 ? (totalBooked * 100 / totalPlaces) : 0;
        fillRateLabel.setText(fillRate + "%");

        // Optional: Add revenue stat
        // revenueLabel.setText(String.format("%.0f DT", totalRevenue));
    }

    private void displayRecentEvents() {
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
        adminFlowEvents.getChildren().clear();
        for (Events event : events) {
            adminFlowEvents.getChildren().add(createAdminEventCard(event));
        }
    }

    private VBox createAdminEventCard(Events event) {
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
    }

    private void displayParticipations(List<Participation> participations) {
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

        //Label userIdLabel = new Label("🆔 ID Utilisateur: " + p.getId_utilisateur());
        //userIdLabel.setStyle("-fx-text-fill: #666;");

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}