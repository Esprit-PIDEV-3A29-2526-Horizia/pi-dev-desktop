package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.logement; // Attention à la casse (Logement au lieu de logement)
import tn.esprit.services.Servicelogement;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class LogementsController implements Initializable {

    @FXML
    private FlowPane logementsFlowPane;

    @FXML
    private TextField searchField;

    @FXML
    private Button addButton;

    private Servicelogement servicelogement = new Servicelogement();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Charger et afficher tous les logements initialement en utilisant la méthode de recherche du service
            displayLogements(servicelogement.rechercher(""));
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les logements : " + e.getMessage());
        }

        // Écouteur de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterLogements(newValue));

        // Action du bouton Ajouter : charger la vue ajoutLogement dans le dashboard
        addButton.setOnAction(event -> Dashboard.loadView("/ajoutLogement.fxml"));
    }

    private void displayLogements(List<logement> logements) {
        logementsFlowPane.getChildren().clear();
        for (logement Logement : logements) {
            VBox card = createLogementCard(Logement);
            logementsFlowPane.getChildren().add(card);
        }
    }

    private void filterLogements(String keyword) {
        try {
            // Utiliser la méthode rechercher du service pour filtrer via la base de données
            List<logement> filtered = servicelogement.rechercher(keyword);
            displayLogements(filtered);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la recherche : " + e.getMessage());
        }
    }

    private VBox createLogementCard(logement logement) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(250);

        // === Conteneur pour l'image avec le prix superposé ===
        var imageContainer = new StackPane();
        imageContainer.setPrefSize(220, 150);
        imageContainer.setStyle("-fx-background-color: #f0f0f0;"); // Fond gris si image absente

        // Image
        ImageView imageView = null;
        String imagePath = logement.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                if (imagePath.startsWith("http")) {
                    imageView = new ImageView(new Image(imagePath));
                } else {
                    imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm()));
                }
                imageView.setFitWidth(220);
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);
            } catch (Exception e) {
                System.err.println("Erreur chargement image pour " + logement.getNom() + " : " + e.getMessage());
                imageView = null;
            }
        }

        if (imageView != null) {
            imageContainer.getChildren().add(imageView);
        } else {
            // Optionnel : ajouter un placeholder gris si pas d'image
            Region placeholder = new Region();
            placeholder.setStyle("-fx-background-color: #e0e0e0;");
            placeholder.setPrefSize(220, 150);
            imageContainer.getChildren().add(placeholder);
        }

        // Prix superposé sur l'image
        Label prixLabel = new Label(logement.getTarif_nuit() + " DT/nuit");
        prixLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #E8B156; -fx-padding: 5; -fx-background-radius: 5;");
        StackPane.setAlignment(prixLabel, Pos.BOTTOM_RIGHT); // Positionner en bas à droite
        imageContainer.getChildren().add(prixLabel);

        // Ajouter le conteneur d'image à la carte
        card.getChildren().add(imageContainer);

        // Nom
        Label nomLabel = new Label(logement.getNom());
        nomLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Adresse
        Label adresseLabel = new Label("📍" + logement.getAdresse());
        adresseLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");

        // Disponibilité
        Label dispoLabel = new Label(logement.isDisponibilite() ? "Disponible" : "Non disponible");
        dispoLabel.setStyle("-fx-background-color: #81ae8d; -fx-font-size: 12; -fx-text-fill: white; -fx-background-radius: 4;");

        // Bouton Voir détails
        Button detailsBtn = new Button("Voir détails");
        detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setOnAction(e -> {
            Dashboard.setSelectedLogement(logement);  // Définir le logement sélectionné
            Dashboard.loadView("/DetailsLogement.fxml");  // Charger la vue des détails
        });

        card.getChildren().addAll(nomLabel, adresseLabel, dispoLabel, detailsBtn);
        return card;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}