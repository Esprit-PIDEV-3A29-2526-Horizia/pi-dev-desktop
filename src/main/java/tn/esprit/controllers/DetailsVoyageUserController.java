package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.entites.Voyage;

public class DetailsVoyageUserController {

    @FXML private Label lblTitre, lblPrix, lblDates, lblPlaces, badgeStatus;
    @FXML private ImageView imgVoyage;
    @FXML private Text txtDescription;
    @FXML private Button btnReserver;


    private Voyage voyage;

    public void initData(Voyage v) {
        this.voyage = v;
        lblTitre.setText(v.getDestination().toUpperCase());
        lblPrix.setText(v.getPrix() + " DT");
        lblDates.setText("Du " + v.getDate_depart() + " au " + v.getDate_retour());
        lblPlaces.setText(v.getPlaces_restantes() + " / " + v.getPlaces_total());
        txtDescription.setText(v.getDescription());
        if (v.getPlaces_restantes() <= 0) {
            badgeStatus.setText("COMPLET");
            badgeStatus.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-padding: 5 12; -fx-background-radius: 10;");
            btnReserver.setDisable(true);
            btnReserver.setText("Plus de places");
        } else {
            badgeStatus.setText("DISPONIBLE");
            badgeStatus.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A; -fx-padding: 5 12; -fx-background-radius: 10;");
        }
        if (v.getImage_url() != null && !v.getImage_url().isEmpty()) {
            imgVoyage.setImage(new Image(v.getImage_url(), true));
        }
    }

    @FXML
    private void handleReservation() {
        System.out.println("Bouton Réserver cliqué pour : " + voyage.getDestination());
    }

    @FXML
    private void fermer() {
        ((Stage) lblTitre.getScene().getWindow()).close();
    }
}