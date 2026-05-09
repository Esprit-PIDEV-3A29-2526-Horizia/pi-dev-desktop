package tn.esprit.frontend.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import tn.esprit.backend.entities.Commentaire;
import tn.esprit.backend.utils.Session;
import tn.esprit.frontend.common.CommentairesController;

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

        // Formater la date pour éviter les problèmes d'affichage
        if (commentaire.getDateCreation() != null) {
            dateLabel.setText(commentaire.getDateCreation().toLocalDate().toString());
        } else {
            dateLabel.setText("");
        }
    }

    public void setParentController(CommentairesController controller) {
        this.parentController = controller;
        boolean peutAgir = Session.estConnecte() &&
                (Session.getUtilisateur().getId() == commentaire.getUtilisateurId() || Session.estAdmin());

        modifierBtn.setVisible(peutAgir);
        modifierBtn.setManaged(peutAgir);
        supprimerBtn.setVisible(peutAgir);
        supprimerBtn.setManaged(peutAgir);

        // ✅ Ajouter des largeurs minimales pour éviter les "..."
        if (modifierBtn != null) {
            modifierBtn.setMinWidth(70);
            modifierBtn.setPrefWidth(70);
        }
        if (supprimerBtn != null) {
            supprimerBtn.setMinWidth(70);
            supprimerBtn.setPrefWidth(70);
        }

        modifierBtn.setOnAction(e -> parentController.modifierCommentaire(commentaire));
        supprimerBtn.setOnAction(e -> parentController.supprimerCommentaire(commentaire));
    }
}