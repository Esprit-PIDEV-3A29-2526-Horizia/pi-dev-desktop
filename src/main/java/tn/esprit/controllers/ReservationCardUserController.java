package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.entites.Reservation;

public class ReservationCardUserController {
    @FXML private Label lblDestination, lblDate, lblPersonnes, lblStatut;

    public void setData(Reservation res) {
        // Destination en gras comme sur le modèle admin
        lblDestination.setText(res.getDestination().toUpperCase());
        lblDestination.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        lblDate.setText("Réserve le : " + res.getDate_reservation());
        lblPersonnes.setText(res.getNbr_personnes() + " personnes");

        // Style du badge selon le statut (Couleurs pastel)
        String s = res.getStatut().toLowerCase();
        lblStatut.setText(res.getStatut().toUpperCase());

        if (s.contains("confir")) {
            lblStatut.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A; -fx-background-radius: 10; -fx-padding: 4 12; -fx-font-weight: bold;");
        } else if (s.contains("atten")) {
            lblStatut.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-background-radius: 10; -fx-padding: 4 12; -fx-font-weight: bold;");
        } else {
            lblStatut.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-background-radius: 10; -fx-padding: 4 12; -fx-font-weight: bold;");
        }
    }
}