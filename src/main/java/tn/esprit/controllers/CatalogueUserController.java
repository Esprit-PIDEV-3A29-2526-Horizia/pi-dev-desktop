package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entites.Voyage;
import tn.esprit.services.VoyageService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CatalogueUserController implements Initializable {

    @FXML private GridPane voyageGrid;
    private final VoyageService vs = new VoyageService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        afficherVoyages();
    }

    private void afficherVoyages() {
        voyageGrid.getChildren().clear();

        List<Voyage> voyages = vs.afficher();
        int column = 0;
        int row = 0;

        try {
            for (Voyage v : voyages) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/VoyageCard.fxml"));
                VBox card = loader.load();

                VoyageCardController cardCtrl = loader.getController();
                if (cardCtrl != null) {
                    cardCtrl.setData(v);
                } else {
                    System.err.println("Erreur: fx:controller manquant dans VoyageCard.fxml");
                }

                card.setOnMouseClicked(event -> ouvrirDetailsUser(v));

                voyageGrid.add(card, column++, row);

                if (column == 3) {
                    column = 0;
                    row++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ouvrirDetailsUser(Voyage v) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsVoyage.fxml"));
            Parent root = loader.load();

            DetailsVoyageController controller = loader.getController();
            if (controller != null) {
                controller.initData(v);
                controller.setModeUser(); // ✅ maintenant existe
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du voyage - " + v.getDestination());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- DÉCONNEXION ---
    @FXML
    void handleDeconnexion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Horizia - Connexion");
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur lors du retour au login");
            e.printStackTrace();
        }
    }
}
