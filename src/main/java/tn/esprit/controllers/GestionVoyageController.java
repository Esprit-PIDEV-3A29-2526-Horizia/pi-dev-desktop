package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
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

public class GestionVoyageController implements Initializable {

    @FXML private Label lblDestActive;
    @FXML private Label lblPlacesTotales;
    @FXML private Label lblPromo;
    @FXML private TextField tfRecherche;
    @FXML private FlowPane gridVoyages;
    @FXML private ComboBox<String> comboTri;
    @FXML private BorderPane mainBorderPane;

    private final VoyageService vs = new VoyageService();
    private List<Voyage> listeOriginale = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (comboTri != null) {
            comboTri.getItems().addAll(
                    "Tous",
                    "Prix : Croissant",
                    "Prix : Décroissant",
                    "Date : Plus proche"
            );
            comboTri.setValue("Tous");
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

    private void appliquerFiltresEtTris() {
        String keyword = tfRecherche.getText().toLowerCase();
        List<Voyage> resultats = listeOriginale.stream()
                .filter(v -> v.getDestination().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        String tri = comboTri.getValue();
        if (tri != null) {
            switch (tri) {
                case "Tous" -> {}
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
                }
                gridVoyages.getChildren().add(card);
            }
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
        long promoCount = voyages.stream().filter(v -> v.getPrix() <= 2000).count();
        if (lblPromo != null) lblPromo.setText(String.valueOf(promoCount));
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

    @FXML private void naviguerCategories() {
        changerScene("/GestionCategorie.fxml");
    }

    @FXML private void naviguerReservations() {
        changerScene("/GestionReservationsAdmin.fxml");
    }

    private void changerScene(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            if (mainBorderPane != null && mainBorderPane.getScene() != null) {
                mainBorderPane.getScene().setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirFormulaireAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterVoyage.fxml"));
            Parent ajoutView = loader.load();

            AjouterVoyageController controller = loader.getController();
            controller.setParentController(this);
            controller.setOnVoyageAjouteCallback(() -> {
                retourALaListe();
            });

            if (mainBorderPane != null) {
                mainBorderPane.setCenter(ajoutView);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ouvrirDetailsDansMemeFenetre(Voyage voyage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsVoyage.fxml"));
            Parent detailsView = loader.load();

            DetailsVoyageController controller = loader.getController();
            if (controller != null) {
                controller.initData(voyage);
                controller.setParentController(this);
            }

            if (mainBorderPane != null) {
                mainBorderPane.setCenter(detailsView);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void retourALaListe() {
        // Recharger les données
        chargerDonnees();

        // Recharger la vue de la liste
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionVoyage.fxml"));
            Parent root = loader.load();

            GestionVoyageController newController = loader.getController();

            if (mainBorderPane != null) {
                // Remplacer uniquement le centre
                if (newController.getMainBorderPane() != null) {
                    mainBorderPane.setCenter(newController.getMainBorderPane().getCenter());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BorderPane getMainBorderPane() {
        return mainBorderPane;
    }
}