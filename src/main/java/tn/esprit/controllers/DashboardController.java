package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import tn.esprit.entities.Publication;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private StackPane contentPane;

    private static Publication selectedPublication;
    private static DashboardController instance;
    private Object currentController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        loadView("/DashboardView.fxml");
    }

    @FXML
    public void showVoyages() {
        loadView("/Voyages.fxml");
    }

    @FXML
    public void showReservations() {
        loadView("/Reservations.fxml");
    }

    @FXML
    public void showPublications() {
        loadView("/Publications.fxml");
    }

    @FXML
    public void showLogements() {
        loadView("/Logements.fxml");
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            if (loader.getLocation() == null) {
                System.err.println("❌ FXML non trouvé: " + fxmlPath);
                return;
            }

            Node view = loader.load();
            currentController = loader.getController();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // CORRECTION: Méthode pour rafraîchir les publications
    public void refreshPublications() {
        if (currentController instanceof PublicationsController) {
            ((PublicationsController) currentController).refresh(); // Utiliser refresh() au lieu de refreshData()
        } else {
            showPublications();
        }
    }

    // Rafraîchir le dashboard (stats)
    public void refreshDashboard() {
        if (currentController instanceof DashboardViewController) {
            ((DashboardViewController) currentController).refreshStats();
        } else {
            showDashboard();
        }
    }

    public static DashboardController getInstance() {
        return instance;
    }

    public static void setSelectedPublication(Publication p) {
        selectedPublication = p;
    }

    public static Publication getSelectedPublication() {
        return selectedPublication;
    }

    public Object getCurrentController() {
        return currentController;
    }
}