package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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

public class CatalogueUserController implements Initializable {

    @FXML private GridPane voyageGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboTri; // Nouveau : Menu de tri

    private final VoyageService vs = new VoyageService();
    private List<Voyage> listeOriginale = new ArrayList<>(); // Cache pour éviter les appels DB constants

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisation du menu de tri
        if (comboTri != null) {
            comboTri.getItems().addAll("Prix : Croissant", "Prix : Décroissant", "Date : Plus proche");
            comboTri.setOnAction(e -> appliquerFiltresEtTris());
        }

        // Recherche en temps réel lors de la saisie
        searchField.textProperty().addListener((obs, old, newValue) -> appliquerFiltresEtTris());

        chargerDonnees();
    }

    private void chargerDonnees() {
        listeOriginale = vs.afficher();
        chargerVoyages(listeOriginale);
    }

    /**
     * Combine la recherche par texte et le tri sélectionné
     */
    private void appliquerFiltresEtTris() {
        String keyword = searchField.getText().toLowerCase();

        // 1. Filtrage par destination
        List<Voyage> resultats = listeOriginale.stream()
                .filter(v -> v.getDestination().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        // 2. Application du tri
        String tri = (comboTri != null) ? comboTri.getValue() : null;
        if (tri != null) {
            switch (tri) {
                case "Prix : Croissant" -> resultats.sort(Comparator.comparingDouble(Voyage::getPrix));
                case "Prix : Décroissant" -> resultats.sort(Comparator.comparingDouble(Voyage::getPrix).reversed());
                case "Date : Plus proche" -> resultats.sort(Comparator.comparing(Voyage::getDate_depart));
            }
        }

        chargerVoyages(resultats);
    }

    public void chargerVoyages(List<Voyage> voyages) {
        if (voyageGrid == null) return;

        voyageGrid.getChildren().clear();
        int column = 0;
        int row = 0;

        for (Voyage v : voyages) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/VoyageCardUser.fxml"));
                VBox card = loader.load();

                VoyageCardUserController controller = loader.getController();
                if (controller != null) {
                    controller.setData(v); // Applique le texte en gras et les styles
                }

                // Affichage en 3 colonnes comme sur votre capture
                voyageGrid.add(card, column, row);
                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRecherche() {
        appliquerFiltresEtTris();
    }

    @FXML
    private void afficherCatalogue() {
        searchField.clear();
        if(comboTri != null) comboTri.setValue(null);
        chargerDonnees();
    }

    @FXML
    private void afficherHistorique(ActionEvent event) {
        changerScene(event, "/MesReservations.fxml", "Mes Réservations");
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
}