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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class DetailsVoyageController {

    @FXML private Label lblTitre, lblStatut, lblDest, lblDates, lblPrix, lblPlaces;
    @FXML private ImageView imgVoyage;
    @FXML private Label lblDescription;

    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    private Voyage currentVoyage;
    private final VoyageService vs = new VoyageService();
    private GestionVoyageController parentController;

    public void setModeUser() {
        if (btnModifier != null) btnModifier.setVisible(false);
        if (btnSupprimer != null) btnSupprimer.setVisible(false);
        // optionnel : retire l'espace
        if (btnModifier != null) btnModifier.setManaged(false);
        if (btnSupprimer != null) btnSupprimer.setManaged(false);
    }

    public void setParentController(GestionVoyageController parentController) {
        this.parentController = parentController;
    }
    public void initData(Voyage v) {
        if (v == null) return;
        this.currentVoyage = v;
        String titre = (v.getTitre() != null && !v.getTitre().isBlank())
                ? v.getTitre()
                : v.getDestination();
        lblTitre.setText(titre == null ? "" : titre.toUpperCase());
        lblDest.setText(nvl(v.getDestination()));
        lblDescription.setText(nvl(v.getDescription()));
        String d1 = (v.getDate_depart() != null) ? v.getDate_depart().toString() : "--";
        String d2 = (v.getDate_retour() != null) ? v.getDate_retour().toString() : "--";
        lblDates.setText("Du " + d1 + " au " + d2);
        lblPrix.setText(v.getPrix() + " DT");
        lblPlaces.setText(v.getPlaces_restantes() + "/" + v.getPlaces_total());
        if (v.getPlaces_restantes() <= 0) {
            lblStatut.setText("COMPLET");
            lblStatut.setStyle("-fx-background-color: #FED7D7; -fx-text-fill: #C5302E;");
        } else {
            lblStatut.setText("DISPONIBLE");
            lblStatut.setStyle("-fx-background-color: #C6F6D5; -fx-text-fill: #2F855A;");
        }
        loadImage(v.getImage_url());
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
            System.out.println("Image introuvable: " + path);
        } catch (Exception e) {
            imgVoyage.setImage(null);
            System.out.println("Erreur chargement image: " + path + " | " + e.getMessage());
        }
    }

    @FXML
    void handleSupprimer() {
        if (currentVoyage == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer ce voyage ?");
        alert.setContentText("Voyage : " + nvl(currentVoyage.getDestination()) + "\nID : " + currentVoyage.getId());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            vs.supprimer(currentVoyage.getId());
            handleFermer();
        }
    }

    @FXML
    void handleModifier() {
        if (currentVoyage == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterVoyage.fxml"));
            Parent root = loader.load();
            AjouterVoyageController controller = loader.getController();
            if (controller != null) controller.prepareModif(currentVoyage);
            Stage stage = new Stage();
            stage.setTitle("Modifier le Voyage - ID: " + currentVoyage.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            handleFermer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleFermer() {
        Stage st = (Stage) lblTitre.getScene().getWindow();
        st.close();
    }

    private String nvl(String s) {
        return (s == null) ? "" : s;
    }
}