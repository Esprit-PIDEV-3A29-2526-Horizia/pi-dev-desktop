package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.backend.entities.Role;
import tn.esprit.backend.entities.Utilisateur;
import tn.esprit.backend.utils.DatabaseInitializer;
import tn.esprit.backend.utils.Session;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialiser la base de données
            DatabaseInitializer.initialize();

            // Mode test admin
            connecterAdminTest();

            String fxmlPath = determinerVue();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            primaryStage.setScene(scene);

            if (Session.estConnecte() && Session.estAdmin()) {
                primaryStage.setTitle("Horizia - Administration");
                primaryStage.setMaximized(true);
            } else if (Session.estConnecte()) {
                primaryStage.setTitle("Horizia - " + Session.getUtilisateur().getPrenom());
                primaryStage.setWidth(1400);
                primaryStage.setHeight(900);
            } else {
                primaryStage.setTitle("Horizia");
                primaryStage.setWidth(1000);
                primaryStage.setHeight(700);
            }

            primaryStage.show();

        } catch (Exception e) {
            System.err.println("❌ Erreur démarrage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void connecterAdminTest() {
        Utilisateur admin = new Utilisateur();
        admin.setId(1);
        admin.setNom("Bouaicha");
        admin.setPrenom("Baha");
        admin.setEmail("baha@horizia.com");
        admin.setRole(Role.ADMIN);
        admin.setActif(true);
        Session.connecter(admin);
        System.out.println("✅ MODE ADMIN: " + admin.getNomComplet());
    }

    private String determinerVue() {
        if (!Session.estConnecte()) {
            return "/views/common/login.fxml";
        }
        return Session.estAdmin()
                ? "/views/admin/admin_dashboard.fxml"
                : "/views/user/user_main.fxml";
    }

    public static void main(String[] args) {
        launch(args);
    }
}