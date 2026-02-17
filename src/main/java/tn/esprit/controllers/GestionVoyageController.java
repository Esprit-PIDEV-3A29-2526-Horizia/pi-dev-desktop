package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entites.Voyage;
import tn.esprit.services.VoyageService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GestionVoyageController implements Initializable {

    @FXML private Label lblDestActive;
    @FXML private Label lblPlacesTotales;
    @FXML private Label lblPromo;
    @FXML private TextField tfRecherche;
    @FXML private FlowPane gridVoyages;

    private final VoyageService vs = new VoyageService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshVoyages(vs.afficher());
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        try {
            // Charge la vue de connexion (Vérifiez si c'est Login.fxml ou Loqin.fxml dans vos dossiers)
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Horizia - Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRecherche(KeyEvent event) {
        String keyword = tfRecherche.getText();
        List<Voyage> result = (keyword == null || keyword.isBlank()) ? vs.afficher() : vs.rechercher(keyword);
        refreshVoyages(result);
    }

    private void refreshVoyages(List<Voyage> voyages) {
        if (gridVoyages == null) return;
        gridVoyages.getChildren().clear();
        updateStats(voyages);

        try {
            for (Voyage v : voyages) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/VoyageCard.fxml"));
                VBox card = loader.load();

                VoyageCardController ctrl = loader.getController();
                if (ctrl != null) {
                    ctrl.setData(v);
                    // Résout l'erreur "Cannot resolve method setParentController"
                    ctrl.setParentController(this);
                    card.setOnMouseClicked(e -> ouvrirDetails(v));
                }
                gridVoyages.getChildren().add(card);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ouvrirDetails(Voyage v) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsVoyage.fxml"));
            Parent root = loader.load();

            DetailsVoyageController controller = loader.getController();
            if (controller != null) {
                controller.initData(v);
                // Résout l'erreur visible sur votre capture image_15f574.jpg
                controller.setParentController(this);
            }

            Stage stage = new Stage();
            stage.setTitle("Détails du voyage - " + v.getDestination());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshVoyages(vs.afficher());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateStats(List<Voyage> voyages) {
        if (lblDestActive != null) lblDestActive.setText(String.valueOf(voyages.size()));
        if (lblPlacesTotales != null) {
            int total = voyages.stream().mapToInt(Voyage::getPlaces_total).sum();
            lblPlacesTotales.setText(String.valueOf(total));
        }
    }

    @FXML private void ouvrirFormulaireAjout() { /* Votre code d'ajout existant */ }
    @FXML private void naviguerCategories() { /* Votre code de navigation existant */ }
}