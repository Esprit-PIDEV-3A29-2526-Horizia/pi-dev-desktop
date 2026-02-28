package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entites.Reservation;
import tn.esprit.services.ReservationService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class GestionReservationsAdminController implements Initializable {

    @FXML private VBox containerToutesReservations;
    private final ReservationService rs = new ReservationService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        rafraichirListe();
    }

    private void rafraichirListe() {
        if (containerToutesReservations == null) return;

        containerToutesReservations.getChildren().clear();

        List<Reservation> reservations = rs.getAllReservations();
        if (reservations != null) {
            for (Reservation res : reservations) {
                containerToutesReservations.getChildren().add(creerLigne(res));
            }
        }
    }
    private AdminDashboardController dashboardController;

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
    }

    // ===== Helpers statut =====
    private String normalizeStatut(String s) {
        if (s == null || s.isBlank()) return "EN_ATTENTE";
        return s.trim().toUpperCase(Locale.ROOT);
    }

    private String labelStatut(String s) {
        return switch (normalizeStatut(s)) {
            case "CONFIRMEE" -> "Confirmée";
            case "ANNULEE" -> "Annulée";
            default -> "En attente";
        };
    }

    private String styleChipStatut(String s) {
        String st = normalizeStatut(s);
        return switch (st) {
            case "CONFIRMEE" -> "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46;";
            case "ANNULEE"   -> "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;";
            default          -> "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E;";
        } + " -fx-padding: 5 12; -fx-background-radius: 15; -fx-font-weight: bold;";
    }

    private HBox creerLigne(Reservation res) {

        HBox ligne = new HBox();
        ligne.setStyle("-fx-background-color: white; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0; -fx-padding: 15;");

        GridPane grid = new GridPane();
        grid.prefWidthProperty().bind(ligne.widthProperty());

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(15); col3.setHalignment(HPos.CENTER);
        ColumnConstraints col4 = new ColumnConstraints(); col4.setPercentWidth(15); col4.setHalignment(HPos.CENTER);
        ColumnConstraints col5 = new ColumnConstraints(); col5.setPercentWidth(20); col5.setHalignment(HPos.CENTER);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4, col5);

        // 1) Destination
        Label lblDest = new Label(res.getDestination() != null ? res.getDestination() : "");
        lblDest.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");
        grid.add(lblDest, 0, 0);

        // 2) User
        Label lblUser = new Label("ID: " + res.getIdUser());
        lblUser.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        grid.add(lblUser, 1, 0);

        // 3) Places
        Label lblPlaces = new Label(String.valueOf(res.getNbr_personnes()));
        lblPlaces.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");
        grid.add(lblPlaces, 2, 0);

        // 4) Statut (chip)
        String statutDb = normalizeStatut(res.getStatut());
        Label lblStatut = new Label(labelStatut(statutDb));
        lblStatut.setStyle(styleChipStatut(statutDb));
        grid.add(lblStatut, 3, 0);

        // 5) Action (bouton toujours présent, grisé si confirmée/annulée)
        Button btnConfirmer = new Button("Confirmer");

        boolean dejaConfirmee = statutDb.equals("CONFIRMEE");
        boolean annulee = statutDb.equals("ANNULEE");

        if (dejaConfirmee) {
            btnConfirmer.setText("Confirmée");
        }

        boolean disabled = dejaConfirmee || annulee;
        btnConfirmer.setDisable(disabled);

        if (disabled) {
            btnConfirmer.setStyle("""
                -fx-background-color: #CBD5E1;
                -fx-text-fill: #475569;
                -fx-background-radius: 5;
                -fx-font-weight: bold;
                -fx-cursor: default;
                """);
        } else {
            btnConfirmer.setStyle("""
                -fx-background-color: #0D9488;
                -fx-text-fill: white;
                -fx-background-radius: 5;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """);

            btnConfirmer.setOnAction(e -> {
                rs.confirmerReservation(res.getId());
                rafraichirListe();
            });
        }

        grid.add(btnConfirmer, 4, 0);

        ligne.getChildren().add(grid);
        return ligne;
    }

    @FXML
    private void naviguerCategories(ActionEvent event) { changerScene("/GestionCategorie.fxml", event); }

    @FXML
    private void naviguerVoyages(ActionEvent event) { changerScene("/GestionVoyage.fxml", event); }

    @FXML
    private void handleDeconnexion(ActionEvent event) { changerScene("/Login.fxml", event); }

    private void changerScene(String fxml, ActionEvent event) {
        try {
            URL resource = getClass().getResource(fxml);
            if (resource == null) throw new IOException("FXML non trouvé : " + fxml);
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de navigation : " + e.getMessage());
        }
    }
}