package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entites.Voyage;

import java.io.IOException;

public class VoyageCardUserController {

    @FXML private VBox cardContainer; // Assurez-vous d'ajouter fx:id="cardContainer" à la VBox racine dans le FXML
    @FXML private ImageView imgVoyage;
    @FXML private Label lblDestination, lblPrix, lblDates, lblPlacesInfo, badgeStatus;
    @FXML private ProgressBar progressPlaces;
    @FXML private Button btnReserver;

    private Voyage voyage;

    public void setData(Voyage v) {
        this.voyage = v;
        lblDestination.setText(v.getDestination());
        lblPrix.setText(v.getPrix() + " DT");
        lblDates.setText("Du " + v.getDate_depart() + " au " + v.getDate_retour());
        lblPlacesInfo.setText("Places restantes : " + v.getPlaces_restantes() + " / " + v.getPlaces_total());

        // Chargement de l'image (Oublié dans votre version précédente)
        if (v.getImage_url() != null && !v.getImage_url().isEmpty()) {
            try {
                imgVoyage.setImage(new Image(v.getImage_url(), true));
            } catch (Exception e) {
                System.err.println("Erreur chargement image voyage : " + v.getDestination());
            }
        }

        // Logique de la barre de progression
        int restantes = v.getPlaces_restantes();
        int totales = v.getPlaces_total();
        double ratio = (double) (totales - restantes) / totales;
        progressPlaces.setProgress(ratio);

        // STYLE DYNAMIQUE ET BADGES
        applyStyles(restantes, ratio);

        // AJOUT DES EFFETS INTERACTIFS
        setupHoverEffects();
    }

    private void applyStyles(int restantes, double ratio) {
        // Style de la barre
        if (ratio > 0.8) {
            progressPlaces.setStyle("-fx-accent: #E8B156;"); // Presque plein (Orange)
        } else {
            progressPlaces.setStyle("-fx-accent: #10B981;"); // Beaucoup de place (Vert)
        }

        // Style du badge
        if (restantes <= 0) {
            badgeStatus.setText("COMPLET");
            badgeStatus.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-background-radius: 15; -fx-padding: 2 10; -fx-font-weight: bold;");
            btnReserver.setText("Plein");
            btnReserver.setDisable(true);
            btnReserver.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #94A3B8; -fx-background-radius: 10;");
        } else {
            badgeStatus.setText("DISPONIBLE");
            badgeStatus.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #059669; -fx-background-radius: 15; -fx-padding: 2 10; -fx-font-weight: bold;");
            btnReserver.setDisable(false);
        }
    }

    private void setupHoverEffects() {
        if (cardContainer == null) return;

        cardContainer.setOnMouseEntered(e -> {
            cardContainer.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 10); " +
                    "-fx-translate-y: -5; -fx-cursor: hand;");
        });

        cardContainer.setOnMouseExited(e -> {
            cardContainer.setStyle("-fx-background-color: white; -fx-background-radius: 20; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 5); " +
                    "-fx-translate-y: 0;");
        });
    }

    @FXML
    private void handleDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsVoyageUser.fxml"));
            Parent root = loader.load();
            DetailsVoyageUserController ctrl = loader.getController();
            ctrl.initData(this.voyage);

            Stage stage = new Stage();
            stage.setTitle("Détails - " + voyage.getDestination());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReserver() {
        try {
            // 1. Charger le fichier FXML que vous venez de me donner
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReserverVoyage.fxml"));
            Parent root = loader.load();

            // 2. Récupérer le contrôleur pour lui envoyer les infos du voyage
            ReserverVoyageController ctrl = loader.getController();
            ctrl.initData(this.voyage); // <-- IMPORTANT: cette méthode initialise le voyage

            // 3. Remplacer le contenu de la fenêtre actuelle
            Scene scene = btnReserver.getScene(); // btnReserver est le bouton sur lequel on clique
            scene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur : Impossible de charger ReserverVoyage.fxml");
        }
    }
}