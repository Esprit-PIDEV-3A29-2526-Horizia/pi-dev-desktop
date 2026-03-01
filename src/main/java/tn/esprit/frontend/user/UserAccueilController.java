package tn.esprit.frontend.user;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.utils.SelectedItem;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserAccueilController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private FlowPane itemsGrid;

    private PublicationService publicationService = new PublicationService();
    private List<Publication> allPublications;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        allPublications = publicationService.getAll();
        afficherPublications(allPublications);

        searchField.textProperty().addListener((obs, old, val) -> filtrer());
        searchBtn.setOnAction(e -> filtrer());
    }

    private void filtrer() {
        String recherche = searchField.getText().toLowerCase().trim();
        if (recherche.isEmpty()) {
            afficherPublications(allPublications);
            return;
        }
        List<Publication> filtrees = allPublications.stream()
                .filter(p -> p.getTitre().toLowerCase().contains(recherche) ||
                        p.getDescription().toLowerCase().contains(recherche))
                .collect(Collectors.toList());
        afficherPublications(filtrees);
    }

    private void afficherPublications(List<Publication> publications) {
        itemsGrid.getChildren().clear();
        for (Publication p : publications) {
            VBox card = createCard(p);
            itemsGrid.getChildren().add(card);
        }
    }

    private VBox createCard(Publication p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(250);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                String path = p.getImage().startsWith("/") ? p.getImage() : "/" + p.getImage();
                Image img = new Image(getClass().getResourceAsStream(path));
                imageView.setImage(img);
            } catch (Exception e) {
                // Image par défaut (optionnel)
            }
        }

        // Titre
        Label titre = new Label(p.getTitre());
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Catégorie
        Label categorie = new Label(p.getCategorie().getLabel());
        categorie.setStyle("-fx-text-fill: #3b82f6;");

        // Auteur
        Label auteur = new Label("Par " + (p.getAuteur() != null ? p.getAuteur() : "Anonyme"));
        auteur.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        // Bouton Voir détails
        Button details = new Button("Voir détails");
        details.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 8 20;");
        details.setOnAction(e -> {
            SelectedItem.setCurrentPublication(p);
            UserMainController.getInstance().showExplorer(); // ou une vue de détail
        });

        card.getChildren().addAll(imageView, titre, categorie, auteur, details);
        return card;
    }
}