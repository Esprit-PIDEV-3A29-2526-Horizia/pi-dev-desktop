package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import tn.esprit.entities.Publication;
import tn.esprit.services.CommentaireService;
import tn.esprit.services.FavorisService;
import tn.esprit.services.PublicationService;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SelectedItem;
import tn.esprit.utils.SessionManager;

import java.sql.SQLException;
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
    @FXML private Label commentairesLabel;
    @FXML private HBox adminActionsBox;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button likeButton;
    @FXML private Button commentairesBtn;
    @FXML private Button favoriBtn;

    private Publication publication;
    private PublicationService publicationService = new PublicationService();
    private CommentaireService commentaireService = new CommentaireService();
    private FavorisService favorisService = new FavorisService();
    private Consumer<Publication> onEditCallback;
    private Consumer<Publication> onDeleteCallback;
    private boolean estFavori = false;
    private boolean adminMode = false;

    @FXML
    public void initialize() {
        chargerIcones();

        if (likeButton != null) {
            likeButton.setOnAction(e -> {
                if (SessionManager.isLoggedIn()) {
                    try {
                        publicationService.incrementerLikes(publication.getId());
                        publication.setLikes(publication.getLikes() + 1);
                        likesLabel.setText(String.valueOf(publication.getLikes()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    showAlert("Connexion requise", "Connectez-vous pour aimer.");
                }
            });
        }

        if (commentairesBtn != null) {
            commentairesBtn.setOnAction(e -> {
                SelectedItem.setCurrentPublication(publication);
                NavigationManager.loadView("/views/common/Commentaires.fxml", "Commentaires");
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

        if (favoriBtn != null) {
            favoriBtn.setOnAction(e -> {
                if (SessionManager.isLoggedIn()) {
                    try {
                        int userId = SessionManager.getCurrentUser().getId();
                        if (estFavori) {
                            favorisService.supprimerFavori(userId, publication.getId());
                            estFavori = false;
                            favoriBtn.setStyle(
                                    "-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: #888;" +
                                            "-fx-font-size: 18; -fx-background-radius: 25; -fx-cursor: hand; -fx-padding: 0;"
                            );
                        } else {
                            favorisService.ajouterFavori(userId, publication.getId());
                            estFavori = true;
                            favoriBtn.setStyle(
                                    "-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: #f59e0b;" +
                                            "-fx-font-size: 18; -fx-background-radius: 25; -fx-cursor: hand; -fx-padding: 0;"
                            );
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showAlert("Erreur", "Impossible de modifier le favori.");
                    }
                } else {
                    showAlert("Connexion requise", "Connectez-vous pour ajouter aux favoris.");
                }
            });
        }
    }

    private void chargerIcones() {
        if (commentairesBtn != null) {
            FontIcon icon = new FontIcon("fas-comment");
            icon.setIconSize(16);
            icon.setIconColor(Color.web("#3b82f6"));
            commentairesBtn.setGraphic(icon);
            commentairesBtn.setText("");
            commentairesBtn.setStyle("-fx-background-color: #e0f2fe; -fx-background-radius: 6; -fx-cursor: hand;");
        }
        if (modifierBtn != null) {
            FontIcon icon = new FontIcon("fas-pencil-alt");
            icon.setIconSize(16);
            icon.setIconColor(Color.WHITE);
            modifierBtn.setGraphic(icon);
            modifierBtn.setText("");
            modifierBtn.setStyle("-fx-background-color: #f59e0b; -fx-background-radius: 6; -fx-cursor: hand;");
        }
        if (supprimerBtn != null) {
            FontIcon icon = new FontIcon("fas-trash");
            icon.setIconSize(16);
            icon.setIconColor(Color.WHITE);
            supprimerBtn.setGraphic(icon);
            supprimerBtn.setText("");
            supprimerBtn.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 6; -fx-cursor: hand;");
        }
    }

    public void setAdminMode(boolean mode) {
        this.adminMode = mode;
        appliquerMode();
    }

    public void setPublication(Publication p) {
        this.publication = p;

        titreLabel.setText(p.getTitre());
        if (p.getCategorie() != null) {
            categorieLabel.setText(p.getCategorie().getLabel());
        }
        auteurLabel.setText("Par " + (p.getAuteur() != null ? p.getAuteur() : "Anonyme"));
        dateLabel.setText(p.getDateCreation().toLocalDate().toString());

        String desc = p.getDescription();
        if (desc != null && desc.length() > 100) {
            desc = desc.substring(0, 100) + "...";
        }
        descriptionLabel.setText(desc);
        likesLabel.setText(String.valueOf(p.getLikes()));

        if (commentairesLabel != null) {
            try {
                int nbCommentaires = commentaireService.getByPublication(p.getId()).size();
                commentairesLabel.setText(String.valueOf(nbCommentaires));
            } catch (Exception e) {
                commentairesLabel.setText("0");
            }
        }

        chargerImage(p);

        // Vérifier les favoris
        if (SessionManager.isLoggedIn() && favoriBtn != null) {
            try {
                int userId = SessionManager.getCurrentUser().getId();
                estFavori = favorisService.estFavori(userId, p.getId());
                favoriBtn.setText("★");
                favoriBtn.setStyle(
                        estFavori
                                ? "-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: #f59e0b; -fx-font-size: 18; -fx-background-radius: 25; -fx-cursor: hand; -fx-padding: 0;"
                                : "-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: #888; -fx-font-size: 18; -fx-background-radius: 25; -fx-cursor: hand; -fx-padding: 0;"
                );
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        appliquerMode();
    }

    private void appliquerMode() {
        if (adminMode) {
            // Mode admin : tout visible
            setVisible(likeButton, true);
            setVisible(likesLabel, true);
            setVisible(favoriBtn, true);
            setVisible(commentairesBtn, true);
            setVisible(commentairesLabel, true);
            setVisible(modifierBtn, true);
            setVisible(supprimerBtn, true);
        } else {
            // Mode utilisateur : like + commentaire + favori seulement
            setVisible(likeButton, true);
            setVisible(likesLabel, true);
            setVisible(favoriBtn, true);
            setVisible(commentairesBtn, true);
            setVisible(commentairesLabel, true);
            setVisible(modifierBtn, false);
            setVisible(supprimerBtn, false);
        }
    }

    private void setVisible(javafx.scene.Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
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

    public void setOnEditCallback(Consumer<Publication> callback) {
        this.onEditCallback = callback;
    }

    public void setOnDeleteCallback(Consumer<Publication> callback) {
        this.onDeleteCallback = callback;
    }

    public Publication getPublication() { return publication; }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}