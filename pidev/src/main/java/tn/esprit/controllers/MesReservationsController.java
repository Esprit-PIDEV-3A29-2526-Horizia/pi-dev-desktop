package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tn.esprit.entities.logement;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.Status;
import tn.esprit.services.Servicereservationlog;
import tn.esprit.services.Servicelogement;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class MesReservationsController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Status> filterStatusCombo;
    @FXML
    private Button btnRechercher;
    @FXML
    private Button btnReset;
    @FXML
    private FlowPane flowReservations;
    @FXML
    private Label emptyMessage;

    private Servicereservationlog serviceReservation = new Servicereservationlog();
    private Servicelogement serviceLogement = new Servicelogement();

    private ObservableList<reservationlog> reservationsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        filterStatusCombo.getItems().addAll(Status.values());
        filterStatusCombo.setPromptText("Tous les statuts");

        chargerReservations();

        btnRechercher.setOnAction(e -> filtrerReservations());
        btnReset.setOnAction(e -> resetFiltres());

        // Recherche en temps réel
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filtrerReservations());
        filterStatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> filtrerReservations());
    }

    private void chargerReservations() {
        try {
            List<reservationlog> toutes = serviceReservation.afficher();
            // Filtrer par client statique (ID 14) – à remplacer par l'ID dynamique plus tard
            List<reservationlog> duClient = toutes.stream()
                    .filter(r -> r.getIdc() == 14)
                    .collect(Collectors.toList());
            reservationsList.setAll(duClient);
            afficherReservations(reservationsList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les réservations : " + e.getMessage());
        }
    }

    private void afficherReservations(List<reservationlog> reservations) {
        flowReservations.getChildren().clear();
        if (reservations.isEmpty()) {
            emptyMessage.setVisible(true);
            emptyMessage.setManaged(true);
        } else {
            emptyMessage.setVisible(false);
            emptyMessage.setManaged(false);
            for (reservationlog r : reservations) {
                VBox card = createReservationCard(r);
                flowReservations.getChildren().add(card);
            }
        }
    }

    private VBox createReservationCard(reservationlog r) {
        // Récupérer le logement associé
        logement log = null;
        try {
            log = serviceLogement.rechercherParId(r.getId_l());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String nomLogement = (log != null) ? log.getNom() : "Logement inconnu";
        String adresseLogement = (log != null) ? log.getAdresse() : "";

        // Formatage des dates
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateArrivee = sdf.format(r.getDate_debut());
        String dateDepart = sdf.format(r.getDate_fin());

        // Création de la carte
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        card.setPrefWidth(300);
        card.setMaxWidth(300);

        Label lblNom = new Label(nomLogement);
        lblNom.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblNom.setStyle("-fx-text-fill: #23779C;");

        Label lblAdresse = new Label(adresseLogement);
        lblAdresse.setStyle("-fx-text-fill: #81AE8D; -fx-font-size: 14;");

        Label lblDates = new Label("📅 " + dateArrivee + " → " + dateDepart);
        lblDates.setStyle("-fx-text-fill: #3D94CA; -fx-font-size: 14;");

        Label lblMontant = new Label("💰 " + r.getMontant() + " DT");
        lblMontant.setStyle("-fx-text-fill: #E8B156; -fx-font-size: 16; -fx-font-weight: bold;");

        Label lblStatut = new Label("Statut : " + r.getStatus().toString());
        lblStatut.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14;");

        // Boutons d'action
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-background-color: #3D94CA; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 15; -fx-cursor: hand;");
        btnModifier.setOnAction(e -> modifierReservation(r));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 15; -fx-cursor: hand;");
        btnSupprimer.setOnAction(e -> supprimerReservation(r));

        actions.getChildren().addAll(btnModifier, btnSupprimer);
        card.getChildren().addAll(lblNom, lblAdresse, lblDates, lblMontant, lblStatut, actions);

        return card;
    }

    private void filtrerReservations() {
        String searchText = searchField.getText().toLowerCase().trim();
        Status selectedStatus = filterStatusCombo.getValue();

        List<reservationlog> filtered = reservationsList.stream()
                .filter(r -> {
                    if (selectedStatus != null && r.getStatus() != selectedStatus) {
                        return false;
                    }
                    try {
                        logement log = serviceLogement.rechercherParId(r.getId_l());
                        if (log != null && !log.getNom().toLowerCase().contains(searchText)) {
                            return false;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        afficherReservations(filtered);
    }

    private void resetFiltres() {
        searchField.clear();
        filterStatusCombo.setValue(null);
        afficherReservations(reservationsList);
    }

    private void modifierReservation(reservationlog r) {
        SessionManager.setEditingReservation(r);
        NavigationManager.loadView("/ReservationForm.fxml");
    }

    private void supprimerReservation(reservationlog r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la réservation");
        confirm.setContentText("Voulez-vous vraiment supprimer cette réservation ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                serviceReservation.supprimer(r.getId());
                chargerReservations(); // Recharger la liste après suppression
                showAlert("Succès", "Réservation supprimée.");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer : " + e.getMessage());
            }
        }
    }

    @FXML
    private void retourAccueil() {
        NavigationManager.loadView("/accueil.fxml");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}