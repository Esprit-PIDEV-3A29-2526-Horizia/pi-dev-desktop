package tn.esprit.frontend.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Commentaire;
import tn.esprit.backend.services.CommentaireService;
import tn.esprit.backend.utils.Session;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminCommentairesController implements Initializable {

    @FXML private FlowPane commentairesContainer;
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Button refreshBtn;

    private CommentaireService commentaireService = new CommentaireService();
    private List<Commentaire> tousLesCommentaires;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.estAdmin()) return;

        chargerCommentaires();

        searchBtn.setOnAction(e -> rechercher());
        refreshBtn.setOnAction(e -> chargerCommentaires());
        searchField.setOnAction(e -> rechercher());
    }

    private void chargerCommentaires() {
        tousLesCommentaires = commentaireService.getAll();
        afficherCommentaires(tousLesCommentaires);
        searchField.clear();
    }

    private void rechercher() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            afficherCommentaires(tousLesCommentaires);
            return;
        }
        List<Commentaire> resultats = commentaireService.rechercher(keyword);
        afficherCommentaires(resultats);
    }

    private void afficherCommentaires(List<Commentaire> commentaires) {
        commentairesContainer.getChildren().clear();
        for (Commentaire c : commentaires) {
            try {
                VBox card = createCommentaireCard(c);
                commentairesContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private VBox createCommentaireCard(Commentaire c) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/CommentaireAdminCard.fxml"));
        VBox card = loader.load();

        CommentaireAdminCardController controller = loader.getController();
        controller.setCommentaire(c);
        controller.setOnDeleteCallback(() -> {
            commentaireService.supprimer(c.getId());
            chargerCommentaires();
        });

        return card;
    }
}