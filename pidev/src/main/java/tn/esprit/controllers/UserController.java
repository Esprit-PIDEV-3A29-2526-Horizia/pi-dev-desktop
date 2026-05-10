package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import tn.esprit.entities.Events;
import tn.esprit.entities.User;
import tn.esprit.services.RefreshService;
import tn.esprit.services.ServiceEvent;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserController implements Initializable {

    // Navbar partagée
    @FXML private NavbarController navbarController;

    @FXML private FlowPane flowEvents;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button allFilterBtn;
    @FXML private Button concertFilterBtn;
    @FXML private Button spectacleFilterBtn;
    @FXML private Button conferenceFilterBtn;
    @FXML private Button btnRetour;

    private ServiceEvent serviceEvent;
    private List<Events> allEvents;
    private String currentCategory = "Tous";
    private Timer timer = new Timer(true);
    private TimerTask searchTask;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Récupérer l'utilisateur connecté
        currentUser = SessionManager.getCurrentUser();

        // Mettre à jour la navbar
        if (navbarController != null) {
            navbarController.updateUserInfo();
            // Activer le bouton "Événements" dans la navbar
            navbarController.setActiveEvenements();
        }

        System.out.println("🔍 UserController - Utilisateur: " +
                (currentUser != null ? currentUser.getEmail() : "null"));

        serviceEvent = new ServiceEvent();
        sortCombo.getItems().addAll("Titre", "Prix", "Date", "Places");

        setupCategoryFilters();

        loadEvents();

        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (searchTask != null) {
                searchTask.cancel();
            }

            searchTask = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (newVal.isEmpty()) {
                            filterByCategory(currentCategory);
                        } else {
                            filterEvents(newVal);
                        }
                    });
                }
            };
            timer.schedule(searchTask, 300);
        });

        sortCombo.setOnAction(e -> sortEvents());

        RefreshService.setUserController(this);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (navbarController != null) {
            navbarController.updateUserInfo();
        }
        System.out.println("👤 Utilisateur reçu dans UserController: " +
                (user != null ? user.getEmail() : "non connecté"));
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
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage());
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
        if (flowEvents == null) return;

        flowEvents.getChildren().clear();
        if (events != null && !events.isEmpty()) {
            for (Events event : events) {
                flowEvents.getChildren().add(createEventCard(event));
            }
        } else {
            Label emptyLabel = new Label("Aucun événement trouvé");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16px;");
            flowEvents.getChildren().add(emptyLabel);
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

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefWidth(280);
        imageContainer.setPrefHeight(180);
        imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 15;");

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

        Label category = new Label(event.getCategorie());
        category.setStyle("-fx-background-color: #DACEB6; -fx-text-fill: #23779C; " +
                "-fx-background-radius: 15; -fx-padding: 5 15; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label title = new Label(event.getTitre());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #23779C;");
        title.setWrapText(true);

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

        HBox priceBox = new HBox(15);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label(String.format("%.0f DT", event.getPrix()));
        price.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #81AE8D;");

        Label available = new Label(event.getPlacesRestantes() + " places");
        available.setStyle("-fx-text-fill: " + (event.getPlacesRestantes() > 0 ? "#27ae60" : "#e74c3c") +
                "; -fx-font-size: 14px; -fx-font-weight: bold;");

        priceBox.getChildren().addAll(price, available);

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

    private void navigateToEventDetails(Events event) {
        try {
            System.out.println("Tentative de chargement de EventDetails.fxml");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            controller.setEvent(event);
            if (currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            BorderPane mainPane = (BorderPane) flowEvents.getScene().getRoot();
            mainPane.setCenter(root);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refreshEvents(){
        loadEvents();
    }
}