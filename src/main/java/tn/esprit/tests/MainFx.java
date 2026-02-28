package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.backend.entities.Role;           // ✅ Fixed import
import tn.esprit.backend.entities.Utilisateur;    // ✅ Fixed import
import tn.esprit.backend.utils.Session;           // ✅ Fixed import

public class MainFx extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Test mode - connect a test user
            connecterUtilisateurTest();

            // Check if user is connected
            if (Session.estConnecte()) {
                System.out.println("✅ Utilisateur connecté: " + Session.getUtilisateur().getNomComplet());
            }

            // Load the appropriate view based on user role
            String fxmlPath;
            if (Session.estAdmin()) {
                fxmlPath = "/views/admin/admin_dashboard.fxml";
                primaryStage.setTitle("Horizia - Administration");
            } else {
                fxmlPath = "/views/user/UserAccueil.fxml";
                primaryStage.setTitle("Horizia - Accueil");
            }

            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connecterUtilisateurTest() {
        // Create test user
        Utilisateur user = new Utilisateur();
        user.setId(1);
        user.setNom("Ben Ahmed");
        user.setPrenom("Khalil");
        user.setEmail("khalil@horizia.com");
        user.setRole(Role.ADMIN);  // ✅ Fixed: Use Role enum directly

        // Connect user
        Session.connecter(user);

        System.out.println("🔑 Mode ADMIN activé: " + user.getNomComplet());
    }

    public static void main(String[] args) {
        launch(args);
    }
}