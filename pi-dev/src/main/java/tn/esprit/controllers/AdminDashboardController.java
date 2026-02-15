package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tn.esprit.entities.User;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private Label lblWelcome;
    @FXML private AnchorPane contentArea;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnProfils;
    @FXML private Button btnStats;
    @FXML private Button btnSettings;

    private User currentUser;

    @FXML
    public void initialize() {
        // Charger la vue par défaut
        showDashboard();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        lblWelcome.setText("Bienvenue, " + user.getNom() + " " + user.getPrenom());
    }

    @FXML
    private void showDashboard() {
        // Charger la vue du tableau de bord
        loadPage("/fxml/DashboardContent.fxml");
        setActiveButton(btnDashboard);
    }

    @FXML
    void showUsers() {
        // Charger la vue de gestion des membres
        loadPage("/fxml/MemberList.fxml");
        setActiveButton(btnUsers);
    }

    @FXML
    private void showProfils() {
        // Charger la vue de gestion des profils
        loadPage("/fxml/ProfilList.fxml");
        setActiveButton(btnProfils);
    }

    @FXML
    private void showStats() {
        // Charger la vue des statistiques
        loadPage("/fxml/Statistics.fxml");
        setActiveButton(btnStats);
    }

    @FXML
    private void showSettings() {
        // Charger la vue des paramètres
        loadPage("/fxml/Settings.fxml");
        setActiveButton(btnSettings);
    }

    void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent page = loader.load();

            // Passer la référence du dashboard
            Object controller = loader.getController();
            if (controller instanceof MemberListController) {
                ((MemberListController) controller).setDashboardController(this);
            }

            contentArea.getChildren().setAll(page);
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setContent(Parent content) {
        contentArea.getChildren().setAll(content);
        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);
    }
    private void setActiveButton(Button activeButton) {
        // Réinitialiser tous les boutons
        btnDashboard.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        btnUsers.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        btnProfils.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        btnStats.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        btnSettings.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");

        // Mettre en surbrillance le bouton actif
        activeButton.setStyle("-fx-background-color: #2a5298; -fx-background-radius: 10; -fx-text-fill: white;");
    }
    @FXML
    private void showAddMember() {
        loadPage("/fxml/AddMember.fxml");
        // Vous pouvez ajouter un style spécial pour le bouton si nécessaire
    }

    // Ou si vous voulez l'ouvrir depuis la liste des membres
    public void openAddMemberForm() {
        loadPage("/fxml/AddMember.fxml");
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion Administrateur");
            stage.setMaximized(false);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}