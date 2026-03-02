package tn.esprit.utils;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.User;

import java.io.IOException;

public class NavigationManager {
    private static Stage primaryStage;
    private static int windowWidth = 1200;
    private static int windowHeight = 700;
    private static User currentUser;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(false);
        primaryStage.setTitle("Horiza - Application");
        System.out.println("✅ NavigationManager initialisé");
    }

    public static void setDimensions(int width, int height) {
        windowWidth = width;
        windowHeight = height;
        if (primaryStage != null && primaryStage.getScene() != null) {
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
            primaryStage.centerOnScreen();
        }
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void showLogin() {
        loadView("/fxml/Login.fxml", "Connexion");
    }

    public static void showUserAccueil() {
        loadView("/fxml/accueil.fxml", "Accueil");
    }

    public static void showAdminDashboard() {
        loadView("/fxml/Dashboard.fxml", "Administration");
    }

    public static void showCatalogue() {
        loadView("/fxml/CatalogueUser.fxml", "Catalogue");
    }

    public static void showMesReservations() {
        loadView("/fxml/MesReservations.fxml", "Mes Réservations");
    }

    public static void showEvenements() {
        loadView("/fxml/UserEvenements.fxml", "Nos Événements");
    }

    public static void loadView(String fxmlPath, String title) {
        try {
            System.out.println("🔄 Navigation vers: " + fxmlPath + " (" + title + ")");

            if (primaryStage == null) {
                System.err.println("❌ primaryStage est null! Appelez setPrimaryStage() d'abord");
                return;
            }

            // Vérifier que le fichier existe
            if (NavigationManager.class.getResource(fxmlPath) == null) {
                System.err.println("❌ Fichier introuvable: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent newRoot = loader.load();

            // Passer l'utilisateur au contrôleur s'il implémente UserAware
            Object controller = loader.getController();
            if (controller instanceof UserAware && currentUser != null) {
                ((UserAware) controller).setCurrentUser(currentUser);
                System.out.println("✅ User injecté dans " + controller.getClass().getSimpleName());
            }

            // Animation de transition
            if (primaryStage.getScene() != null) {
                Parent oldRoot = primaryStage.getScene().getRoot();

                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldRoot);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);

                fadeOut.setOnFinished(e -> {
                    primaryStage.setScene(new Scene(newRoot, windowWidth, windowHeight));
                    primaryStage.setTitle("Horiza - " + title);
                    primaryStage.centerOnScreen();

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newRoot);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();
                });

                fadeOut.play();
            } else {
                // Premier chargement
                primaryStage.setScene(new Scene(newRoot, windowWidth, windowHeight));
                primaryStage.setTitle("Horiza - " + title);
                primaryStage.centerOnScreen();
                primaryStage.show();
            }

            System.out.println("✅ Vue chargée: " + fxmlPath);

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static FXMLLoader loadViewWithController(String fxmlPath) {
        try {
            if (primaryStage == null) {
                System.err.println("❌ primaryStage est null!");
                return null;
            }

            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            primaryStage.setScene(new Scene(root, windowWidth, windowHeight));
            primaryStage.centerOnScreen();
            primaryStage.show();

            return loader;

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void logout() {
        currentUser = null;
        showLogin();
    }
}