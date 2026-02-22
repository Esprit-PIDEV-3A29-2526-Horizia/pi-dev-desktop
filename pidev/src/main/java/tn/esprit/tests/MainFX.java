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
            // ÉTAPE 1: Initialiser le NavigationManager avec la stage principale
            NavigationManager.setPrimaryStage(primaryStage);
            System.out.println("NavigationManager initialisé avec succès");

            // ÉTAPE 2: Charger la page de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));

            // Vérifier que le fichier existe
            if (getClass().getResource("/fxml/Login.fxml") == null) {
                System.err.println("ERREUR: Fichier Login.fxml introuvable!");
                System.err.println("Chemin vérifié: /fxml/Login.fxml");
                return;
            }

            Parent root = loader.load();

            // ÉTAPE 3: Configurer la scène
            Scene scene = new Scene(root);
            primaryStage.setTitle("Connexion - Système de Réservation");
            primaryStage.setScene(scene);

            // Option: permettre le redimensionnement pour une meilleure expérience
            primaryStage.setResizable(true);  // Changé à true pour permettre le redimensionnement

            primaryStage.centerOnScreen();
            primaryStage.show();

            System.out.println("Application démarrée avec succès");

        } catch (Exception e) {
            System.err.println("Erreur au démarrage de l'application:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}