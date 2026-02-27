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

public class DetailsVoyageController {

    @FXML private Label lblTitre;
    @FXML private Label lblDestination;
    @FXML private Label lblDescription;
    @FXML private Label lblDates;
    @FXML private Label lblPrix;
    @FXML private Label lblPlaces;
    @FXML private Label lblStatut;
    @FXML private ImageView imgVoyage;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnFermer;

    private Voyage voyageActuel;
    private GestionVoyageController parentController;
    private final VoyageService vs = new VoyageService();

    public void initData(Voyage v) {
        if (v == null) return;

        this.voyageActuel = v;

        try {
            if (lblTitre != null) lblTitre.setText(v.getTitre() != null ? v.getTitre() : "Sans titre");
            if (lblDestination != null) lblDestination.setText(v.getDestination() != null ? v.getDestination() : "Non spécifiée");
            if (lblDescription != null) lblDescription.setText(v.getDescription() != null ? v.getDescription() : "Aucune description disponible");
            if (lblDates != null) lblDates.setText("Du " + v.getDate_depart() + " au " + v.getDate_retour());
            if (lblPrix != null) lblPrix.setText(String.format("%.2f DT", v.getPrix()));
            if (lblPlaces != null) lblPlaces.setText(v.getPlaces_restantes() + "/" + v.getPlaces_total() + " places");

            if (lblStatut != null) {
                String statut = v.getPlaces_restantes() > 0 ? "DISPONIBLE" : "COMPLET";
                String couleur = v.getPlaces_restantes() > 0 ? "#E3D9C6" : "#C5302E";
                String textColor = v.getPlaces_restantes() > 0 ? "#7A6B4B" : "white";
                lblStatut.setText(statut);
                lblStatut.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: " + textColor +
                        "; -fx-padding: 5 15; -fx-background-radius: 10; -fx-font-weight: bold;");
            }

            if (imgVoyage != null && v.getImage_url() != null && !v.getImage_url().isEmpty()) {
                try {
                    Image img = new Image(v.getImage_url(), true);
                    imgVoyage.setImage(img);
                } catch (Exception e) {
                    System.err.println("Erreur chargement image: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur dans initData: " + e.getMessage());
        }
    }

    public void setParentController(GestionVoyageController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleModifier() {
        if (voyageActuel == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterVoyage.fxml"));
            Parent modifView = loader.load();

            AjouterVoyageController controller = loader.getController();
            controller.prepareModif(voyageActuel);
            controller.setParentController(parentController);
            controller.setOnVoyageAjouteCallback(() -> {
                if (parentController != null) {
                    parentController.retourALaListe();
                }
            });

            if (parentController != null && parentController.getMainBorderPane() != null) {
                parentController.getMainBorderPane().setCenter(modifView);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupprimer() {
        if (voyageActuel == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le voyage");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + voyageActuel.getTitre() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            vs.supprimer(voyageActuel.getId());

            if (parentController != null) {
                parentController.retourALaListe();
            }
        }
    }

    @FXML
    private void handleFermer() {
        if (parentController != null) {
            parentController.retourALaListe();
        }
    }
}