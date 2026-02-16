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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import tn.esprit.entities.Events;
import tn.esprit.services.ServiceEvent;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    @FXML private FlowPane flowEvents;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button allFilterBtn;
    @FXML private Button myEventsBtn;
    @FXML private Button concertFilterBtn;
    @FXML private Button spectacleFilterBtn;
    @FXML private Button conferenceFilterBtn;

    private ServiceEvent serviceEvent;
    private List<Events> allEvents;
    private String currentCategory = "Tous";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();
        sortCombo.getItems().addAll("Titre", "Prix", "Date", "Places");

        setupCategoryFilters();
        myEventsBtn.setOnAction(e -> navigateToMyEvents());
        loadEvents();

        // Search functionality
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.isEmpty()) {
                filterByCategory(currentCategory);
            } else {
                filterEvents(newVal);
            }
        });

        // Sort functionality
        sortCombo.setOnAction(e -> sortEvents());
    }

    private void setupCategoryFilters() {
        allFilterBtn.setOnAction(e -> {
            currentCategory = "Tous";
            loadEvents();
        });

        concertFilterBtn.setOnAction(e -> {
            currentCategory = "Concert";
            filterByCategory("Concert");
        });

        spectacleFilterBtn.setOnAction(e -> {
            currentCategory = "Spectacle";
            filterByCategory("Spectacle");
        });

        conferenceFilterBtn.setOnAction(e -> {
            currentCategory = "Conférence";
            filterByCategory("Conférence");
        });
    }

    private void loadEvents() {
        try {
            allEvents = serviceEvent.afficher();
            displayEvents(allEvents);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterByCategory(String category) {
        try {
            List<Events> filtered = allEvents.stream()
                    .filter(e -> e.getCategorie().equalsIgnoreCase(category))
                    .toList();
            displayEvents(filtered);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterEvents(String keyword) {
        try {
            List<Events> filtered = serviceEvent.rechercher(keyword);
            // Apply category filter if not "Tous"
            if (!currentCategory.equals("Tous")) {
                filtered = filtered.stream()
                        .filter(e -> e.getCategorie().equalsIgnoreCase(currentCategory))
                        .toList();
            }
            displayEvents(filtered);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sortEvents() {
        String selected = sortCombo.getValue();
        if (selected == null || allEvents == null) return;

        String column = switch (selected) {
            case "Titre" -> "titre";
            case "Prix" -> "prix";
            case "Date" -> "date_debut";
            case "Places" -> "places_restantes";
            default -> "id_event";
        };

        try {
            List<Events> sorted = serviceEvent.trier(column, "ASC");
            // Apply category filter
            if (!currentCategory.equals("Tous")) {
                sorted = sorted.stream()
                        .filter(e -> e.getCategorie().equalsIgnoreCase(currentCategory))
                        .toList();
            }
            displayEvents(sorted);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayEvents(List<Events> events) {
        flowEvents.getChildren().clear();
        for (Events event : events) {
            flowEvents.getChildren().add(createEventCard(event));
        }
    }

    private VBox createEventCard(Events event) {
        VBox card = new VBox();
        card.setPrefWidth(320);
        card.setSpacing(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-border-color: #DACEB6; -fx-border-radius: 20; -fx-border-width: 1;");

        // Image container with fixed size and clip
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefWidth(280);
        imageContainer.setPrefHeight(180);
        imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 15;");

        // Create a clip to ensure image fits within rounded corners
        Rectangle clip = new Rectangle(280, 180);
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        imageContainer.setClip(clip);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(false);

        if (event.getImage_url() != null && !event.getImage_url().isEmpty()) {
            try {
                Image image = new Image(event.getImage_url(), 280, 180, false, true);
                imageView.setImage(image);
                imageContainer.getChildren().add(imageView);
            } catch (Exception e) {
                addImagePlaceholder(imageContainer);
            }
        } else {
            addImagePlaceholder(imageContainer);
        }

        // Category badge
        Label category = new Label(event.getCategorie());
        category.setStyle("-fx-background-color: #DACEB6; -fx-text-fill: #23779C; " +
                "-fx-background-radius: 15; -fx-padding: 5 15; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Title
        Label title = new Label(event.getTitre());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #23779C;");
        title.setWrapText(true);

        // Date and location
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateText = "";
        if (event.getDateDebut() != null) {
            dateText = "📅 " + sdf.format(event.getDateDebut());
            if (event.getDateFin() != null) {
                dateText += " - " + sdf.format(event.getDateFin());
            }
        }
        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        Label location = new Label("📍 " + (event.getLocation() != null ? event.getLocation() : "À déterminer"));
        location.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        // Price and availability
        HBox priceBox = new HBox(15);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label(String.format("%.0f DT", event.getPrix()));
        price.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #81AE8D;");

        Label available = new Label(event.getPlacesRestantes() + " places");
        available.setStyle("-fx-text-fill: " + (event.getPlacesRestantes() > 0 ? "#27ae60" : "#e74c3c") +
                "; -fx-font-size: 14px; -fx-font-weight: bold;");

        priceBox.getChildren().addAll(price, available);

        // Progress bar for places
        ProgressBar progressBar = new ProgressBar();
        double progress = 1.0 - ((double) event.getPlacesRestantes() / event.getCapaciteMax());
        progressBar.setProgress(progress);
        progressBar.setPrefWidth(280);
        progressBar.setStyle("-fx-accent: " + (event.getPlacesRestantes() > 0 ? "#81AE8D" : "#e74c3c") + ";");

        HBox progressLabels = new HBox(10);
        progressLabels.setAlignment(Pos.CENTER_LEFT);
        Label filled = new Label((event.getCapaciteMax() - event.getPlacesRestantes()) + " réservé(s)");
        filled.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        Label total = new Label("sur " + event.getCapaciteMax());
        total.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
        progressLabels.getChildren().addAll(filled, total);

        // Details button - now opens a new page instead of showing form in card
        Button detailsBtn = new Button("Voir détails");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setStyle("-fx-background-color: #E8B156; -fx-text-fill: black; -fx-font-size: 16px; " +
                "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 12; -fx-cursor: hand;");

        if (event.getPlacesRestantes() == 0) {
            detailsBtn.setDisable(true);
            detailsBtn.setText("COMPLET");
            detailsBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16px; " +
                    "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 12;");
        } else {
            //detailsBtn.setOnAction(e -> openEventDetails(event));\
            detailsBtn.setOnAction(e -> navigateToEventDetails(event));
        }

        card.getChildren().addAll(imageContainer, category, title, dateLabel, location,
                priceBox, progressBar, progressLabels, detailsBtn);
        return card;
    }

    private void addImagePlaceholder(StackPane container) {
        Label placeholder = new Label("📷");
        placeholder.setStyle("-fx-font-size: 40px; -fx-text-fill: #DACEB6;");
        container.getChildren().add(placeholder);
    }

    private void openEventDetails(Events event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            controller.setEvent(event);

            // REPLACE current scene instead of opening new window
            Stage stage = (Stage) flowEvents.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Détails de l'événement");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails de l'événement");
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
    private void handleSearch() {
        String keyword = searchField.getText();
        if (!keyword.isEmpty()) {
            filterEvents(keyword);
        } else {
            if (currentCategory.equals("Tous")) {
                displayEvents(allEvents);
            } else {
                filterByCategory(currentCategory);
            }
        }
    }

   /* private void navigateToMyEvents() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MyEvents.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) flowEvents.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("EventHub - Mes réservations");
        }catch(Exception e){
            e.printStackTrace();
        }
    }*/
   private void navigateToMyEvents() {
       try{
           FXMLLoader loader = new FXMLLoader(getClass().getResource("/MyEvents.fxml"));
           Parent root = loader.load();
           Stage stage = (Stage) flowEvents.getScene().getWindow();

           // FORCE the size
           Scene scene = new Scene(root, 1200, 700);
           stage.setScene(scene);
           stage.setWidth(1200);
           stage.setHeight(700);
           stage.setTitle("EventHub - Mes réservations");

       }catch(Exception e){
           e.printStackTrace();
       }
   }

    /*private void navigateToEventDetails(Events event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent root = loader.load();
            EventDetailsController controller = loader.getController();
            controller.setEvent(event);

            Stage stage = (Stage) flowEvents.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700)); // This sets full size
            stage.setTitle("Détails de l'événement");
        }catch (Exception e){
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails");
        }
    }*/
    private void navigateToEventDetails(Events event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent root = loader.load();
            EventDetailsController controller = loader.getController();
            controller.setEvent(event);

            Stage stage = (Stage) flowEvents.getScene().getWindow();

            // FORCE the size
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setWidth(1200);
            stage.setHeight(700);
            stage.setTitle("Détails de l'événement");

        }catch (Exception e){
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails");
        }
    }
}