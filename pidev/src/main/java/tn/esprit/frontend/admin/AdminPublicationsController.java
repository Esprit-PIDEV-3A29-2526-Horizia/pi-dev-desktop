package tn.esprit.frontend.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Categorie;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.backend.utils.Session;
import tn.esprit.frontend.components.PublicationCardController;

import java.io.IOException;
import java.net.URL;
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

    private PublicationService service = new PublicationService();
    private List<Publication> allPublications;
    private Categorie currentCategorie = Categorie.TOUS;
    private Button activeFilterBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!Session.estAdmin()) {
            LOGGER.severe("Accès non autorisé");
            return;
        }

        sortCombo.getItems().addAll("Plus récents", "Plus anciens", "Plus aimés", "A-Z");
        sortCombo.setValue("Plus récents");
        sortCombo.setOnAction(e -> appliquerFiltres());

        searchField.textProperty().addListener((obs, old, val) -> appliquerFiltres());

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

    private void chargerDonnees() {
        allPublications = service.getAll();
        appliquerFiltres();
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
                    .filter(p -> p.getTitre().toLowerCase().contains(search) ||
                            p.getDescription().toLowerCase().contains(search) ||
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
            }
        }

        afficherPublications(filtered);
    }

    private void afficherPublications(List<Publication> publications) {
        publicationsGrid.getChildren().clear();
        for (Publication p : publications) {
            try {
                VBox card = createAdminCard(p);
                publicationsGrid.getChildren().add(card);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erreur création carte", e);
            }
        }
    }

    private VBox createAdminCard(Publication p) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/PublicationCard.fxml"));
        VBox card = loader.load();

        PublicationCardController controller = loader.getController();
        controller.setPublication(p);
        controller.setAdminMode(true);

        controller.setOnEditCallback(publication -> {
            SelectedItem.setCurrentPublication(publication);
            AdminDashboardController.getInstance().openEditPublicationModal(publication);
        });

        controller.setOnDeleteCallback(this::supprimerPublication);

        return card;
    }

    private void supprimerPublication(Publication p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la publication");
        confirm.setContentText("Voulez-vous vraiment supprimer \"" + p.getTitre() + "\" ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.supprimer(p.getId());
                    chargerDonnees();
                    showAlert("Succès", "Publication supprimée !");
                } catch (Exception e) {
                    showAlert("Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void ajouterPublication() {
        AdminDashboardController.getInstance().openAddPublicationModal();
    }

    @FXML public void filtrerTous() { setFilter(btnTous, Categorie.TOUS); }
    @FXML public void filtrerPlage() { setFilter(btnPlage, Categorie.PLAGE); }
    @FXML public void filtrerMontagne() { setFilter(btnMontagne, Categorie.MONTAGNE); }
    @FXML public void filtrerVille() { setFilter(btnVille, Categorie.VILLE); }
    @FXML public void filtrerDesert() { setFilter(btnDesert, Categorie.DESERT); }
    @FXML public void filtrerCampagne() { setFilter(btnCampagne, Categorie.CAMPAGNE); }

    private void setFilter(Button btn, Categorie cat) {
        if (activeFilterBtn != null) {
            activeFilterBtn.setStyle("-fx-background-color: white; -fx-text-fill: #334155; " +
                    "-fx-border-color: #e2e8f0; -fx-border-radius: 25; -fx-background-radius: 25;");
        }
        btn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-background-radius: 25; -fx-padding: 8 20;");
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