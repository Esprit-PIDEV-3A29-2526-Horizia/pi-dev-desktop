package tn.esprit.frontend.components;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.CommentaireService;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.backend.utils.Session;
import tn.esprit.frontend.admin.AdminDashboardController;
import tn.esprit.frontend.user.UserMainController;

import java.util.function.Consumer;

public class PublicationCardController {

    @FXML private VBox cardRoot;
    @FXML private ImageView imageView;
    @FXML private Label titreLabel;
    @FXML private Label categorieLabel;
    @FXML private Label auteurLabel;
    @FXML private Label dateLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label likesLabel;
    @FXML private Label commentsLabel;
    @FXML private HBox adminActionsBox;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button likeButton;          // Bouton like
    @FXML private Button commentairesBtn;     // Bouton pour ouvrir les commentaires

    private Publication publication;
    private PublicationService publicationService = new PublicationService();
    private CommentaireService commentaireService = new CommentaireService();
    private Consumer<Publication> onEditCallback;
    private Consumer<Publication> onDeleteCallback;

    @FXML
    public void initialize() {
        if (likeButton != null) {
            likeButton.setOnAction(e -> {
                if (Session.estConnecte()) {
                    publicationService.incrementerLikes(publication.getId());
                    publication.setLikes(publication.getLikes() + 1);
                    likesLabel.setText("♥ " + publication.getLikes());
                } else {
                    showAlert("Connexion requise", "Vous devez être connecté pour aimer.");
                }
            });
        }

        if (commentairesBtn != null) {
            commentairesBtn.setOnAction(e -> {
                SelectedItem.setCurrentPublication(publication);
                if (UserMainController.getInstance() != null) {
                    UserMainController.getInstance().loadView("/views/common/Commentaires.fxml");
                } else if (AdminDashboardController.getInstance() != null) {
                    AdminDashboardController.getInstance().loadView("/views/common/Commentaires.fxml");
                }
            });
        }

        if (modifierBtn != null) {
            modifierBtn.setOnAction(e -> {
                if (onEditCallback != null && publication != null) {
                    onEditCallback.accept(publication);
                }
            });
        }

        if (supprimerBtn != null) {
            supprimerBtn.setOnAction(e -> {
                if (onDeleteCallback != null && publication != null) {
                    onDeleteCallback.accept(publication);
                }
            });
        }
    }

    public void setPublication(Publication p) {
        this.publication = p;

        titreLabel.setText(p.getTitre());
        categorieLabel.setText(p.getCategorie().getDisplay());
        auteurLabel.setText("Par " + (p.getAuteur() != null ? p.getAuteur() : "Anonyme"));
        dateLabel.setText("📅 " + p.getDateCreation().toLocalDate().toString());

        String desc = p.getDescription();
        if (desc != null && desc.length() > 100) {
            desc = desc.substring(0, 100) + "...";
        }
        descriptionLabel.setText(desc);

        likesLabel.setText("♥ " + p.getLikes());
        int commentCount = commentaireService.getByPublication(p.getId()).size();
        commentsLabel.setText("💬 " + commentCount);

        chargerImage(p);
    }

    private void chargerImage(Publication p) {
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                String imagePath = p.getImage().startsWith("/") ? p.getImage() : "/" + p.getImage();
                Image img = new Image(getClass().getResourceAsStream(imagePath));
                if (img != null && !img.isError()) {
                    imageView.setImage(img);
                    return;
                }
            } catch (Exception e) {
                // Ignoré
            }
        }
        setDefaultImage();
    }

    private void setDefaultImage() {
        try {
            Image defaultImg = new Image(getClass().getResourceAsStream("/images/default.png"));
            imageView.setImage(defaultImg);
        } catch (Exception e) {
            // Pas d'image par défaut
        }
    }

    public void setAdminMode(boolean adminMode) {
        if (adminActionsBox != null) {
            adminActionsBox.setVisible(adminMode);
            adminActionsBox.setManaged(adminMode);
        }
    }

    public void setOnEditCallback(Consumer<Publication> callback) {
        this.onEditCallback = callback;
    }

    public void setOnDeleteCallback(Consumer<Publication> callback) {
        this.onDeleteCallback = callback;
    }

    public Publication getPublication() { return publication; }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void modifier() {
        // Logique pour modifier la publication
        System.out.println("Modifier la publication");
    }

    @FXML
    private void supprimer() {
        // Logique pour supprimer la publication
        System.out.println("Supprimer la publication");
    }
}