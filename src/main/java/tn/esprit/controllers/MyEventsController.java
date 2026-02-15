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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MyEventsController implements Initializable {

    @FXML private FlowPane myEventsFlow;
    @FXML private Button homeBtn;
    @FXML private Button eventsBtn;
    @FXML private Button myEventsBtn;

    private ServiceEvent serviceEvent;
    private ServiceParticipation serviceParticipation;
    private List<Participation> allParticipations;
    private List<Events> allEvents;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();
        serviceParticipation = new ServiceParticipation();

        setupNavigation();
        loadData();
    }

    private void setupNavigation() {
        homeBtn.setOnAction(e -> navigateTo("/UserHome.fxml", "EventHub - Accueil"));
        eventsBtn.setOnAction(e -> navigateTo("/UserHome.fxml", "EventHub - Accueil"));
        // myEventsBtn is current page
    }

    private void navigateTo(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) homeBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            allEvents = serviceEvent.afficher();
            allParticipations = serviceParticipation.afficher();

            displayMyEvents();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayMyEvents() {
        myEventsFlow.getChildren().clear();

        if (allParticipations.isEmpty()) {
            Label noEvents = new Label("Vous n'avez aucune réservation");
            noEvents.setStyle("-fx-font-size: 18px; -fx-text-fill: #666; -fx-padding: 50;");
            myEventsFlow.getChildren().add(noEvents);
            return;
        }

        for (Participation p : allParticipations) {
            // For now, show ALL participations
            // Later, you'll filter by user ID: if (p.getId_utilisateur() == currentUserId)
            Events event = findEventById(p.getId_event());
            if (event != null) {
                myEventsFlow.getChildren().add(createMyEventCard(event, p));
            }
        }
    }

    private Events findEventById(int id) {
        for (Events e : allEvents) {
            if (e.getId_event() == id) {
                return e;
            }
        }
        return null;
    }

    private VBox createMyEventCard(Events event, Participation participation) {
        VBox card = new VBox();
        card.setPrefWidth(320);
        card.setSpacing(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-border-color: #81AE8D; -fx-border-radius: 20; -fx-border-width: 2;");

        // Event title
        Label title = new Label(event.getTitre());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #23779C;");
        title.setWrapText(true);

        // Event category
        Label category = new Label(event.getCategorie());
        category.setStyle("-fx-background-color: #DACEB6; -fx-text-fill: #23779C; " +
                "-fx-background-radius: 12; -fx-padding: 3 10; -fx-font-size: 12px;");

        // Reservation details
        Label places = new Label("📋 " + participation.getNombrePlaces() + " place(s) réservée(s)");
        places.setStyle("-fx-text-fill: #666;");

        Label total = new Label(String.format("💰 %.0f DT", participation.getMontantTotal()));
        total.setStyle("-fx-text-fill: #81AE8D; -fx-font-size: 20px; -fx-font-weight: bold;");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Label date = new Label("📅 Réservé le " + sdf.format(participation.getDateParticipation()));
        date.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        Label status = new Label(participation.getStatut());
        status.setStyle("-fx-background-color: #81AE8D; -fx-text-fill: white; " +
                "-fx-padding: 5 15; -fx-background-radius: 15; -fx-font-size: 12px;");

        // View event details button
        Button viewBtn = new Button("Voir l'événement");
        viewBtn.setStyle("-fx-background-color: #E8B156; -fx-text-fill: black; " +
                "-fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 12px;");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setOnAction(e -> navigateToEventDetails(event));

        card.getChildren().addAll(title, category, places, total, date, status, viewBtn);
        return card;
    }

    private void navigateToEventDetails(Events event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            controller.setEvent(event);

            Stage stage = (Stage) myEventsFlow.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Détails de l'événement");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}