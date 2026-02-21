package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import tn.esprit.entities.logement;

public class Dashboard {

    @FXML
    private StackPane contentPane;

    private logement selectedLogement;

    // SINGLETON
    private static Dashboard instance;

    public Dashboard() {
        instance = this; // Stocke l'instance actuelle
    }

    public static Dashboard getInstance() {
        return instance;
    }


    @FXML
    public void initialize() {
        // ⚡ Stocke l'instance Dashboard dans le root pour y accéder depuis les autres controllers
        if (contentPane.getScene() != null && contentPane.getScene().getRoot() != null) {
            contentPane.getScene().getRoot().setUserData(this);
        }
    }

    // ⚡ Charge une page dans le contentPane
    public void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (contentPane != null) {
                contentPane.getChildren().setAll(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ⚡ Getter & Setter du logement sélectionné
    public void setSelectedLogement(logement log) {
        this.selectedLogement = log;
    }

    public logement getSelectedLogement() {
        return selectedLogement;
    }

    // --- Boutons du menu Dashboard ---
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
    private void showLogement() {
        loadView("/fxml/Logements.fxml");
    }
}