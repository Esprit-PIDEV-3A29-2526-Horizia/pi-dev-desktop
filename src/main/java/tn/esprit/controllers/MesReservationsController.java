package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import tn.esprit.entites.Reservation;
import tn.esprit.services.ReservationService;

import java.io.IOException;
import java.net.URL;
import java.util.List;

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
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );
        ImageView img = new ImageView();
        img.setFitHeight(55);
        img.setFitWidth(80);
        img.setPreserveRatio(true);
        Image image = chargerImage(res.getImageUrl());
        img.setImage(image);
        String dest = (res.getDestination() != null) ? res.getDestination() : "Voyage";
        Label lblDest = new Label(dest);
        lblDest.setPrefWidth(320);
        lblDest.setStyle("-fx-font-weight: bold; -fx-font-size: 15; -fx-text-fill: #0F172A;");
        Label lblPers = new Label(res.getNbr_personnes() + " personnes");
        lblPers.setPrefWidth(140);
        lblPers.setAlignment(Pos.CENTER);
        lblPers.setStyle("-fx-text-fill: #334155;");
        String statut = (res.getStatut() != null) ? res.getStatut() : "En attente";
        Label lblStatut = new Label(statut);
        lblStatut.setPrefWidth(120);
        lblStatut.setAlignment(Pos.CENTER);
        lblStatut.setPadding(new Insets(6, 14, 6, 14));
        lblStatut.setStyle(styleStatut(statut));
        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle("-fx-text-fill: #EF4444; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-weight: bold;");
        btnAnnuler.setOnAction(e -> {
            rs.annulerReservation(res.getId());
            chargerMesReservations();
        });
        card.getChildren().addAll(img, lblDest, lblPers, lblStatut, btnAnnuler);
        return card;
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
                System.err.println("Image introuvable dans resources : " + url);
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
