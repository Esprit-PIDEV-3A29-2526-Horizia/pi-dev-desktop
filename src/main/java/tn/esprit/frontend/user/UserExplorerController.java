package tn.esprit.frontend.user;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Categorie;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.utils.Session;
import tn.esprit.frontend.components.PublicationCardController;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserExplorerController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private FlowPane itemsGrid;
    @FXML private Button btnTous, btnPlage, btnMontagne, btnVille, btnDesert, btnCampagne, searchBtn;

    private PublicationService publicationService = new PublicationService();
    private List<Publication> allPublications;
    private Categorie currentCategorie = Categorie.TOUS;
    private Button activeFilterBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        allPublications = publicationService.getAll();

        sortCombo.getItems().addAll("Plus récents", "Plus anciens", "Plus aimés", "A-Z");
        sortCombo.setValue("Plus récents");
        sortCombo.setOnAction(e -> appliquerFiltres());

        searchField.textProperty().addListener((obs, old, val) -> appliquerFiltres());
        searchBtn.setOnAction(e -> appliquerFiltres());

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

    // ✅ Utilisation de PublicationCard.fxml avec adminMode=false
    private VBox createCard(Publication p) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/PublicationCard.fxml"));
        VBox card = loader.load();

        PublicationCardController controller = loader.getController();
        controller.setPublication(p);
        controller.setAdminMode(false);  // Cache Modifier et Supprimer

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