package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entites.Voyage;
import tn.esprit.services.VoyageService;

import java.io.IOException;
import java.util.Optional;

public class DetailsVoyageController {

    @FXML private Label lblTitre, lblStatut, lblDest, lblDates, lblPrix, lblPlaces;
    @FXML private ImageView imgVoyage;
    @FXML private Label lblDescription;

    private Voyage currentVoyage;
    private final VoyageService vs = new VoyageService();

    public void initData(Voyage v) {
        this.currentVoyage = v;
        lblTitre.setText(v.getDestination().toUpperCase());
        lblDest.setText(v.getDestination());
        lblDescription.setText(v.getDescription());
        lblDates.setText("Du " + v.getDate_depart() + " au " + v.getDate_retour());
        lblPrix.setText(v.getPrix() + " DT");
        lblPlaces.setText(v.getPlaces_restantes() + "/" + v.getPlaces_total());

        // Badge dynamique
        if (v.getPlaces_restantes() == 0) {
            lblStatut.setText("COMPLET");
            lblStatut.setStyle("-fx-background-color: #FED7D7; -fx-text-fill: #C5302E;");
        }

        if (v.getImage_url() != null && !v.getImage_url().isEmpty()) {
            imgVoyage.setImage(new Image(v.getImage_url()));
        }
    }


    @FXML
    void handleSupprimer() {
        // Création de l'alerte personnalisée (comme sur ton image)
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer ce voyage ?");
        alert.setContentText("Voyage : " + currentVoyage.getDestination() + "\nID : " + currentVoyage.getId());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            vs.supprimer(currentVoyage.getId());
            handleFermer();
            // Note : Il faudra rafraîchir la liste principale après ça
        }
    }

    @FXML
    void handleModifier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterVoyage.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la fenêtre d'ajout/modif
            AjouterVoyageController controller = loader.getController();
            controller.prepareModif(currentVoyage);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Voyage - ID: " + currentVoyage.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Fermer la fenêtre de détails après modification
            handleFermer();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML void handleFermer() {
        ((Stage) lblTitre.getScene().getWindow()).close();
    }
}