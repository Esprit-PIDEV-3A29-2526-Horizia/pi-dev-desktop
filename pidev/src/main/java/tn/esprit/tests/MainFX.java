package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Charger le dashboard principal (Dashboard.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml")); // Chemin vers votre FXML du dashboard
        Parent root = loader.load();

        // Créer la scène
        Scene scene = new Scene(root, 1200, 700); // Taille comme dans votre FXML (prefWidth et prefHeight)
        stage.setTitle("Horizia - Dashboard"); // Titre de l'app
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}