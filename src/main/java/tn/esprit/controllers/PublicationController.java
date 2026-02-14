package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Publication;
import tn.esprit.services.ServicePublication;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PublicationController implements Initializable {

    @FXML
    private FlowPane publicationsFlowPane;

    @FXML
    private TextField searchField;

    @FXML
    private Button addButton;

    private ServicePublication servicePublication = new ServicePublication();
    private List<Publication> allPublications;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try { loadPublications(); }
        catch (SQLException e) { showAlert("Erreur", "Impossible de charger les publications : " + e.getMessage()); }

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterPublications(newVal));

        addButton.setOnAction(event -> Dashboard.loadView("/AjoutPublication.fxml"));
    }

    private void loadPublications() throws SQLException {
        allPublications = servicePublication.afficher();
        displayPublications(allPublications);
    }

    private void displayPublications(List<Publication> publications) {
        publicationsFlowPane.getChildren().clear();
        for (Publication pub : publications) {
            VBox card = createPublicationCard(pub);
            publicationsFlowPane.getChildren().add(card);
        }
    }

    private void filterPublications(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            displayPublications(allPublications);
        } else {
            List<Publication> filtered = allPublications.stream()
                    .filter(p -> p.getTitre().toLowerCase().contains(keyword.toLowerCase())
                            || p.getContenu().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
            displayPublications(filtered);
        }
    }

    private VBox createPublicationCard(Publication pub) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(250);

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(220, 150);
        imageContainer.setStyle("-fx-background-color: #f0f0f0;");

        ImageView imageView = null;
        String imagePath = pub.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                if (imagePath.startsWith("http")) {
                    imageView = new ImageView(new Image(imagePath));
                } else {
                    imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm()));
                }
                imageView.setFitWidth(220);
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);
            } catch (Exception e) { imageView = null; }
        }

        if (imageView != null) imageContainer.getChildren().add(imageView);
        else {
            Region placeholder = new Region();
            placeholder.setStyle("-fx-background-color: #e0e0e0;");
            placeholder.setPrefSize(220, 150);
            imageContainer.getChildren().add(placeholder);
        }

        card.getChildren().add(imageContainer);

        // Titre, Contenu, Date, Statut
        Label titreLabel = new Label(pub.getTitre());
        titreLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label contenuLabel = new Label(pub.getContenu());
        contenuLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");
        contenuLabel.setWrapText(true);
        contenuLabel.setMaxHeight(60);

        Label dateLabel = new Label("📅 " + pub.getDatePublication());
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #95a5a6;");

        Label dispoLabel = new Label(pub.isActif() ? "Publié" : "Non publié");
        dispoLabel.setStyle("-fx-background-color: #81ae8d; -fx-font-size: 12; -fx-text-fill: white; -fx-background-radius: 4;");

        Button detailsBtn = new Button("Voir détails");
        detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setOnAction(e -> System.out.println("Détails de : " + pub.getTitre()));

        card.getChildren().addAll(titreLabel, contenuLabel, dateLabel, dispoLabel, detailsBtn);
        return card;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
