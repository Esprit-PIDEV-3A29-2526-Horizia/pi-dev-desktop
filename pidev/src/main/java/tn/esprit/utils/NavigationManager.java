package tn.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationManager {
    private static Stage primaryStage;  // Stage partagé pour toute l'application

    // Méthode pour définir le Stage principal (appelée dans MainFX.java)
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    // Méthode pour charger une vue FXML et changer la scène
    public static void loadView(String fxmlPath) {
        try {
            if (primaryStage == null) {
                throw new IllegalStateException("PrimaryStage non défini. Appelez setPrimaryStage() d'abord.");
            }
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la vue : " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
            // Optionnel : afficher une alerte ou rester sur la page actuelle
        }
    }
}