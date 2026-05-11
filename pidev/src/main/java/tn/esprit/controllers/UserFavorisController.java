package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Publication;
import tn.esprit.services.FavorisService;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SelectedItem;
import tn.esprit.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class UserFavorisController implements Initializable {

    // Navbar partagée
    @FXML private NavbarController navbarController;

    @FXML private FlowPane favorisGrid;
    @FXML private Button btnExplorer;

    private FavorisService favorisService = new FavorisService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Mettre à jour la navbar
        if (navbarController != null) {
            navbarController.updateUserInfo();
            navbarController.setActivePublications();
        }

        chargerFavoris();
        btnExplorer.setOnAction(e -> naviguerVersExplorer());
    }

    private void naviguerVersExplorer() {
        NavigationManager.loadView("/fxml/UserExplorer.fxml", "Explorer les publications");
    }

    private void chargerFavoris() {
        favorisGrid.getChildren().clear();

        if (!SessionManager.isLoggedIn()) {
            afficherMessage("Connectez-vous pour voir vos favoris.");
            return;
        }

        try {
            List<Publication> favoris = favorisService.getFavorisByUtilisateur(SessionManager.getCurrentUser().getId());
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
        label.setStyle("-fx-font-size: 16; -fx-text-fill: #64748b; -fx-padding: 50;");
        favorisGrid.getChildren().add(label);
    }

    private VBox createCard(Publication p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); -fx-border-color: #e2e8f0; -fx-border-radius: 15;");
        card.setPrefWidth(280);
        card.setMaxWidth(280);

        Label titre = new Label(p.getTitre());
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titre.setWrapText(true);

        String categorieLabel = p.getCategorie() != null ? p.getCategorie().getLabel() : "Non catégorisé";
        Label categorie = new Label(categorieLabel);
        categorie.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 14; -fx-background-color: #EFF6FF; -fx-background-radius: 12; -fx-padding: 3 10;");

        Label auteur = new Label("✍️ " + (p.getAuteur() != null ? p.getAuteur() : "Anonyme"));
        auteur.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13;");

        Label likes = new Label("❤️ " + p.getLikes());
        likes.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13;");

        Button details = new Button("Voir détails");
        details.setStyle("-fx-background-color: #E8B156; -fx-text-fill: black; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");
        details.setMaxWidth(Double.MAX_VALUE);
        details.setOnAction(e -> {
            SelectedItem.setCurrentPublication(p);
            NavigationManager.loadView("/fxml/Commentaires.fxml", "Commentaires");
        });

        card.getChildren().addAll(titre, categorie, auteur, likes, details);
        return card;
    }
}