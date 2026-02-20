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
import java.net.URL;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class VoyageCardUserController {

    @FXML private VBox cardContainer;
    @FXML private ImageView imgVoyage;
    @FXML private Label lblDestination, lblPrix, lblDates, lblPlacesInfo, badgeStatus;
    @FXML private Button btnReserver;

    private Voyage voyage;

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRANCE);

    public void setData(Voyage v) {
        this.voyage = v;
        if (v == null) return;

        if (lblDestination != null) lblDestination.setText(safe(v.getDestination()));
        if (lblPrix != null) lblPrix.setText(nf.format(v.getPrix()) + " DT");

        LocalDate d1 = toLocalDate(v.getDate_depart());
        LocalDate d2 = toLocalDate(v.getDate_retour());
        if (lblDates != null) {
            lblDates.setText("Du " + (d1 != null ? df.format(d1) : "--/--/----")
                    + " au " + (d2 != null ? df.format(d2) : "--/--/----"));
        }

        int restantes = v.getPlaces_restantes();
        if (lblPlacesInfo != null) lblPlacesInfo.setText("Places restantes : " + restantes);

        loadImage(v.getImage_url());
        applyStyles(restantes);
        setupHoverEffects();
    }

    private void loadImage(String url) {
        if (imgVoyage == null) return;

        try {
            if (url != null && !url.isBlank()) {
                imgVoyage.setImage(new Image(url, true));
                return;
            }
        } catch (Exception ignored) {}

        URL fallback = getClass().getResource("/images/default_trip.jpg");
        if (fallback != null) imgVoyage.setImage(new Image(fallback.toExternalForm()));
    }

    private void applyStyles(int restantes) {
        if (restantes <= 0) {
            if (badgeStatus != null) {
                badgeStatus.setText("COMPLET");
                badgeStatus.setStyle("-fx-background-color:#FEE2E2; -fx-text-fill:#EF4444; -fx-background-radius:14; -fx-padding:3 10; -fx-font-weight:bold; -fx-font-size:11;");
            }
            if (btnReserver != null) {
                btnReserver.setText("Plein");
                btnReserver.setDisable(true);
                btnReserver.setStyle("-fx-background-color:#E2E8F0; -fx-text-fill:#94A3B8; -fx-background-radius:12; -fx-font-weight:bold;");
            }
        } else {
            if (badgeStatus != null) {
                badgeStatus.setText("DISPONIBLE");
                badgeStatus.setStyle("-fx-background-color:#DCFCE7; -fx-text-fill:#059669; -fx-background-radius:14; -fx-padding:3 10; -fx-font-weight:bold; -fx-font-size:11;");
            }
            if (btnReserver != null) {
                btnReserver.setText("Réserver");
                btnReserver.setDisable(false);
                btnReserver.setStyle("-fx-background-color:#E8B156; -fx-text-fill:white; -fx-background-radius:12; -fx-font-weight:bold; -fx-cursor: hand;");
            }
        }
    }

    private void setupHoverEffects() {
        if (cardContainer == null) return;

        String normal =
                "-fx-background-color: white; -fx-background-radius: 18; -fx-padding: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.10), 14, 0, 0, 6);";

        String hover =
                "-fx-background-color: white; -fx-background-radius: 18; -fx-padding: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.18), 18, 0, 0, 10);" +
                        "-fx-translate-y: -3; -fx-cursor: hand;";

        cardContainer.setStyle(normal);
        cardContainer.setOnMouseEntered(e -> cardContainer.setStyle(hover));
        cardContainer.setOnMouseExited(e -> cardContainer.setStyle(normal));
    }

    @FXML
    private void handleReserver() {
        if (voyage == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReserverVoyage.fxml"));
            Parent root = loader.load();

            ReserverVoyageController ctrl = loader.getController();
            if (ctrl != null) ctrl.initData(voyage);

            Scene scene = btnReserver.getScene();
            scene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // (Optionnel) si tu veux garder Details, tu peux le remettre
    @FXML
    private void handleDetails() {
        if (voyage == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsVoyageUser.fxml"));
            Parent root = loader.load();
            DetailsVoyageUserController ctrl = loader.getController();
            if (ctrl != null) ctrl.initData(voyage);

            Stage stage = new Stage();
            stage.setTitle("Détails - " + safe(voyage.getDestination()));
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LocalDate toLocalDate(Date d) {
        return (d == null) ? null : d.toLocalDate();
    }
    private String safe(String s) { return (s == null) ? "" : s; }
}