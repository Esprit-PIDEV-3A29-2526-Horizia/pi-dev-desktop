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
    private static User currentUser;

    // ──────────────────────────────────────────
    //  Initialisation
    // ──────────────────────────────────────────

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setTitle("Horizia - Application");
        System.out.println("✅ NavigationManager initialisé");
    }

    /** Conservé pour rétrocompatibilité — la taille est gérée par setMaximized */
    public static void setDimensions(int width, int height) {
        // no-op : on utilise le plein écran désormais
    }

    // ──────────────────────────────────────────
    //  Utilisateur
    // ──────────────────────────────────────────

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    // ──────────────────────────────────────────
    //  Raccourcis de navigation
    // ──────────────────────────────────────────

    public static void showLogin() {
        loadView("/fxml/Login.fxml", "Connexion");
    }

    public static void showUserAccueil() {
        loadView("/fxml/CatalogueUser.fxml", "Accueil");
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

    public static void logout() {
        currentUser = null;
        showLogin();
    }

    // ──────────────────────────────────────────
    //  Méthode principale de navigation
    // ──────────────────────────────────────────

    public static void loadView(String fxmlPath, String title) {
        try {
            System.out.println("🔄 Navigation vers: " + fxmlPath);

            if (primaryStage == null) {
                System.err.println("❌ primaryStage est null!");
                return;
            }

            if (NavigationManager.class.getResource(fxmlPath) == null) {
                System.err.println("❌ Fichier introuvable: " + fxmlPath);
                return;
            }

            // ✅ Mémoriser l'état AVANT de toucher quoi que ce soit
            final boolean wasMaximized = primaryStage.isMaximized();
            final double savedWidth    = primaryStage.getWidth();
            final double savedHeight   = primaryStage.getHeight();

            FXMLLoader loader = new FXMLLoader(
                    NavigationManager.class.getResource(fxmlPath));
            Parent newRoot = loader.load();

            // Injection utilisateur si le controller le supporte
            Object controller = loader.getController();
            if (controller instanceof UserAware && currentUser != null) {
                ((UserAware) controller).setCurrentUser(currentUser);
                System.out.println("✅ User injecté dans "
                        + controller.getClass().getSimpleName());
            }

            Scene scene = primaryStage.getScene();

            if (scene != null) {
                // ✅ Réutiliser la scène existante : évite tout redimensionnement
                Parent oldRoot = scene.getRoot();

                FadeTransition fadeOut = new FadeTransition(
                        Duration.millis(150), oldRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);

                fadeOut.setOnFinished(e -> {
                    // Swap du contenu sans recréer la scène
                    scene.setRoot(newRoot);
                    primaryStage.setTitle("Horizia - " + title);

                    // ✅ Restaurer l'état maximisé
                    if (wasMaximized) {
                        primaryStage.setMaximized(false);
                        primaryStage.setMaximized(true);
                    } else {
                        primaryStage.setWidth(savedWidth);
                        primaryStage.setHeight(savedHeight);
                    }

                    FadeTransition fadeIn = new FadeTransition(
                            Duration.millis(150), newRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });

                fadeOut.play();

            } else {
                // Premier chargement (ne devrait pas arriver après MainFX)
                Scene newScene = new Scene(newRoot);
                primaryStage.setScene(newScene);
                primaryStage.setTitle("Horizia - " + title);
                primaryStage.setMaximized(true);
                primaryStage.show();
            }

            System.out.println("✅ Vue chargée: " + fxmlPath);

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlPath
                    + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ──────────────────────────────────────────
    //  Chargement avec accès au controller
    // ──────────────────────────────────────────

    public static FXMLLoader loadViewWithController(String fxmlPath) {
        try {
            if (primaryStage == null) {
                System.err.println("❌ primaryStage est null!");
                return null;
            }

            if (NavigationManager.class.getResource(fxmlPath) == null) {
                System.err.println("❌ Fichier introuvable: " + fxmlPath);
                return null;
            }

            final boolean wasMaximized = primaryStage.isMaximized();
            final double savedWidth    = primaryStage.getWidth();
            final double savedHeight   = primaryStage.getHeight();

            FXMLLoader loader = new FXMLLoader(
                    NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = primaryStage.getScene();
            if (scene != null) {
                scene.setRoot(root);
            } else {
                primaryStage.setScene(new Scene(root));
            }

            primaryStage.setTitle("Horizia");

            if (wasMaximized) {
                primaryStage.setMaximized(false);
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(savedWidth);
                primaryStage.setHeight(savedHeight);
            }

            primaryStage.show();
            return loader;

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement " + fxmlPath
                    + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}