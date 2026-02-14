package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class Dashboard {

    @FXML
    private StackPane contentPane;

    private static StackPane staticContentPane;

    @FXML
    public void initialize() {
        // Initialiser la référence statique pour permettre le chargement depuis d'autres contrôleurs
        staticContentPane = contentPane;
        // Par défaut, charger la vue Tableau de bord ou laisser vide
        loadView("/DashboardContent.fxml");
    }

    // Méthode statique pour charger les vues dans contentPane
    public static void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(Dashboard.class.getResource(fxmlPath));
            staticContentPane.getChildren().clear();
            staticContentPane.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboard() {
        loadView("/DashboardContent.fxml"); // Vue par défaut du tableau de bord
    }

    @FXML
    private void showVoyages() {
        loadView("/Voyages.fxml");
    }

    @FXML
    private void showReservations() {
        loadView("/Reservations.fxml");
    }

    @FXML
    private void showPublications() {
        loadView("/Publications.fxml"); // Ici tu affiches le PublicationController
    }

    @FXML
    private void showlogement() {
        loadView("/Logements.fxml");
    }
}
