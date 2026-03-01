package tn.esprit.frontend.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.CommentaireService;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.services.UtilisateurService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardContentController implements Initializable {

    @FXML private Label totalPublicationsLabel;
    @FXML private Label totalUtilisateursLabel;
    @FXML private Label totalCommentairesLabel;
    @FXML private Label totalLikesLabel;

    private PublicationService publicationService = new PublicationService();
    private UtilisateurService utilisateurService = new UtilisateurService();
    private CommentaireService commentaireService = new CommentaireService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerStatistiques();
    }

    private void chargerStatistiques() {
        List<Publication> publications = publicationService.getAll();

        totalPublicationsLabel.setText(String.valueOf(publications.size()));

        int totalUsers = utilisateurService.getAll().size();
        totalUtilisateursLabel.setText(String.valueOf(totalUsers));

        int totalComments = publications.stream()
                .mapToInt(p -> commentaireService.getByPublication(p.getId()).size())
                .sum();
        totalCommentairesLabel.setText(String.valueOf(totalComments));

        int totalLikes = publications.stream().mapToInt(Publication::getLikes).sum();
        totalLikesLabel.setText(String.valueOf(totalLikes));
    }
}