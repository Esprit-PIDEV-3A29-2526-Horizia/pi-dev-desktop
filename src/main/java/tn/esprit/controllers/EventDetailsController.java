package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import tn.esprit.services.ServiceEvent;
import tn.esprit.services.ServiceParticipation;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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

    private Events event;
    private ServiceEvent serviceEvent;
    private ServiceParticipation serviceParticipation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();
        serviceParticipation = new ServiceParticipation();
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

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        String dateText = "";
        if (event.getDateDebut() != null) {
            dateText = sdf.format(event.getDateDebut());
            if (event.getDateFin() != null) {
                dateText += " - " + sdf.format(event.getDateFin());
            }
        }
        datesLabel.setText(dateText);

        // Price and capacity
        priceLabel.setText(String.format("%.0f DT", event.getPrix()));
        availableLabel.setText(String.valueOf(event.getPlacesRestantes()));
        capacityLabel.setText(String.valueOf(event.getCapaciteMax()));

        // Load image
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
                Image image = new Image(event.getImage_url(), 500, 250, true, true);
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
        // Setup spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, event.getPlacesRestantes(), 1);
        placesSpinner.setValueFactory(valueFactory);

        // Update total price when spinner changes
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
        // Validate fields
        if (nomField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir votre nom et email");
            return;
        }

        try {
            int places = placesSpinner.getValue();

            // Create participation
            Participation p = new Participation();
            p.setId_event(event.getId_event());
            p.setNombrePlaces(places);
            p.setMontantTotal((float) (places * event.getPrix()));
            p.setStatut("Confirmée");
            p.setDateParticipation(new Timestamp(System.currentTimeMillis()));

            // Save to database
            serviceParticipation.ajouter(p);

            // Update available places
            int newPlaces = event.getPlacesRestantes() - places;
            serviceEvent.updatePlaces(event.getId_event(), newPlaces);

            showAlert("Succès", "Réservation effectuée avec succès!");

            // Close window
            ((Stage) nomField.getScene().getWindow()).close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la réservation: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        //((Stage) titleLabel.getScene().getWindow()).close();
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/UserHome.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("EventHub - Acceuil");
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
}