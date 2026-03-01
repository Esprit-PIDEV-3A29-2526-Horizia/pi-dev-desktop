package tn.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class NavigationManager {
    private static Stage primaryStage;
    private static int windowWidth = 1200;
    private static int windowHeight = 700;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        // ✅ S'assurer que la fenêtre n'est pas redimensionnable
        primaryStage.setResizable(false);
    }

    public static void setDimensions(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    public static void loadView(String fxmlPath, String catalogue) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            // ✅ Réutiliser la MÊME scène avec les mêmes dimensions
            primaryStage.setScene(new Scene(root, windowWidth, windowHeight));
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode utilitaire pour obtenir le contrôleur si besoin
    public static FXMLLoader loadViewWithController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            // ✅ Utiliser les dimensions définies dans MainFX
            primaryStage.setScene(new Scene(root, windowWidth, windowHeight));
            primaryStage.centerOnScreen();
            primaryStage.show();

            return loader;  // Retourne le loader pour accéder au contrôleur

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}