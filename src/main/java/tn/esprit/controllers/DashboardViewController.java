package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import tn.esprit.entities.Publication;
import tn.esprit.services.PublicationService;
import tn.esprit.services.CommentaireService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardViewController implements Initializable {

    @FXML private Label totalPublicationsLabel;
    @FXML private Label totalCommentairesLabel;
    @FXML private Label publicationsAujourdhuiLabel;
    @FXML private Label topPublicationLabel;

    private PublicationService publicationService = new PublicationService();
    private CommentaireService commentaireService = new CommentaireService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerStatistiques();
    }

    // ============================================
    // NOUVELLE MÉTHODE : Rafraîchir les stats
    // ============================================

    public void refreshStats() {
        System.out.println("🔄 Rafraîchissement des statistiques...");
        chargerStatistiques();
    }

    // ============================================

    private void chargerStatistiques() {
        // Récupérer toutes les publications
        List<Publication> publications = publicationService.getAll();

        // Total publications
        int totalPubs = publications.size();
        if (totalPublicationsLabel != null) {
            totalPublicationsLabel.setText(String.valueOf(totalPubs));
        }

        // Publication avec le plus de likes
        if (topPublicationLabel != null) {
            if (!publications.isEmpty()) {
                Publication topPub = publications.stream()
                        .max((p1, p2) -> Integer.compare(p1.getLikes(), p2.getLikes()))
                        .orElse(null);

                if (topPub != null) {
                    topPublicationLabel.setText(topPub.getTitreAbrege(30) + " (" + topPub.getLikes() + " ❤️)");
                }
            } else {
                topPublicationLabel.setText("Aucune publication");
            }
        }

        // Total commentaires
        int totalComms = 0;
        for (Publication p : publications) {
            totalComms += commentaireService.getByPublication(p.getId()).size();
        }
        if (totalCommentairesLabel != null) {
            totalCommentairesLabel.setText(String.valueOf(totalComms));
        }

        // Publications aujourd'hui
        long aujourdhui = publications.stream()
                .filter(p -> p.getDatePublication().toLocalDate().equals(java.time.LocalDate.now()))
                .count();
        if (publicationsAujourdhuiLabel != null) {
            publicationsAujourdhuiLabel.setText(String.valueOf(aujourdhui));
        }
    }
}