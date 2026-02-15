package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.utils.DatabaseConnection;

import java.sql.Connection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // ═══════════════════════════════════════════════════════
            // TEST DE CONNEXION À LA BASE DE DONNÉES
            // ═══════════════════════════════════════════════════════
            System.out.println("╔════════════════════════════════════════════════╗");
            System.out.println("║   HORIZIA - Système de Gestion de Location    ║");
            System.out.println("╚════════════════════════════════════════════════╝");
            System.out.println("\n[1/3] Test de connexion à la base de données...");

            Connection testConnection = DatabaseConnection.getInstance().getConnection();

            if (testConnection != null && !testConnection.isClosed()) {
                System.out.println("✓ Connexion à la base de données : SUCCÈS");
                System.out.println("    Database : horizia");
                System.out.println("    Host     : localhost:3306");
            } else {
                throw new Exception("Connexion à la base de données échouée !");
            }
            // Test simple : une seule grande
            Image icon64 = new Image(getClass().getResourceAsStream("/images/logo.png"));
            primaryStage.getIcons().clear(); // vide les précédentes si besoin
            primaryStage.getIcons().add(icon64);
            // ═══════════════════════════════════════════════════════
            // CHARGEMENT DE L'INTERFACE AVEC SIDEBAR
            // ═══════════════════════════════════════════════════════
            System.out.println("\n[2/3] Chargement de l'interface principale...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainLayout.fxml"));
            Parent root = loader.load();

            System.out.println("✓ Interface principale chargée avec succès");

            // ═══════════════════════════════════════════════════════
            // AFFICHAGE DE LA FENÊTRE PRINCIPALE
            // ═══════════════════════════════════════════════════════
            System.out.println("\n[3/3] Lancement de l'application...");

            Scene scene = new Scene(root, 1400, 800);
            primaryStage.setTitle("Horizia - Gestion de Location de Voitures");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true); // Plein écran pour mieux voir la sidebar
            primaryStage.show();

            System.out.println("✓ Application lancée avec succès !");
            System.out.println("\n╔════════════════════════════════════════════════╗");
            System.out.println("║         APPLICATION PRÊTE À L'EMPLOI !         ║");
            System.out.println("╚════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            // ═══════════════════════════════════════════════════════
            // GESTION DES ERREURS
            // ═══════════════════════════════════════════════════════
            System.err.println("✗ ERREUR FATALE AU DÉMARRAGE :");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();

            // Affichage d'une alerte à l'utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de démarrage");
            alert.setHeaderText("Impossible de démarrer l'application");
            alert.setContentText(
                    "Détails de l'erreur :\n" + e.getMessage() +
                            "\n\nVérifiez que :\n" +
                            "1. MySQL est bien démarré (XAMPP/WAMP)\n" +
                            "2. La base 'horizia' existe\n" +
                            "3. Les fichiers FXML sont au bon chemin\n" +
                            "4. Les dépendances Maven sont chargées"
            );
            alert.showAndWait();

            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}