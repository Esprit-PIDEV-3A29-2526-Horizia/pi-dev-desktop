package tn.esprit.frontend.common;  // ✅ Fixed package

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import tn.esprit.backend.entities.Publication;  // ✅ Added

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
    public void showpublication() {
        loadView("/views/common/publication.fxml");
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

    // ✅ Fixed: Made static so it can be called from other controllers
    public static void loadViewStatic(String fxmlPath) {
        if (instance != null) {
            instance.loadView(fxmlPath);
        } else {
            System.err.println("DashboardController instance is null");
        }
    }

    public void refreshpublication() {
        if (currentController instanceof PublicationsController) {
            ((PublicationsController) currentController).refresh();
        } else {
            showpublication();
        }
    }

    public void refreshDashboard() {
        // TODO: Implement if DashboardViewController exists
        showDashboard();
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