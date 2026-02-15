package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import tn.esprit.entities.Publication;
import tn.esprit.services.PublicationService;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PublicationsController implements Initializable {

    @FXML
    private FlowPane publicationsFlowPane;

    @FXML
    private TextField searchField;

    @FXML
    private Button addButton;

    @FXML
    private ComboBox<String> sortComboBox;

    private PublicationService publicationService = new PublicationService();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration du ComboBox pour le tri
        sortComboBox.getItems().addAll(
                "Plus récentes d'abord",
                "Plus anciennes d'abord",
                "Titre A-Z",
                "Titre Z-A"
        );
        sortComboBox.setValue("Plus récentes d'abord");

        // Ajouter un listener pour le tri
        sortComboBox.valueProperty().addListener((observable, oldValue, newValue) -> filterPublications(searchField.getText()));

        // Charger les publications initialement
        List<Publication> allPublications = publicationService.getAll();
        displayPublications(sortPublications(allPublications, sortComboBox.getValue()));

        // Écouteur de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterPublications(newValue));

        // Action du bouton Ajouter
        addButton.setOnAction(event -> Dashboard.loadView("/AjouterPublication.fxml"));
    }

    private void displayPublications(List<Publication> publications) {
        publicationsFlowPane.getChildren().clear();
        for (Publication publication : publications) {
            VBox card = createPublicationCard(publication);
            publicationsFlowPane.getChildren().add(card);
        }
    }

    private void filterPublications(String keyword) {
        List<Publication> filtered;

        if (keyword == null || keyword.trim().isEmpty()) {
            filtered = publicationService.getAll();
        } else {
            filtered = publicationService.rechercherParTitre(keyword);
        }

        // Appliquer le tri sur les résultats filtrés
        filtered = sortPublications(filtered, sortComboBox.getValue());

        displayPublications(filtered);
    }

    // Méthode pour trier la liste en mémoire
    private List<Publication> sortPublications(List<Publication> publications, String sortOption) {
        switch (sortOption) {
            case "Plus récentes d'abord":
                return publications.stream()
                        .sorted(Comparator.comparing(Publication::getDatePublication).reversed())
                        .collect(Collectors.toList());
            case "Plus anciennes d'abord":
                return publications.stream()
                        .sorted(Comparator.comparing(Publication::getDatePublication))
                        .collect(Collectors.toList());
            case "Titre A-Z":
                return publications.stream()
                        .sorted(Comparator.comparing(Publication::getTitre, String.CASE_INSENSITIVE_ORDER))
                        .collect(Collectors.toList());
            case "Titre Z-A":
                return publications.stream()
                        .sorted(Comparator.comparing(Publication::getTitre, String.CASE_INSENSITIVE_ORDER).reversed())
                        .collect(Collectors.toList());
            default:
                return publications;
        }
    }

    private VBox createPublicationCard(Publication publication) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(280);

        // === Conteneur pour l'image avec la date superposée ===
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(250, 180);
        imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");

        // Image
        ImageView imageView = null;
        String imagePath = publication.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                if (imagePath.startsWith("http")) {
                    imageView = new ImageView(new Image(imagePath, true));
                } else {
                    imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm()));
                }
                imageView.setFitWidth(250);
                imageView.setFitHeight(180);
                imageView.setPreserveRatio(true);
            } catch (Exception e) {
                System.err.println("Erreur chargement image pour " + publication.getTitre() + " : " + e.getMessage());
                imageView = null;
            }
        }

        if (imageView != null) {
            imageContainer.getChildren().add(imageView);
        } else {
            // Placeholder si pas d'image
            Region placeholder = new Region();
            placeholder.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 8;");
            placeholder.setPrefSize(250, 180);
            imageContainer.getChildren().add(placeholder);
        }

        // Date de publication superposée sur l'image
        Label dateLabel = new Label(publication.getDatePublication().format(dateFormatter));
        dateLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: rgba(52, 152, 219, 0.9); -fx-padding: 5 10; -fx-background-radius: 5;");
        StackPane.setAlignment(dateLabel, Pos.TOP_RIGHT);
        imageContainer.getChildren().add(dateLabel);

        // Ajouter le conteneur d'image à la carte
        card.getChildren().add(imageContainer);

        // Titre
        Label titreLabel = new Label(publication.getTitre());
        titreLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titreLabel.setWrapText(true);
        titreLabel.setMaxWidth(250);

        // Description (tronquée)
        String description = publication.getDescription();
        if (description != null && description.length() > 100) {
            description = description.substring(0, 100) + "...";
        }
        Label descriptionLabel = new Label(description);
        descriptionLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(250);
        descriptionLabel.setMaxHeight(60);

        // Boutons d'action
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button modifierBtn = new Button("Modifier");
        modifierBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 6 15;");
        modifierBtn.setOnAction(e -> {
            Dashboard.setSelectedPublication(publication);
            Dashboard.loadView("/ModifierPublication.fxml");
        });

        Button supprimerBtn = new Button("Supprimer");
        supprimerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 6 15;");
        supprimerBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer la publication");
            confirm.setContentText("Êtes-vous sûr de vouloir supprimer '" + publication.getTitre() + "' ?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    publicationService.supprimer(publication.getId());
                    filterPublications(searchField.getText());
                }
            });
        });

        buttonsBox.getChildren().addAll(modifierBtn, supprimerBtn);

        card.getChildren().addAll(titreLabel, descriptionLabel, buttonsBox);
        return card;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}