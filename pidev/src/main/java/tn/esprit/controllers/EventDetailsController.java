package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import tn.esprit.entities.Events;
import tn.esprit.entities.Participation;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceEvent;
import tn.esprit.services.ServiceParticipation;
import tn.esprit.services.WeatherService;
import tn.esprit.services.WeatherService.WeatherInfo;
import tn.esprit.services.LastFmService;
import tn.esprit.services.LastFmService.ArtistInfo;
import tn.esprit.services.LastFmService.TrackInfo;
import tn.esprit.services.RecommandationService;
import tn.esprit.utils.EmailService;
import tn.esprit.utils.SessionManager;

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

    @FXML private VBox weatherBox;
    @FXML private Label weatherLabel;
    @FXML private ImageView weatherIcon;
    @FXML private Label tempLabel;
    @FXML private Label humidityLabel;
    @FXML private Label windLabel;

    @FXML private TabPane detailsTabPane;
    @FXML private Tab artistTab;
    @FXML private VBox artistInfoBox;
    @FXML private Label artistNameLabel;
    @FXML private ImageView artistImageView;
    @FXML private Label artistBioLabel;
    @FXML private Label artistListenersLabel;
    @FXML private FlowPane artistTagsBox;
    @FXML private VBox topTracksBox;
    @FXML private Hyperlink artistLink;

    private LastFmService lastFmService;
    private WeatherService weatherService;
    private RecommandationService recommandationService;
    private List<Events> allEvents;
    private Events event;
    private ServiceEvent serviceEvent;
    private ServiceParticipation serviceParticipation;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();
        serviceParticipation = new ServiceParticipation();
        recommandationService = new RecommandationService();
        weatherService = new WeatherService();
        lastFmService = new LastFmService();

        currentUser = SessionManager.getCurrentUser();
        loadAllEvents();
    }

    public void setEvent(Events event) {
        this.event = event;
        displayEventDetails();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("✅ Utilisateur reçu dans EventDetailsController: " +
                (user != null ? user.getEmail() : "null"));
    }

    private void displayEventDetails() {
        if (event == null) return;

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

        if (event.getCategorie().equalsIgnoreCase("Concert") ||
                event.getCategorie().equalsIgnoreCase("Festival")) {

            String artistName = extractArtistName(event.getTitre());
            System.out.println("🎤 Event title: " + event.getTitre());
            System.out.println("🎤 Extracted artist: " + artistName);

            loadArtistInfo(artistName);
        } else {
            artistTab.setDisable(true);
        }

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
        if (event != null && event.getImage_url() != null && !event.getImage_url().isEmpty()) {
            try {
                Image image = new Image(event.getImage_url(), 1100, 300, true, true);
                eventImage.setImage(image);
                imageContainer.getChildren().setAll(eventImage);
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
        if (currentUser != null) {
            nomField.setText(currentUser.getNom() + " " + currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
        }

        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, event.getPlacesRestantes(), 1);
        placesSpinner.setValueFactory(valueFactory);

        placesSpinner.valueProperty().addListener((obs, old, val) -> updateTotalPrice());
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
            p.setUser_id(currentUser != null ? currentUser.getId() : 0);
            p.setNombre_places(places);
            p.setMontant_total(places * event.getPrix());
            p.setStatut("Confirmée");
            p.setDate_participation(new Timestamp(System.currentTimeMillis()));
            p.setEmail_snapshot(emailField.getText());
            p.setNom_snapshot(currentUser != null ? currentUser.getNom() : "");
            p.setPrenom_snapshot(currentUser != null ? currentUser.getPrenom() : "");
            p.setTelephone_snapshot(telephoneField.getText());

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserEvenements.fxml"));
            Parent eventsView = loader.load();

            UserController eventsController = loader.getController();
            if (eventsController != null && currentUser != null) {
                eventsController.setCurrentUser(currentUser);
            }

            BorderPane mainPane = (BorderPane) titleLabel.getScene().getRoot();
            mainPane.setCenter(eventsView);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de revenir à la liste: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
            if (currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            BorderPane mainPane = (BorderPane) titleLabel.getScene().getRoot();
            mainPane.setCenter(root);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'événement");
        }
    }

    private void loadWeather(String location) {
        if (location == null || location.isEmpty() || location.equals("Lieu non spécifié")) {
            weatherBox.setVisible(false);
            return;
        }

        String city = location.split(",")[0].trim();

        new Thread(() -> {
            try {
                WeatherInfo weather = weatherService.getWeatherForCity(city);

                javafx.application.Platform.runLater(() -> {
                    try {
                        weatherLabel.setText(weather.getDescription());
                        tempLabel.setText(weather.getFormattedTemp());
                        humidityLabel.setText("💧 " + weather.getHumidity() + "%");
                        windLabel.setText("💨 " + String.format("%.0f km/h", weather.getWindSpeed() * 3.6));

                        Image icon = new Image(weather.getIconUrl(), 50, 50, true, true);
                        weatherIcon.setImage(icon);

                        weatherBox.setVisible(true);
                        weatherBox.setManaged(true);

                    } catch (Exception e) {
                        weatherBox.setVisible(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error in weather API thread: " + e.getMessage());
            }
        }).start();
    }

    private void loadArtistInfo(String artistName) {
        if (artistName == null || artistName.isEmpty()) {
            artistTab.setDisable(true);
            return;
        }

        artistTab.setDisable(false);

        new Thread(() -> {
            ArtistInfo artist = lastFmService.getArtistInfo(artistName);
            List<TrackInfo> topTracks = lastFmService.getTopTracks(artistName, 5);

            javafx.application.Platform.runLater(() -> {
                try {
                    artistNameLabel.setText(artist.getName());
                    artistBioLabel.setText(artist.getBio());
                    artistListenersLabel.setText("👥 " + artist.getFormattedListeners() + " auditeurs");
                    artistLink.setText("Voir sur Last.fm");
                    artistLink.setOnAction(e -> {
                        try {
                            java.awt.Desktop.getDesktop().browse(new java.net.URI(artist.getUrl()));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });

                    updateArtistTags(artist.getTags());

                    if (artist.getImageUrl() != null && !artist.getImageUrl().isEmpty()) {
                        Image image = new Image(artist.getImageUrl(), 150, 150, true, true);
                        artistImageView.setImage(image);
                    }

                    topTracksBox.getChildren().clear();
                    int rank = 1;
                    for (TrackInfo track : topTracks) {
                        HBox trackBox = new HBox(10);
                        trackBox.setAlignment(Pos.CENTER_LEFT);

                        Label rankLabel = new Label(rank + ".");
                        rankLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #23779C; -fx-min-width: 25;");

                        Hyperlink trackLink = new Hyperlink(track.getName());
                        trackLink.setOnAction(e -> {
                            try {
                                java.awt.Desktop.getDesktop().browse(new java.net.URI(track.getUrl()));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });

                        Label playsLabel = new Label("(" + track.getPlayCount() + " écoutes)");
                        playsLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

                        trackBox.getChildren().addAll(rankLabel, trackLink, playsLabel);
                        topTracksBox.getChildren().add(trackBox);
                        rank++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    private void updateArtistTags(List<String> tags) {
        artistTagsBox.getChildren().clear();
        if (tags == null || tags.isEmpty()) {
            Label noTags = new Label("Aucun genre disponible");
            noTags.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            artistTagsBox.getChildren().add(noTags);
            return;
        }

        for (String tag : tags) {
            Label tagLabel = new Label("#" + tag);
            tagLabel.setStyle("-fx-background-color: #DACEB6; -fx-text-fill: #23779C; " +
                    "-fx-background-radius: 15; -fx-padding: 5 12; -fx-font-size: 12px;");
            artistTagsBox.getChildren().add(tagLabel);
        }
    }

    private String extractArtistName(String eventTitle) {
        if (eventTitle == null || eventTitle.isEmpty()) return "";

        String artist = eventTitle
                .replaceAll("(?i)concert", "")
                .replaceAll("(?i)live", "")
                .replaceAll("(?i)in concert", "")
                .replaceAll("(?i)at", "")
                .replaceAll("(?i)festival", "")
                .replaceAll("(?i)tour", "")
                .replaceAll("(?i)show", "")
                .replaceAll("(?i)performance", "")
                .replaceAll("(?i)feat\\.", "")
                .replaceAll("(?i)featuring", "")
                .replaceAll("-", "")
                .trim();

        return artist;
    }
}