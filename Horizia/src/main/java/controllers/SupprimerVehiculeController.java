package controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Marque;
import org.example.entities.Modele;
import org.example.entities.Vehicule;
import org.example.services.MarqueService;
import org.example.services.ModeleService;
import org.example.services.VehiculeService;

import java.util.List;

public class SupprimerVehiculeController {

    @FXML private TextField txtRechercheImmat;
    @FXML private Label lblMessageRecherche;
    @FXML private VBox vboxConfirmation;

    @FXML private Label lblImmat;
    @FXML private Label lblModele;
    @FXML private Label lblAnnee;
    @FXML private Label lblEtat;
    @FXML private Label lblKilometrage;
    @FXML private Label lblPrix;
    @FXML private Label lblMessage;

    private VehiculeService vehiculeService;
    private MarqueService marqueService;
    private ModeleService modeleService;
    private Vehicule vehiculeCourant;

    @FXML
    public void initialize() {
        vehiculeService = new VehiculeService();
        marqueService = new MarqueService();
        modeleService = new ModeleService();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Supprimer Véhicule - Chargée");
        System.out.println("═══════════════════════════════════════════════");

        txtRechercheImmat.requestFocus();
    }

    @FXML
    private void rechercherVehicule() {
        String immat = txtRechercheImmat.getText();

        if (immat == null || immat.trim().isEmpty()) {
            afficherErreurRecherche("⚠️ Veuillez saisir une immatriculation !");
            return;
        }

        System.out.println("→ Recherche du véhicule : " + immat);

        List<Vehicule> vehicules = vehiculeService.rechercherParImmatriculation(immat.trim());

        if (vehicules.isEmpty()) {
            afficherErreurRecherche("✗ Aucun véhicule trouvé avec cette immatriculation !");
            cacherConfirmation();
            return;
        }

        vehiculeCourant = vehicules.get(0);
        System.out.println("✓ Véhicule trouvé : " + vehiculeCourant.getImmatriculation());

        afficherSuccesRecherche("✓ Véhicule trouvé ! Vérifiez les détails ci-dessous.");
        afficherDetails();
        afficherConfirmation();
    }

    private void afficherDetails() {
        lblImmat.setText(vehiculeCourant.getImmatriculation());

        // Récupérer modèle et marque
        Modele modele = modeleService.getModeleById(vehiculeCourant.getIdModele());
        if (modele != null) {
            Marque marque = marqueService.getMarqueById(modele.getIdMarque());
            if (marque != null) {
                lblModele.setText(marque.getNomMarque() + " " + modele.getNomModele());
            }
        }

        lblAnnee.setText(String.valueOf(vehiculeCourant.getAnnee()));
        lblEtat.setText(vehiculeCourant.getEtat());
        lblKilometrage.setText(String.format("%,d km", vehiculeCourant.getKilometrage()));
        lblPrix.setText(String.format("%.3f TND", vehiculeCourant.getPrixParJour()));

        System.out.println("✓ Détails affichés");
    }

    @FXML
    private void confirmerSuppression() {
        if (vehiculeCourant == null) {
            afficherErreur("⚠️ Aucun véhicule sélectionné !");
            return;
        }

        System.out.println("→ Suppression du véhicule : " + vehiculeCourant.getImmatriculation());

        boolean succes = vehiculeService.supprimerVehicule(vehiculeCourant.getIdVehicule());

        if (succes) {
            System.out.println("✓ Véhicule supprimé avec succès");
            afficherSucces("✓ Le véhicule a été supprimé définitivement !");

            // Fermer automatiquement après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        Stage stage = (Stage) txtRechercheImmat.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            System.err.println("✗ Échec de la suppression");
            afficherErreur("✗ Impossible de supprimer ce véhicule !\n" +
                    "Il est peut-être lié à des locations existantes.");
        }
    }

    private void afficherConfirmation() {
        vboxConfirmation.setVisible(true);
        vboxConfirmation.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(400), vboxConfirmation);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void cacherConfirmation() {
        vboxConfirmation.setVisible(false);
        vboxConfirmation.setManaged(false);
    }

    @FXML
    private void annuler() {
        System.out.println("✖ Annulation de la suppression");
        Stage stage = (Stage) txtRechercheImmat.getScene().getWindow();
        stage.close();
    }

    private void afficherSuccesRecherche(String message) {
        lblMessageRecherche.setText(message);
        lblMessageRecherche.setStyle(
                "-fx-text-fill: #27ae60; " +
                        "-fx-background-color: #d5f4e6; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-color: #27ae60; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8;"
        );
        lblMessageRecherche.setVisible(true);
        lblMessageRecherche.setManaged(true);
    }

    private void afficherErreurRecherche(String message) {
        lblMessageRecherche.setText(message);
        lblMessageRecherche.setStyle(
                "-fx-text-fill: #e74c3c; " +
                        "-fx-background-color: #fadbd8; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-color: #e74c3c; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8;"
        );
        lblMessageRecherche.setVisible(true);
        lblMessageRecherche.setManaged(true);
    }

    private void afficherSucces(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle(
                "-fx-text-fill: #27ae60; " +
                        "-fx-background-color: #d5f4e6; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-color: #27ae60; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8;"
        );
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void afficherErreur(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle(
                "-fx-text-fill: #e74c3c; " +
                        "-fx-background-color: #fadbd8; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-color: #e74c3c; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8;"
        );
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }
}