package tn.esprit.frontend.user;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.FavorisService;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.entities.User;
import tn.esprit.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class UserFavorisController implements Initializable {

    @FXML private FlowPane favorisGrid;
    @FXML private Button btnExplorer;

    private FavorisService favorisService = new FavorisService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerFavoris();
        btnExplorer.setOnAction(e -> UserMainController.getInstance().showExplorer());
    }

    private void chargerFavoris() {
        favorisGrid.getChildren().clear();
        if (!SessionManager.isLoggedIn()) {
            afficherMessage("Connectez-vous pour voir vos favoris.");
            return;
        }
        try {
            User currentUser = SessionManager.getCurrentUser();
            List<Publication> favoris = favorisService.getFavorisByUtilisateur(currentUser.getId());
            if (favoris.isEmpty()) {
                afficherMessage("Vous n'avez encore aucun favori.");
            } else {
                for (Publication p : favoris) {
                    VBox card = createCard(p);
                    favorisGrid.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            afficherMessage("Erreur lors du chargement des favoris.");
        }
    }

    private void afficherMessage(String message) {
        Label label = new Label(message);
        label.setStyle("-fx-font-size: 16; -fx-text-fill: #64748b;");
        favorisGrid.getChildren().add(label);
    }

    private VBox createCard(Publication p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); -fx-border-color: #e2e8f0; -fx-border-radius: 15;");
        card.setPrefWidth(280);

        Label titre = new Label(p.getTitre());
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titre.setWrapText(true);

        Label categorie = new Label(p.getCategorie().getLabel()); // Assurez-vous que getLabel() existe
        categorie.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 14;");

        Label auteur = new Label("Par " + (p.getAuteur() != null ? p.getAuteur() : "Anonyme"));
        auteur.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13;");

        Label likes = new Label("♥ " + p.getLikes());
        likes.setStyle("-fx-text-fill: #ef4444;");

        Button details = new Button("Voir détails");
        details.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        details.setOnAction(e -> {
            SelectedItem.setCurrentPublication(p);
            UserMainController.getInstance().loadView("/views/common/Commentaires.fxml");
        });

        card.getChildren().addAll(titre, categorie, auteur, likes, details);
        return card;
    }
}