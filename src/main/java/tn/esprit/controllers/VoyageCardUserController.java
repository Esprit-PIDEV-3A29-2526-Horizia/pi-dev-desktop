package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entites.Voyage;

import java.io.IOException;

public class VoyageCardUserController {

    @FXML private VBox cardContainer;
    @FXML private ImageView imgVoyage;
    @FXML private Label lblDestination, lblPrix, lblDates, lblPlacesInfo, badgeStatus;
    @FXML private Button btnReserver;

    private Voyage voyage;

    public void setData(Voyage v) {
        this.voyage = v;
        //protège contre injection manquante
        if (lblDestination != null) lblDestination.setText(v.getDestination());
        if (lblPrix != null) lblPrix.setText(v.getPrix() + " DT");
        if (lblDates != null) lblDates.setText("Du " + v.getDate_depart() + " au " + v.getDate_retour());
        int restantes = v.getPlaces_restantes();
        int totales = v.getPlaces_total();
        if (lblPlacesInfo != null) {
            lblPlacesInfo.setText("Places restantes : " + restantes + " / " + totales);
        }
        // Chargement image
        if (imgVoyage != null && v.getImage_url() != null && !v.getImage_url().isEmpty()) {
            try {
                imgVoyage.setImage(new Image(v.getImage_url(), true));
            } catch (Exception e) {
                System.err.println("Erreur chargement image voyage : " + v.getDestination());
            }
        }
        applyStyles(restantes);
        setupHoverEffects();
    }

    private void applyStyles(int restantes) {

        if (restantes <= 0) {
            if (badgeStatus != null) {
                badgeStatus.setText("COMPLET");
                badgeStatus.setStyle(
                        "-fx-background-color: #FEE2E2; " +
                                "-fx-text-fill: #EF4444; " +
                                "-fx-background-radius: 15; " +
                                "-fx-padding: 2 10; " +
                                "-fx-font-weight: bold;"
                );
            }
            if (btnReserver != null) {
                btnReserver.setText("Plein");
                btnReserver.setDisable(true);
                btnReserver.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #94A3B8; -fx-background-radius: 15; -fx-font-weight: bold; -fx-cursor: hand;");
            }

        } else {
            if (badgeStatus != null) {
                badgeStatus.setText("DISPONIBLE");
                badgeStatus.setStyle(
                        "-fx-background-color: #DCFCE7; " +
                                "-fx-text-fill: #059669; " +
                                "-fx-background-radius: 15; " +
                                "-fx-padding: 2 10; " +
                                "-fx-font-weight: bold;"
                );
            }
            if (btnReserver != null) {
                btnReserver.setText("Réserver");
                btnReserver.setDisable(false);
            }
        }
    }

    private void setupHoverEffects() {
        if (cardContainer == null) return;
        cardContainer.setOnMouseEntered(e -> cardContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 10); " +
                        "-fx-translate-y: -5; -fx-cursor: hand;"
        ));
        cardContainer.setOnMouseExited(e -> cardContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 20; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 5); " +
                        "-fx-translate-y: 0;"
        ));
    }

    @FXML
    private void handleDetails() {
        if (voyage == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsVoyageUser.fxml"));
            Parent root = loader.load();
            DetailsVoyageUserController ctrl = loader.getController();
            ctrl.initData(this.voyage);
            Stage stage = new Stage();
            stage.setTitle("Détails - " + voyage.getDestination());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReserver() {
        if (voyage == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReserverVoyage.fxml"));
            Parent root = loader.load();

            ReserverVoyageController ctrl = loader.getController();
            ctrl.initData(this.voyage);
            // Remplacer la scène courante
            Scene scene = btnReserver.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur : Impossible de charger ReserverVoyage.fxml");
        }
    }
}
