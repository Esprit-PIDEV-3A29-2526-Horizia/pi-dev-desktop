package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    // On récupère le conteneur principal pour changer de vue (Catégories)
    @FXML private VBox mainContainer;
    @FXML private Button btnDeconnexion;
    private final VoyageService vs = new VoyageService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshVoyages(vs.afficher());
    }

    // --- NAVIGATION ---

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        try {
            // 1. Charger la page de connexion
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));

            // 2. Récupérer la fenêtre (Stage) actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 3. Remplacer la scène par celle du Login
            stage.setScene(new Scene(root));
            stage.setTitle("Horizia - Connexion");
            stage.show();

            System.out.println("Déconnexion réussie !");
        } catch (IOException e) {
            System.err.println("Erreur lors de la déconnexion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void naviguerCategories() {
        try {
            // On remplace le contenu actuel par la gestion des catégories
            Parent root = FXMLLoader.load(getClass().getResource("/GestionCategorie.fxml"));
            // Utilise la scène actuelle pour changer de racine
            gridVoyages.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur navigation catégories: " + e.getMessage());
        }
    }

    @FXML
    private void ouvrirFormulaireAjout() {
        try {
            // Ouvre le formulaire d'ajout dans une fenêtre surgissante (Pop-up)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterVoyage.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter un nouveau voyage");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Rafraîchir la liste après la fermeture de la fenêtre d'ajout
            refreshVoyages(vs.afficher());
        } catch (IOException e) {
            System.err.println("Erreur ouverture formulaire: " + e.getMessage());
        }
    }

    // --- LOGIQUE VOYAGE (Inchangée mais stabilisée) ---

    @FXML
    private void handleRecherche(KeyEvent event) {
        String keyword = tfRecherche.getText();
        List<Voyage> result = (keyword == null || keyword.isBlank()) ? vs.afficher() : vs.rechercher(keyword);
        refreshVoyages(result);
    }

    public void refreshVoyages(List<Voyage> voyages) {
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
                controller.setParentController(this);
            }
            Stage stage = new Stage();
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
}