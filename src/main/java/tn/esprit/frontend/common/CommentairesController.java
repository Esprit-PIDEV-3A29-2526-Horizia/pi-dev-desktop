package tn.esprit.frontend.common;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Commentaire;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.CommentaireService;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.backend.utils.Session;
import tn.esprit.frontend.admin.AdminDashboardController;
import tn.esprit.frontend.components.CommentaireCardController;
import tn.esprit.frontend.user.UserMainController;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CommentairesController implements Initializable {

    @FXML private Label publicationInfoLabel;
    @FXML private TextField auteurField;
    @FXML private TextArea contenuField;
    @FXML private Button ajouterBtn;
    @FXML private VBox commentairesContainer;
    @FXML private Button retourBtn;

    private CommentaireService commentaireService = new CommentaireService();
    private Publication publication;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        publication = SelectedItem.getCurrentPublication();
        if (publication == null) {
            retour();
            return;
        }
        publicationInfoLabel.setText("Publication: " + publication.getTitre());

        if (Session.estConnecte()) {
            auteurField.setText(Session.getUtilisateur().getNomComplet());
            auteurField.setEditable(false);
        }

        ajouterBtn.setOnAction(e -> ajouterCommentaire());
        retourBtn.setOnAction(e -> retour());

        loadCommentaires();
    }

    private void loadCommentaires() {
        commentairesContainer.getChildren().clear();
        List<Commentaire> commentaires = commentaireService.getByPublication(publication.getId());

        for (Commentaire c : commentaires) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/CommentaireCard.fxml"));
                VBox card = loader.load();
                CommentaireCardController controller = loader.getController();
                controller.setCommentaire(c);
                controller.setParentController(this);
                commentairesContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void ajouterCommentaire() {
        if (!Session.estConnecte()) {
            showAlert("Connexion requise", "Vous devez être connecté pour commenter.");
            return;
        }
        String contenu = contenuField.getText().trim();
        if (contenu.isEmpty()) {
            showAlert("Erreur", "Le commentaire ne peut pas être vide.");
            return;
        }

        Commentaire c = new Commentaire();
        c.setPublicationId(publication.getId());
        c.setUtilisateurId(Session.getUtilisateur().getId());
        c.setAuteur(Session.getUtilisateur().getNomComplet());
        c.setContenu(contenu);
        commentaireService.ajouter(c);
        contenuField.clear();
        loadCommentaires();
    }

    public void modifierCommentaire(Commentaire commentaire) {
        if (!peutModifierOuSupprimer(commentaire)) return;

        TextInputDialog dialog = new TextInputDialog(commentaire.getContenu());
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText(null);
        dialog.setContentText("Nouveau contenu:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            commentaire.setContenu(result.get());
            commentaireService.modifier(commentaire);
            loadCommentaires();
        }
    }

    public void supprimerCommentaire(Commentaire commentaire) {
        if (!peutModifierOuSupprimer(commentaire)) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Supprimer ce commentaire ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            commentaireService.supprimer(commentaire.getId());
            loadCommentaires();
        }
    }

    private boolean peutModifierOuSupprimer(Commentaire c) {
        return Session.estConnecte() &&
                (Session.getUtilisateur().getId() == c.getUtilisateurId() || Session.estAdmin());
    }

    private void retour() {
        if (UserMainController.getInstance() != null) {
            UserMainController.getInstance().loadView("/views/user/UserExplorer.fxml");
        } else if (AdminDashboardController.getInstance() != null) {
            AdminDashboardController.getInstance().loadView("/views/admin/GestionPublications.fxml");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}