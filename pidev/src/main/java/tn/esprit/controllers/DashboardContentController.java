package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.logement;
import tn.esprit.entities.user;
import tn.esprit.services.Servicereservationlog;
import tn.esprit.services.Servicelogement;
import tn.esprit.services.Serviceuser;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
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
    private VBox reservationsVBox;  // Nouveau : conteneur pour les cartes de réservations

    private Servicereservationlog serviceReservation = new Servicereservationlog();
    private Servicelogement serviceLogement = new Servicelogement();
    private Serviceuser serviceUser = new Serviceuser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        // Création des barres d'activité (basées sur les données réelles)
        createActivityBars(reservations);

        // Remplir le VBox avec toutes les réservations sous forme de cartes
        loadReservationsCards(reservations);
    }

    private void createActivityBars(List<reservationlog> reservations) {
        String[] jours = {"L", "M", "M", "J", "J", "V", "S", "D"};  // Lundi à Dimanche
        Map<String, Integer> counts = new HashMap<>();
        for (String jour : jours) {
            counts.put(jour, 0);
        }

        // Compter les réservations par jour de la semaine
        SimpleDateFormat sdf = new SimpleDateFormat("u");  // Jour de la semaine (1=Lundi, 7=Dimanche)
        for (reservationlog r : reservations) {
            String dayOfWeek = sdf.format(r.getDate_debut());
            int day = Integer.parseInt(dayOfWeek);
            String key = jours[day - 1];  // Mapper 1-7 à L-D
            counts.put(key, counts.get(key) + 1);
        }

        // Trouver le max pour normaliser les hauteurs
        int maxCount = counts.values().stream().max(Integer::compare).orElse(1);
        double maxHeight = 100.0;

        activityBarsContainer.getChildren().clear();
        for (String jour : jours) {
            VBox barContainer = new VBox(5);
            barContainer.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);

            Region bar = new Region();
            double hauteur = 20 + (counts.get(jour) * (maxHeight - 20) / maxCount);  // Hauteur proportionnelle, min 20
            bar.setPrefHeight(hauteur);
            bar.setPrefWidth(30);
            bar.setStyle("-fx-background-color: #3B82F6; -fx-background-radius: 4 4 0 0;");

            Label jourLabel = new Label(jour);
            jourLabel.setStyle("-fx-font-size: 12;");

            barContainer.getChildren().addAll(bar, jourLabel);
            activityBarsContainer.getChildren().add(barContainer);
        }
    }

    private void loadReservationsCards(List<reservationlog> reservations) {
        reservationsVBox.getChildren().clear();  // Vider les cartes existantes

        // Trier par date de début décroissante
        List<reservationlog> triees = reservations.stream()
                .sorted((r1, r2) -> r2.getDate_debut().compareTo(r1.getDate_debut()))
                .collect(Collectors.toList());

        for (reservationlog r : triees) {
            HBox carte = creerCarteReservation(r);
            reservationsVBox.getChildren().add(carte);
        }
    }

    private HBox creerCarteReservation(reservationlog r) {
        // Récupérer les détails
        String clientName = "Inconnu";
        String logementName = "Inconnu";
        try {
            user client = serviceUser.rechercherParId(r.getIdc());
            if (client != null) {
                clientName = client.getNom() + " " + client.getPrenom();
            }
        } catch (SQLException e) {
            clientName = "Erreur";
        }

        try {
            logement log = serviceLogement.rechercherParId(r.getId_l());
            if (log != null) {
                logementName = log.getNom();
            }
        } catch (SQLException e) {
            logementName = "Erreur";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateDebut = sdf.format(r.getDate_debut());
        String dateFin = sdf.format(r.getDate_fin());
        String total = String.format("%.0f DT", r.getMontant());
        String status = r.getStatus().name();
        String statusDisplay = switch (status) {
            case "CONFIRMED" -> "Confirmé";
            case "PENDING" -> "En Attente";
            case "COMPLETED" -> "Terminé";
            default -> status;
        };

        // Créer la carte (HBox avec VBox pour organiser)
        HBox hbox = new HBox(20);
        hbox.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-border-radius: 8;");

        VBox vb1 = new VBox(5);
        vb1.setPrefWidth(150);
        Label lblClient = new Label("Client: " + clientName);
        lblClient.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label lblLogement = new Label("Logement: " + logementName);
        lblLogement.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");
        vb1.getChildren().addAll(lblClient, lblLogement);

        VBox vb2 = new VBox(5);
        vb2.setPrefWidth(150);
        Label lblDateDebut = new Label("Date Début: " + dateDebut);
        lblDateDebut.setStyle("-fx-font-size: 14; -fx-text-fill: #111827;");
        Label lblDateFin = new Label("Date Fin: " + dateFin);
        lblDateFin.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");
        vb2.getChildren().addAll(lblDateDebut, lblDateFin);

        VBox vb3 = new VBox(5);
        vb3.setPrefWidth(100);
        Label lblTotal = new Label("Total: " + total);
        lblTotal.setStyle("-fx-font-size: 14; -fx-text-fill: #111827;");
        Label lblStatus = new Label("Status: " + statusDisplay);
        lblStatus.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #10B981;");
        vb3.getChildren().addAll(lblTotal, lblStatus);

        hbox.getChildren().addAll(vb1, vb2, vb3);
        return hbox;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}