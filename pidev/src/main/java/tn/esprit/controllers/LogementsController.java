package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.logement; // Attention à la casse (Logement au lieu de logement)
import tn.esprit.services.Servicelogement;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class LogementsController implements Initializable {

    @FXML
    private FlowPane logementsFlowPane;

    @FXML
    private TextField searchField;

    @FXML
    private Button addButton;

    private Servicelogement servicelogement = new Servicelogement();
    private List<logement> allLogements; // Pour conserver la liste complète

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            loadLogements();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les logements : " + e.getMessage());
        }

        // Écouteur de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterLogements(newValue));

        // Action du bouton Ajouter
        addButton.setOnAction(event -> {
            // Ouvrir le formulaire d'ajout (à implémenter selon votre architecture)
            System.out.println("Ouvrir formulaire d'ajout");
            // Exemple : charger une nouvelle vue
            // try {
            //     Parent root = FXMLLoader.load(getClass().getResource("/ajoutLogement.fxml"));
            //     Stage stage = (Stage) addButton.getScene().getWindow();
            //     stage.setScene(new Scene(root));
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }
        });
    }

    private void loadLogements() throws SQLException {
        allLogements = servicelogement.afficher(); // Récupère tous les logements
        displayLogements(allLogements);
    }

    private void displayLogements(List<logement> logements) {
        logementsFlowPane.getChildren().clear();
        for (logement Logement : logements) {
            VBox card = createLogementCard(Logement);
            logementsFlowPane.getChildren().add(card);
        }
    }

    private void filterLogements(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            displayLogements(allLogements);
        } else {
            List<logement> filtered = allLogements.stream()
                    .filter(l -> l.getNom().toLowerCase().contains(keyword.toLowerCase())
                            || l.getAdresse().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
            displayLogements(filtered);
        }
    }

    private VBox createLogementCard(logement  logement) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(250);

        // Image du logement (uniquement si disponible et chargeable)
        ImageView imageView = null;
        String imagePath = logement.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                if (imagePath.startsWith("http") || imagePath.startsWith("https")) {
                    // URL externe
                    imageView = new ImageView(new Image(imagePath));
                } else {
                    // Chemin relatif/local (ressource)
                    imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm()));
                }
                imageView.setFitWidth(220);
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);
            } catch (Exception e) {
                // Échec du chargement : on n'ajoute pas l'image
                System.err.println("Erreur de chargement de l'image pour " + logement.getNom() + " : " + e.getMessage());
                imageView = null;
            }
        }

        // Si l'image a pu être chargée, on l'ajoute à la carte
        if (imageView != null) {
            card.getChildren().add(imageView);
        }

        // Prix
        Label prixLabel = new Label(logement.getTarif_nuit() + "DT/ nuit");
        prixLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Nom
        Label nomLabel = new Label(logement.getNom());
        nomLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Adresse
        Label adresseLabel = new Label(logement.getAdresse());
        adresseLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");

        // Disponibilité
        Label dispoLabel = new Label(logement.isDisponibilite() ? "Disponible" : "Non disponible");
        dispoLabel.setStyle("-fx-background-color: #81ae8d;-fx-font-size: 12; -fx-text-fill: white; -fx-background-radius: 4;");


        // Bouton Voir détails
        Button detailsBtn = new Button("Voir détails");
        detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setOnAction(e -> {
            System.out.println("Détails de : " + logement.getNom());
            // Ouvrir une nouvelle fenêtre ou changer de vue
        });

        card.getChildren().addAll(prixLabel, nomLabel, adresseLabel, dispoLabel, detailsBtn);
        return card;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}