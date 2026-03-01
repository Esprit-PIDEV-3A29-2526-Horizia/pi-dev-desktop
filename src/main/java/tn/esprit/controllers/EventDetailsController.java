package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Events;
import tn.esprit.entities.Participation;
import tn.esprit.services.EmailService;
import tn.esprit.services.ServiceEvent;
import tn.esprit.services.ServiceParticipation;

//weather
import tn.esprit.services.WeatherService;
import tn.esprit.services.WeatherService.WeatherInfo;


import tn.esprit.services.RecommandationService;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EventDetailsController implements Initializable {

    @FXML private StackPane imageContainer;
    @FXML private ImageView eventImage;
    @FXML private Label categoryLabel;
    @FXML private Label titleLabel;
    @FXML private Label locationLabel;
    @FXML private Label datesLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priceLabel;
    @FXML private Label availableLabel;
    @FXML private Label capacityLabel;

    @FXML private VBox bookingForm;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private Spinner<Integer> placesSpinner;
    @FXML private Label totalPriceLabel;

    @FXML private VBox fullMessage;

    @FXML private VBox recommandationsBox;
    @FXML private FlowPane recommandationsFlow;
    @FXML private ToggleGroup niveauRecommandation;
    @FXML private RadioButton radioSimple;
    @FXML private RadioButton radioSimilarite;
    @FXML private RadioButton radioCollaboratif;

    //weather
    @FXML private VBox weatherBox;
    @FXML private Label weatherLabel;
    @FXML private ImageView weatherIcon;
    @FXML private Label tempLabel;
    @FXML private Label humidityLabel;
    @FXML private Label windLabel;

    private WeatherService weatherService;

    private RecommandationService recommandationService;
    private List<Events> allEvents;

    private Events event;
    private ServiceEvent serviceEvent;
    private ServiceParticipation serviceParticipation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();
        serviceParticipation = new ServiceParticipation();

        recommandationService = new RecommandationService();
        loadAllEvents();

        //weather
        weatherService = new WeatherService();
    }

    public void setEvent(Events event) {
        this.event = event;
        displayEventDetails();
    }

    private void displayEventDetails() {
        titleLabel.setText(event.getTitre());
        categoryLabel.setText(event.getCategorie());
        descriptionLabel.setText(event.getDescription() != null ? event.getDescription() : "Aucune description disponible");
        locationLabel.setText(event.getLocation() != null ? event.getLocation() : "Lieu non spécifié");
        loadWeather(event.getLocation());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        String dateText = "";
        if (event.getDateDebut() != null) {
            dateText = sdf.format(event.getDateDebut());
            if (event.getDateFin() != null) {
                dateText += " - " + sdf.format(event.getDateFin());
            }
        }
        datesLabel.setText(dateText);

        priceLabel.setText(String.format("%.0f DT", event.getPrix()));
        availableLabel.setText(String.valueOf(event.getPlacesRestantes()));
        capacityLabel.setText(String.valueOf(event.getCapaciteMax()));

        loadEventImage();

        if (event.getPlacesRestantes() > 0) {
            setupBookingForm();
            bookingForm.setVisible(true);
            bookingForm.setManaged(true);
            fullMessage.setVisible(false);
            fullMessage.setManaged(false);
        } else {
            bookingForm.setVisible(false);
            bookingForm.setManaged(false);
            fullMessage.setVisible(true);
            fullMessage.setManaged(true);
        }
    }

    private void loadEventImage() {
        if (event.getImage_url() != null && !event.getImage_url().isEmpty()) {
            try {
                Image image = new Image(event.getImage_url(), 1100, 300, true, true);
                eventImage.setImage(image);
            } catch (Exception e) {
                showImagePlaceholder();
            }
        } else {
            showImagePlaceholder();
        }
    }

    private void showImagePlaceholder() {
        Label placeholder = new Label("📷");
        placeholder.setStyle("-fx-font-size: 60px; -fx-text-fill: #DACEB6;");
        imageContainer.getChildren().clear();
        imageContainer.getChildren().add(placeholder);
    }

    private void setupBookingForm() {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, event.getPlacesRestantes(), 1);
        placesSpinner.setValueFactory(valueFactory);

        placesSpinner.valueProperty().addListener((obs, old, val) -> {
            updateTotalPrice();
        });

        updateTotalPrice();
    }

    private void updateTotalPrice() {
        int places = placesSpinner.getValue();
        double total = places * event.getPrix();
        totalPriceLabel.setText(String.format("%.0f DT", total));
    }

    @FXML
    private void handleConfirmBooking() {
        if (nomField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir votre nom et email");
            return;
        }

        try {
            int places = placesSpinner.getValue();

            Participation p = new Participation();
            p.setId_event(event.getId_event());
            p.setNombrePlaces(places);
            p.setMontantTotal((float) (places * event.getPrix()));
            p.setStatut("Confirmée");
            p.setDateParticipation(new Timestamp(System.currentTimeMillis()));

            serviceParticipation.ajouter(p);

            int newPlaces = event.getPlacesRestantes() - places;
            serviceEvent.updatePlaces(event.getId_event(), newPlaces);

            try {
                EmailService.sendConfirmationEmail(
                        emailField.getText(),
                        nomField.getText(),
                        event,
                        p
                );
                showAlert("Succès", "Réservation effectuée avec succès!\nUn email de confirmation vous a été envoyé.");
            } catch (Exception e) {
                showAlert("Succès", "Réservation effectuée avec succès!");
            }

            handleBack();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la réservation: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/UserHome.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("EventHub - Accueil");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //recommendation
    private void loadAllEvents() {
        try {
            allEvents = serviceEvent.afficher();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRecommandation() {
        if (allEvents == null) return;

        List<Events> recommandations = new ArrayList<>();

        if (radioSimple.isSelected()) {
            recommandations = recommandationService.recommanderParCategorie(event, allEvents);
        } else if (radioSimilarite.isSelected()) {
            recommandations = recommandationService.recommanderParSimilarite(event, allEvents);
        } else if (radioCollaboratif.isSelected()) {
            recommandations = recommandationService.recommenderCollaboratif(event, allEvents);
        }

        afficherRecommandations(recommandations);
    }

    private void afficherRecommandations(List<Events> recommandations) {
        recommandationsFlow.getChildren().clear();

        if (recommandations.isEmpty()) {
            Label noRec = new Label("Aucune recommandation disponible");
            noRec.setStyle("-fx-text-fill: #666; -fx-padding: 20;");
            recommandationsFlow.getChildren().add(noRec);
            return;
        }

        for (Events rec : recommandations) {
            VBox recCard = createRecommandationCard(rec);
            recommandationsFlow.getChildren().add(recCard);
        }
    }

    private VBox createRecommandationCard(Events rec) {
        VBox card = new VBox();
        card.setPrefWidth(200);
        card.setSpacing(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-border-color: #DACEB6; -fx-border-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");

        Label title = new Label(rec.getTitre());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #23779C;");
        title.setWrapText(true);

        Label category = new Label("🎫 " + rec.getCategorie());
        category.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        Label price = new Label(String.format("%.0f DT", rec.getPrix()));
        price.setStyle("-fx-text-fill: #81AE8D; -fx-font-weight: bold;");

        Button voirBtn = new Button("Voir");
        voirBtn.setStyle("-fx-background-color: #E8B156; -fx-text-fill: black; " +
                "-fx-padding: 5 15; -fx-background-radius: 8; -fx-cursor: hand;");
        voirBtn.setOnAction(e -> navigateToEventDetails(rec));

        card.getChildren().addAll(title, category, price, voirBtn);
        return card;
    }

    private void navigateToEventDetails(Events event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            controller.setEvent(event);

            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Détails de l'événement - " + event.getTitre());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'événement");
        }
    }



    //weather api
    // In loadWeather method, add these debug lines:

    private void loadWeather(String location) {
        System.out.println("Loading weather for location: " + location); // DEBUG

        if (location == null || location.isEmpty() || location.equals("Lieu non spécifié")) {
            System.out.println("Location invalid, hiding weather box"); // DEBUG
            weatherBox.setVisible(false);
            return;
        }

        // Extraire la ville (prendre la première partie avant la virgule)
        String city = location.split(",")[0].trim();
        System.out.println("Extracted city: " + city); // DEBUG

        new Thread(() -> {
            try {
                System.out.println("Calling weather API for: " + city); // DEBUG
                WeatherInfo weather = weatherService.getWeatherForCity(city);
                System.out.println("Weather API response received: " + weather); // DEBUG

                javafx.application.Platform.runLater(() -> {
                    try {
                        // Afficher les infos
                        weatherLabel.setText(weather.getDescription());
                        tempLabel.setText(weather.getFormattedTemp());
                        humidityLabel.setText("💧 " + weather.getHumidity() + "%");
                        windLabel.setText("💨 " + String.format("%.0f km/h", weather.getWindSpeed() * 3.6));

                        // Charger l'icône
                        String iconUrl = weather.getIconUrl();
                        System.out.println("Loading icon from: " + iconUrl); // DEBUG
                        Image icon = new Image(iconUrl, 50, 50, true, true);
                        weatherIcon.setImage(icon);

                        weatherBox.setVisible(true);
                        weatherBox.setManaged(true);
                        System.out.println("Weather box should now be visible"); // DEBUG

                    } catch (Exception e) {
                        System.err.println("Error updating UI with weather data:");
                        e.printStackTrace();
                        weatherBox.setVisible(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error in weather API thread:");
                e.printStackTrace();
            }
        }).start();
    }

}