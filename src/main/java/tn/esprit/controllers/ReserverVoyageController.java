package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.entites.Voyage;
import tn.esprit.services.ReservationService;
import java.io.IOException;

public class ReserverVoyageController {

    @FXML private Label lblTitre, lblDescription, lblPrixUnitaire, lblPrixTotal;
    @FXML private ImageView imgVoyage;
    @FXML private Spinner<Integer> spinnerPlaces;

    private Voyage selectedVoyage;
    private final ReservationService rs = new ReservationService();

    public void initData(Voyage v) {
        if (v == null) return;
        this.selectedVoyage = v;

        // Mise à jour des textes
        lblTitre.setText("Voyage à " + v.getDestination().toUpperCase());
        lblDescription.setText(v.getDescription());
        lblPrixUnitaire.setText(v.getPrix() + " DT");

        // Chargement de l'image
        if (v.getImage_url() != null && !v.getImage_url().isEmpty()) {
            try {
                imgVoyage.setImage(new Image(v.getImage_url(), true));
            } catch (Exception e) {
                System.err.println("Erreur image : " + e.getMessage());
            }
        }

        // Configuration du Spinner
        int max = v.getPlaces_restantes();
        if (max > 0) {
            spinnerPlaces.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, 1));
        } else {
            spinnerPlaces.setDisable(true);
        }

        // Calcul du prix initial
        mettreAJourPrix(1);
        spinnerPlaces.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) mettreAJourPrix(newVal);
        });
    }

    private void mettreAJourPrix(int nb) {
        if (selectedVoyage != null) {
            double total = nb * selectedVoyage.getPrix();
            lblPrixTotal.setText(String.format("%.0f DT", total));
        }
    }

    @FXML
    void confirmerReservation() {
        System.out.println("Clic sur Confirmer détecté !"); //
        try {
            int nbr = spinnerPlaces.getValue();

            // ATTENTION : Vérifiez que la colonne 'id_user' existe dans votre table SQL !
            // L'erreur "Unknown column 'id_user'" vient de là.
            rs.effectuerReservation(selectedVoyage.getId(), 1, nbr);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Réservation réussie !");
            alert.showAndWait();
            retour();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur base de données : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CatalogueUser.fxml"));
            Parent root = loader.load();
            lblTitre.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}