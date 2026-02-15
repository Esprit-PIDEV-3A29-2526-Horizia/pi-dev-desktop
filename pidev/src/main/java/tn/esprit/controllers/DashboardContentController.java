package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.logement;
import tn.esprit.entities.user;
import tn.esprit.entities.Status;  // Import de l'enum Status
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
    private VBox reservationsVBox;
    @FXML
    private TextField searchField;  // Nouveau : champ de recherche
    @FXML
    private ComboBox<String> sortComboBox;  // Nouveau : combo pour le tri

    private Servicereservationlog serviceReservation = new Servicereservationlog();
    private Servicelogement serviceLogement = new Servicelogement();
    private Serviceuser serviceUser = new Serviceuser();
    private List<reservationlog> allReservations;  // Stocke toutes les réservations pour filtrage/tri

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurer le ComboBox pour le tri
        sortComboBox.setItems(FXCollections.observableArrayList("Montant croissant", "Montant décroissant"));
        sortComboBox.setValue("Montant décroissant");  // Valeur par défaut

        // Ajouter des listeners pour la recherche et le tri
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateReservationsDisplay());
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateReservationsDisplay());

        try {
            loadDashboardData();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données : " + e.getMessage());
        }
    }

    private void loadDashboardData() throws SQLException {
        allReservations = serviceReservation.afficher();

        // Calcul des statistiques (inchangé)
        int total = allReservations.size();
        int todayCount = 0;
        double revenu = 0.0;
        LocalDate aujourdhui = LocalDate.now();

        for (reservationlog r : allReservations) {
            LocalDate dateDebut = r.getDate_debut().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (dateDebut.equals(aujourdhui)) {
                todayCount++;
            }
            revenu += r.getMontant();
        }

        totalReservationsLabel.setText(String.valueOf(total));
        reservationsJourLabel.setText(String.valueOf(todayCount));
        revenuTotalLabel.setText(String.format("%.0f DT", revenu));

        // Création des barres d'activité (inchangé)
        createActivityBars(allReservations);

        // Charger les réservations avec recherche/tri appliqués
        updateReservationsDisplay();
    }

    private void updateReservationsDisplay() {
        try {
            List<reservationlog> filteredAndSorted = applySearchAndSort(allReservations);
            loadReservationsCards(filteredAndSorted);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    private List<reservationlog> applySearchAndSort(List<reservationlog> reservations) throws SQLException {
        // Appliquer la recherche par statut
        String searchText = searchField.getText().toLowerCase();
        List<reservationlog> filtered = reservations;
        if (!searchText.isEmpty()) {
            filtered = serviceReservation.rechercherParAttribut("status", searchText);  // Recherche par statut
        }

        // Appliquer le tri par montant
        String sortOption = sortComboBox.getValue();
        boolean ascending = "Montant croissant".equals(sortOption);
        return serviceReservation.trierParAttribut("montant", ascending);
    }

    private void createActivityBars(List<reservationlog> reservations) {
        String[] jours = {"L", "M", "M", "J", "V", "S", "D"};  // Lundi à Dimanche
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

        if (reservations.isEmpty()) {
            Label noDataLabel = new Label("Aucune réservation trouvée.");
            noDataLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #6B7280;");
            reservationsVBox.getChildren().add(noDataLabel);
            return;
        }

        for (reservationlog r : reservations) {
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
        String status = r.getStatus().name().toLowerCase();
        String statusDisplay = switch (status) {
            case "confirmée" -> "Confirmée";
            case "annulée" -> "Annulée";
            case "en_attente" -> "En Attente";
            case "terminée" -> "Terminée";
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