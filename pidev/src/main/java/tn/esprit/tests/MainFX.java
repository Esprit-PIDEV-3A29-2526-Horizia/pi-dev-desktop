package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.utils.NavigationManager;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            NavigationManager.setPrimaryStage(primaryStage);

            if (getClass().getResource("/fxml/Login.fxml") == null) {
                System.err.println("❌ ERREUR: Login.fxml introuvable!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Horizia - Connexion");
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();
            primaryStage.setMaximized(true);
            primaryStage.show();

            System.out.println("✅ Application démarrée");

        } catch (Exception e) {
            System.err.println("❌ Erreur démarrage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}