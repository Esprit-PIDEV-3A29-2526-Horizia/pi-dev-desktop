package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Publication;
import tn.esprit.services.ServicePublication;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class PublicationController implements Initializable {

    @FXML private FlowPane flowPane;
    @FXML private TextField searchField;
    @FXML private Button addButton;

    private ServicePublication service = new ServicePublication();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadPublications();

        // Recherche dynamique
        searchField.textProperty().addListener((obs, oldVal, newVal) -> search(newVal));

        // Bouton ajouter
        addButton.setOnAction(e ->
                DashboardController.getInstance()
                        .loadView("/views/AjoutPublication.fxml"));
    }

    // ================= CHARGER PUBLICATIONS =================
    private void loadPublications() {
        try {
            List<Publication> list = service.afficher();
            display(list);
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    // ================= RECHERCHE =================
    private void search(String keyword) {
        try {
            List<Publication> list = service.rechercher(keyword);
            display(list);
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    // ================= AFFICHAGE =================
    private void display(List<Publication> list) {
        flowPane.getChildren().clear();

        for (Publication p : list) {
            flowPane.getChildren().add(createCard(p));
        }
    }

    // ================= CREATION CARD =================
    private VBox createCard(Publication p) {

        VBox card = new VBox(10);
        card.setStyle("-fx-background-color:white; -fx-padding:15; -fx-background-radius:10;");
        card.setPrefWidth(250);

        // Image
        ImageView img = new ImageView();
        img.setFitWidth(220);
        img.setFitHeight(140);

        if (p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                img.setImage(new Image(p.getImage()));
            } catch (Exception ignored) {}
        }

        // Titre
        Label titre = new Label(p.getTitre());
        titre.setStyle("-fx-font-weight:bold; -fx-font-size:16;");

        // Lieu
        Label lieu = new Label("📍 " + p.getLieu());

        // Prix
        Label prix = new Label("💰 " + p.getTarif() + " DT");

        // Bouton détails
        Button details = new Button("Voir détails");

        details.setOnAction(e -> {
            DashboardController.getInstance().setSelectedPublication(p);
            DashboardController.getInstance()
                    .loadView("/views/DetailsPublication.fxml");
        });

        // Bouton modifier
        Button modifier = new Button("Modifier");

        modifier.setOnAction(e -> {
            DashboardController.getInstance().setSelectedPublication(p);
            DashboardController.getInstance()
                    .loadView("/views/ModifierPublication.fxml");
        });

        // Bouton supprimer
        Button supprimer = new Button("Supprimer");

        supprimer.setOnAction(e -> {
            try {
                service.supprimer(p.getId());
                loadPublications();
            } catch (SQLException ex) {
                showAlert("Erreur", ex.getMessage());
            }
        });

        card.getChildren().addAll(img, titre, lieu, prix, details, modifier, supprimer);

        return card;
    }

    // ================= ALERT =================
    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}