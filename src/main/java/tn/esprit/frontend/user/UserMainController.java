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
import tn.esprit.backend.utils.Session;
import tn.esprit.frontend.utils.Navigator;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UserMainController implements Initializable {

    @FXML private StackPane contentPane;
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Label welcomeLabel;
    @FXML private Button btnAccueil, btnExplorer, btnFavoris, btnMesPublications, btnProfil, btnModeAdmin, btnDeconnexion;

    private static UserMainController instance;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        Navigator.setContentPane(contentPane);

        if (Session.estConnecte()) {
            welcomeLabel.setText("Bienvenue, " + Session.getUtilisateur().getPrenom() + " !");
            if (Session.estAdmin()) {
                btnModeAdmin.setVisible(true);
                btnModeAdmin.setManaged(true);
            }
        }

        btnAccueil.setOnAction(e -> showAccueil());
        btnExplorer.setOnAction(e -> showExplorer());
        btnFavoris.setOnAction(e -> showFavoris());
        btnMesPublications.setOnAction(e -> showPublications());
        btnProfil.setOnAction(e -> showProfil());
        btnModeAdmin.setOnAction(e -> switchToAdmin());
        btnDeconnexion.setOnAction(e -> deconnecter());
        searchBtn.setOnAction(e -> rechercher());
        searchField.setOnAction(e -> rechercher());

        showAccueil();
    }

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

    @FXML public void showAccueil() { loadView("/views/user/UserAccueil.fxml"); }
    @FXML public void showExplorer() { loadView("/views/user/UserExplorer.fxml"); }
    @FXML public void showFavoris() { loadView("/views/user/UserFavoris.fxml"); }
    @FXML public void showPublications() { loadView("/views/user/UserPublications.fxml"); }
    @FXML public void showProfil() { loadView("/views/user/UserProfil.fxml"); }
    @FXML
    public void showChat() {
        loadView("/views/chat/ChatView.fxml");
    }
    private void rechercher() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            showExplorer();
        }
    }

    private void switchToAdmin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/admin/admin_dashboard.fxml"));
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Horizia - Administration");
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de passer en mode admin.");
        }
    }

    @FXML
    public void deconnecter() {
        Session.deconnecter();
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