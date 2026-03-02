package tn.esprit.frontend.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.CommentaireService;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.backend.utils.Session;
import tn.esprit.frontend.components.PublicationCardController;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserPublicationsController implements Initializable {

    @FXML private Label totalLabel;
    @FXML private Label likesLabel;
    @FXML private Label commentsLabel;
    @FXML private FlowPane itemsGrid;
    @FXML private Button btnAjouter;

    private PublicationService publicationService = new PublicationService();
    private CommentaireService commentaireService = new CommentaireService();
    private List<Publication> mesPublications;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.estConnecte()) return;

        chargerPublications();

        btnAjouter.setOnAction(e -> {
            // Charger le formulaire d'ajout dans le contentPane utilisateur
            UserMainController.getInstance().loadView("/views/common/AjouterPublication.fxml");
        });
    }

    private void chargerPublications() {
        int userId = Session.getUtilisateur().getId();
        mesPublications = publicationService.getAll().stream()
                .filter(p -> p.getUtilisateurId() == userId)
                .toList();

        totalLabel.setText(String.valueOf(mesPublications.size()));
        int totalLikes = mesPublications.stream().mapToInt(Publication::getLikes).sum();
        likesLabel.setText(String.valueOf(totalLikes));
        int totalComments = mesPublications.stream()
                .mapToInt(p -> commentaireService.getByPublication(p.getId()).size())
                .sum();
        commentsLabel.setText(String.valueOf(totalComments));

        afficherPublications(mesPublications);
    }

    private void afficherPublications(List<Publication> publications) {
        itemsGrid.getChildren().clear();
        for (Publication p : publications) {
            try {
                VBox card = createCard(p);
                itemsGrid.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private VBox createCard(Publication p) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/PublicationCard.fxml"));
        VBox card = loader.load();

        PublicationCardController controller = loader.getController();
        controller.setPublication(p);
        controller.setAdminMode(true); // Mode édition pour ses propres publications

        controller.setOnEditCallback(publication -> {
            SelectedItem.setCurrentPublication(publication);
            // Rediriger vers le formulaire de modification (commun)
            UserMainController.getInstance().loadView("/views/common/ModifierPublication.fxml");
        });

        controller.setOnDeleteCallback(publication -> {
            supprimerPublication(publication);
        });

        return card;
    }

    private void supprimerPublication(Publication p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Supprimer \"" + p.getTitre() + "\" ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                publicationService.supprimer(p.getId());
                chargerPublications();
                showAlert("Succès", "Publication supprimée !");
            } catch (Exception e) {
                showAlert("Erreur", e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(
                title.equals("Succès") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}