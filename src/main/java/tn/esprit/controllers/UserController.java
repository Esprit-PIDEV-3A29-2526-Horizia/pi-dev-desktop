package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(180);
        imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 15;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        if (event.getImage_url() != null && !event.getImage_url().isEmpty()) {
            try {
                Image image = new Image(event.getImage_url(), true);
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

        // Book button
        Button bookBtn = new Button("Réserver");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setStyle("-fx-background-color: #E8B156; -fx-text-fill: black; -fx-font-size: 16px; " +
                "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 12; -fx-cursor: hand;");

        if (event.getPlacesRestantes() == 0) {
            bookBtn.setDisable(true);
            bookBtn.setText("COMPLET");
            bookBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16px; " +
                    "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 12;");
        } else {
            bookBtn.setOnAction(e -> showBookingForm(event, card));
        }

        card.getChildren().addAll(imageContainer, category, title, dateLabel, location,
                priceBox, progressBar, progressLabels, bookBtn);
        return card;
    }

    private void addImagePlaceholder(StackPane container) {
        Label placeholder = new Label("📷");
        placeholder.setStyle("-fx-font-size: 40px; -fx-text-fill: #DACEB6;");
        container.getChildren().add(placeholder);
    }

    private void showBookingForm(Events event, VBox card) {
        // Remove any existing form
        if (card.getChildren().size() > 9) {
            card.getChildren().remove(9, card.getChildren().size());
        }

        // Create booking form directly in the card
        VBox form = new VBox(15);
        form.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 15; -fx-padding: 20; -fx-margin: 10 0 0 0;");

        Label formTitle = new Label("Réservation");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #23779C;");

        // Form fields
        TextField nomField = new TextField();
        nomField.setPromptText("Votre nom complet");
        nomField.setStyle("-fx-background-radius: 10; -fx-padding: 10;");

        TextField emailField = new TextField();
        emailField.setPromptText("Votre email");
        emailField.setStyle("-fx-background-radius: 10; -fx-padding: 10;");

        // Number of places
        HBox placesBox = new HBox(10);
        placesBox.setAlignment(Pos.CENTER_LEFT);
        Label placesLabel = new Label("Places:");
        Spinner<Integer> placesSpinner = new Spinner<>(1, event.getPlacesRestantes(), 1);
        placesSpinner.setPrefWidth(80);
        placesBox.getChildren().addAll(placesLabel, placesSpinner);

        // Total price
        Label totalPrice = new Label("Total: " + event.getPrix() + " DT");
        totalPrice.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #81AE8D;");

        placesSpinner.valueProperty().addListener((obs, old, val) -> {
            totalPrice.setText("Total: " + (val * event.getPrix()) + " DT");
        });

        // Confirm button
        Button confirmBtn = new Button("Confirmer");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setStyle("-fx-background-color: #81AE8D; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 10; -fx-cursor: hand;");

        confirmBtn.setOnAction(e -> {
            if (nomField.getText().isEmpty() || emailField.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs");
                return;
            }

            try {
                // Update places in database
                int newPlaces = event.getPlacesRestantes() - placesSpinner.getValue();
                serviceEvent.updatePlaces(event.getId_event(), newPlaces);
                event.setPlacesRestantes(newPlaces);

                showAlert("Succès", "Réservation effectuée!");

                // Refresh display
                flowEvents.getChildren().clear();
                loadEvents();

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Erreur", "Erreur lors de la réservation");
            }
        });

        form.getChildren().addAll(formTitle, nomField, emailField, placesBox, totalPrice, confirmBtn);
        card.getChildren().add(form);
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
}