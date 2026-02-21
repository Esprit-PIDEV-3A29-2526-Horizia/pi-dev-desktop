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
import tn.esprit.entities.logement;
import tn.esprit.services.Servicelogement;

import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
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

    @FXML
    private ComboBox<String> sortComboBox;

    private Servicelogement servicelogement = new Servicelogement();
    private Dashboard dashboard;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Récupérer l'instance Dashboard depuis le root
        if (logementsFlowPane.getScene() != null
                && logementsFlowPane.getScene().getRoot().getUserData() instanceof Dashboard db) {
            this.dashboard = db;
        }

        // Configuration du ComboBox pour le tri
        sortComboBox.getItems().addAll("Tarif croissant", "Tarif décroissant", "Disponible d'abord", "Non disponible d'abord");
        sortComboBox.setValue("Tarif croissant"); // Valeur par défaut
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterLogements(searchField.getText()));

        // Écouteur de recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterLogements(newVal));

        // Charger les logements
        loadAllLogements();

        // Action du bouton Ajouter
        addButton.setOnAction(event -> {
            if (dashboard != null) {
                dashboard.loadView("/fxml/ajoutLogement.fxml");
            }
        });
    }

    private void loadAllLogements() {
        try {
            List<logement> allLogements = servicelogement.rechercher("");
            displayLogements(sortLogements(allLogements, sortComboBox.getValue()));
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les logements : " + e.getMessage());
        }
    }

    private void filterLogements(String keyword) {
        try {
            List<logement> filtered = servicelogement.rechercher(keyword);
            filtered = sortLogements(filtered, sortComboBox.getValue());
            displayLogements(filtered);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la recherche/tri : " + e.getMessage());
        }
    }

    private List<logement> sortLogements(List<logement> logements, String sortOption) {
        switch (sortOption) {
            case "Tarif croissant":
                return logements.stream().sorted(Comparator.comparing(logement::getTarif_nuit)).collect(Collectors.toList());
            case "Tarif décroissant":
                return logements.stream().sorted(Comparator.comparing(logement::getTarif_nuit).reversed()).collect(Collectors.toList());
            case "Disponible d'abord":
                return logements.stream().sorted(Comparator.comparing(logement::isDisponibilite).reversed()).collect(Collectors.toList());
            case "Non disponible d'abord":
                return logements.stream().sorted(Comparator.comparing(logement::isDisponibilite)).collect(Collectors.toList());
            default:
                return logements;
        }
    }

    private void displayLogements(List<logement> logements) {
        logementsFlowPane.getChildren().clear();
        for (logement log : logements) {
            VBox card = createLogementCard(log);
            logementsFlowPane.getChildren().add(card);
        }
    }

    private VBox createLogementCard(logement logement) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(250);

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(220, 150);
        imageContainer.setStyle("-fx-background-color: #f0f0f0;");

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
            }
        }

        if (imageView != null) imageContainer.getChildren().add(imageView);
        else {
            Region placeholder = new Region();
            placeholder.setPrefSize(220, 150);
            placeholder.setStyle("-fx-background-color: #e0e0e0;");
            imageContainer.getChildren().add(placeholder);
        }

        Label prixLabel = new Label(logement.getTarif_nuit() + " DT/nuit");
        prixLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #E8B156; -fx-padding: 5; -fx-background-radius: 5;");
        StackPane.setAlignment(prixLabel, Pos.BOTTOM_RIGHT);
        imageContainer.getChildren().add(prixLabel);

        card.getChildren().add(imageContainer);

        Label nomLabel = new Label(logement.getNom());
        nomLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        Label adresseLabel = new Label("📍" + logement.getAdresse());
        adresseLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");
        card.getChildren().addAll(nomLabel, adresseLabel);

        Label dispoLabel = new Label(logement.isDisponibilite() ? "Disponible" : "Non disponible");
        dispoLabel.setStyle("-fx-background-color: #81ae8d; -fx-font-size: 12; -fx-text-fill: white; -fx-background-radius: 4;");
        card.getChildren().add(dispoLabel);

        Button detailsBtn = new Button("Voir détails");
        detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);

        detailsBtn.setOnAction(e -> {
            try {
                Dashboard dashboard = Dashboard.getInstance(); // Singleton
                if (dashboard != null) {
                    dashboard.setSelectedLogement(logement);
                    dashboard.loadView("/fxml/DetailsLogement.fxml");
                } else {
                    System.err.println("❌ Dashboard est NULL !");
                }
            } catch (Exception ex) {
                ex.printStackTrace(); // Toujours gérer les exceptions
            }
        });

        card.getChildren().add(detailsBtn);

        return card;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}