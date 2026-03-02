package tn.esprit.frontend.common;  // ✅ Fixed package

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Commentaire;  // ✅ Added
import tn.esprit.backend.entities.Publication;  // ✅ Added
import tn.esprit.backend.entities.Utilisateur;  // ✅ Added
import tn.esprit.backend.services.CommentaireService;  // ✅ Added
import tn.esprit.backend.utils.Session;  // ✅ Added

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DetailsPublicationController implements Initializable {

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
    private Utilisateur utilisateurConnecte;  // ✅ Fixed: lowercase -> uppercase
    private CommentaireService commentaireService = new CommentaireService();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.publication = DashboardController.getSelectedPublication();  // ✅ Fixed: DashboardController
        this.utilisateurConnecte = Session.getUtilisateur();  // ✅ Fixed: method name

        if (publication == null) {
            DashboardController.loadViewStatic("/views/common/Publications.fxml");  // ✅ Fixed
            return;
        }

        afficherPublication();
        chargerCommentaires();

        retourBtn.setOnAction(e -> DashboardController.loadViewStatic("/views/common/Publications.fxml"));  // ✅ Fixed
        publierBtn.setOnAction(e -> publierCommentaire());
    }

    private void afficherPublication() {
        titreLabel.setText(publication.getTitre());
        // ✅ Fixed: getDatePublication() -> getDateCreation()
        dateLabel.setText("Publié le " + publication.getDateCreation().format(dateFormatter));
        descriptionLabel.setText(publication.getDescription());

        if (publication.getImage() != null && !publication.getImage().isEmpty()) {
            try {
                Image img = new Image(getClass().getResource(publication.getImage()).toExternalForm());
                imagePublication.setImage(img);
            } catch (Exception e) {
                System.out.println("Image non trouvée");
            }
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
        card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 8;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nomLabel = new Label(c.getNomUtilisateur());
        nomLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label dateLabel = new Label(c.getDateCreation().format(dateFormatter));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
        if (c.isModifie()) {
            dateLabel.setText(dateLabel.getText() + " (modifié)");
        }

        header.getChildren().addAll(nomLabel, dateLabel);

        Label contenuLabel = new Label(c.getContenu());
        contenuLabel.setWrapText(true);
        contenuLabel.setStyle("-fx-text-fill: #555;");

        card.getChildren().addAll(header, contenuLabel);

        if (utilisateurConnecte != null &&
                (utilisateurConnecte.getId() == c.getUtilisateurId() || utilisateurConnecte.isAdmin())) {

            HBox actions = new HBox(10);
            actions.setAlignment(Pos.CENTER_RIGHT);

            if (utilisateurConnecte.getId() == c.getUtilisateurId()) {
                Button modifBtn = new Button("Modifier");
                modifBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f39c12; -fx-cursor: hand;");
                modifBtn.setOnAction(e -> modifierCommentaire(c, card, contenuLabel));
                actions.getChildren().add(modifBtn);
            }

            Button supprBtn = new Button("Supprimer");
            supprBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand;");
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
        c.setContenu(contenu);

        commentaireService.ajouter(c);
        nouveauCommentaireArea.clear();
        chargerCommentaires();
    }

    private void modifierCommentaire(Commentaire c, VBox card, Label contenuLabel) {
        TextArea editArea = new TextArea(c.getContenu());
        editArea.setWrapText(true);
        editArea.setPrefRowCount(2);

        Button saveBtn = new Button("Enregistrer");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> {
            String nouveauContenu = editArea.getText().trim();
            if (nouveauContenu.isEmpty() || nouveauContenu.length() < 2) {
                showAlert("Erreur", "Commentaire invalide !");
                return;
            }
            c.setContenu(nouveauContenu);
            commentaireService.modifier(c);
            chargerCommentaires();
        });

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setOnAction(e -> chargerCommentaires());

        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        card.getChildren().remove(contenuLabel);
        card.getChildren().add(1, editArea);
        card.getChildren().add(buttons);
    }

    private void supprimerCommentaire(Commentaire c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Supprimer ce commentaire ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                commentaireService.supprimer(c.getId());
                chargerCommentaires();
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