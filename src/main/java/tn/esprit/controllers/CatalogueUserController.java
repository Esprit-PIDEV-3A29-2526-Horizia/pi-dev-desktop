package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
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
    @FXML private TextField searchField;

    private final VoyageService vs = new VoyageService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerVoyages(vs.afficher());
    }

    public void chargerVoyages(List<Voyage> voyages) {
        if (voyageGrid == null) return;
        voyageGrid.getChildren().clear();

        int column = 0;
        int row = 1;

        try {
            for (Voyage v : voyages) {
                // ON CHARGE LE FXML USER ICI
                URL cardResource = getClass().getResource("/VoyageCardUser.fxml");
                if (cardResource == null) {
                    System.err.println("ERREUR : VoyageCardUser.fxml introuvable dans resources !");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(cardResource);
                VBox card = loader.load();

                // ON UTILISE LE CONTROLEUR USER
                VoyageCardUserController ctrl = loader.getController();
                if (ctrl != null) {
                    ctrl.setData(v);
                }

                if (column == 3) {
                    column = 0;
                    row++;
                }
                voyageGrid.add(card, column++, row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void afficherHistorique(ActionEvent event) {
        changerScene(event, "/HistoriqueReservations.fxml", "Mes Réservations");
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        changerScene(event, "/Login.fxml", "Connexion");
    }

    private void changerScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML private void handleRecherche() {
        String keyword = searchField.getText();
        chargerVoyages(vs.rechercher(keyword));
    }

    @FXML private void afficherCatalogue() {
        chargerVoyages(vs.afficher());
    }
}