package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Categorie;
import tn.esprit.entities.Publication;
import tn.esprit.entities.User;
import tn.esprit.services.CommentaireService;
import tn.esprit.services.PublicationService;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SelectedItem;
import tn.esprit.utils.SessionManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserPublicationsController implements Initializable {

    @FXML private NavbarController navbarController;
    @FXML private Label totalLabel;
    @FXML private Label likesLabel;
    @FXML private Label commentsLabel;
    @FXML private FlowPane itemsGrid;
    @FXML private Button btnAjouter;

    private PublicationService publicationService = new PublicationService();
    private CommentaireService commentaireService = new CommentaireService();
    private List<Publication> mesPublications;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUser = SessionManager.getCurrentUser();

        if (navbarController != null) {
            navbarController.updateUserInfo();
            navbarController.setActivePublications();
        }

        if (!SessionManager.isLoggedIn()) {
            NavigationManager.showLogin();
            return;
        }

        chargerPublications();
        btnAjouter.setOnAction(e -> naviguerAjouterPublication());
    }

    private void naviguerAjouterPublication() {
        NavigationManager.loadView("/fxml/AjouterPublication.fxml", "Ajouter une publication");
    }

    private void chargerPublications() {
        int userId = currentUser.getId();
        mesPublications = publicationService.getAll().stream()
                .filter(p -> p.getUtilisateurId() == userId)
                .toList();

        totalLabel.setText(String.valueOf(mesPublications.size()));

        int totalLikes = mesPublications.stream()
                .mapToInt(Publication::getLikes).sum();
        likesLabel.setText(String.valueOf(totalLikes));

        int totalComments = mesPublications.stream()
                .mapToInt(p -> commentaireService.getByPublication(p.getId()).size())
                .sum();
        commentsLabel.setText(String.valueOf(totalComments));

        afficherPublications(mesPublications);
    }

    private void afficherPublications(List<Publication> publications) {
        itemsGrid.getChildren().clear();

        if (publications.isEmpty()) {
            Label emptyLabel = new Label("Vous n'avez aucune publication");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16px; -fx-padding: 50;");
            itemsGrid.getChildren().add(emptyLabel);
            return;
        }

        for (Publication p : publications) {
            VBox card = creerCarte(p);
            itemsGrid.getChildren().add(card);
        }
    }

    private VBox creerCarte(Publication p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(300);
        card.setMaxWidth(300);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(270);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);

        if (p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                String path = p.getImage().startsWith("/") ? p.getImage() : "/" + p.getImage();
                Image img = new Image(getClass().getResourceAsStream(path));
                imageView.setImage(img);
            } catch (Exception e) {
                setDefaultImage(imageView);
            }
        } else {
            setDefaultImage(imageView);
        }

        // Titre
        Label titre = new Label(p.getTitre());
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1A3C5A;");
        titre.setWrapText(true);

        // Catégorie
        String categorieLabel = p.getCategorie() != null ? p.getCategorie().getLabel() : "Non catégorisé";
        Label categorie = new Label(categorieLabel);
        categorie.setStyle("-fx-text-fill: #3b82f6; -fx-background-color: #EFF6FF; -fx-background-radius: 15; -fx-padding: 4 12;");

        // Auteur
        Label auteur = new Label("✍️ " + (p.getAuteur() != null ? p.getAuteur() : "Anonyme"));
        auteur.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px");

        // Date
        Label date = new Label("📅 " + (p.getDateCreation() != null ? p.getDateCreation().toLocalDate().toString() : "Date inconnue"));
        date.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px");

        // Likes
        Label likes = new Label("❤️ " + p.getLikes());
        likes.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px");

        // Boutons d'action
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button editBtn = new Button("✏️ Modifier");
        editBtn.setStyle("-fx-background-color: #3D94CA; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 12; -fx-font-size: 12px;");
        editBtn.setOnAction(e -> {
            SelectedItem.setCurrentPublication(p);
            NavigationManager.loadView("/fxml/ModifierPublication.fxml", "Modifier la publication");
        });

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 12; -fx-font-size: 12px;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText(null);
            confirm.setContentText("Supprimer \"" + p.getTitre() + "\" ?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    publicationService.supprimer(p.getId());
                    chargerPublications();
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Succès");
                    success.setHeaderText(null);
                    success.setContentText("Publication supprimée avec succès !");
                    success.showAndWait();
                } catch (Exception ex) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Erreur");
                    error.setHeaderText(null);
                    error.setContentText(ex.getMessage());
                    error.showAndWait();
                }
            }
        });

        actionBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(imageView, titre, categorie, auteur, date, likes, actionBox);
        return card;
    }

    private void setDefaultImage(ImageView imageView) {
        try {
            Image defaultImg = new Image(getClass().getResourceAsStream("/images/default.jpg"));
            imageView.setImage(defaultImg);
        } catch (Exception e) {
            // Pas d'image par défaut
        }
    }
}