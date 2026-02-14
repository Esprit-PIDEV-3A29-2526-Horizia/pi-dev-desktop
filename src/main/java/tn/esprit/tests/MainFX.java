package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Check if files exist
            System.out.println("=== CHECKING FXML FILES ===");
            System.out.println("UserHome.fxml exists? " + (getClass().getResource("/UserHome.fxml") != null));
            System.out.println("AdminHome.fxml exists? " + (getClass().getResource("/AdminHome.fxml") != null));
            System.out.println("UserHome URL: " + getClass().getResource("/UserHome.fxml"));
            System.out.println("AdminHome URL: " + getClass().getResource("/AdminHome.fxml"));

            // Load User Interface
            System.out.println("\n=== LOADING USER INTERFACE ===");
            FXMLLoader userLoader = new FXMLLoader(getClass().getResource("/UserHome.fxml"));
            Parent userRoot = userLoader.load();
            System.out.println("✅ User FXML loaded successfully");
            System.out.println("User Controller: " + userLoader.getController().getClass().getName());

            Stage userStage = new Stage();
            userStage.setTitle("EventHub - Espace Utilisateur");
            userStage.setScene(new Scene(userRoot, 1200, 700));
            userStage.setX(100);
            userStage.setY(50);
            userStage.show();
            System.out.println("✅ User window shown");

            // Load Admin Interface
            System.out.println("\n=== LOADING ADMIN INTERFACE ===");
            FXMLLoader adminLoader = new FXMLLoader(getClass().getResource("/AdminHome.fxml"));
            Parent adminRoot = adminLoader.load();
            System.out.println("✅ Admin FXML loaded successfully");
            System.out.println("Admin Controller: " + adminLoader.getController().getClass().getName());

            Stage adminStage = new Stage();
            adminStage.setTitle("EventHub - Administration");
            adminStage.setScene(new Scene(adminRoot, 1200, 700));
            adminStage.setX(650);
            adminStage.setY(50);
            adminStage.show();
            System.out.println("✅ Admin window shown");

            primaryStage.hide();

        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}