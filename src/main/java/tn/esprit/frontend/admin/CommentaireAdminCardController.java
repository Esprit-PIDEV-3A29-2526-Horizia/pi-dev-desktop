package tn.esprit.frontend.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import tn.esprit.backend.entities.Commentaire;
import tn.esprit.backend.services.PublicationService;

public class CommentaireAdminCardController {

    @FXML private Label auteurLabel;
    @FXML private Label dateLabel;
    @FXML private Label contenuLabel;
    @FXML private Label publicationLabel;
    @FXML private Button supprimerBtn;

    private Commentaire commentaire;
    private Runnable onDeleteCallback;
    private PublicationService publicationService = new PublicationService();

    public void setCommentaire(Commentaire c) {
        this.commentaire = c;
        auteurLabel.setText(c.getAuteur() != null ? c.getAuteur() : "Anonyme");
        dateLabel.setText(c.getDateCreation().toLocalDate().toString());
        contenuLabel.setText(c.getContenu());

        try {
            var pub = publicationService.getById(c.getPublicationId());
            publicationLabel.setText("📄 Publication: " + (pub != null ? pub.getTitre() : "ID: " + c.getPublicationId()));
        } catch (Exception e) {
            publicationLabel.setText("📄 Publication ID: " + c.getPublicationId());
        }
    }

    public void setOnDeleteCallback(Runnable callback) {
        this.onDeleteCallback = callback;
        supprimerBtn.setOnAction(e -> onDeleteCallback.run());
    }
}