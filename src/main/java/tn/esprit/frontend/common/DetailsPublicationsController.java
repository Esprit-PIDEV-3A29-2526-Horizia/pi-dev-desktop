package tn.esprit.frontend.common;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Commentaire;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.entities.Utilisateur;
import tn.esprit.backend.services.CommentaireService;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.backend.utils.Session;
import tn.esprit.frontend.admin.AdminDashboardController;
import tn.esprit.frontend.user.UserMainController;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DetailsPublicationsController implements Initializable {

    @FXML private Button retourBtn;
    @FXML private ImageView imagePublication;
    @FXML private Label titreLabel;
    @FXML private Label dateLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label nbCommentairesLabel;
    @FXML private TextArea nouveauCommentaireArea;
    @FXML private Button publierBtn;
    @FXML private VBox commentairesContainer;

    private Publication publication;
    private Utilisateur utilisateurConnecte;
    private CommentaireService commentaireService = new CommentaireService();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.publication = SelectedItem.getCurrentPublication();
        this.utilisateurConnecte = Session.getUtilisateur();

        if (publication == null) {
            showAlert("Erreur", "Aucune publication sélectionnée");
            goBack();
            return;
        }

        afficherPublication();
        chargerCommentaires();

        retourBtn.setOnAction(e -> goBack());
        publierBtn.setOnAction(e -> publierCommentaire());
    }

    private void goBack() {
        try {
            if (Session.estAdmin()) {
                AdminDashboardController.getInstance().showPublications();
            } else {
                UserMainController.getInstance().showPublications();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afficherPublication() {
        titreLabel.setText(publication.getTitre());
        dateLabel.setText("Publié le " + publication.getDateCreation().format(dateFormatter));
        descriptionLabel.setText(publication.getDescription());

        if (publication.getImage() != null && !publication.getImage().isEmpty()) {
            try {
                String imagePath = publication.getImage();
                if (!imagePath.startsWith("/")) {
                    imagePath = "/" + imagePath;
                }
                Image img = new Image(getClass().getResourceAsStream(imagePath));
                if (img != null && !img.isError()) {
                    imagePublication.setImage(img);
                } else {
                    setDefaultImage();
                }
            } catch (Exception e) {
                setDefaultImage();
            }
        } else {
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        try {
            Image defaultImg = new Image(getClass().getResourceAsStream("/images/default.png"));
            imagePublication.setImage(defaultImg);
        } catch (Exception e) {
            // Pas d'image par défaut
        }
    }

    private void chargerCommentaires() {
        commentairesContainer.getChildren().clear();
        var commentaires = commentaireService.getByPublication(publication.getId());
        nbCommentairesLabel.setText("(" + commentaires.size() + ")");

        for (Commentaire c : commentaires) {
            commentairesContainer.getChildren().add(creerCarteCommentaire(c));
        }
    }

    private VBox creerCarteCommentaire(Commentaire c) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String nomAuteur = c.getAuteur() != null ? c.getAuteur() : "Anonyme";
        Label nomLabel = new Label(nomAuteur);
        nomLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label dateLabelComment = new Label(c.getDateCreation().format(dateFormatter));
        dateLabelComment.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
        if (c.isModifie()) {
            dateLabelComment.setText(dateLabelComment.getText() + " (modifié)");
        }

        header.getChildren().addAll(nomLabel, dateLabelComment);

        Label contenuLabel = new Label(c.getContenu());
        contenuLabel.setWrapText(true);
        contenuLabel.setStyle("-fx-text-fill: #555;");

        card.getChildren().addAll(header, contenuLabel);

        if (utilisateurConnecte != null &&
                (utilisateurConnecte.getId() == c.getUtilisateurId() || (Session.estAdmin()))) {

            HBox actions = new HBox(10);
            actions.setAlignment(Pos.CENTER_RIGHT);

            if (utilisateurConnecte.getId() == c.getUtilisateurId()) {
                Button modifBtn = new Button("Modifier");
                modifBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 15;");
                modifBtn.setOnAction(e -> modifierCommentaire(c, card, contenuLabel));
                actions.getChildren().add(modifBtn);
            }

            Button supprBtn = new Button("Supprimer");
            supprBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 15;");
            supprBtn.setOnAction(e -> supprimerCommentaire(c));
            actions.getChildren().add(supprBtn);

            card.getChildren().add(actions);
        }

        return card;
    }

    private void publierCommentaire() {
        String contenu = nouveauCommentaireArea.getText().trim();

        if (contenu.isEmpty()) {
            showAlert("Erreur", "Le commentaire ne peut pas être vide !");
            return;
        }
        if (contenu.length() < 2) {
            showAlert("Erreur", "Le commentaire doit contenir au moins 2 caractères !");
            return;
        }
        if (contenu.length() > 500) {
            showAlert("Erreur", "Le commentaire ne peut pas dépasser 500 caractères !");
            return;
        }
        if (utilisateurConnecte == null) {
            showAlert("Erreur", "Vous devez être connecté pour commenter !");
            return;
        }

        Commentaire c = new Commentaire();
        c.setPublicationId(publication.getId());
        c.setUtilisateurId(utilisateurConnecte.getId());
        c.setAuteur(utilisateurConnecte.getNomComplet());
        c.setContenu(contenu);
        c.setDateCreation(java.time.LocalDateTime.now());

        commentaireService.ajouter(c);
        nouveauCommentaireArea.clear();
        chargerCommentaires();
        showAlert("Succès", "Commentaire ajouté !");
    }

    private void modifierCommentaire(Commentaire c, VBox card, Label contenuLabel) {
        TextArea editArea = new TextArea(c.getContenu());
        editArea.setWrapText(true);
        editArea.setPrefRowCount(3);
        editArea.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5;");

        Button saveBtn = new Button("Enregistrer");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 15;");
        saveBtn.setOnAction(e -> {
            String nouveauContenu = editArea.getText().trim();
            if (nouveauContenu.isEmpty() || nouveauContenu.length() < 2) {
                showAlert("Erreur", "Commentaire invalide !");
                return;
            }
            c.setContenu(nouveauContenu);
            c.setModifie(true);
            commentaireService.modifier(c);
            chargerCommentaires();
        });

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 15;");
        cancelBtn.setOnAction(e -> chargerCommentaires());

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().remove(contenuLabel);
        card.getChildren().add(1, editArea);
        card.getChildren().add(buttons);
    }

    private void supprimerCommentaire(Commentaire c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer ce commentaire ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                commentaireService.supprimer(c.getId());
                chargerCommentaires();
                showAlert("Succès", "Commentaire supprimé !");
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert.AlertType type = title.equals("Succès") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}