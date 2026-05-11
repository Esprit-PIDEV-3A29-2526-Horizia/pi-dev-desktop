package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.logement;
import tn.esprit.entities.User;
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
    private TextField searchField;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private ScrollPane activityScrollPane;
    @FXML
    private ScrollPane reservationsScrollPane;

    private Servicereservationlog serviceReservation = new Servicereservationlog();
    private Servicelogement serviceLogement = new Servicelogement();
    private Serviceuser serviceUser = new Serviceuser();
    private List<reservationlog> allReservations;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurer le ComboBox pour le tri
        sortComboBox.setItems(FXCollections.observableArrayList("Montant croissant", "Montant décroissant", "Date récente", "Date ancienne"));
        sortComboBox.setValue("Date récente");

        // Ajouter des listeners pour la recherche et le tri
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateReservationsDisplay());
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateReservationsDisplay());

        // Rendre les cartes responsive
        setupResponsiveLayout();

        try {
            loadDashboardData();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données : " + e.getMessage());
        }
    }

    private void setupResponsiveLayout() {
        // Permettre au VBox de réserver des cartes de grandir
        if (reservationsVBox != null) {
            reservationsVBox.setFillWidth(true);
        }

        // Configurer le ScrollPane pour qu'il suive la taille de la fenêtre
        if (reservationsScrollPane != null) {
            reservationsScrollPane.setFitToWidth(true);
            reservationsScrollPane.setFitToHeight(true);
        }

        if (activityScrollPane != null) {
            activityScrollPane.setFitToWidth(true);
        }
    }

    private void loadDashboardData() throws SQLException {
        allReservations = serviceReservation.afficher();

        // Calcul des statistiques
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

        // Création des barres d'activité
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
        String searchText = searchField.getText().toLowerCase().trim();
        List<reservationlog> filtered = reservations;

        if (!searchText.isEmpty()) {
            filtered = reservations.stream()
                    .filter(r -> r.getStatus().name().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
        }

        // Appliquer le tri
        String sortOption = sortComboBox.getValue();

        switch (sortOption) {
            case "Montant croissant":
                filtered.sort(Comparator.comparing(reservationlog::getMontant));
                break;
            case "Montant décroissant":
                filtered.sort(Comparator.comparing(reservationlog::getMontant).reversed());
                break;
            case "Date récente":
                filtered.sort(Comparator.comparing(reservationlog::getDate_debut).reversed());
                break;
            case "Date ancienne":
                filtered.sort(Comparator.comparing(reservationlog::getDate_debut));
                break;
            default:
                filtered.sort(Comparator.comparing(reservationlog::getDate_debut).reversed());
                break;
        }

        return filtered;
    }

    private void createActivityBars(List<reservationlog> reservations) {
        String[] jours = {"L", "M", "M", "J", "V", "S", "D"};
        Map<String, Integer> counts = new HashMap<>();
        for (String jour : jours) {
            counts.put(jour, 0);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("u");
        for (reservationlog r : reservations) {
            String dayOfWeek = sdf.format(r.getDate_debut());
            int day = Integer.parseInt(dayOfWeek);
            String key = jours[day - 1];
            counts.put(key, counts.get(key) + 1);
        }

        int maxCount = counts.values().stream().max(Integer::compare).orElse(1);
        double maxHeight = 100.0;

        activityBarsContainer.getChildren().clear();

        for (String jour : jours) {
            VBox barContainer = new VBox(5);
            barContainer.setAlignment(Pos.BOTTOM_CENTER);
            barContainer.setPrefWidth(50);
            barContainer.setMinWidth(40);
            barContainer.setMaxWidth(80);
            HBox.setHgrow(barContainer, Priority.ALWAYS);

            Region bar = new Region();
            double hauteur = 20 + (counts.get(jour) * (maxHeight - 20) / maxCount);
            bar.setPrefHeight(hauteur);
            bar.setMinHeight(20);
            bar.setPrefWidth(30);
            bar.setStyle("-fx-background-color: #3B82F6; -fx-background-radius: 4 4 0 0;");

            Label jourLabel = new Label(jour);
            jourLabel.setStyle("-fx-font-size: 12;");
            Label countLabel = new Label(String.valueOf(counts.get(jour)));
            countLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #6B7280;");

            barContainer.getChildren().addAll(bar, countLabel, jourLabel);
            activityBarsContainer.getChildren().add(barContainer);
        }
    }

    private void loadReservationsCards(List<reservationlog> reservations) {
        reservationsVBox.getChildren().clear();

        if (reservations.isEmpty()) {
            Label noDataLabel = new Label("Aucune réservation trouvée.");
            noDataLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #6B7280;");
            noDataLabel.setAlignment(Pos.CENTER);
            reservationsVBox.getChildren().add(noDataLabel);
            return;
        }

        for (reservationlog r : reservations) {
            HBox carte = creerCarteReservation(r);
            carte.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(carte, Priority.ALWAYS);
            reservationsVBox.getChildren().add(carte);
        }
    }

    private HBox creerCarteReservation(reservationlog r) {
        String clientName = "Inconnu";
        String logementName = "Inconnu";

        try {
            User client = serviceUser.rechercherParId(r.getIdc());
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

        String statusColor;
        switch (r.getStatus()) {
            case confirmée:
                statusColor = "#10B981";
                break;
            case annulée:
                statusColor = "#EF4444";
                break;
            case en_attente:
                statusColor = "#F59E0B";
                break;
            case terminée:
                statusColor = "#6B7280";
                break;
            default:
                statusColor = "#6B7280";
        }

        HBox hbox = new HBox(20);
        hbox.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-border-radius: 8;");
        hbox.setAlignment(Pos.CENTER_LEFT);

        // Colonne 1 - Client et Logement
        VBox vb1 = new VBox(5);
        VBox.setVgrow(vb1, Priority.ALWAYS);
        vb1.setPrefWidth(200);

        Label lblClient = new Label("👤 " + clientName);
        lblClient.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label lblLogement = new Label("🏠 " + logementName);
        lblLogement.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");
        vb1.getChildren().addAll(lblClient, lblLogement);

        // Colonne 2 - Dates
        VBox vb2 = new VBox(5);
        VBox.setVgrow(vb2, Priority.ALWAYS);
        vb2.setPrefWidth(180);

        Label lblDateDebut = new Label("📅 Début: " + dateDebut);
        lblDateDebut.setStyle("-fx-font-size: 13; -fx-text-fill: #111827;");
        Label lblDateFin = new Label("📅 Fin: " + dateFin);
        lblDateFin.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");
        vb2.getChildren().addAll(lblDateDebut, lblDateFin);

        // Colonne 3 - Montant et Status
        VBox vb3 = new VBox(5);
        VBox.setVgrow(vb3, Priority.ALWAYS);
        vb3.setPrefWidth(150);
        vb3.setAlignment(Pos.CENTER_RIGHT);

        Label lblTotal = new Label("💰 " + total);
        lblTotal.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label lblStatus = new Label(r.getStatus().toString());
        lblStatus.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");
        vb3.getChildren().addAll(lblTotal, lblStatus);

        HBox.setHgrow(vb1, Priority.ALWAYS);
        HBox.setHgrow(vb2, Priority.ALWAYS);
        HBox.setHgrow(vb3, Priority.NEVER);

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