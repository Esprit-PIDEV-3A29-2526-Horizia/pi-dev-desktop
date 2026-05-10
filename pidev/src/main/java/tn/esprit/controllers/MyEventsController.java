package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.entities.Events;
import tn.esprit.entities.Participation;
import tn.esprit.entities.User;
import tn.esprit.services.QRCodeService;
import tn.esprit.services.ServiceEvent;
import tn.esprit.services.ServiceParticipation;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyEventsController implements Initializable {

    @FXML private NavbarController navbarController;
    @FXML private FlowPane myEventsFlow;

    private ServiceEvent serviceEvent;
    private ServiceParticipation serviceParticipation;
    private List<Participation> allParticipations;
    private List<Events> allEvents;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceEvent = new ServiceEvent();
        serviceParticipation = new ServiceParticipation();

        currentUser = SessionManager.getCurrentUser();

        if (navbarController != null) {
            navbarController.updateUserInfo();
            navbarController.setActiveReservations();
        }

        System.out.println("🔍 MyEventsController - Utilisateur: " +
                (currentUser != null ? currentUser.getEmail() : "null"));

        loadData();
    }

    private void loadData() {
        try {
            allEvents = serviceEvent.afficher();

            // Filtrer les participations par user_id
            if (currentUser != null) {
                allParticipations = serviceParticipation.getParticipationsByUserId(currentUser.getId());
            } else {
                allParticipations = serviceParticipation.afficher();
            }

            System.out.println("📋 Nombre de participations pour l'utilisateur: " + allParticipations.size());
            displayMyEvents();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayMyEvents() {
        myEventsFlow.getChildren().clear();

        if (allParticipations == null || allParticipations.isEmpty()) {
            Label noEvents = new Label("Vous n'avez aucune réservation d'événement");
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
        if (allEvents == null) return null;
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

        Label places = new Label("📋 " + participation.getNombre_places() + " place(s) réservée(s)");
        places.setStyle("-fx-text-fill: #666;");

        Label total = new Label(String.format("💰 %.0f DT", participation.getMontant_total()));
        total.setStyle("-fx-text-fill: #81AE8D; -fx-font-size: 20px; -fx-font-weight: bold;");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Label date = new Label("📅 Réservé le " + sdf.format(participation.getDate_participation()));
        date.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        Label status = new Label(participation.getStatut());
        status.setStyle("-fx-background-color: #81AE8D; -fx-text-fill: white; " +
                "-fx-padding: 5 15; -fx-background-radius: 15; -fx-font-size: 12px;");

        // Afficher les informations snapshot si disponibles
        VBox snapshotBox = new VBox(5);
        snapshotBox.setVisible(false);
        snapshotBox.setManaged(false);

        if (participation.getNom_snapshot() != null && !participation.getNom_snapshot().isEmpty()) {
            Label snapshotInfo = new Label("👤 Réservé par: " + participation.getPrenom_snapshot() + " " + participation.getNom_snapshot());
            snapshotInfo.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
            snapshotBox.getChildren().add(snapshotInfo);
            snapshotBox.setVisible(true);
            snapshotBox.setManaged(true);
        }

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

        Button qrBtn = new Button("📱 QR Code");
        qrBtn.setStyle("-fx-background-color: #23779C; -fx-text-fill: white; " +
                "-fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 12px;");
        qrBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(qrBtn, Priority.ALWAYS);
        qrBtn.setOnAction(e -> showQRCode(participation));

        buttonBox.getChildren().addAll(modifyBtn, deleteBtn, qrBtn);

        Button viewBtn = new Button("Voir l'événement");
        viewBtn.setStyle("-fx-background-color: #E8B156; -fx-text-fill: black; " +
                "-fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 12px;");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setOnAction(e -> navigateToEventDetails(event));

        card.getChildren().addAll(title, category, places, total, date, status, snapshotBox, buttonBox, viewBtn);
        return card;
    }

    private void showQRCode(Participation participation) {
        Image qrImage = QRCodeService.generateQRCode(participation);

        if (qrImage != null) {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("QR Code - Réservation #" + participation.getId_participation());
            dialog.setHeaderText("🎫 Votre billet électronique");

            ImageView qrView = new ImageView(qrImage);
            qrView.setFitWidth(300);
            qrView.setFitHeight(300);

            VBox content = new VBox(15);
            content.setAlignment(Pos.CENTER);
            content.setPadding(new Insets(20));

            Label info = new Label("Présentez ce QR code à l'entrée de l'événement");
            info.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

            Label reservationInfo = new Label(String.format("Réservation #%d - %d place(s)",
                    participation.getId_participation(), participation.getNombre_places()));
            reservationInfo.setStyle("-fx-text-fill: #23779C; -fx-font-weight: bold; -fx-font-size: 14px;");

            content.getChildren().addAll(reservationInfo, qrView, info);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        } else {
            showAlert("Erreur", "Impossible de générer le QR code");
        }
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

        int maxPlaces = event.getPlacesRestantes() + participation.getNombre_places();
        Spinner<Integer> placesSpinner = new Spinner<>(1, maxPlaces, participation.getNombre_places());
        placesSpinner.setEditable(true);

        Label totalLabel = new Label(String.format("%.0f DT", participation.getNombre_places() * event.getPrix()));

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
                    int oldPlaces = participation.getNombre_places();
                    int newPlaces = placesSpinner.getValue();

                    if (newPlaces != oldPlaces) {
                        participation.setNombre_places(newPlaces);
                        participation.setMontant_total(newPlaces * event.getPrix());

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
                int newPlaces = event.getPlacesRestantes() + participation.getNombre_places();
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
            if (currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) myEventsFlow.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
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