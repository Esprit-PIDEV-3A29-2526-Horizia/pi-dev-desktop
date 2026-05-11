package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.entities.logement;
import tn.esprit.services.Serviceuser;

import java.io.IOException;
import java.util.List;

public class AdminDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblAdminName;
    @FXML private Label lblAdminEmail;
    @FXML private AnchorPane contentArea;
    @FXML private StackPane contentPane;
    @FXML private ScrollPane mainScrollPane;

    // Labels pour les statistiques
    @FXML private Label totalMembresLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalAgentsLabel;
    @FXML private Label totalClientsLabel;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnProfils;
    @FXML private Button btnStats;
    @FXML private Button btnSettings;
    @FXML private Button btnVoyages;
    @FXML private Button btnReservations;
    @FXML private Button btncatalogue;
    @FXML private Button btnLogements;
    @FXML private Button btnEvenements;
    @FXML private Button btnLocationDashboard;
    @FXML private Button btnPublications;
    @FXML private Button btnCommentaires;

    private User currentUser;
    private Serviceuser serviceUser = new Serviceuser();
    private static logement selectedLogement;

    // Instance unique du controller
    private static AdminDashboardController instance;

    public AdminDashboardController() {
        instance = this;
    }

    public static AdminDashboardController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation AdminDashboardController ===");
        System.out.println("lblWelcome = " + lblWelcome);
        System.out.println("contentArea = " + contentArea);

        // Charger les statistiques au démarrage
        loadStats();
        showDashboard();
    }

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

    @FXML
    private void showEvenements() {
        System.out.println("=== Chargement de la gestion des événements ===");
        loadPage("/AdminHome.fxml");
        setActiveButton(btnEvenements);
    }

    @FXML
    private void showLocationDashboard() {
        System.out.println("=== Chargement du dashboard location ===");
        loadPage("/DashboardView.fxml");
        setActiveButton(btnLocationDashboard);
    }

    public void updateStats(int total, int admins, int agents, int clients) {
        if (totalMembresLabel != null) totalMembresLabel.setText(String.valueOf(total));
        if (totalAdminsLabel != null) totalAdminsLabel.setText(String.valueOf(admins));
        if (totalAgentsLabel != null) totalAgentsLabel.setText(String.valueOf(agents));
        if (totalClientsLabel != null) totalClientsLabel.setText(String.valueOf(clients));
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            if (lblWelcome != null) {
                lblWelcome.setText("Bienvenue, " + user.getNom() + " " + user.getPrenom());
            }
            if (lblAdminName != null) {
                lblAdminName.setText(user.getNom() + " " + user.getPrenom());
            }
            if (lblAdminEmail != null) {
                lblAdminEmail.setText(user.getEmail());
            }
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
        setActiveButton(btnStats);
    }

    private void animatePageTransition(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(AdminDashboardController.class.getResource(fxmlPath));
            Parent newContent = loader.load();

            Object controller = loader.getController();
            if (controller instanceof StatisticsController) {
                ((StatisticsController) controller).setDashboardController(this);
            }

            newContent.setOpacity(0);
            contentArea.getChildren().setAll(newContent);

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

    static void loadPage(String fxmlFile) {
        try {
            System.out.println("loadPage: " + fxmlFile);
            FXMLLoader loader = new FXMLLoader(AdminDashboardController.class.getResource(fxmlFile));

            if (loader.getLocation() == null) {
                System.err.println("❌ Fichier introuvable: " + fxmlFile);
                return;
            }

            Parent page = loader.load();

            Object controller = loader.getController();

            // Membres
            if (controller instanceof MemberListController) {
                ((MemberListController) controller).setDashboardController(getInstance());
                System.out.println("✅ DashboardController passé à MemberListController");
            }
            if (controller instanceof AddMemberController) {
                ((AddMemberController) controller).setDashboardController(getInstance());
                System.out.println("✅ DashboardController passé à AddMemberController");
            }
            if (controller instanceof EditMemberController) {
                ((EditMemberController) controller).setDashboardController(getInstance());
                System.out.println("✅ DashboardController passé à EditMemberController");
            }

            // Profils
            if (controller instanceof ProfilListController) {
                ((ProfilListController) controller).setDashboardController(getInstance());
                System.out.println("✅ DashboardController passé à ProfilListController");
            }
            if (controller instanceof AddProfilController) {
                ((AddProfilController) controller).setDashboardController(getInstance());
                System.out.println("✅ DashboardController passé à AddProfilController");
            }

            // Logements
            if (controller instanceof LogementsController) {
                System.out.println("✅ DashboardController passé à LogementsController");
            }
            if (controller instanceof DetailsLogementController) {
                System.out.println("✅ DashboardController passé à DetailsLogementController");
            }
            if (controller instanceof AjoutLogementController) {
                System.out.println("✅ DashboardController passé à AjoutLogementController");
            }
            if (controller instanceof ModifierLogementController) {
                System.out.println("✅ DashboardController passé à ModifierLogementController");
            }

            AdminDashboardController instance = getInstance();
            if (instance != null && instance.contentArea != null) {
                instance.contentArea.getChildren().setAll(page);
                AnchorPane.setTopAnchor(page, 0.0);
                AnchorPane.setBottomAnchor(page, 0.0);
                AnchorPane.setLeftAnchor(page, 0.0);
                AnchorPane.setRightAnchor(page, 0.0);
                System.out.println("✅ Page affichée dans contentArea");
            } else {
                System.err.println("❌ Erreur: contentArea est null");
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur loadPage pour " + fxmlFile);
            e.printStackTrace();
        }
    }

    public void setContent(Parent content) {
        if (contentArea != null) {
            contentArea.getChildren().setAll(content);
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
        }
    }

    private void setActiveButton(Button activeButton) {
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #E5E7EB; -fx-alignment: center-left; " +
                "-fx-padding: 12; -fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 14px; " +
                "-fx-pref-width: 220;";

        String activeStyle = "-fx-background-color: #3D94CA; -fx-text-fill: white; -fx-alignment: center-left; " +
                "-fx-padding: 12; -fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 14px; " +
                "-fx-pref-width: 220; -fx-font-weight: bold;";

        // Réinitialiser tous les boutons
        if (btnDashboard != null) btnDashboard.setStyle(inactiveStyle);
        if (btnUsers != null) btnUsers.setStyle(inactiveStyle);
        if (btnProfils != null) btnProfils.setStyle(inactiveStyle);
        if (btnStats != null) btnStats.setStyle(inactiveStyle);
        if (btnSettings != null) btnSettings.setStyle(inactiveStyle);
        if (btnEvenements != null) btnEvenements.setStyle(inactiveStyle);
        if (btnLocationDashboard != null) btnLocationDashboard.setStyle(inactiveStyle);
        if (btnVoyages != null) btnVoyages.setStyle(inactiveStyle);
        if (btnReservations != null) btnReservations.setStyle(inactiveStyle);
        if(btncatalogue != null) btncatalogue.setStyle(inactiveStyle);
        if (btnLogements != null) btnLogements.setStyle(inactiveStyle);
        if (btnPublications != null) btnPublications.setStyle(inactiveStyle);
        if (btnCommentaires != null) btnCommentaires.setStyle(inactiveStyle);
        if (btnStats != null) btnStats.setStyle(inactiveStyle);

        // Activer le bouton sélectionné
        if (activeButton != null) {
            activeButton.setStyle(activeStyle);
        }
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
            FXMLLoader loader = new FXMLLoader(AdminDashboardController.class.getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion Administrateur");
            stage.setMaximized(false);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showLogements(ActionEvent event) {
        System.out.println("=== Chargement de la gestion des logements ===");
        loadPage("/fxml/Logements.fxml");
        setActiveButton(btnLogements);
    }

    @FXML
    private void showVoyages() {
        System.out.println("=== Chargement de la gestion des voyages ===");
        loadPage("/fxml/GestionVoyage.fxml");
        setActiveButton(btnVoyages);
    }

    @FXML
    private void showReservations() {
        System.out.println("=== Chargement de la gestion des réservations ===");
        loadPage("/fxml/GestionReservationsAdmin.fxml");
        setActiveButton(btnReservations);
    }
    @FXML
    private void showcatalogue() {
        System.out.println("=== Chargement de la gestion des réservations ===");
        loadPage("/fxml/GestionCategorie.fxml");
        setActiveButton(btncatalogue);
    }

    @FXML
    private void showPublications(ActionEvent event) {
        System.out.println("=== Chargement de la gestion des publications ===");
        loadPage("/fxml/GestionPublications.fxml");
        setActiveButton(btnPublications);
    }

    @FXML
    private void showCommentaires(ActionEvent event) {
        System.out.println("=== Chargement de la gestion des commentaires ===");
        loadPage("/fxml/Commentaires.fxml");
        setActiveButton(btnCommentaires);
    }

    @FXML
    private void openChat(ActionEvent event) {
        System.out.println("=== Ouverture du chat ===");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChatView.fxml"));
            Parent chatView = loader.load();

            Stage chatStage = new Stage();
            chatStage.setTitle("Chat - Horozia");
            chatStage.setScene(new Scene(chatView));
            chatStage.setMinWidth(400);
            chatStage.setMinHeight(500);
            chatStage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'ouverture du chat");
            e.printStackTrace();
        }
    }

    @FXML
    private void showProfile(ActionEvent event) {
        System.out.println("=== Chargement du profil ===");
        // Charge la liste des profils (ou créez une vue profil dédiée)
        loadPage("/fxml/ProfilList.fxml");
        setActiveButton(btnProfils);
    }

    public static void setSelectedLogement(logement log) {
        selectedLogement = log;
    }

    public static logement getSelectedLogement() {
        return selectedLogement;
    }
}