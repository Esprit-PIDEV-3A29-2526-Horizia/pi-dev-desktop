package tn.esprit.frontend.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.frontend.utils.Navigator;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UserMainController implements Initializable {

    @FXML private StackPane contentPane;
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Label welcomeLabel;
    @FXML private Button btnAccueil;
    @FXML private Button btnExplorer;
    @FXML private Button btnFavoris;
    @FXML private Button btnMesPublications;
    @FXML private Button btnProfil;
    @FXML private Button btnDeconnexion;
    @FXML private Button btnChat;

    private static UserMainController instance;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        Navigator.setContentPane(contentPane);

        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            welcomeLabel.setText("Bienvenue, " + currentUser.getPrenom() + " !");
            // Le bouton admin a été supprimé
        }

        btnAccueil.setOnAction(e -> showAccueil());
        btnExplorer.setOnAction(e -> showExplorer());
        btnFavoris.setOnAction(e -> showFavoris());
        btnMesPublications.setOnAction(e -> showPublications());
        btnProfil.setOnAction(e -> showProfil());
        btnDeconnexion.setOnAction(e -> deconnecter());
        btnChat.setOnAction(e -> showChat());
        searchBtn.setOnAction(e -> rechercher());
        searchField.setOnAction(e -> rechercher());

        showAccueil();
    }

    /**
     * Charge une vue dans la zone de contenu.
     * @param fxmlPath chemin vers le fichier FXML (doit commencer par /)
     */
    public void loadView(String fxmlPath) {
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger: " + fxmlPath);
        }
    }

    // ===== Méthodes de navigation pour l'espace utilisateur =====
    @FXML public void showAccueil() { loadView("/views/user/UserAccueil.fxml"); }
    @FXML public void showExplorer() { loadView("/fxml/Logements.fxml"); }
    @FXML public void showFavoris() { loadView("/views/user/UserFavoris.fxml"); }
    @FXML public void showPublications() { loadView("/views/user/UserPublications.fxml"); }
    @FXML public void showProfil() { loadView("/fxml/UserProfil.fxml"); }
    @FXML public void showChat() { loadView("/views/chat/ChatView.fxml"); }

    // ===== Méthodes de navigation pour l'en-tête commun =====
    @FXML public void retourAccueil() { showAccueil(); }
    @FXML public void afficherLogements() { showExplorer(); }
    @FXML public void voirMesReservations() { loadView("/fxml/MesReservations.fxml"); }
    @FXML public void afficherEvenements() { loadView("/fxml/UserHome.fxml"); }
    @FXML public void afficherLocation() { loadView("/client/AccueilClient.fxml"); }  // ou AfficherVehiculesView.fxml
    @FXML public void afficherVoyager() { loadView("/fxml/CatalogueUser.fxml"); }
    @FXML public void showUserProfile() { showProfil(); }
    @FXML public void handleLogout() { deconnecter(); }

    // ===== Autres actions =====
    private void rechercher() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            showExplorer();
        }
    }

    @FXML
    public void deconnecter() {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/common/login.fxml"));
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Horizia - Connexion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static UserMainController getInstance() { return instance; }
}