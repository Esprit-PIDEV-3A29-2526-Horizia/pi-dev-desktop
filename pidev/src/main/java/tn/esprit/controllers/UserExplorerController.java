package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Categorie;
import tn.esprit.entities.Publication;
import tn.esprit.entities.User;
import tn.esprit.services.FavorisService;
import tn.esprit.services.PublicationService;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SelectedItem;
import tn.esprit.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserExplorerController implements Initializable {

    @FXML private NavbarController navbarController;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private FlowPane itemsGrid;
    @FXML private Button btnTous, btnPlage, btnMontagne, btnVille, btnDesert, btnCampagne;

    private PublicationService publicationService = new PublicationService();
    private FavorisService favorisService = new FavorisService();
    private List<Publication> allPublications;
    private Categorie currentCategorie = Categorie.TOUS;
    private Button activeFilterBtn;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUser = SessionManager.getCurrentUser();

        if (navbarController != null) {
            navbarController.updateUserInfo();
            navbarController.setActivePublications();
        }

        System.out.println("🔍 UserExplorerController - Utilisateur: " +
                (currentUser != null ? currentUser.getEmail() : "null"));

        allPublications = publicationService.getAll();

        sortCombo.getItems().addAll("Plus récents", "Plus anciens", "Plus aimés", "A-Z");
        sortCombo.setValue("Plus récents");
        sortCombo.setOnAction(e -> appliquerFiltres());

        searchField.textProperty().addListener((obs, old, val) -> appliquerFiltres());

        btnTous.setOnAction(e -> setFilter(btnTous, Categorie.TOUS));
        btnPlage.setOnAction(e -> setFilter(btnPlage, Categorie.PLAGE));
        btnMontagne.setOnAction(e -> setFilter(btnMontagne, Categorie.MONTAGNE));
        btnVille.setOnAction(e -> setFilter(btnVille, Categorie.VILLE));
        btnDesert.setOnAction(e -> setFilter(btnDesert, Categorie.DESERT));
        btnCampagne.setOnAction(e -> setFilter(btnCampagne, Categorie.CAMPAGNE));

        activeFilterBtn = btnTous;
        setFilter(btnTous, Categorie.TOUS);
    }

    private void appliquerFiltres() {
        if (allPublications == null) return;

        List<Publication> filtered = allPublications;

        if (currentCategorie != Categorie.TOUS) {
            filtered = filtered.stream()
                    .filter(p -> p.getCategorie() == currentCategorie)
                    .collect(Collectors.toList());
        }

        String search = searchField.getText().toLowerCase().trim();
        if (!search.isEmpty()) {
            filtered = filtered.stream()
                    .filter(p -> (p.getTitre() != null && p.getTitre().toLowerCase().contains(search)) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(search)) ||
                            (p.getAuteur() != null && p.getAuteur().toLowerCase().contains(search)))
                    .collect(Collectors.toList());
        }

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
                    break;
            }
        }

        afficherPublications(filtered);
    }

    private void afficherPublications(List<Publication> publications) {
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
        card.setPrefWidth(280);
        card.setMaxWidth(280);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(250);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);

        if (p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                String path = p.getImage().startsWith("/") ? p.getImage() : "/" + p.getImage();
                Image img = new Image(getClass().getResourceAsStream(path));
                imageView.setImage(img);
            } catch (Exception e) {
                setDefaultImage(imageView);
            }
        } else {
            setDefaultImage(imageView);
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
        auteur.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px");

        // Date
        Label date = new Label("📅 " + (p.getDateCreation() != null ? p.getDateCreation().toLocalDate().toString() : "Date inconnue"));
        date.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px");

        // Vérifier si la publication est en favori
        boolean estFavori = false;
        if (currentUser != null) {
            try {
                estFavori = favorisService.estFavori(currentUser.getId(), p.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        boolean finalEstFavori = estFavori;

        // Bouton Favori (étoile)
        Button favoriBtn = new Button(finalEstFavori ? "⭐" : "☆");
        favoriBtn.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #f59e0b; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 12; -fx-font-size: 14px; -fx-font-weight: bold;");
        favoriBtn.setOnAction(e -> {
            if (currentUser == null) {
                showAlert("Connexion requise", "Veuillez vous connecter pour ajouter aux favoris");
                return;
            }
            try {
                if (finalEstFavori) {
                    favorisService.supprimerFavori(currentUser.getId(), p.getId());
                    favoriBtn.setText("☆");
                    showAlert("Succès", "Publication retirée des favoris");
                } else {
                    favorisService.ajouterFavori(currentUser.getId(), p.getId());
                    favoriBtn.setText("⭐");
                    showAlert("Succès", "Publication ajoutée aux favoris");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Erreur", "Impossible de modifier les favoris");
            }
        });

        // Bouton Commentaires
        Button commentBtn = new Button("💬 Commentaires");
        commentBtn.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #3b82f6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 12; -fx-font-size: 12px;");
        commentBtn.setOnAction(e -> {
            SelectedItem.setCurrentPublication(p);
            NavigationManager.loadView("/fxml/Commentaires.fxml", "Commentaires");
        });

        // Bouton Like
        Button likeBtn = new Button("❤️ " + p.getLikes());
        likeBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 12; -fx-font-size: 12px;");
        likeBtn.setOnAction(e -> {
            if (currentUser == null) {
                showAlert("Connexion requise", "Veuillez vous connecter pour aimer");
                return;
            }
            try {
                publicationService.incrementerLikes(p.getId());
                p.setLikes(p.getLikes() + 1);
                likeBtn.setText("❤️ " + p.getLikes());
                showAlert("Succès", "Vous avez aimé cette publication");
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Erreur", "Impossible d'aimer cette publication");
            }
        });

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        actionBox.getChildren().addAll(commentBtn, likeBtn, favoriBtn);

        // Label likes séparé (optionnel)
        Label likesLabel = new Label("❤️ " + p.getLikes() + " likes");
        likesLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px");

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(categorie, auteur, date);

        card.getChildren().addAll(imageView, titre, infoBox, actionBox);
        return card;
    }

    private void setDefaultImage(ImageView imageView) {
        try {
            Image defaultImg = new Image(getClass().getResourceAsStream("/images/default.jpg"));
            imageView.setImage(defaultImg);
        } catch (Exception e) {
            // Pas d'image par défaut
        }
    }

    private void setFilter(Button btn, Categorie cat) {
        if (activeFilterBtn != null) {
            activeFilterBtn.setStyle("-fx-background-color: #DACEB6; -fx-text-fill: #1F2937; -fx-font-weight: bold; " +
                    "-fx-background-radius: 18; -fx-padding: 6 16; -fx-cursor: hand;");
        }
        btn.setStyle("-fx-background-color: #E8B156; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 18; -fx-padding: 6 16; -fx-cursor: hand;");
        activeFilterBtn = btn;
        currentCategorie = cat;
        appliquerFiltres();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}