package tn.esprit.frontend.user;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.entities.User;
import tn.esprit.utils.SessionManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserAccueilController implements Initializable {

    // Composants de l'en-tête
    @FXML private Button btnAccueil;
    @FXML private Button btnNosLogements;
    @FXML private Button btnMesReservations;
    @FXML private Button btnEvenements;
    @FXML private Button btnLocation;
    @FXML private Button btnVoyager;
    @FXML private HBox userBox;
    @FXML private Label userNameLabel;
    @FXML private Button btnLogout;

    // Composants de la recherche
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button btnTous, btnPlage, btnMontagne, btnVille, btnDesert, btnCampagne;
    @FXML private FlowPane itemsGrid;

    private PublicationService publicationService = new PublicationService();
    private List<Publication> allPublications;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUser = SessionManager.getCurrentUser();
        setupNavBar();

        allPublications = publicationService.getAll();
        afficherPublications(allPublications);

        // Recherche
        searchField.textProperty().addListener((obs, old, val) -> filtrer());
        searchBtn.setOnAction(e -> filtrer());

        // Tri
        sortCombo.getItems().addAll("Plus récents", "Plus anciens", "Plus aimés");
        sortCombo.setValue("Plus récents");
        sortCombo.setOnAction(e -> appliquerTri());

        // Filtres par catégorie
        btnTous.setOnAction(e -> setFilter(btnTous, null));
        btnPlage.setOnAction(e -> setFilter(btnPlage, "PLAGE"));
        btnMontagne.setOnAction(e -> setFilter(btnMontagne, "MONTAGNE"));
        btnVille.setOnAction(e -> setFilter(btnVille, "VILLE"));
        btnDesert.setOnAction(e -> setFilter(btnDesert, "DESERT"));
        btnCampagne.setOnAction(e -> setFilter(btnCampagne, "CAMPAGNE"));
    }

    private void setupNavBar() {
        if (SessionManager.isLoggedIn() && currentUser != null) {
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        } else {
            userNameLabel.setText("Connexion");
        }

        // Actions des boutons de navigation (déléguées à UserMainController)
        btnAccueil.setOnAction(e -> retourAccueil());
        btnNosLogements.setOnAction(e -> afficherLogements());
        btnMesReservations.setOnAction(e -> voirMesReservations());
        btnEvenements.setOnAction(e -> afficherEvenements());
        btnLocation.setOnAction(e -> afficherLocation());
        btnVoyager.setOnAction(e -> afficherVoyager());

        // User box
        userBox.setOnMouseClicked(e -> showUserProfile());
        userBox.setOnMouseEntered(e -> onUserBoxHover());
        userBox.setOnMouseExited(e -> onUserBoxExit());

        // Déconnexion
        btnLogout.setOnAction(e -> handleLogout());
    }

    // ===== Méthodes de navigation =====
    @FXML private void retourAccueil() {
        UserMainController.getInstance().showAccueil();
    }

    @FXML private void afficherLogements() {
        UserMainController.getInstance().showExplorer();
    }

    @FXML private void voirMesReservations() {
        UserMainController.getInstance().loadView("/views/MesReservations.fxml");
    }

    @FXML private void afficherEvenements() {
        UserMainController.getInstance().loadView("/views/Evenements.fxml");
    }

    @FXML private void afficherLocation() {
        UserMainController.getInstance().loadView("/views/ReservationVoiure.fxml"); // ou "/client/AccueilClient.fxml"
    }

    @FXML private void afficherVoyager() {
        UserMainController.getInstance().loadView("/views/Voyager.fxml"); // ou "/fxml/CatalogueUser.fxml"
    }

    @FXML private void showUserProfile() {
        if (SessionManager.isLoggedIn()) {
            UserMainController.getInstance().showProfil();
        } else {
            UserMainController.getInstance().deconnecter(); // redirection vers login
        }
    }

    @FXML private void onUserBoxHover() {
        userBox.setStyle("-fx-background-color: #2C7AA0; -fx-background-radius: 25; -fx-padding: 8 20; -fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");
    }

    @FXML private void onUserBoxExit() {
        userBox.setStyle("-fx-background-color: #3D94CA; -fx-background-radius: 25; -fx-padding: 8 20; -fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0; -fx-effect: null;");
    }

    @FXML private void handleLogout() {
        SessionManager.logout();
        UserMainController.getInstance().deconnecter(); // redirige vers login
    }

    // ===== Gestion des publications =====
    private void filtrer() {
        String recherche = searchField.getText().toLowerCase().trim();
        List<Publication> result;
        if (recherche.isEmpty()) {
            result = allPublications;
        } else {
            result = allPublications.stream()
                    .filter(p -> p.getTitre().toLowerCase().contains(recherche) ||
                            p.getDescription().toLowerCase().contains(recherche))
                    .collect(Collectors.toList());
        }
        afficherPublications(result);
    }

    private void appliquerTri() {
        String tri = sortCombo.getValue();
        List<Publication> liste = allPublications;
        if (tri != null) {
            switch (tri) {
                case "Plus anciens":
                    liste = allPublications.stream()
                            .sorted((p1, p2) -> p1.getDateCreation().compareTo(p2.getDateCreation()))
                            .collect(Collectors.toList());
                    break;
                case "Plus aimés":
                    liste = allPublications.stream()
                            .sorted((p1, p2) -> Integer.compare(p2.getLikes(), p1.getLikes()))
                            .collect(Collectors.toList());
                    break;
                default: // "Plus récents"
                    liste = allPublications.stream()
                            .sorted((p1, p2) -> p2.getDateCreation().compareTo(p1.getDateCreation()))
                            .collect(Collectors.toList());
            }
        }
        afficherPublications(liste);
    }

    private void setFilter(Button btn, String categorie) {
        // Pour un vrai style actif, il faudrait gérer une variable activeFilterBtn
        if (categorie == null) {
            afficherPublications(allPublications);
        } else {
            List<Publication> filtrees = allPublications.stream()
                    .filter(p -> p.getCategorie().name().equals(categorie))
                    .collect(Collectors.toList());
            afficherPublications(filtrees);
        }
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
                // Ignorer
            }
        }

        Label titre = new Label(p.getTitre());
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label categorie = new Label(p.getCategorie().getLabel());
        categorie.setStyle("-fx-text-fill: #3b82f6;");

        Label auteur = new Label("Par " + (p.getAuteur() != null ? p.getAuteur() : "Anonyme"));
        auteur.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        Button details = new Button("Voir détails");
        details.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 8 20;");
        details.setOnAction(e -> {
            SelectedItem.setCurrentPublication(p);
            UserMainController.getInstance().loadView("/views/common/Commentaires.fxml");
        });

        card.getChildren().addAll(imageView, titre, categorie, auteur, details);
        return card;
    }
}