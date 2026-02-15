package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.entities.reservationlog;
import tn.esprit.services.Servicereservationlog;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardContentController implements Initializable {

    @FXML
    private Label totalReservationsLabel;
    @FXML
    private Label reservationsJourLabel;
    @FXML
    private Label revenuTotalLabel;
    @FXML
    private HBox activityBarsContainer;
    @FXML
    private TableView<ReservationDisplay> reservationsTable;
    @FXML
    private TableColumn<ReservationDisplay, String> clientColumn;
    @FXML
    private TableColumn<ReservationDisplay, String> dateDebutColumn;
    @FXML
    private TableColumn<ReservationDisplay, String> totalColumn;   // Changé en String pour le formatage
    @FXML
    private TableColumn<ReservationDisplay, String> statusColumn;

    private Servicereservationlog serviceReservation = new Servicereservationlog();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration des colonnes du tableau
        clientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("montantFormatted"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));

        try {
            loadDashboardData();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données : " + e.getMessage());
        }
    }

    private void loadDashboardData() throws SQLException {
        List<reservationlog> reservations = serviceReservation.afficher();

        // Calcul des statistiques
        int total = reservations.size();
        int todayCount = 0;
        double revenu = 0.0;
        LocalDate aujourdhui = LocalDate.now();

        for (reservationlog r : reservations) {
            LocalDate dateDebut = r.getDate_debut().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (dateDebut.equals(aujourdhui)) {
                todayCount++;
            }
            revenu += r.getMontant();
        }

        totalReservationsLabel.setText(String.valueOf(total));
        reservationsJourLabel.setText(String.valueOf(todayCount));
        revenuTotalLabel.setText(String.format("%.0f DT", revenu));

        // Création des barres d'activité (simulées)
        createActivityBars();

        // Remplir le tableau avec les 5 dernières réservations
        List<reservationlog> dernieres = reservations.stream()
                .sorted((r1, r2) -> r2.getDate_debut().compareTo(r1.getDate_debut()))
                .limit(5)
                .collect(Collectors.toList());

        ObservableList<ReservationDisplay> items = FXCollections.observableArrayList();
        for (reservationlog r : dernieres) {
            items.add(new ReservationDisplay(r));
        }
        reservationsTable.setItems(items);
    }

    private void createActivityBars() {
        String[] jours = {"L", "M", "M", "J", "J", "V", "S", "D"};
        Random rand = new Random();
        activityBarsContainer.getChildren().clear();

        for (String jour : jours) {
            VBox barContainer = new VBox(5);
            barContainer.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);

            Region bar = new Region();
            double hauteur = 20 + rand.nextDouble() * 80; // hauteur entre 20 et 100
            bar.setPrefHeight(hauteur);
            bar.setPrefWidth(30);
            bar.setStyle("-fx-background-color: #3B82F6; -fx-background-radius: 4 4 0 0;");

            Label jourLabel = new Label(jour);
            jourLabel.setStyle("-fx-font-size: 12;");

            barContainer.getChildren().addAll(bar, jourLabel);
            activityBarsContainer.getChildren().add(barContainer);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe interne pour l'affichage des réservations dans le tableau
    public static class ReservationDisplay {
        private final String clientName;
        private final int dateDebut;
        private final float montant;
        private final String status;

        public ReservationDisplay(reservationlog r) {
            this.clientName = getClientName(r.getIdc());
            this.dateDebut = r.getDate_debut().getDate();
            this.montant = r.getMontant();
            this.status = r.getStatus().name();
        }

        private String getClientName(int idc) {
            // Noms fictifs pour l'exemple (à remplacer par une vraie jointure si possible)
            switch (idc) {
                case 1: return "Mohamed Ali";
                case 2: return "Chasls";
                case 3: return "Ehtals";
                case 4: return "Sami";
                case 5: return "Fatma";
                default: return "Client " + idc;
            }
        }

        public String getClientName() {
            return clientName;
        }

        public int getDateDebut() {
            return dateDebut;
        }

        public String getMontantFormatted() {
            return String.format("%.0f DT", montant);
        }

        public String getStatusDisplay() {
            switch (status) {
                case "CONFIRMED": return "Confirmé";
                case "PENDING": return "En Attente";
                case "COMPLETED": return "Terminé";
                default: return status;
            }
        }
    }
}