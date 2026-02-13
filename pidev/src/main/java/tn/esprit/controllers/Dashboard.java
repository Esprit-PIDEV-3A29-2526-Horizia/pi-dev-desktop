package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class Dashboard {

    @FXML
    private StackPane contentPane;

    // Méthode existante pour les logements

    public void showlogement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Logements.fxml")); // Chemin vers votre FXML
            Parent logementsView = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(logementsView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Ajoutez ces deux méthodes
    @FXML
    private void showVoyages() {
        // À implémenter plus tard
        System.out.println("Voyages cliqué");
        // Exemple : contentPane.getChildren().setAll(charger vue voyages);
    }

    @FXML
    private void showReservations() {
        // À implémenter plus tard
        System.out.println("Réservations cliqué");
    }

    // Si vous avez aussi showDashboard, ajoutez-la
    @FXML
    private void showDashboard() {
        // À implémenter
    }
}