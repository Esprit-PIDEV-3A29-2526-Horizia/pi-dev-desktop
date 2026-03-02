package tn.esprit.frontend.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Categorie;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.CommentaireService;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.services.UtilisateurService;
import tn.esprit.backend.utils.Session;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardStyleController implements Initializable {

    @FXML private Label totalPubsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private FlowPane itemsGrid;
    @FXML private Button searchBtn;
    @FXML private Button btnTous, btnPlage, btnMontagne, btnVille;

    private PublicationService publicationService = new PublicationService();
    private UtilisateurService utilisateurService = new UtilisateurService();
    private CommentaireService commentaireService = new CommentaireService();
    private List<Publication> allPublications;
    private Categorie currentCategorie = Categorie.TOUS;
    private Button activeFilterBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.estAdmin()) {
            // Redirection ou message
            return;
        }

        // Charger les données
        allPublications = publicationService.getAll();

        // Statistiques
        totalPubsLabel.setText(String.valueOf(allPublications.size()));
        totalUsersLabel.setText(String.valueOf(utilisateurService.getAll().size()));
        int totalComments = allPublications.stream()
                .mapToInt(p -> commentaireService.getByPublication(p.getId()).size())
                .sum();
        totalCommentsLabel.setText(String.valueOf(totalComments));

        // Tri
        sortCombo.getItems().addAll("Plus récents", "Plus anciens", "Plus aimés", "A-Z");
        sortCombo.setValue("Plus récents");
        sortCombo.setOnAction(e -> appliquerFiltres());

        // Recherche
        searchField.textProperty().addListener((obs, old, val) -> appliquerFiltres());
        searchBtn.setOnAction(e -> appliquerFiltres());

        // Filtres
        btnTous.setOnAction(e -> setFilter(btnTous, Categorie.TOUS));
        btnPlage.setOnAction(e -> setFilter(btnPlage, Categorie.PLAGE));
        btnMontagne.setOnAction(e -> setFilter(btnMontagne, Categorie.MONTAGNE));
        btnVille.setOnAction(e -> setFilter(btnVille, Categorie.VILLE));

        activeFilterBtn = btnTous;
        setFilter(btnTous, Categorie.TOUS);
    }

    private void appliquerFiltres() {
        if (allPublications == null) return;

        List<Publication> filtered = allPublications;

        // Filtre catégorie
        if (currentCategorie != Categorie.TOUS) {
            filtered = filtered.stream()
                    .filter(p -> p.getCategorie() == currentCategorie)
                    .collect(Collectors.toList());
        }

        // Recherche
        String search = searchField.getText().toLowerCase().trim();
        if (!search.isEmpty()) {
            filtered = filtered.stream()
                    .filter(p -> p.getTitre().toLowerCase().contains(search) ||
                            p.getDescription().toLowerCase().contains(search))
                    .collect(Collectors.toList());
        }

        // Tri
        String sortType = sortCombo.getValue();
        if (sortType != null) {
            switch (sortType) {
                case "Plus anciens":
                    filtered.sort((p1, p2) -> p1.getDateCreation().compareTo(p2.getDateCreation()));
                    break;
                case "Plus aimés":
                    filtered.sort((p1, p2) -> Integer.compare(p2.getLikes(), p1.getLikes()));
                    break;
                case "A-Z":
                    filtered.sort((p1, p2) -> p1.getTitre().compareToIgnoreCase(p2.getTitre()));
                    break;
                default:
                    filtered.sort((p1, p2) -> p2.getDateCreation().compareTo(p1.getDateCreation()));
            }
        }

        afficherPublications(filtered);
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
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); -fx-border-color: #e2e8f0; -fx-border-radius: 15;");
        card.setPrefWidth(280);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(250);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                String path = p.getImage().startsWith("/") ? p.getImage() : "/" + p.getImage();
                Image img = new Image(getClass().getResourceAsStream(path));
                imageView.setImage(img);
            } catch (Exception e) {
                // ignorer
            }
        }

        Label titre = new Label(p.getTitre());
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titre.setWrapText(true);

        Label categorie = new Label(p.getCategorie().getLabel());
        categorie.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 14;");

        Label auteur = new Label("Par " + (p.getAuteur() != null ? p.getAuteur() : "Anonyme"));
        auteur.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13;");

        Label likes = new Label("❤️ " + p.getLikes());
        likes.setStyle("-fx-text-fill: #ef4444;");

        // Bouton Modifier/Supprimer (admin)
        Button modifier = new Button("Modifier");
        modifier.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 5 15; -fx-cursor: hand;");
        // Action à définir selon votre logique

        card.getChildren().addAll(imageView, titre, categorie, auteur, likes, modifier);
        return card;
    }

    private void setFilter(Button btn, Categorie cat) {
        if (activeFilterBtn != null) {
            activeFilterBtn.setStyle("-fx-background-color: white; -fx-text-fill: #334155; " +
                    "-fx-border-color: #e2e8f0; -fx-border-radius: 25; -fx-background-radius: 25;");
        }
        btn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-background-radius: 25; -fx-padding: 10 25;");
        activeFilterBtn = btn;
        currentCategorie = cat;
        appliquerFiltres();
    }
}