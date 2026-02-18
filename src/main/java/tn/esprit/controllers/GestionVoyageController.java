package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GestionVoyageController implements Initializable {

    @FXML private Label lblDestActive;
    @FXML private Label lblPlacesTotales;
    @FXML private Label lblPromo;
    @FXML private TextField tfRecherche;
    @FXML private FlowPane gridVoyages;
    @FXML private ComboBox<String> comboTri; // Nouveau : Menu de tri

    private final VoyageService vs = new VoyageService();
    private List<Voyage> listeOriginale = new ArrayList<>(); // Stockage pour filtrage rapide

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation du ComboBox de tri
        if (comboTri != null) {
            comboTri.getItems().addAll("Prix : Croissant", "Prix : Décroissant", "Date : Plus proche");
            comboTri.setOnAction(e -> appliquerFiltresEtTris());
        }

        chargerDonnees();
    }

    private void chargerDonnees() {
        listeOriginale = vs.afficher();
        refreshVoyages(listeOriginale);
    }

    @FXML
    private void handleRecherche(KeyEvent event) {
        appliquerFiltresEtTris();
    }

    /**
     * Centralise la logique de recherche et de tri pour éviter les conflits
     */
    private void appliquerFiltresEtTris() {
        String keyword = tfRecherche.getText().toLowerCase();

        // 1. Filtrage par texte (Destination)
        List<Voyage> resultats = listeOriginale.stream()
                .filter(v -> v.getDestination().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        // 2. Application du Tri si sélectionné
        String tri = comboTri.getValue();
        if (tri != null) {
            switch (tri) {
                case "Prix : Croissant" -> resultats.sort(Comparator.comparingDouble(Voyage::getPrix));
                case "Prix : Décroissant" -> resultats.sort(Comparator.comparingDouble(Voyage::getPrix).reversed());
                case "Date : Plus proche" -> resultats.sort(Comparator.comparing(Voyage::getDate_depart));
            }
        }

        refreshVoyages(resultats);
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

    // --- Méthodes de Navigation et Stats (Inchangées mais nécessaires) ---

    private void updateStats(List<Voyage> voyages) {
        if (lblDestActive != null) lblDestActive.setText(String.valueOf(voyages.size()));
        if (lblPlacesTotales != null) {
            int total = voyages.stream().mapToInt(Voyage::getPlaces_total).sum();
            lblPlacesTotales.setText(String.valueOf(total));
        }
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Horizia - Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void naviguerCategories() { changerScene("/GestionCategorie.fxml"); }
    @FXML private void naviguerReservations() { changerScene("/GestionReservationsAdmin.fxml"); }

    private void changerScene(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            gridVoyages.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterVoyage.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            chargerDonnees(); // Rafraîchir après ajout
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
            chargerDonnees();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}