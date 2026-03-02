package tn.esprit.frontend.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import tn.esprit.backend.entities.Commentaire;
import tn.esprit.entities.User;
import tn.esprit.frontend.common.CommentairesController;
import tn.esprit.utils.SessionManager;

public class CommentaireCardController {

    @FXML private Label auteurLabel;
    @FXML private Label contenuLabel;
    @FXML private Label dateLabel;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;

    private Commentaire commentaire;
    private CommentairesController parentController;

    @FXML
    public void initialize() { }

    public void setCommentaire(Commentaire commentaire) {
        this.commentaire = commentaire;
        auteurLabel.setText(commentaire.getAuteur());
        contenuLabel.setText(commentaire.getContenu());
        dateLabel.setText(commentaire.getDateCreation().toString());
    }

    public void setParentController(CommentairesController controller) {
        this.parentController = controller;

        boolean peutAgir = false;
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            peutAgir = currentUser.getId() == commentaire.getUtilisateurId() || SessionManager.isAdmin();
        }

        modifierBtn.setVisible(peutAgir);
        supprimerBtn.setVisible(peutAgir);

        // Définir les actions seulement si nécessaire (optionnel)
        if (peutAgir) {
            modifierBtn.setOnAction(e -> parentController.modifierCommentaire(commentaire));
            supprimerBtn.setOnAction(e -> parentController.supprimerCommentaire(commentaire));
        }
    }
}