package tn.esprit.frontend.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.CommentaireService;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.entities.User;
import tn.esprit.frontend.components.PublicationCardController;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserPublicationsController implements Initializable {

    // Composants de l'en-tête
    @FXML private Button btnAccueil;
    @FXML private Button btnNosLogements;
    @FXML private Button btnMesReservations;
    @FXML private Button btnEvenements;
    @FXML private Button btnLocation;
    @FXML private Button btnVoyager;
    @FXML private Button btnpublication;
    @FXML private HBox userBox;
    @FXML private Label userNameLabel;
    @FXML private Button btnLogout;

    // Composants de la page
    @FXML private Label totalLabel;
    @FXML private Label likesLabel;
    @FXML private Label commentsLabel;
    @FXML private FlowPane itemsGrid;
    @FXML private Button btnAjouter;

    private PublicationService publicationService = new PublicationService();
    private CommentaireService commentaireService = new CommentaireService();
    private List<Publication> mesPublications;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUser = SessionManager.getCurrentUser();
        setupNavBar();

        if (!SessionManager.isLoggedIn()) return;
        chargerPublications();

        btnAjouter.setOnAction(e -> {
            // Chemin à adapter selon l'emplacement réel du fichier
            NavigationManager.loadView("/views/common/AjouterPublication.fxml", "Ajouter publication");
        });
    }

    private void setupNavBar() {
        if (SessionManager.isLoggedIn() && currentUser != null) {
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        } else {
            userNameLabel.setText("Connexion");
        }

        btnAccueil.setOnAction(e -> retourAccueil());
        btnNosLogements.setOnAction(e -> afficherLogements());
        btnMesReservations.setOnAction(e -> voirMesReservations());
        btnEvenements.setOnAction(e -> afficherEvenements());
        btnLocation.setOnAction(e -> afficherLocation());
        btnVoyager.setOnAction(e -> afficherVoyager());
        btnpublication.setOnAction(e -> afficherPublications());

        userBox.setOnMouseClicked(e -> showUserProfile());
        userBox.setOnMouseEntered(e -> onUserBoxHover());
        userBox.setOnMouseExited(e -> onUserBoxExit());
        btnLogout.setOnAction(e -> handleLogout());
    }

    // ===== Méthodes de navigation avec chemins adaptés =====
    @FXML private void retourAccueil() {
        NavigationManager.loadView("/fxml/accueil.fxml", "Accueil");
    }

    @FXML private void afficherLogements() {
        // Si la vue des logements est dans /fxml/ (ex: Explorateur de logements)
        NavigationManager.loadView("/fxml/accueil.fxml", "Nos Logements");
    }

    @FXML private void voirMesReservations() {
        NavigationManager.loadView("/fxml/MesReservations.fxml", "Mes Réservations");
    }

    @FXML private void afficherEvenements() {
        NavigationManager.loadView("/fxml/Evenements.fxml", "Événements");
    }

    @FXML private void afficherLocation() {
        NavigationManager.loadView("/fxml/ReservationVoiure.fxml", "Location");
    }

    @FXML private void afficherVoyager() {
        NavigationManager.loadView("/fxml/CatalogueUser.fxml", "Voyages");
    }

    @FXML private void afficherPublications() {
        // Recharge la liste actuelle
        chargerPublications();
    }

    @FXML private void showUserProfile() {
        if (SessionManager.isLoggedIn()) {
            NavigationManager.loadView("/views/user/UserProfil.fxml", "Mon Profil");
        } else {
            NavigationManager.showLogin();
        }
    }

    @FXML private void onUserBoxHover() {
        userBox.setStyle("-fx-background-color: #2C7AA0; -fx-background-radius: 25; -fx-padding: 8 20; -fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");
    }

    @FXML private void onUserBoxExit() {
        userBox.setStyle("-fx-background-color: #3D94CA; -fx-background-radius: 25; -fx-padding: 8 20; -fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0; -fx-effect: null;");
    }

    @FXML private void handleLogout() {
        NavigationManager.logout();
    }

    // ===== Méthodes métier (inchangées) =====
    private void chargerPublications() {
        int userId = currentUser.getId();
        mesPublications = publicationService.getAll().stream()
                .filter(p -> p.getUtilisateurId() == userId)
                .collect(Collectors.toList());

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
        controller.setAdminMode(true);

        controller.setOnEditCallback(publication -> {
            SelectedItem.setCurrentPublication(publication);
            NavigationManager.loadView("/views/common/ModifierPublication.fxml", "Modifier publication");
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