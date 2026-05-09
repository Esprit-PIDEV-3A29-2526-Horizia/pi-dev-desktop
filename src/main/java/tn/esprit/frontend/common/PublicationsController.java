package tn.esprit.frontend.common;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.CommentaireService;
import tn.esprit.backend.services.PublicationService;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.frontend.components.PublicationCardController;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PublicationsController implements Initializable {

    @FXML private FlowPane publicationContainer;
    @FXML private Button ajouterBtn;
    @FXML private TextField rechercheField;
    @FXML private ComboBox<String> triComboBox;

    private PublicationService publicationService = new PublicationService();
    private CommentaireService commentaireService = new CommentaireService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        triComboBox.getItems().addAll("Plus récentes", "Plus anciennes", "A-Z", "Z-A", "Plus aimées");
        triComboBox.setValue("Plus récentes");

        loadpublication();

        ajouterBtn.setOnAction(e ->
                DashboardController.loadViewStatic("/views/common/AjouterPublication.fxml")
        );
        rechercheField.setOnKeyReleased(e -> rechercher());
        triComboBox.setOnAction(e -> loadpublication());
    }

    public void loadpublication() {
        publicationContainer.getChildren().clear();

        List<Publication> list = publicationService.getAll();
        list = sortpublication(list, triComboBox.getValue());

        for (Publication p : list) {
            try {
                // false = explorateur, pas de modifier/supprimer
                VBox card = creerCarte(p, false);
                publicationContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private VBox creerCarte(Publication p, boolean adminMode) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/components/PublicationCard.fxml")
        );
        VBox card = loader.load();

        PublicationCardController controller = loader.getController();
        controller.setPublication(p);
        controller.setAdminMode(adminMode);

        if (adminMode) {
            controller.setOnEditCallback(publication -> {
                SelectedItem.setCurrentPublication(publication);
                DashboardController.loadViewStatic("/views/common/ModifierPublication.fxml");
            });
            controller.setOnDeleteCallback(publication -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setContentText("Supprimer \"" + publication.getTitre() + "\" ?");
                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    try {
                        publicationService.supprimer(publication.getId());
                        loadpublication();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        return card;
    }

    private List<Publication> sortpublication(List<Publication> list, String sortType) {
        if (sortType == null) return list;
        switch (sortType) {
            case "Plus anciennes":
                return list.stream()
                        .sorted((p1, p2) -> p1.getDateCreation().compareTo(p2.getDateCreation()))
                        .collect(Collectors.toList());
            case "A-Z":
                return list.stream()
                        .sorted((p1, p2) -> p1.getTitre().compareToIgnoreCase(p2.getTitre()))
                        .collect(Collectors.toList());
            case "Z-A":
                return list.stream()
                        .sorted((p1, p2) -> p2.getTitre().compareToIgnoreCase(p1.getTitre()))
                        .collect(Collectors.toList());
            case "Plus aimées":
                return list.stream()
                        .sorted((p1, p2) -> Integer.compare(p2.getLikes(), p1.getLikes()))
                        .collect(Collectors.toList());
            default:
                return list.stream()
                        .sorted((p1, p2) -> p2.getDateCreation().compareTo(p1.getDateCreation()))
                        .collect(Collectors.toList());
        }
    }

    private void rechercher() {
        String keyword = rechercheField.getText().trim();
        publicationContainer.getChildren().clear();

        List<Publication> list = publicationService.getAll().stream()
                .filter(p -> p.getTitre().toLowerCase().contains(keyword.toLowerCase()) ||
                        p.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        for (Publication p : list) {
            try {
                publicationContainer.getChildren().add(creerCarte(p, false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void refresh() {
        loadpublication();
    }
}