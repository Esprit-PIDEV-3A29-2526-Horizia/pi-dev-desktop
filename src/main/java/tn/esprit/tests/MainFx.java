package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainFx extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Chargement du fichier FXML depuis la racine de resources
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/GestionCategorie.fxml")));

            // Création de la scène
            Scene scene = new Scene(root);

            // Chargement du fichier CSS (optionnel si déjà lié dans le FXML)
            // Cela garantit que tes styles de cartes et de sidebar sont appliqués
            if (getClass().getResource("/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            }

            // --- PERSONNALISATION DE LA FENÊTRE ---
            primaryStage.setTitle("Horizia - Dashboard Administration");

            // Ajout du logo dans la barre de titre (en haut à gauche de la fenêtre)
            try {
                primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png"))));
            } catch (Exception e) {
                System.out.println("Icône de fenêtre non trouvée, utilisation de l'icône par défaut.");
            }

            primaryStage.setScene(scene);

            // Tailles optimales pour ton design
            primaryStage.setMinWidth(1200); // Augmenté un peu pour la sidebar
            primaryStage.setMinHeight(750);

            // Centrer la fenêtre à l'écran
            primaryStage.centerOnScreen();

            primaryStage.show();
            System.out.println("Horizia Admin Dashboard lancé avec succès !");

        } catch (IOException e) {
            System.err.println("Erreur critique lors du chargement du FXML.");
            System.err.println("Détail : " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Ressource introuvable ! Vérifiez l'emplacement du FXML ou du Logo.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}