package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.entites.Voyage;
import tn.esprit.services.VoyageService;

import java.io.IOException;
import java.util.Optional;

public class VoyageCardController {

    @FXML private Label lblTitre;
    @FXML private Label lblDestination;
    @FXML private Label lblDate;
    @FXML private Label lblPrix;
    @FXML private Label lblPlaces;
    @FXML private ImageView imgVoyage;
    @FXML private Button btnDetails;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    private Voyage voyage;
    private GestionVoyageController parentController;
    private final VoyageService voyageService = new VoyageService();

    public void setData(Voyage v) {
        this.voyage = v;

        if (v != null) {
            lblTitre.setText(v.getTitre() != null ? v.getTitre() : "Sans titre");
            lblDestination.setText("Destination : " + (v.getDestination() != null ? v.getDestination() : "Non spécifiée"));
            lblDate.setText("Du " + v.getDate_depart() + " au " + v.getDate_retour());
            lblPrix.setText(String.format("%.0f DT", v.getPrix()));
            lblPlaces.setText(v.getPlaces_restantes() + "/" + v.getPlaces_total() + " places");

            // Changer la couleur selon disponibilité
            if (v.getPlaces_restantes() <= 0) {
                lblPlaces.setStyle("-fx-text-fill: #C5302E; -fx-font-size: 12; -fx-font-weight: bold;");
            } else {
                lblPlaces.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12; -fx-font-weight: bold;");
            }

            // Charger l'image
            if (v.getImage_url() != null && !v.getImage_url().isEmpty()) {
                try {
                    Image img = new Image(v.getImage_url(), true);
                    imgVoyage.setImage(img);
                } catch (Exception e) {
                    System.err.println("❌ Erreur chargement image: " + e.getMessage());
                    imgVoyage.setImage(null);
                }
            }
        }
    }

    public void setParentController(GestionVoyageController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleDetails() {
        if (voyage != null && parentController != null) {
            parentController.ouvrirDetailsDansMemeFenetre(voyage);
        }
    }

    @FXML
    private void handleModifier() {
        if (voyage == null || parentController == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjouterVoyage.fxml"));
            Parent root = loader.load();

            AjouterVoyageController controller = loader.getController();
            controller.prepareModif(voyage);
            controller.setOnVoyageAjouteCallback(() -> {
                if (parentController != null) {
                    parentController.retourALaListe();
                }
            });

            if (parentController.getMainBorderPane() != null) {
                parentController.getMainBorderPane().setCenter(root);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupprimer() {
        if (voyage == null || parentController == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le voyage");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le voyage \"" + voyage.getTitre() + "\" ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Supprimer de la base de données
            voyageService.supprimer(voyage.getId());

            // Rafraîchir la liste
            parentController.retourALaListe();
        }
    }
}