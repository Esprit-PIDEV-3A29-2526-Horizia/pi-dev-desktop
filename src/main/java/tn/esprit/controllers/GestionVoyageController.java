package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entites.Voyage;
import tn.esprit.services.CategorieService;
import tn.esprit.services.VoyageService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GestionVoyageController {

    @FXML private FlowPane gridVoyages;
    @FXML private TextField tfRecherche;
    @FXML private Label lblDestActive, lblPlacesTotales, lblPromo;

    private final VoyageService vs = new VoyageService();
    private final CategorieService cs = new CategorieService();
    private final ObservableList<Voyage> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadData();
        setupSearch();
    }

    private void loadData() {
        masterData.setAll(vs.afficher());
        renderGrid(masterData);
        updateStats();
    }

    private void renderGrid(List<Voyage> voyages) {
        gridVoyages.getChildren().clear();
        for (Voyage v : voyages) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/VoyageCard.fxml"));
                VBox card = loader.load();

                // 1. Liaison du bouton Détails (IMPORTANT)
                Button btnDetails = (Button) card.lookup("#btnDetails");
                if (btnDetails != null) {
                    btnDetails.setOnAction(event -> {
                        System.out.println("Clic sur Détails pour : " + v.getDestination());
                        ouvrirDetails(v);
                    });
                } else {
                    System.out.println("⚠️ Alerte : Le bouton avec l'ID #btnDetails est introuvable dans VoyageCard.fxml");
                }

                // 2. Récupération et remplissage des autres champs
                Label titre = (Label) card.lookup("#lblTitre");
                Label destination = (Label) card.lookup("#lblDestination");
                Label prix = (Label) card.lookup("#lblPrix");
                Label dates = (Label) card.lookup("#lblDate");
                Label placesLabel = (Label) card.lookup("#lblPlaces");
                ProgressBar progress = (ProgressBar) card.lookup("#progressPlaces");
                ImageView imgView = (ImageView) card.lookup("#imgVoyage");

                if (titre != null) titre.setText(v.getDestination());
                if (destination != null) destination.setText("Destination : " + v.getDestination());
                if (prix != null) prix.setText(v.getPrix() + " DT");
                if (dates != null) dates.setText("Du " + v.getDate_depart() + " au " + v.getDate_retour());

                // Gestion des places
                int total = v.getPlaces_total();
                int restantes = v.getPlaces_restantes();
                int occupees = total - restantes;

                if (placesLabel != null) placesLabel.setText(occupees + "/" + total + " places");
                if (progress != null && total > 0) {
                    double ratio = (double) occupees / total;
                    progress.setProgress(ratio);
                    progress.setStyle(ratio > 0.9 ? "-fx-accent: #c5302e;" : "-fx-accent: #E8B156;");
                }

                // Gestion de l'image
                if (imgView != null) {
                    try {
                        if (v.getImage_url() != null && !v.getImage_url().isEmpty()) {
                            imgView.setImage(new Image(v.getImage_url(), true)); // true pour chargement en arrière-plan
                        } else {
                            imgView.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur image pour " + v.getDestination() + " : " + e.getMessage());
                    }
                }

                gridVoyages.getChildren().add(card);

            } catch (IOException e) {
                System.err.println("Erreur de chargement de VoyageCard.fxml");
                e.printStackTrace();
            }
        }
    }

    // --- NOUVELLE MÉTHODE POUR AFFICHER LES DÉTAILS ---
    private void ouvrirDetails(Voyage v) {
        try {
            // Chargement du FXML de la fenêtre de détails
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsVoyage.fxml"));
            Parent root = loader.load();

            // Envoi des données au contrôleur de la fenêtre de détails
            DetailsVoyageController controller = loader.getController();
            controller.initData(v);

            // Création et affichage de la fenêtre (Stage)
            Stage stage = new Stage();
            stage.setTitle("Détails - " + v.getDestination());
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL); // Bloque la fenêtre principale
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur chargement DetailsVoyage.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        if (tfRecherche != null) {
            tfRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
                List<Voyage> filtered = masterData.stream()
                        .filter(v -> v.getDestination().toLowerCase().contains(newVal.toLowerCase()))
                        .collect(Collectors.toList());
                renderGrid(filtered);
            });
        }
    }

    private void updateStats() {
        if (lblDestActive != null) lblDestActive.setText(String.valueOf(masterData.size()));
        if (lblPlacesTotales != null) {
            int totalSum = masterData.stream().mapToInt(Voyage::getPlaces_total).sum();
            lblPlacesTotales.setText(String.valueOf(totalSum));
        }
        if (lblPromo != null) {
            long promoCount = masterData.stream().filter(v -> v.getPrix() < 500).count();
            lblPromo.setText(String.valueOf(promoCount));
        }
    }

    @FXML
    void naviguerCategories(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/GestionCategorie.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    void ouvrirFormulaireAjout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterVoyage.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nouveau Voyage");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}