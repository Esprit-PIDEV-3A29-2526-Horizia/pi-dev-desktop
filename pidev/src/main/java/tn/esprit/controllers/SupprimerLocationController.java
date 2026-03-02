package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Location;
import tn.esprit.entities.Vehicule;
import tn.esprit.services.LocationService;
import tn.esprit.services.VehiculeService;

import java.text.SimpleDateFormat;
import java.util.List;

public class SupprimerLocationController {

    @FXML private TextField txtRechercheClient;
    @FXML private Label lblMessageRecherche;
    @FXML private VBox vboxConfirmation;
    @FXML private Label lblClient;
    @FXML private Label lblVehicule;
    @FXML private Label lblDates;
    @FXML private Label lblMontant;
    @FXML private Label lblStatut;
    @FXML private Label lblMessage;

    private LocationService locationService;
    private VehiculeService vehiculeService;
    private Location locationCourante;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        locationService = new LocationService();
        vehiculeService = new VehiculeService();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Supprimer Location - Chargée");
        System.out.println("═══════════════════════════════════════════════");
    }

    @FXML
    private void rechercherLocation() {
        String client = txtRechercheClient.getText();

        if (client == null || client.trim().isEmpty()) {
            afficherErreurRecherche("⚠️ Veuillez saisir un nom de client !");
            return;
        }

        System.out.println("→ Recherche location pour : " + client);

        List<Location> locations = locationService.rechercherParClient(client.trim());

        if (locations.isEmpty()) {
            afficherErreurRecherche("✗ Aucune location trouvée pour ce client !");
            vboxConfirmation.setVisible(false);
            vboxConfirmation.setManaged(false);
            return;
        }

        locationCourante = locations.get(0);
        System.out.println("✓ Location trouvée : #" + locationCourante.getIdLocation());

        afficherSuccesRecherche("✓ Location trouvée !");
        afficherDetails();
        vboxConfirmation.setVisible(true);
        vboxConfirmation.setManaged(true);
    }

    private void afficherDetails() {
        lblClient.setText(locationCourante.getClientNomComplet());

        Vehicule v = vehiculeService.getVehiculeById(locationCourante.getIdVehicule());
        lblVehicule.setText(v != null ? v.getImmatriculation() : "Inconnu");

        lblDates.setText(sdf.format(locationCourante.getDateDebut()) + " → " +
                sdf.format(locationCourante.getDateFinPrev()));

        lblMontant.setText(String.format("%.3f TND", locationCourante.getMontantTotal()));
        lblStatut.setText(locationCourante.getStatut());
    }

    @FXML
    private void confirmerSuppression() {
        if (locationCourante == null) {
            afficherErreur("⚠️ Aucune location sélectionnée !");
            return;
        }

        System.out.println("→ Suppression de la location #" + locationCourante.getIdLocation());

        boolean succes = locationService.supprimerLocation(locationCourante.getIdLocation());

        if (succes) {
            System.out.println("✓ Location supprimée avec succès");
            afficherSucces("✓ La location a été supprimée définitivement !");

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        Stage stage = (Stage) txtRechercheClient.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            System.err.println("✗ Échec de la suppression");
            afficherErreur("✗ Impossible de supprimer cette location !");
        }
    }

    @FXML
    private void annuler() {
        System.out.println("✖ Annulation de la suppression");
        Stage stage = (Stage) txtRechercheClient.getScene().getWindow();
        stage.close();
    }

    private void afficherSuccesRecherche(String msg) {
        lblMessageRecherche.setText(msg);
        lblMessageRecherche.setStyle("-fx-text-fill: #27ae60; -fx-background-color: #d5f4e6; -fx-font-weight: bold; -fx-border-color: #27ae60; -fx-border-width: 2; -fx-border-radius: 8;");
        lblMessageRecherche.setVisible(true);
        lblMessageRecherche.setManaged(true);
    }

    private void afficherErreurRecherche(String msg) {
        lblMessageRecherche.setText(msg);
        lblMessageRecherche.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fadbd8; -fx-font-weight: bold; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
        lblMessageRecherche.setVisible(true);
        lblMessageRecherche.setManaged(true);
    }

    private void afficherSucces(String msg) {
        lblMessage.setText(msg);
        lblMessage.setStyle("-fx-text-fill: #27ae60; -fx-background-color: #d5f4e6; -fx-font-weight: bold; -fx-border-color: #27ae60; -fx-border-width: 2; -fx-border-radius: 8;");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void afficherErreur(String msg) {
        lblMessage.setText(msg);
        lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fadbd8; -fx-font-weight: bold; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }
}