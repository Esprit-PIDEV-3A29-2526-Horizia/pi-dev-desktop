package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import tn.esprit.entities.Commentaire;

public class CommentaireCardController {

    @FXML private Label auteurLabel;
    @FXML private Label contenuLabel;
    @FXML private Label dateLabel;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;

    private Commentaire commentaire;
    private CommentairesController parentController;

    @FXML
    public void initialize() {
        // Les actions sont définies dans setCommentaire pour avoir accès aux données
    }

    public void setCommentaire(Commentaire commentaire) {
        this.commentaire = commentaire;
        auteurLabel.setText(commentaire.getAuteur());
        contenuLabel.setText(commentaire.getContenu());
        dateLabel.setText(commentaire.getDateCreation().toString());

        // Attacher les événements
        modifierBtn.setOnAction(event -> parentController.modifierCommentaire(commentaire));
        supprimerBtn.setOnAction(event -> parentController.supprimerCommentaire(commentaire));
    }

    public void setParentController(CommentairesController parentController) {
        this.parentController = parentController;
    }
}