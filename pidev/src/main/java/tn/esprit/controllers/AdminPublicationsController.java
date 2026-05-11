package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Categorie;
import tn.esprit.entities.Publication;
import tn.esprit.services.PublicationService;
import tn.esprit.utils.SelectedItem;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AdminPublicationsController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AdminPublicationsController.class.getName());

    @FXML private FlowPane publicationsGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button btnAjouter;
    @FXML private Button btnTous, btnPlage, btnMontagne, btnVille, btnDesert, btnCampagne;
    @FXML private Button searchBtn;

    // ✅ FIX: final car jamais réassigné
    private final PublicationService service = new PublicationService();
    private List<Publication> allPublications;
    private Categorie currentCategorie = Categorie.TOUS;
    private Button activeFilterBtn;
    private AdminDashboardController dashboardController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dashboardController = AdminDashboardController.getInstance();

        // ✅ FIX: SessionManager = méthodes statiques, pas d'getInstance()
        if (!SessionManager.estAdmin()) {
            LOGGER.severe("Accès non autorisé - utilisateur non admin");
            showAccessDenied();
            return;
        }

        System.out.println("✅ AdminPublicationsController initialisé - Admin: " +
                SessionManager.getCurrentUser().getEmail());

        sortCombo.getItems().addAll("Plus récents", "Plus anciens", "Plus aimés", "A-Z");
        sortCombo.setValue("Plus récents");
        sortCombo.setOnAction(e -> appliquerFiltres());

        searchField.textProperty().addListener((obs, old, val) -> appliquerFiltres());
        searchBtn.setOnAction(e -> appliquerFiltres());
        btnAjouter.setOnAction(e -> ajouterPublication());

        btnTous.setOnAction(e -> setFilter(btnTous, Categorie.TOUS));
        btnPlage.setOnAction(e -> setFilter(btnPlage, Categorie.PLAGE));
        btnMontagne.setOnAction(e -> setFilter(btnMontagne, Categorie.MONTAGNE));
        btnVille.setOnAction(e -> setFilter(btnVille, Categorie.VILLE));
        btnDesert.setOnAction(e -> setFilter(btnDesert, Categorie.DESERT));
        btnCampagne.setOnAction(e -> setFilter(btnCampagne, Categorie.CAMPAGNE));

        chargerDonnees();
        activeFilterBtn = btnTous;
        setFilter(btnTous, Categorie.TOUS);
    }

    private void showAccessDenied() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Accès refusé");
        alert.setHeaderText("Vous n'avez pas les droits d'administration");
        alert.setContentText("Cette section est réservée aux administrateurs.");
        alert.showAndWait();

        try {
            // ✅ FIX: showDashboard() est private dans AdminDashboardController
            //         → on utilise loadPage() qui est static et accessible
            AdminDashboardController.loadPage("/fxml/DashboardContent.fxml");
        } catch (Exception e) {
            LOGGER.severe("Erreur redirection: " + e.getMessage());
        }
    }

    private void chargerDonnees() {
        try {
            allPublications = service.getAll();
            System.out.println("✅ " + allPublications.size() + " publications chargées");
            appliquerFiltres();
        } catch (Exception e) {
            LOGGER.severe("Erreur chargement données: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger les publications: " + e.getMessage());
            allPublications = new ArrayList<>();
        }
    }

    private void appliquerFiltres() {
        if (allPublications == null || allPublications.isEmpty()) {
            afficherPublications(new ArrayList<>());
            return;
        }

        List<Publication> filtered = new ArrayList<>(allPublications);

        // Filtre par catégorie
        if (currentCategorie != Categorie.TOUS) {
            filtered = filtered.stream()
                    .filter(p -> p.getCategorie() == currentCategorie)
                    .collect(Collectors.toList());
        }

        // Filtre par recherche
        String search = searchField.getText().toLowerCase().trim();
        if (!search.isEmpty()) {
            filtered = filtered.stream()
                    .filter(p -> (p.getTitre() != null && p.getTitre().toLowerCase().contains(search)) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(search)) ||
                            (p.getAuteur() != null && p.getAuteur().toLowerCase().contains(search)))
                    .collect(Collectors.toList());
        }

        // Tri
        String sortType = sortCombo.getValue();
        if (sortType != null && !filtered.isEmpty()) {
            switch (sortType) {
                case "Plus anciens":
                    filtered.sort((p1, p2) -> {
                        if (p1.getDateCreation() == null) return 1;
                        if (p2.getDateCreation() == null) return -1;
                        return p1.getDateCreation().compareTo(p2.getDateCreation());
                    });
                    break;
                case "Plus aimés":
                    // ✅ FIX: getLikes() retourne int primitif → pas de null check
                    filtered.sort((p1, p2) -> Integer.compare(p2.getLikes(), p1.getLikes()));
                    break;
                case "A-Z":
                    filtered.sort((p1, p2) -> {
                        if (p1.getTitre() == null) return 1;
                        if (p2.getTitre() == null) return -1;
                        return p1.getTitre().compareToIgnoreCase(p2.getTitre());
                    });
                    break;
                default: // "Plus récents"
                    filtered.sort((p1, p2) -> {
                        if (p1.getDateCreation() == null) return 1;
                        if (p2.getDateCreation() == null) return -1;
                        return p2.getDateCreation().compareTo(p1.getDateCreation());
                    });
            }
        }

        afficherPublications(filtered);
    }

    private void afficherPublications(List<Publication> publications) {
        publicationsGrid.getChildren().clear();

        if (publications.isEmpty()) {
            Label noDataLabel = new Label("Aucune publication trouvée");
            noDataLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #64748b; -fx-padding: 50;");
            publicationsGrid.getChildren().add(noDataLabel);
            return;
        }

        for (Publication p : publications) {
            try {
                publicationsGrid.getChildren().add(createAdminCard(p));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erreur création carte pour: " + p.getTitre(), e);
            }
        }

        System.out.println("✅ Affichage de " + publications.size() + " publications");
    }

    private VBox createAdminCard(Publication p) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/components/PublicationCard.fxml")
        );
        VBox card = loader.load();

        PublicationCardController controller = loader.getController();

        if (controller != null) {
            // ✅ FIX: setAdminBackendMode() n'existe pas → setAdminMode(true) existe bien
            controller.setAdminMode(true);
            // ✅ FIX: setPublication(Publication) existe bien dans PublicationCardController
            controller.setPublication(p);

            controller.setOnEditCallback(publication -> {
                if (publication != null) {
                    openEditPublicationModal(publication);
                }
            });

            controller.setOnDeleteCallback(this::supprimerPublication);
        }

        return card;
    }

    private void openEditPublicationModal(Publication publication) {
        try {
            // ✅ On place la publication dans SelectedItem AVANT de charger le FXML
            //    ModifierPublicationsController la lit via SelectedItem.getCurrentPublication()
            //    dans son initialize() → pas besoin de setPublication() ni setReturnToUser()
            SelectedItem.setCurrentPublication(publication);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierPublications.fxml"));
            DialogPane dialogPane = loader.load();

            // ✅ FIX: ModifierPublicationsController n'a pas setPublication() ni setReturnToUser()
            //         Il récupère la publication depuis SelectedItem dans initialize()
            //         Aucun appel supplémentaire nécessaire ici

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Modifier la publication");
            dialog.setHeaderText("Modification de la publication: " + publication.getTitre());

            dialogPane.getStylesheets().add(
                    getClass().getResource("/css/global.css").toExternalForm()
            );

            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    chargerDonnees();
                    showAlert("Succès", "Publication modifiée avec succès !");
                }
            });

        } catch (IOException e) {
            LOGGER.severe("Erreur ouverture modal modification: " + e.getMessage());
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification");
        }
    }

    private void supprimerPublication(Publication p) {
        if (p == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la publication");
        confirm.setContentText("Voulez-vous vraiment supprimer \"" + p.getTitre() + "\" ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.supprimer(p.getId());
                    showAlert("Succès", "Publication supprimée avec succès !");
                    chargerDonnees();
                } catch (Exception e) {
                    LOGGER.severe("Erreur suppression: " + e.getMessage());
                    showAlert("Erreur", "Impossible de supprimer la publication: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void ajouterPublication() {
        try {
            if (dashboardController != null) {
                // ✅ FIX: openAddPublicationModal() n'existe pas dans AdminDashboardController
                //         → on utilise loadPage() qui est static
                AdminDashboardController.loadPage("/fxml/AjouterPublication.fxml");
            } else {
                LOGGER.severe("AdminDashboardController instance is null");
                showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
            }
        } catch (Exception e) {
            LOGGER.severe("Erreur ajout publication: " + e.getMessage());
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML public void filtrerTous()     { setFilter(btnTous,     Categorie.TOUS);     }
    @FXML public void filtrerPlage()    { setFilter(btnPlage,    Categorie.PLAGE);    }
    @FXML public void filtrerMontagne() { setFilter(btnMontagne, Categorie.MONTAGNE); }
    @FXML public void filtrerVille()    { setFilter(btnVille,    Categorie.VILLE);    }
    @FXML public void filtrerDesert()   { setFilter(btnDesert,   Categorie.DESERT);   }
    @FXML public void filtrerCampagne() { setFilter(btnCampagne, Categorie.CAMPAGNE); }

    private void setFilter(Button btn, Categorie cat) {
        if (btn == null) return;

        String activeStyle = "-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-background-radius: 25; -fx-padding: 8 20; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: white; -fx-text-fill: #334155; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 25; -fx-background-radius: 25; " +
                "-fx-padding: 8 20; -fx-cursor: hand;";

        if (activeFilterBtn != null) activeFilterBtn.setStyle(inactiveStyle);
        btn.setStyle(activeStyle);
        activeFilterBtn = btn;
        currentCategorie = cat;
        appliquerFiltres();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(
                title.equals("Succès") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}