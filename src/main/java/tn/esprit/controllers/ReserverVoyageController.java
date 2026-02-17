package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entites.Voyage;
import tn.esprit.services.ReservationService;
import java.sql.Date;

public class ReserverVoyageController {

    @FXML private TextField tfNbrPersonnes;
    @FXML private DatePicker dpDepart, dpRetour;

    private Voyage selectedVoyage;
    private final ReservationService rs = new ReservationService();

    public void setData(Voyage v) {
        this.selectedVoyage = v;
        // On pré-remplit les dates avec celles du voyage original par défaut
        dpDepart.setValue(v.getDate_depart().toLocalDate());
        dpRetour.setValue(v.getDate_retour().toLocalDate());
    }

    @FXML
    void handleConfirmer() {
        try {
            int nbr = Integer.parseInt(tfNbrPersonnes.getText());

            // Vérification des places disponibles
            if (nbr <= 0) {
                afficherAlerte("Erreur", "Le nombre de personnes doit être supérieur à 0.");
                return;
            }

            if (nbr > selectedVoyage.getPlaces_restantes()) {
                afficherAlerte("Plus de place", "Il ne reste que " + selectedVoyage.getPlaces_restantes() + " places.");
                return;
            }

            // Ici, idUser est statique pour le moment (ex: 1), il faudra utiliser l'utilisateur connecté plus tard
            rs.effectuerReservation(selectedVoyage.getId(), 1, nbr);

            Alert success = new Alert(Alert.AlertType.INFORMATION, "Votre réservation a été enregistrée !");
            success.showAndWait();
            handleAnnuler();

        } catch (NumberFormatException e) {
            afficherAlerte("Erreur", "Veuillez saisir un nombre valide.");
        }
    }

    @FXML void handleAnnuler() {
        ((Stage) tfNbrPersonnes.getScene().getWindow()).close();
    }

    private void afficherAlerte(String titre, String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }
}