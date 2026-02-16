package tn.esprit.controllers;
import tn.esprit.controllers.ProfilListController;
import tn.esprit.controllers.AddProfilController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceUser;

import java.io.IOException;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblAdminName;
    @FXML private Label lblAdminEmail;
    @FXML private AnchorPane contentArea;

    // AJOUTEZ CES 4 LIGNES - Labels pour les statistiques de la sidebar
    @FXML private Label totalMembresLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalAgentsLabel;
    @FXML private Label totalClientsLabel;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnProfils;
    @FXML private Button btnStats;
    @FXML private Button btnSettings;

    private User currentUser;
    private ServiceUser serviceUser = new ServiceUser(); // AJOUTEZ CETTE LIGNE

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation AdminDashboardController ===");
        System.out.println("lblWelcome = " + lblWelcome);
        System.out.println("contentArea = " + contentArea);
        System.out.println("btnDashboard = " + btnDashboard);

        if (lblWelcome == null) {
            System.err.println("❌ lblWelcome est null! Vérifiez que:");
            System.err.println("   1. Le Label a fx:id='lblWelcome' dans le FXML");
            System.err.println("   2. L'import de Label est présent");
            System.err.println("   3. Le fichier FXML est correctement chargé");
        }

        // Charger les statistiques au démarrage
        loadStats();
        showDashboard();
    }

    // AJOUTEZ CETTE MÉTHODE - Pour charger les statistiques
    private void loadStats() {
        try {
            List<User> users = serviceUser.afficher();
            int total = users.size();

            long admins = users.stream().filter(u -> u.getType() != null && u.getType().equals("ADMIN")).count();
            long agents = users.stream().filter(u -> u.getType() != null && u.getType().equals("AGENT")).count();
            long clients = users.stream().filter(u -> u.getType() != null && u.getType().equals("CLIENT")).count();

            updateStats(total, (int) admins, (int) agents, (int) clients);

            System.out.println("✅ Statistiques chargées: Total=" + total +
                    " Admins=" + admins + " Agents=" + agents + " Clients=" + clients);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des statistiques:");
            e.printStackTrace();
        }
    }

    public void updateStats(int total, int admins, int agents, int clients) {
        // Vérifier que les labels ne sont pas null avant de les utiliser
        if (totalMembresLabel != null) totalMembresLabel.setText(String.valueOf(total));
        if (totalAdminsLabel != null) totalAdminsLabel.setText(String.valueOf(admins));
        if (totalAgentsLabel != null) totalAgentsLabel.setText(String.valueOf(agents));
        if (totalClientsLabel != null) totalClientsLabel.setText(String.valueOf(clients));
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            lblWelcome.setText("Bienvenue, " + user.getNom() + " " + user.getPrenom());
            if (lblAdminName != null) lblAdminName.setText(user.getNom() + " " + user.getPrenom());
            if (lblAdminEmail != null) lblAdminEmail.setText(user.getEmail());
        }
    }

    @FXML
    private void showDashboard() {
        loadPage("/fxml/DashboardContent.fxml");
        setActiveButton(btnDashboard);
    }

    @FXML
    void showUsers() {
        System.out.println("=== Chargement de la liste des membres ===");
        loadPage("/fxml/MemberList.fxml");
        setActiveButton(btnUsers);
    }

    @FXML
    void showProfils() {
        System.out.println("=== Chargement de la liste des profils ===");
        loadPage("/fxml/ProfilList.fxml");
        setActiveButton(btnProfils);
    }

    @FXML
    private void showStats() {
        System.out.println("=== Accès aux statistiques ===");
        animatePageTransition("/fxml/Statistics.fxml");
    }
    private void animatePageTransition(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newContent = loader.load();

            // Passer le contrôleur
            Object controller = loader.getController();
            if (controller instanceof StatisticsController) {
                ((StatisticsController) controller).setDashboardController(this);
            }

            // Animation simple (optionnelle)
            newContent.setOpacity(0);
            contentArea.getChildren().setAll(newContent);

            // Animation de fondu
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), newContent
            );
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            System.out.println("✅ Page chargée avec animation : " + fxmlPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showSettings() {
        loadPage("/fxml/Settings.fxml");
        setActiveButton(btnSettings);
    }

    void loadPage(String fxmlFile) {
        try {
            System.out.println("loadPage: " + fxmlFile);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent page = loader.load();

            Object controller = loader.getController();

            // Membres
            if (controller instanceof MemberListController) {
                ((MemberListController) controller).setDashboardController(this);
                System.out.println("✅ DashboardController passé à MemberListController");
            }
            if (controller instanceof AddMemberController) {
                ((AddMemberController) controller).setDashboardController(this);
                System.out.println("✅ DashboardController passé à AddMemberController");
            }
            if (controller instanceof EditMemberController) {
                ((EditMemberController) controller).setDashboardController(this);
                System.out.println("✅ DashboardController passé à EditMemberController");
            }

            // Profils
            if (controller instanceof ProfilListController) {
                ((ProfilListController) controller).setDashboardController(this);
                System.out.println("✅ DashboardController passé à ProfilListController");
            }
            if (controller instanceof AddProfilController) {
                ((AddProfilController) controller).setDashboardController(this);
                System.out.println("✅ DashboardController passé à AddProfilController");
            }

            contentArea.getChildren().setAll(page);
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);

            System.out.println("✅ Page affichée dans contentArea");

        } catch (IOException e) {
            System.err.println("❌ Erreur loadPage pour " + fxmlFile);
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
        // Utiliser les classes CSS au lieu des styles inline
        btnDashboard.getStyleClass().remove("active");
        btnUsers.getStyleClass().remove("active");
        btnProfils.getStyleClass().remove("active");
        btnStats.getStyleClass().remove("active");
        btnSettings.getStyleClass().remove("active");

        activeButton.getStyleClass().add("active");
    }

    @FXML
    private void showAddMember() {
        loadPage("/fxml/AddMember.fxml");
    }

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