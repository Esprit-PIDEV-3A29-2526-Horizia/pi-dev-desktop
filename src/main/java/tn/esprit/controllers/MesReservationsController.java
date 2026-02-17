package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Pos;
import tn.esprit.entites.Reservation;
import tn.esprit.services.ReservationService;
import java.io.IOException;
import java.util.List;

public class MesReservationsController {

    @FXML private VBox containerReservations;
    private final ReservationService rs = new ReservationService();

    @FXML
    public void initialize() {
        chargerMesReservations();
    }

    private void chargerMesReservations() {
        containerReservations.getChildren().clear();
        // ID utilisateur statique à 1 pour le moment (à lier avec ta session plus tard)
        List<Reservation> list = rs.getReservationsParUtilisateur(1);

        for (Reservation r : list) {
            containerReservations.getChildren().add(creerCardReservation(r));
        }
    }

    private HBox creerCardReservation(Reservation res) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // Image du voyage (Utilise une image par défaut si URL vide)
        ImageView img = new ImageView(new Image("https://via.placeholder.com/80", true));
        img.setFitHeight(60); img.setFitWidth(80);

        Label lblDest = new Label("Voyage à " + res.getDestination());
        lblDest.setPrefWidth(300);
        lblDest.setStyle("-fx-font-weight: bold; -fx-font-size: 15;");

        Label lblPers = new Label(res.getNb_places() + " Voyageurs");
        lblPers.setPrefWidth(150); lblPers.setAlignment(Pos.CENTER);

        Label lblStatut = new Label("Confirmé");
        lblStatut.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 5 15; -fx-background-radius: 15;");
        lblStatut.setPrefWidth(120); lblStatut.setAlignment(Pos.CENTER);

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-text-fill: #EF4444; -fx-background-color: transparent; -fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> {
            rs.annulerReservation(res.getId());
            chargerMesReservations(); // Rafraîchir
        });

        card.getChildren().addAll(img, lblDest, lblPers, lblStatut, btnAnnuler);
        HBox.setMargin(lblDest, new javafx.geometry.Insets(0, 0, 0, 20));

        return card;
    }

    @FXML
    private void allerAuCatalogue() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/CatalogueUser.fxml"));
        containerReservations.getScene().setRoot(root);
    }
    @FXML
    private void deconnexion() {
        try {
            // Code pour revenir à la page de Login
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            containerReservations.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}