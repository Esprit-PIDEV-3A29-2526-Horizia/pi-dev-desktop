package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import tn.esprit.entities.User;

import java.io.IOException;
import java.io.InputStream;

public class UserFrontEndController {

    @FXML
    private GridPane gridDestinations;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadDestinations();
    }

    private void loadDestinations() {
        // Exemple de destinations
        String[][] destinations = {
                {"Paris", "france.jpg"},
                {"Tokyo", "tokyo.jpg"},
                {"Sydney", "sydney.jpg"},
                {"Bali", "bali.jpg"},
                {"New York", "newyork.jpg"},
                {"Rome", "rome.jpg"}
        };

        gridDestinations.getChildren().clear();
        int col = 0;
        int row = 0;

        for (String[] dest : destinations) {
            VBox card = createDestinationCard(dest[0], dest[1]);
            gridDestinations.add(card, col, row);
            col++;
            if (col > 2) { // 3 colonnes
                col = 0;
                row++;
            }
        }
    }

    private VBox createDestinationCard(String name, String imagePath) {
        VBox card = new VBox(5);
        card.setStyle("-fx-border-color: gray; -fx-padding: 10; -fx-background-color: white;");
        card.setPrefWidth(200);
        card.setPrefHeight(250);

        // Image avec gestion d'erreur
        ImageView img = new ImageView();
        img.setFitWidth(180);
        img.setFitHeight(120);
        img.setPreserveRatio(true);

        // Charger l'image avec gestion d'erreur
        try {
            // Essayer de charger l'image depuis le dossier resources/images/
            InputStream imageStream = getClass().getResourceAsStream("/images/" + imagePath);

            if (imageStream != null) {
                // Image trouvée
                img.setImage(new Image(imageStream));
                System.out.println("Image chargée avec succès: " + imagePath);
            } else {
                // Image non trouvée - utiliser une image par défaut
                System.out.println("Image non trouvée: /images/" + imagePath + " - Utilisation de l'image par défaut");

                // Essayer de charger une image par défaut
                InputStream defaultImageStream = getClass().getResourceAsStream("/images/logo.jpg");
                if (defaultImageStream != null) {
                    img.setImage(new Image(defaultImageStream));
                } else {
                    // Si pas d'image par défaut, créer une image colorée virtuelle
                    System.out.println("Image par défaut non trouvée non plus");
                    // On laisse l'ImageView vide
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + imagePath);
            e.printStackTrace();

            // Essayez de charger l'image par défaut en cas d'erreur
            try {
                InputStream defaultImageStream = getClass().getResourceAsStream("/images/placeholder.jpg");
                if (defaultImageStream != null) {
                    img.setImage(new Image(defaultImageStream));
                }
            } catch (Exception ex) {
                System.err.println("Impossible de charger l'image par défaut");
            }
        }

        Label lblName = new Label(name);
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button btnReserve = new Button("Réserver");
        btnReserve.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white; -fx-cursor: hand;");
        btnReserve.setOnAction(e -> System.out.println("Réserver : " + name));

        card.getChildren().addAll(img, lblName, btnReserve);
        return card;
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) gridDestinations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}