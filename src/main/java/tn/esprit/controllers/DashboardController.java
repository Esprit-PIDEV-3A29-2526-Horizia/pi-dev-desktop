package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;


public class DashboardController {

    @FXML
    private StackPane contentPane;

    private static StackPane staticContentPane;


    @FXML
    public void initialize() {
        // Initialiser la référence statique pour permettre le chargement depuis d'autres contrôleurs
        staticContentPane = contentPane;

        // Ne charger aucune vue par défaut : contentPane reste vide au démarrage
        // L'utilisateur devra cliquer sur un bouton pour charger une vue
    }

    // Méthode statique pour charger les vues depuis d'autres contrôleurs
    public static void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(DashboardController.class.getResource(fxmlPath));
            staticContentPane.getChildren().clear();
            staticContentPane.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void showDashboard() {
        // Charger une vue d'aperçu dashboard (par défaut, charger Logements si aucune vue spécifique n'existe)
        loadView("/Dashboard.fxml"); // Ou créez /Dashboard.fxml pour un aperçu personnalisé
    }

    @FXML
    private void showVoyages() {
        loadView("/Voyages.fxml"); // Assurez-vous que ce fichier existe sans sidebar
    }

    @FXML
    private void showReservations() {
        loadView("/Reservations.fxml"); // Assurez-vous que ce fichier existe sans sidebar
    }

    @FXML
    private void showlogement(ActionEvent event) {
        loadView("/Logements.fxml");
    }
    @FXML
    private void showpublications(ActionEvent event) {
        loadView("/publications.fxml");
    }

}