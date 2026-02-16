package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.Publication;
import tn.esprit.services.PublicationService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PublicationsController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private FlowPane publicationsFlowPane;
    @FXML private ComboBox<String> sortComboBox;

    private PublicationService publicationService = new PublicationService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sortComboBox.getItems().addAll("Plus récentes", "Plus anciennes", "A-Z", "Z-A");
        sortComboBox.setValue("Plus récentes");
        sortComboBox.setOnAction(e -> loadPublications());

        loadPublications();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                loadPublications();
            } else {
                publicationsFlowPane.getChildren().clear();
                for (Publication p : publicationService.rechercherParTitre(newVal)) {
                    addPublicationCard(p);
                }
            }
        });

        addButton.setOnAction(e -> openAddDialog());
    }

    private void loadPublications() {
        publicationsFlowPane.getChildren().clear();

        String tri = sortComboBox.getValue();
        List<Publication> publications = publicationService.getAll(tri);

        System.out.println("📊 Nombre de publications: " + publications.size());

        for (Publication p : publications) {
            System.out.println(" - " + p.getTitre());
            addPublicationCard(p);
        }
    }

    private void addPublicationCard(Publication publication) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/PublicationCard.fxml"));
            VBox card = loader.load();

            PublicationCardController controller = loader.getController();
            controller.setPublication(publication);
            controller.setParentController(this);

            // 🔥 Ajout du gestionnaire de clic pour ouvrir les commentaires
            card.setOnMouseClicked(event -> openComments(publication));

            publicationsFlowPane.getChildren().add(card);
            System.out.println("✅ Carte ajoutée: " + publication.getTitre());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur chargement carte FXML: " + e.getMessage());
        }
    }

    /**
     * Ouvre la vue des commentaires pour la publication sélectionnée.
     * @param publication la publication cliquée
     */
    private void openComments(Publication publication) {
        try {
            // Ajustez le chemin si votre fichier Commentaires.fxml est dans un autre dossier
            // Exemple : "/views/Commentaires.fxml" ou "/Commentaires.fxml"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Commentaires.fxml"));
            Parent root = loader.load();

            CommentairesController controller = loader.getController();
            controller.setPublication(publication);

            // Récupérer la scène actuelle et remplacer sa racine
            Stage stage = (Stage) publicationsFlowPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur ouverture des commentaires : " + e.getMessage());
        }
    }

    private void openAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPublication.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Publication");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadPublications();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refresh() {
        loadPublications();
    }
}