package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger la page de login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Connexion Administrateur");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);  // Empêcher le redimensionnement
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}