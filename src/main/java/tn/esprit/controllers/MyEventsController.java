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
import java.util.Optional;
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

        Label title = new Label(event.getTitre());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #23779C;");
        title.setWrapText(true);

        Label category = new Label(event.getCategorie());
        category.setStyle("-fx-background-color: #DACEB6; -fx-text-fill: #23779C; " +
                "-fx-background-radius: 12; -fx-padding: 3 10; -fx-font-size: 12px;");

        Label places = new Label("📋 " + participation.getNombrePlaces() + " place(s) réservée(s)");
        places.setStyle("-fx-text-fill: #666;");

        Label total = new Label(String.format("💰 %.0f DT", participation.getMontantTotal()));
        total.setStyle("-fx-text-fill: #81AE8D; -fx-font-size: 20px; -fx-font-weight: bold;");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Label date = new Label("📅 Réservé le " + sdf.format(participation.getDateParticipation()));
        date.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        Label status = new Label(participation.getStatut());
        status.setStyle("-fx-background-color: #81AE8D; -fx-text-fill: white; " +
                "-fx-padding: 5 15; -fx-background-radius: 15; -fx-font-size: 12px;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button modifyBtn = new Button("✏️ Modifier");
        modifyBtn.setStyle("-fx-background-color: #3D94CA; -fx-text-fill: white; " +
                "-fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 12px;");
        modifyBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(modifyBtn, Priority.ALWAYS);
        modifyBtn.setOnAction(e -> showModifyDialog(participation, event));

        Button deleteBtn = new Button("🗑️ Annuler");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 12px;");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deleteBtn, Priority.ALWAYS);
        deleteBtn.setOnAction(e -> deleteParticipation(participation, event));

        buttonBox.getChildren().addAll(modifyBtn, deleteBtn);

        Button viewBtn = new Button("Voir l'événement");
        viewBtn.setStyle("-fx-background-color: #E8B156; -fx-text-fill: black; " +
                "-fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 12px;");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setOnAction(e -> navigateToEventDetails(event));

        card.getChildren().addAll(title, category, places, total, date, status, buttonBox, viewBtn);
        return card;
    }

    private void showModifyDialog(Participation participation, Events event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier la réservation");
        dialog.setHeaderText("Modifier le nombre de places pour " + event.getTitre());

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        int maxPlaces = event.getPlacesRestantes() + participation.getNombrePlaces();
        Spinner<Integer> placesSpinner = new Spinner<>(1, maxPlaces, participation.getNombrePlaces());
        placesSpinner.setEditable(true);

        Label totalLabel = new Label(String.format("%.0f DT", participation.getNombrePlaces() * event.getPrix()));

        placesSpinner.valueProperty().addListener((obs, old, val) -> {
            totalLabel.setText(String.format("%.0f DT", val * event.getPrix()));
        });

        grid.add(new Label("Nombre de places:"), 0, 0);
        grid.add(placesSpinner, 1, 0);
        grid.add(new Label("Nouveau total:"), 0, 1);
        grid.add(totalLabel, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveButtonType) {
                try {
                    int oldPlaces = participation.getNombrePlaces();
                    int newPlaces = placesSpinner.getValue();

                    if (newPlaces != oldPlaces) {
                        participation.setNombrePlaces(newPlaces);
                        participation.setMontantTotal((float) (newPlaces * event.getPrix()));

                        int placesDiff = oldPlaces - newPlaces;
                        int newEventPlaces = event.getPlacesRestantes() + placesDiff;

                        serviceParticipation.modifier(participation);
                        serviceEvent.updatePlaces(event.getId_event(), newEventPlaces);

                        loadData();

                        showAlert("Succès", "Réservation modifiée avec succès!");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la modification");
                }
            }
        });
    }

    private void deleteParticipation(Participation participation, Events event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Annuler la réservation");
        confirm.setContentText("Voulez-vous vraiment annuler cette réservation ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int newPlaces = event.getPlacesRestantes() + participation.getNombrePlaces();
                serviceEvent.updatePlaces(event.getId_event(), newPlaces);

                serviceParticipation.supprimer(participation.getId_participation());

                loadData();

                showAlert("Succès", "Réservation annulée avec succès!");

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'annulation");
            }
        }
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}