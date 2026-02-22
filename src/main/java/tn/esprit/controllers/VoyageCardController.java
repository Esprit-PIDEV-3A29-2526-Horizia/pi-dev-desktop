package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entites.Voyage;

import java.io.File;
import java.io.InputStream;

public class VoyageCardController {

    @FXML private Label lblTitre, lblDestination, lblDate, lblPrix, lblPlaces;
    @FXML private ImageView imgVoyage;
    @FXML private Button btnDetails;
    private Voyage voyage;
    private GestionVoyageController parentController;

    @FXML
    private void initialize() {
        if (imgVoyage != null) {
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(280, 160);
            clip.setArcWidth(18);
            clip.setArcHeight(18);
            imgVoyage.setClip(clip);
        }
    }
    public void setParentController(GestionVoyageController parentController) {
        this.parentController = parentController;
    }
    public void setData(Voyage v) {
        this.voyage = v;
        //titre fallback si vide
        String titre = (v.getTitre() != null && !v.getTitre().isBlank()) ? v.getTitre() : v.getDestination();
        lblTitre.setText(titre == null ? "" : titre);
        lblDestination.setText(v.getDestination() == null ? "" : v.getDestination());
        lblPrix.setText(v.getPrix() + " DT");
        if (lblDate != null && v.getDate_depart() != null && v.getDate_retour() != null) {
            lblDate.setText("Du " + v.getDate_depart() + " au " + v.getDate_retour());
        }
        if (lblPlaces != null) {
            lblPlaces.setText(v.getPlaces_restantes() + "/" + v.getPlaces_total() + " places");
        }
        loadImage(v.getImage_url());
    }


    @FXML
    private void handleDetails() {
        if (voyage == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsVoyage.fxml"));
            Parent root = loader.load();
            DetailsVoyageController controller = loader.getController();
            if (controller != null) {
                controller.initData(voyage);
            }
            Stage stage = new Stage();
            stage.setTitle("Détails - " + (voyage.getDestination() == null ? "" : voyage.getDestination()));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            Stage owner = (Stage) btnDetails.getScene().getWindow();
            stage.initOwner(owner);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadImage(String path) {
        try {
            if (imgVoyage == null) return;
            if (path == null || path.isBlank()) {
                imgVoyage.setImage(null);
                return;
            }
            String name = path;
            if (name.startsWith("/images/")) name = name.substring("/images/".length());
            InputStream is = getClass().getResourceAsStream("/images/" + name);
            if (is != null) {
                imgVoyage.setImage(new Image(is));
                return;
            }
            if (path.startsWith("file:") || path.startsWith("http")) {
                imgVoyage.setImage(new Image(path, true));
                return;
            }
            File f = new File(path);
            if (f.exists()) {
                imgVoyage.setImage(new Image(f.toURI().toString(), true));
                return;
            }
            imgVoyage.setImage(null);
        } catch (Exception e) {
            imgVoyage.setImage(null);
        }
    }
}
