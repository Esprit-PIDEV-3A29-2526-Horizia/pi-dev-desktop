package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import tn.esprit.entities.logement; // Ajoutez cette import pour l'entité logement

public class Dashboard {

    @FXML
    private StackPane contentPane;

    private static StackPane staticContentPane;
    private static logement selectedLogement; // Variable statique pour stocker le logement sélectionné

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
            Parent view = FXMLLoader.load(Dashboard.class.getResource(fxmlPath));
            staticContentPane.getChildren().clear();
            staticContentPane.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour définir le logement sélectionné
    public static void setSelectedLogement(logement log) {
        selectedLogement = log;
    }

    // Méthode pour récupérer le logement sélectionné
    public static logement getSelectedLogement() {
        return selectedLogement;
    }

    @FXML
    private void showDashboard() {
        // Charger une vue d'aperçu dashboard (par défaut, charger Logements si aucune vue spécifique n'existe)
        loadView("/Logements.fxml"); // Ou créez /DashboardContent.fxml pour un aperçu personnalisé
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
}