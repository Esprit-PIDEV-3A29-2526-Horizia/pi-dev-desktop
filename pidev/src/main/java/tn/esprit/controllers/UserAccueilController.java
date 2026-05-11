package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Publication;
import tn.esprit.entities.User;
import tn.esprit.services.PublicationService;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;
import tn.esprit.utils.SelectedItem;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserAccueilController implements Initializable {

    // Navbar partagée
    @FXML private NavbarController navbarController;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button searchBtn;
    @FXML private FlowPane itemsGrid;

    // Filtres catégories
    @FXML private Button btnTous;
    @FXML private Button btnPlage;
    @FXML private Button btnMontagne;
    @FXML private Button btnVille;
    @FXML private Button btnDesert;
    @FXML private Button btnCampagne;

    private PublicationService publicationService = new PublicationService();
    private List<Publication> allPublications;
    private User currentUser;
    private String currentCategorie = "Tous";
    private Button activeFilterBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Récupérer l'utilisateur connecté
        currentUser = SessionManager.getCurrentUser();

        // Mettre à jour la navbar
        if (navbarController != null) {
            navbarController.updateUserInfo();
        }

        System.out.println("🔍 UserAccueilController - Utilisateur: " +
                (currentUser != null ? currentUser.getEmail() : "null"));

        // Charger les données
        publicationService = new PublicationService();
        allPublications = publicationService.getAll();

        // Configurer le tri
        if (sortCombo != null) {
            sortCombo.getItems().addAll("Date récente", "Date ancienne", "Titre A-Z", "Plus aimés");
            sortCombo.setValue("Date récente");
            sortCombo.setOnAction(e -> filtrer());
        }

        // Configurer les filtres de catégorie
        setupCategoryFilters();

        // Configurer la recherche
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> filtrer());
        }

        if (searchBtn != null) {
            searchBtn.setOnAction(e -> filtrer());
        }

        // Afficher toutes les publications
        afficherPublications(allPublications);
        if (navbarController != null) {
            navbarController.setActivePublications();
        }
    }

    private void setupCategoryFilters() {
        if (btnTous != null) {
            btnTous.setOnAction(e -> {
                currentCategorie = "Tous";
                updateChipActive(btnTous);
                filtrer();
            });
        }

        if (btnPlage != null) {
            btnPlage.setOnAction(e -> {
                currentCategorie = "Plage";
                updateChipActive(btnPlage);
                filtrer();
            });
        }

        if (btnMontagne != null) {
            btnMontagne.setOnAction(e -> {
                currentCategorie = "Montagne";
                updateChipActive(btnMontagne);
                filtrer();
            });
        }

        if (btnVille != null) {
            btnVille.setOnAction(e -> {
                currentCategorie = "Ville";
                updateChipActive(btnVille);
                filtrer();
            });
        }

        if (btnDesert != null) {
            btnDesert.setOnAction(e -> {
                currentCategorie = "Désert";
                updateChipActive(btnDesert);
                filtrer();
            });
        }

        if (btnCampagne != null) {
            btnCampagne.setOnAction(e -> {
                currentCategorie = "Campagne";
                updateChipActive(btnCampagne);
                filtrer();
            });
        }
    }

    private void updateChipActive(Button active) {
        Button[] all = {btnTous, btnPlage, btnMontagne, btnVille, btnDesert, btnCampagne};
        for (Button b : all) {
            if (b == null) continue;
            if (b == active) {
                b.setStyle("-fx-background-color: #E8B156; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 18; -fx-padding: 6 16; -fx-cursor: hand;");
            } else {
                b.setStyle("-fx-background-color: #DACEB6; -fx-text-fill: #1F2937; -fx-font-weight: bold; " +
                        "-fx-background-radius: 18; -fx-padding: 6 16; -fx-cursor: hand;");
            }
        }
        activeFilterBtn = active;
    }

    private void filtrer() {
        String recherche = searchField != null ? searchField.getText().toLowerCase().trim() : "";

        List<Publication> resultats = allPublications;

        // Filtre par catégorie
        if (!"Tous".equals(currentCategorie) && allPublications != null) {
            resultats = resultats.stream()
                    .filter(p -> p.getCategorie() != null &&
                            p.getCategorie().getLabel().equals(currentCategorie))
                    .collect(Collectors.toList());
        }

        // Filtre par recherche
        if (!recherche.isEmpty() && resultats != null) {
            resultats = resultats.stream()
                    .filter(p -> (p.getTitre() != null && p.getTitre().toLowerCase().contains(recherche)) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(recherche)) ||
                            (p.getAuteur() != null && p.getAuteur().toLowerCase().contains(recherche)))
                    .collect(Collectors.toList());
        }

        // Tri
        if (sortCombo != null && sortCombo.getValue() != null && resultats != null) {
            String tri = sortCombo.getValue();
            switch (tri) {
                case "Titre A-Z":
                    resultats.sort((a, b) -> a.getTitre().compareTo(b.getTitre()));
                    break;
                case "Date récente":
                    resultats.sort((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()));
                    break;
                case "Date ancienne":
                    resultats.sort((a, b) -> a.getDateCreation().compareTo(b.getDateCreation()));
                    break;
                case "Plus aimés":
                    resultats.sort((a, b) -> Integer.compare(b.getLikes(), a.getLikes()));
                    break;
            }
        }

        afficherPublications(resultats);
    }

    private void afficherPublications(List<Publication> publications) {
        if (itemsGrid == null) return;

        itemsGrid.getChildren().clear();

        if (publications == null || publications.isEmpty()) {
            Label emptyLabel = new Label("Aucune publication trouvée");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16px; -fx-padding: 50;");
            itemsGrid.getChildren().add(emptyLabel);
            return;
        }

        for (Publication p : publications) {
            VBox card = createCard(p);
            itemsGrid.getChildren().add(card);
        }
    }

    private VBox createCard(Publication p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(250);
        card.setMaxWidth(250);

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
                // Image par défaut
            }
        }

        // Titre
        Label titre = new Label(p.getTitre());
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1A3C5A;");
        titre.setWrapText(true);

        // Catégorie
        String categorieLabel = p.getCategorie() != null ? p.getCategorie().getLabel() : "Non catégorisé";
        Label categorie = new Label(categorieLabel);
        categorie.setStyle("-fx-text-fill: #3b82f6; -fx-background-color: #EFF6FF; -fx-background-radius: 15; -fx-padding: 4 12;");

        // Auteur
        Label auteur = new Label("✍️ " + (p.getAuteur() != null ? p.getAuteur() : "Anonyme"));
        auteur.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        // Date
        Label date = new Label("📅 " + (p.getDateCreation() != null ? p.getDateCreation().toLocalDate().toString() : "Date inconnue"));
        date.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        // Likes
        Label likes = new Label("❤️ " + p.getLikes());
        likes.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        // Bouton Voir détails
        Button details = new Button("Voir détails");
        details.setStyle("-fx-background-color: #E8B156; -fx-text-fill: black; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 8 20; -fx-font-weight: bold;");
        details.setMaxWidth(Double.MAX_VALUE);
        details.setOnAction(e -> {
            SelectedItem.setCurrentPublication(p);
            NavigationManager.loadView("/fxml/UserExplorer.fxml", "Détails");
        });

        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        bottomBox.getChildren().addAll(likes, details);

        card.getChildren().addAll(imageView, titre, categorie, auteur, date, bottomBox);
        return card;
    }
}