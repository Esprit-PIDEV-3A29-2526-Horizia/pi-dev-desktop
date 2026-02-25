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
        // Publications.fxml est à la racine → SANS /views/
        loadView("/Publications.fxml");
    }

    public static void loadView(String fxmlPath) {
        try {
            URL url = Dashboard.class.getResource(fxmlPath);
            System.out.println("🔍 Chargement: " + fxmlPath);
            System.out.println("📍 URL: " + url);

            if (url == null) {
                System.err.println("❌ ERREUR: " + fxmlPath + " introuvable!");
                return;
            }

            Parent view = FXMLLoader.load(url);
            staticContentPane.getChildren().clear();
            staticContentPane.getChildren().add(view);

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboard() {
        loadView("/dashboard.fxml");  // À la racine
    }

    @FXML
    private void showVoyages() {
        loadView("/Voyages.fxml");  // À la racine
    }

    @FXML
    private void showReservations() {
        loadView("/Reservations.fxml");  // À la racine
    }

    @FXML
    private void showlogement(ActionEvent event) {
        loadView("/Logements.fxml");  // À la racine
    }

    @FXML
    private void showpublications(ActionEvent event) {
        loadView("/Publications.fxml");  // À la racine
    }

    public static Publication getSelectedPublication() {
        return selectedPublication;
    }

    public static void setSelectedPublication(Publication publication) {
        selectedPublication = publication;
    }
}