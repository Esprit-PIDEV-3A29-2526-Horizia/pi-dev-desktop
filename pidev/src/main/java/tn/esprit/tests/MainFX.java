package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Définir le Stage principal pour NavigationManager
        NavigationManager.setPrimaryStage(stage);
// Pour les tests : définir un utilisateur fictif (à supprimer après intégration)
       //SessionManager.setTestAdminUser();
        //
        SessionManager.setTestUser();// Lance l'accueil avec un utilisateur connecté
        // Vérifier la session utilisateur
        String fxmlPath;
        if (SessionManager.isLoggedIn() && SessionManager.isAdmin()) {
            // Utilisateur connecté et admin : charger le dashboard (back-office)
            fxmlPath = "/Dashboard.fxml";
            stage.setTitle("Horizia - Dashboard Admin");
        } else {
            // Utilisateur non connecté ou non admin : charger l'accueil (front-office)
            fxmlPath = "/accueil.fxml";  // Utilise le chemin de votre code original
            stage.setTitle("Horizia Travel Agency");
        }

        // Charger la vue appropriée avec gestion d'erreur
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new RuntimeException("Fichier FXML non trouvé : " + fxmlPath + ". Vérifiez src/main/resources.");
            }
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 700);  // Taille ajustée
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la vue : " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
            // Optionnel : charger une vue par défaut ou quitter
            System.exit(1);
        }

        // Pour les tests : définir un utilisateur admin fictif (à supprimer après intégration)
        // SessionManager.setTestAdminUser();
    }

    public static void main(String[] args) {
        launch(args);
    }
}