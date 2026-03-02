package tn.esprit.frontend.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.entities.Utilisateur;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.backend.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    private static AdminDashboardController instance;

    @FXML private StackPane contentPane;
    @FXML private Button btnDashboard;
    @FXML private Button btnVoyages;
    @FXML private Button btnReservations;
    @FXML private Button btnLogements;
    @FXML private Button btnPublications;
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnStatistiques;
    @FXML private Button btnChat;
    @FXML private Button btnModeUser;
    @FXML private Button btnDeconnexion;
    @FXML private Label welcomeLabel;
    @FXML private Label userRoleLabel;

    private boolean initialized = false;
    private String currentView = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (initialized) return;
        initialized = true;

        instance = this;

        if (!Session.estAdmin()) {
            showAlert("Accès refusé", "Vous devez être administrateur");
            return;
        }

        setupInterface();
        showDashboard();
    }

    private void setupInterface() {
        Utilisateur currentUser = Session.getUtilisateur();
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getPrenom() + " " + currentUser.getNom());
            userRoleLabel.setText("Administrateur");
        }
    }

    public void loadView(String fxmlPath) {
        try {
            if (fxmlPath.equals(currentView)) return;

            System.out.println("🔍 Chargement: " + fxmlPath);
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                url = getClass().getClassLoader().getResource(fxmlPath.substring(1));
            }
            if (url == null) {
                showAlert("Erreur", "Fichier non trouvé:\n" + fxmlPath);
                return;
            }
            Node view = FXMLLoader.load(url);
            contentPane.getChildren().setAll(view);
            currentView = fxmlPath;
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger: " + fxmlPath);
        }
    }

    @FXML public void showDashboard() { loadView("/views/admin/dashboard_content.fxml"); }
    @FXML public void showVoyages()   { loadView("/views/admin/admin_voyages.fxml"); }
    @FXML public void showReservations() { loadView("/views/admin/admin_reservations.fxml"); }
    @FXML public void showLogements() { loadView("/views/admin/admin_logements.fxml"); }
    @FXML public void showPublications() { loadView("/views/admin/GestionPublications.fxml"); }
    @FXML public void showUtilisateurs() { loadView("/views/admin/admin_utilisateurs.fxml"); }
    @FXML public void showStatistiques() { loadView("/views/admin/admin_statistiques.fxml"); }
    @FXML public void showChat() { loadView("/views/chat/ChatView.fxml"); }

    // ✅ Méthodes pour l'ajout et la modification des publications
    public void openAddPublicationModal() {
        try {
            SelectedItem.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/common/AjouterPublication.fxml"));
            Node form = loader.load();
            contentPane.getChildren().setAll(form);
            // 🔥 Forcer le rechargement lors du retour en changeant la valeur de currentView
            currentView = "modal";
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    public void openEditPublicationModal(Publication publication) {
        try {
            SelectedItem.setCurrentPublication(publication);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/common/ModifierPublication.fxml"));
            Node form = loader.load();
            contentPane.getChildren().setAll(form);
            currentView = "edit_modal";
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    @FXML
    public void switchToUserMode() {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Changement de mode");
            confirm.setContentText("Passer en mode utilisateur ?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                Parent root = FXMLLoader.load(getClass().getResource("/views/user/user_main.fxml"));
                Stage stage = (Stage) contentPane.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Horizia - Espace Utilisateur");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void deconnecter() {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Déconnexion");
            confirm.setContentText("Voulez-vous vous déconnecter ?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                Session.deconnecter();
                Parent root = FXMLLoader.load(getClass().getResource("/views/common/login.fxml"));
                Stage stage = (Stage) contentPane.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Horizia - Connexion");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static AdminDashboardController getInstance() { return instance; }
}