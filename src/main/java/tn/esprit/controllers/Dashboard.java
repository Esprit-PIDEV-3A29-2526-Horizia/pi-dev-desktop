package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import tn.esprit.entities.Publication;

import java.io.IOException;
import java.net.URL;

public class Dashboard {

    @FXML
    private StackPane contentPane;
    private static Publication selectedPublication;
    private static StackPane staticContentPane;

    @FXML
    public void initialize() {
        staticContentPane = contentPane;
    }

    // Méthode statique pour charger les vues
    public static void loadView(String fxmlPath) {
        try {
            URL url = Dashboard.class.getResource(fxmlPath);

            // Debug
            System.out.println("🔍 Chargement: " + fxmlPath);
            System.out.println("📍 URL: " + url);

            if (url == null) {
                System.err.println("❌ ERREUR: " + fxmlPath + " introuvable!");
                System.err.println("Vérifie que le fichier existe dans src/main/resources/");
                return;
            }

            Parent view = FXMLLoader.load(url);
            staticContentPane.getChildren().clear();
            staticContentPane.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboard() {
        loadView("/Dashboard.fxml");
    }

    @FXML
    private void showVoyages() {
        loadView("/Voyages.fxml");
    }

    @FXML
    private void showReservations() {
        loadView("/Reservations.fxml");
    }

    @FXML
    private void showlogement(ActionEvent event) {
        loadView("/Logements.fxml");
    }

    @FXML
    private void showpublications(ActionEvent event) {
        loadView("/Publications.fxml"); // ← CORRIGÉ ICI
    }

    // Getters/Setters
    public static Publication getSelectedPublication() {
        return selectedPublication;
    }

    public static void setSelectedPublication(Publication publication) {
        selectedPublication = publication;
    }
}