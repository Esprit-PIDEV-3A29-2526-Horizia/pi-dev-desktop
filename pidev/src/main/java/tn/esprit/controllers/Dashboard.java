package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import tn.esprit.entities.logement;

public class Dashboard {

    @FXML
    private StackPane contentPane;

    private static StackPane staticContentPane;
    private static logement selectedLogement;

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation Dashboard ===");
        staticContentPane = contentPane;

        // Vérifier que les fichiers existent
        checkRequiredFiles();
    }

    private void checkRequiredFiles() {
        String[] files = {
                "/fxml/DashboardContent.fxml",
                "/fxml/Voyages.fxml",
                "/fxml/Reservations.fxml",
                "/fxml/Logements.fxml"
        };

        for (String file : files) {
            if (getClass().getResource(file) == null) {
                System.err.println("⚠️ Fichier manquant: " + file);
            } else {
                System.out.println("✅ Fichier trouvé: " + file);
            }
        }
    }

    public static void loadView(String fxmlPath) {
        try {
            System.out.println("Chargement: " + fxmlPath);

            // S'assurer que le chemin commence par /fxml/
            String fullPath = fxmlPath.startsWith("/") ? fxmlPath : "/" + fxmlPath;
            if (!fullPath.contains("/fxml/")) {
                fullPath = "/fxml" + fullPath;
            }

            Parent view = FXMLLoader.load(Dashboard.class.getResource(fullPath));

            if (staticContentPane != null) {
                staticContentPane.getChildren().clear();
                staticContentPane.getChildren().add(view);
                System.out.println("✅ Vue chargée: " + fullPath);
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            showErrorInPane("Erreur: " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("❌ Fichier introuvable: " + fxmlPath);
            showErrorInPane("Fichier introuvable: " + fxmlPath);
        }
    }

    private static void showErrorInPane(String message) {
        if (staticContentPane != null) {
            Label errorLabel = new Label(message);
            errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 16; -fx-font-weight: bold;");
            staticContentPane.getChildren().clear();
            staticContentPane.getChildren().add(errorLabel);
        }
    }

    public static void setSelectedLogement(logement log) {
        selectedLogement = log;
        System.out.println("✅ Logement sélectionné: " + (log != null ? log.getNom() : "null"));
    }

    public static logement getSelectedLogement() {
        return selectedLogement;
    }

    @FXML
    private void showDashboard() {
        loadView("/fxml/DashboardContent.fxml");
    }

    @FXML
    private void showVoyages() {
        loadView("/fxml/Voyages.fxml");
    }

    @FXML
    private void showReservations() {
        loadView("/fxml/Reservations.fxml");
    }

    @FXML
    private void showlogement(ActionEvent event) {
        loadView("/fxml/Logements.fxml");
    }
}