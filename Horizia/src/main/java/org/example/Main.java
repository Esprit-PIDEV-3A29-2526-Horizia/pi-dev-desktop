package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.services.LocationService;
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
            System.out.println("\n[1/4] Test de connexion à la base de données...");

            Connection testConnection = DatabaseConnection.getInstance().getConnection();

            if (testConnection != null && !testConnection.isClosed()) {
                System.out.println("✓ Connexion à la base de données : SUCCÈS");
                System.out.println("    Database : horizia");
                System.out.println("    Host     : localhost:3306");
            } else {
                throw new Exception("Connexion à la base de données échouée !");
            }

            // ═══════════════════════════════════════════════════════
            // MISE À JOUR AUTOMATIQUE DES STATUTS
            // ═══════════════════════════════════════════════════════
            System.out.println("\n[2/4] Mise à jour automatique des statuts...");
            LocationService locationService = new LocationService();
            int nbMAJ = locationService.mettreAJourStatutsAutomatique();
            System.out.println("   " + nbMAJ + " location(s) mise(s) à jour");

            // ═══════════════════════════════════════════════════════
            // CHARGEMENT DE L'ICÔNE
            // ═══════════════════════════════════════════════════════
            Image icon64 = new Image(getClass().getResourceAsStream("/images/logo.png"));
            primaryStage.getIcons().clear();
            primaryStage.getIcons().add(icon64);

            // ═══════════════════════════════════════════════════════
            // ÉCRAN DE SÉLECTION ADMIN / CLIENT
            // ═══════════════════════════════════════════════════════
            System.out.println("\n[3/4] Affichage de l'écran de sélection...");

            showSelectionScreen(primaryStage, icon64);

            System.out.println("\n╔════════════════════════════════════════════════╗");
            System.out.println("║         APPLICATION PRÊTE À L'EMPLOI !         ║");
            System.out.println("╚════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            System.err.println("✗ ERREUR FATALE AU DÉMARRAGE :");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();

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

    /**
     * Affiche l'écran de sélection Admin/Client
     */
    private void showSelectionScreen(Stage stage, Image icon) {
        try {
            javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(30);
            root.setAlignment(javafx.geometry.Pos.CENTER);
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #ece9e9, #ece9e5); -fx-padding: 50;");

            // Logo
            javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView();
            try {
                Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
                logoView.setImage(logo);
                logoView.setFitWidth(300);
                logoView.setPreserveRatio(true);
                logoView.setSmooth(true);
            } catch (Exception e) {
                System.err.println("⚠ Impossible de charger le logo : " + e.getMessage());
                javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("HORIZIA");
                titleLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");
                root.getChildren().add(titleLabel);
            }

            javafx.scene.control.Label subtitleLabel = new javafx.scene.control.Label("Système de Gestion de Location");
            subtitleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #0384b7;");

            // Conteneur des boutons
            javafx.scene.layout.HBox buttonContainer = new javafx.scene.layout.HBox(40);
            buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);

            // Bouton Admin
            javafx.scene.control.Button btnAdmin = createStyledButton("👤 ADMIN", "#0384b7");
            btnAdmin.setOnAction(e -> openAdminPanel(stage, icon));

            // Bouton Client
            javafx.scene.control.Button btnClient = createStyledButton("🚗 CLIENT", "#f49c11");
            btnClient.setOnAction(e -> openClientPanel(stage, icon));

            buttonContainer.getChildren().addAll(btnAdmin, btnClient);

            if (logoView.getImage() != null) {
                root.getChildren().addAll(logoView, subtitleLabel, buttonContainer);
            } else {
                root.getChildren().addAll(subtitleLabel, buttonContainer);
            }

            Scene scene = new Scene(root, 800, 500);
            stage.setTitle("Horizia - Sélection du mode");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

            System.out.println("✓ Écran de sélection affiché avec succès");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Crée un bouton stylisé
     */
    private javafx.scene.control.Button createStyledButton(String text, String color) {
        javafx.scene.control.Button button = new javafx.scene.control.Button(text);
        button.setPrefSize(250, 120);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: derive(" + color + ", 20%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-scale-x: 1.05;" +
                        "-fx-scale-y: 1.05;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 15;" +
                        "-fx-cursor: hand;"
        ));

        return button;
    }

    private Stage currentAdminStage = null;
    private Stage currentClientStage = null;

    /**
     * Ouvre le panel Admin dans une nouvelle fenêtre
     */
    private void openAdminPanel(Stage selectionStage, Image icon) {
        try {
            if (currentAdminStage != null && currentAdminStage.isShowing()) {
                currentAdminStage.toFront();
                currentAdminStage.requestFocus();
                System.out.println("⚠ Panel Admin déjà ouvert - fenêtre ramenée au premier plan");
                return;
            }

            if (currentClientStage != null && currentClientStage.isShowing()) {
                currentClientStage.close();
                currentClientStage = null;
                System.out.println("✓ Fenêtre Client fermée");
            }

            Stage adminStage = new Stage();
            adminStage.getIcons().add(icon);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainLayout.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1400, 800);
            adminStage.setTitle("Horizia - Panel Administrateur");
            adminStage.setScene(scene);
            adminStage.setResizable(true);
            adminStage.setMaximized(true);

            adminStage.setOnCloseRequest(e -> {
                currentAdminStage = null;
                System.out.println("✓ Panel Admin fermé");
            });

            adminStage.show();
            currentAdminStage = adminStage;

            System.out.println("✓ Panel Admin ouvert avec succès");

        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'ouverture du panel Admin : " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir le panel Admin");
            alert.setContentText("Vérifiez que le fichier MainLayout.fxml existe dans /views/");
            alert.showAndWait();
        }
    }

    /**
     * Ouvre le panel Client dans une nouvelle fenêtre
     */
    private void openClientPanel(Stage selectionStage, Image icon) {
        try {
            if (currentClientStage != null && currentClientStage.isShowing()) {
                currentClientStage.toFront();
                currentClientStage.requestFocus();
                System.out.println("⚠ Panel Client déjà ouvert - fenêtre ramenée au premier plan");
                return;
            }

            if (currentAdminStage != null && currentAdminStage.isShowing()) {
                currentAdminStage.close();
                currentAdminStage = null;
                System.out.println("✓ Fenêtre Admin fermée");
            }

            Stage clientStage = new Stage();
            clientStage.getIcons().add(icon);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client/AccueilClient.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1400, 800);
            clientStage.setTitle("Horizia - Location de Voitures");
            clientStage.setScene(scene);
            clientStage.setResizable(true);
            clientStage.setMaximized(true);

            clientStage.setOnCloseRequest(e -> {
                currentClientStage = null;
                System.out.println("✓ Panel Client fermé");
            });

            clientStage.show();
            currentClientStage = clientStage;

            System.out.println("✓ Panel Client ouvert avec succès");

        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'ouverture du panel Client : " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir le panel Client");
            alert.setContentText("Vérifiez que le fichier AccueilClient.fxml existe dans /views/client/");
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}