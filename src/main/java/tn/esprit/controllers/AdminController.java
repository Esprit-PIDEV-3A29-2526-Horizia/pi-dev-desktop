package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.entities.Events;
import tn.esprit.entities.Participation;
import tn.esprit.services.ServiceEvent;
import tn.esprit.services.ServiceParticipation;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;

public class AdminController implements Initializable {

    // Dashboard Tab
    @FXML private Label totalEventsLabel;
    @FXML private Label totalPlacesLabel;
    @FXML private Label totalParticipationsLabel;
    @FXML private Label fillRateLabel;
    @FXML private TableView<Events> recentActivityTable;

    // Events Management Tab
    @FXML private TextField adminSearchField;
    @FXML private ComboBox<String> adminSortCombo;
    @FXML private FlowPane adminFlowEvents;

    // Participations Tab
    @FXML private TableView<Participation> participationsTable;

    // Navigation Buttons
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
        setupTables();
        loadData();

        // Search functionality
        adminSearchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) {
                displayEvents(allEvents);
            } else {
                filterEvents(newVal);
            }
        });

        // Sort functionality
        adminSortCombo.setOnAction(e -> sortEvents());
    }

    private void setupNavigation() {
        btnDashboard.setOnAction(e -> tabPane.getSelectionModel().select(0));
        btnEvents.setOnAction(e -> tabPane.getSelectionModel().select(1));
        btnParticipations.setOnAction(e -> tabPane.getSelectionModel().select(2));

        // Style for active/inactive states
        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, old, newVal) -> {
            updateButtonStyles(newVal.intValue());
        });
    }

    private void updateButtonStyles(int selectedIndex) {
        Button[] buttons = {btnDashboard, btnEvents, btnParticipations};
        String[] colors = {"#2d9cdb", "transparent", "transparent"};

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

    private void setupTables() {
        // Recent Activity Table
        TableColumn<Events, String> titleCol = new TableColumn<>("Titre");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        titleCol.setPrefWidth(200);

        TableColumn<Events, String> categoryCol = new TableColumn<>("Catégorie");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        categoryCol.setPrefWidth(100);

        TableColumn<Events, Integer> placesCol = new TableColumn<>("Places restantes");
        placesCol.setCellValueFactory(new PropertyValueFactory<>("placesRestantes"));
        placesCol.setPrefWidth(120);

        TableColumn<Events, Float> priceCol = new TableColumn<>("Prix");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        priceCol.setPrefWidth(80);

        recentActivityTable.getColumns().addAll(titleCol, categoryCol, placesCol, priceCol);

        // Participations Table
        TableColumn<Participation, Integer> idCol = new TableColumn<>("ID Événement");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id_event"));
        idCol.setPrefWidth(100);

        TableColumn<Participation, Integer> placesBookedCol = new TableColumn<>("Places");
        placesBookedCol.setCellValueFactory(new PropertyValueFactory<>("nombrePlaces"));
        placesBookedCol.setPrefWidth(80);

        TableColumn<Participation, Float> totalCol = new TableColumn<>("Montant");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        totalCol.setPrefWidth(100);

        TableColumn<Participation, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statusCol.setPrefWidth(100);

        TableColumn<Participation, Timestamp> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateParticipation"));
        dateCol.setPrefWidth(150);

        participationsTable.getColumns().addAll(idCol, placesBookedCol, totalCol, statusCol, dateCol);
    }

    private void loadData() {
        try {
            allEvents = serviceEvent.afficher();
            allParticipations = serviceParticipation.afficher();

            updateStatistics();
            displayEvents(allEvents);
            displayParticipations(allParticipations);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les données");
        }
    }

    private void updateStatistics() {
        int totalEvents = allEvents.size();
        int totalPlaces = 0;
        int totalBooked = 0;

        for (Events event : allEvents) {
            totalPlaces += event.getCapaciteMax();
            totalBooked += (event.getCapaciteMax() - event.getPlacesRestantes());
        }

        totalEventsLabel.setText(String.valueOf(totalEvents));
        totalPlacesLabel.setText(String.valueOf(totalPlaces));
        totalParticipationsLabel.setText(String.valueOf(allParticipations.size()));

        int fillRate = totalPlaces > 0 ? (totalBooked * 100 / totalPlaces) : 0;
        fillRateLabel.setText(fillRate + "%");

        // Show recent events in table
        recentActivityTable.getItems().clear();
        recentActivityTable.getItems().addAll(allEvents.stream().limit(5).toList());
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

        // Title
        Label title = new Label(event.getTitre());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #23779C;");
        title.setWrapText(true);

        // Category
        Label category = new Label(event.getCategorie());
        category.setStyle("-fx-background-color: #DACEB6; -fx-text-fill: #23779C; " +
                "-fx-background-radius: 12; -fx-padding: 3 10; -fx-font-size: 12px;");

        // Details (no IDs shown)
        Label details = new Label(String.format("%.0f DT | %d/%d places",
                event.getPrix(), event.getPlacesRestantes(), event.getCapaciteMax()));
        details.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");

        // Location
        Label location = new Label("📍 " + (event.getLocation() != null ? event.getLocation() : "N/A"));
        location.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // Dates
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateText = "";
        if (event.getDateDebut() != null) {
            dateText = "📅 " + sdf.format(event.getDateDebut());
        }
        Label dates = new Label(dateText);
        dates.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // Action Buttons
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
        participationsTable.getItems().clear();
        participationsTable.getItems().addAll(participations);
    }

    private void filterEvents(String keyword) {
        try {
            List<Events> filtered = serviceEvent.rechercher(keyword);
            displayEvents(filtered);
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
    private void openAddEventForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddEventForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un événement");
            stage.setScene(new Scene(root, 500, 600));
            stage.showAndWait();

            // Refresh after adding
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEditEventForm(Events event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditEventForm.fxml"));
            Parent root = loader.load();

            // Pass event to edit form
            EditEventFormController controller = loader.getController();
            controller.setEvent(event);

            Stage stage = new Stage();
            stage.setTitle("Modifier événement");
            stage.setScene(new Scene(root, 500, 600));
            stage.showAndWait();

            // Refresh after editing
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
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