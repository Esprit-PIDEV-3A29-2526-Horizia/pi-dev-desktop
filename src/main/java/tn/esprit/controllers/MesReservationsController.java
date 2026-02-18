package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import tn.esprit.entites.Reservation;
import tn.esprit.services.ReservationService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class MesReservationsController {

    @FXML private VBox containerReservations;

    private final ReservationService rs = new ReservationService();

    @FXML
    public void initialize() {
        if (containerReservations == null) {
            System.err.println("ContainerReservations est null : vérifie fx:id dans MesReservations.fxml");
            return;
        }
        chargerMesReservations();
    }

    private void chargerMesReservations() {
        containerReservations.getChildren().clear();

        // Remarque : idUser devrait idéalement être récupéré depuis une session utilisateur
        int idUser = 1;
        List<Reservation> list = rs.getReservationsParUtilisateur(idUser);

        if (list == null || list.isEmpty()) {
            Label empty = new Label("Aucune réservation pour le moment.");
            empty.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14;");
            empty.setPadding(new Insets(20));
            containerReservations.getChildren().add(empty);
            return;
        }

        for (Reservation r : list) {
            containerReservations.getChildren().add(creerCardReservation(r));
        }
    }

    private HBox creerCardReservation(Reservation res) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 20, 10, 20));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );

        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.prefWidthProperty().bind(card.widthProperty().subtract(40));

        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(20); col2.setHalignment(HPos.CENTER);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setPercentWidth(20); col3.setHalignment(HPos.CENTER);
        ColumnConstraints col4 = new ColumnConstraints(); col4.setPercentWidth(20); col4.setHalignment(HPos.CENTER);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);

        // --- Colonne 1 : Voyage ---
        HBox voyageBox = new HBox(15);
        voyageBox.setAlignment(Pos.CENTER_LEFT);
        ImageView img = new ImageView();
        img.setFitHeight(50); img.setFitWidth(70); img.setPreserveRatio(true);
        img.setImage(chargerImage(res.getImageUrl()));

        Label lblDest = new Label(res.getDestination() != null ? res.getDestination() : "Voyage");
        lblDest.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #0F172A;");
        voyageBox.getChildren().addAll(img, lblDest);
        grid.add(voyageBox, 0, 0);

        // --- Colonne 2 : Personnes ---
        Label lblPers = new Label(res.getNbr_personnes() + " personnes");
        lblPers.setStyle("-fx-text-fill: #334155;");
        grid.add(lblPers, 1, 0);

        // --- Colonne 3 : Statut ---
        String statut = (res.getStatut() != null) ? res.getStatut() : "En attente";
        Label lblStatut = new Label(statut);
        lblStatut.setStyle(styleStatut(statut));
        lblStatut.setPadding(new Insets(5, 12, 5, 12));
        grid.add(lblStatut, 2, 0);

        // --- Colonne 4 : Actions (Logique de conditionnelle) ---
        // Règle : Si confirmé, on ne peut plus annuler
        if (statut.equalsIgnoreCase("Confirmée") || statut.equalsIgnoreCase("Confirmé")) {
            Label lblValide = new Label("Validée");
            lblValide.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");
            grid.add(lblValide, 3, 0);
        } else {
            Button btnAnnuler = new Button("Annuler");
            btnAnnuler.setStyle("-fx-text-fill: #EF4444; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold;");
            btnAnnuler.setOnAction(e -> handleAnnulation(res));
            grid.add(btnAnnuler, 3, 0);
        }

        card.getChildren().add(grid);
        return card;
    }

    private void handleAnnulation(Reservation res) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation d'annulation");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir annuler votre réservation pour " + res.getDestination() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            rs.annulerReservation(res.getId());
            chargerMesReservations(); // Rafraîchir la liste
        }
    }

    private Image chargerImage(String url) {
        try {
            if (url != null && !url.isBlank()) {
                if (url.startsWith("http")) {
                    return new Image(url, true);
                }
                URL resUrl = getClass().getResource(url.startsWith("/") ? url : "/" + url);
                if (resUrl != null) {
                    return new Image(resUrl.toExternalForm(), true);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image : " + url);
        }
        return new Image("https://via.placeholder.com/80", true);
    }

    private String styleStatut(String statut) {
        if (statut == null) statut = "";
        String s = statut.toLowerCase();

        if (s.contains("attente")) {
            return "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-background-radius: 20; -fx-font-weight: bold;";
        }
        if (s.contains("confirm")) {
            return "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-background-radius: 20; -fx-font-weight: bold;";
        }
        if (s.contains("annul")) {
            return "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-background-radius: 20; -fx-font-weight: bold;";
        }
        return "-fx-background-color: #E2E8F0; -fx-text-fill: #334155; -fx-background-radius: 20; -fx-font-weight: bold;";
    }

    @FXML
    private void allerAuCatalogue() {
        changerRoot("/CatalogueUser.fxml");
    }

    @FXML
    private void deconnexion() {
        changerRoot("/Login.fxml");
    }

    private void changerRoot(String fxml) {
        try {
            URL loc = getClass().getResource(fxml);
            if (loc == null) {
                System.err.println("FXML introuvable : " + fxml);
                return;
            }
            Parent root = FXMLLoader.load(loc);
            containerReservations.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}