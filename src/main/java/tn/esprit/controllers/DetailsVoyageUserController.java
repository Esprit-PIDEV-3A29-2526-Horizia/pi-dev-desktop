package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.entites.Voyage;

public class DetailsVoyageUserController {

    @FXML private Label lblTitre, lblDescription, lblDates, lblPrix, lblPlaces;
    @FXML private Label lblPrixUnitaire, lblTotal, badgeStatus;
    @FXML private Spinner<Integer> spNbPersonnes;
    @FXML private ImageView imgVoyage;

    private Voyage voyage;

    public void initData(Voyage v) {
        this.voyage = v;

        lblTitre.setText("Voyage à " + v.getDestination());
        lblDescription.setText(v.getDescription());
        lblDates.setText("📅 " + v.getDate_depart() + " → " + v.getDate_retour());
        lblPrix.setText("💰 " + v.getPrix() + " DT / pers");
        lblPlaces.setText("👥 " + v.getPlaces_restantes() + " places restantes");

        lblPrixUnitaire.setText(v.getPrix() + " DT");
        lblTotal.setText(v.getPrix() + " DT");

        imgVoyage.setImage(new Image(v.getImage_url(), true));

        spNbPersonnes.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1, v.getPlaces_restantes(), 1
                )
        );

        spNbPersonnes.valueProperty().addListener((obs, o, n) ->
                lblTotal.setText((n * v.getPrix()) + " DT")
        );
    }

    @FXML
    private void confirmerReservation() {
        System.out.println("Réservation confirmée !");
    }

    @FXML
    private void retourCatalogue() {
        // navigation back
    }
}