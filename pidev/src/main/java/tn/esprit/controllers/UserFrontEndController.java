package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.io.InputStream;

public class UserFrontEndController {

    @FXML private GridPane gridDestinations;

    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        SessionManager.setCurrentUser(user);
        loadDestinations();
    }

    private void loadDestinations() {
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
            if (col > 2) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDestinationCard(String name, String imagePath) {
        VBox card = new VBox(5);
        card.setStyle("-fx-border-color: gray; -fx-padding: 10; -fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10;");
        card.setPrefWidth(200);
        card.setPrefHeight(250);

        ImageView img = new ImageView();
        img.setFitWidth(180);
        img.setFitHeight(120);
        img.setPreserveRatio(true);

        try {
            InputStream imageStream = getClass().getResourceAsStream("/images/" + imagePath);

            if (imageStream != null) {
                img.setImage(new Image(imageStream));
                System.out.println("Image chargée avec succès: " + imagePath);
            } else {
                System.out.println("Image non trouvée: /images/" + imagePath + " - Utilisation de l'image par défaut");

                InputStream defaultImageStream = getClass().getResourceAsStream("/images/logo.png");
                if (defaultImageStream != null) {
                    img.setImage(new Image(defaultImageStream));
                } else {
                    // Si pas d'image par défaut, créer un placeholder
                    img.setImage(null);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + imagePath);
            e.printStackTrace();

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
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #23779C;");

        Button btnReserve = new Button("Réserver");
        btnReserve.setStyle("-fx-background-color: #E8B156; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 8 0;");
        btnReserve.setMaxWidth(Double.MAX_VALUE);
        btnReserve.setOnAction(e -> {
            if (SessionManager.isLoggedIn()) {
                System.out.println("Réserver : " + name);
                // Navigation vers la réservation
            } else {
                NavigationManager.showLogin();
            }
        });

        card.getChildren().addAll(img, lblName, btnReserve);
        return card;
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        NavigationManager.showLogin();
    }

    @FXML
    private void handleBack() {
        NavigationManager.showUserAccueil();
    }
}