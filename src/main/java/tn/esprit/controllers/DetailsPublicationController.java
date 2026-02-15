package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.entities.Publication;
import tn.esprit.services.ServicePublication;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;

public class DetailsPublicationController implements Initializable {

    @FXML private Label titreLabel;
    @FXML private Label prixLabel;
    @FXML private Label statutBadge;
    @FXML private ImageView mainImage;
    @FXML private Label descriptionLabel;
    @FXML private Label typeLabel;
    @FXML private Label lieuLabel;
    @FXML private Label likesLabel;
    @FXML private Label dateLabel;
    @FXML private Button retourBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;

    private ServicePublication servicePublication = new ServicePublication();
    private static Publication selectedPublication;

    // Méthode pour définir la publication sélectionnée (appelée avant d'ouvrir cette vue)
    public static void setSelectedPublication(Publication publication) {
        selectedPublication = publication;
    }

    // Méthode pour récupérer la publication sélectionnée
    public static Publication getSelectedPublication() {
        return selectedPublication;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (selectedPublication != null) {
            afficherDetails(selectedPublication);
        } else {
            showAlert("Erreur", "Aucune publication sélectionnée");
        }
    }

    private void afficherDetails(Publication selected) {
        titreLabel.setText(selected.getTitre());
        prixLabel.setText("💰 " + selected.getTarif() + " DT");

        // statut badge
        statutBadge.setText(selected.isEstPublie() ? "Publié" : "Brouillon");
        statutBadge.setStyle(selected.isEstPublie()
                ? "-fx-background-color: #28A745; -fx-text-fill: white; -fx-padding: 10 25; -fx-background-radius: 25;"
                : "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 10 25; -fx-background-radius: 25;");

        typeLabel.setText("🏷️ Type : " + selected.getType());
        lieuLabel.setText("📍 Lieu : " + selected.getLieu());
        likesLabel.setText("❤️ Likes : " + selected.getLikes());
        descriptionLabel.setText(selected.getContenu());

        // date
        if (selected.getDatePublication() != null) {
            dateLabel.setText("📅 " +
                    selected.getDatePublication().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }

        // image
        chargerImage(selected.getImage());
    }

    private void chargerImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image;
                if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                    image = new Image(imagePath, true);
                } else if (imagePath.startsWith("file:/")) {
                    image = new Image(imagePath);
                } else {
                    // Essayer de charger depuis les ressources
                    URL imageUrl = getClass().getResource(imagePath);
                    if (imageUrl != null) {
                        image = new Image(imageUrl.toExternalForm());
                    } else {
                        // Essayer comme chemin de fichier
                        image = new Image("file:" + imagePath);
                    }
                }
                mainImage.setImage(image);
            } catch (Exception e) {
                System.out.println("Erreur chargement image : " + e.getMessage());
                // Image par défaut
                mainImage.setImage(new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream("/images/default.png"))));
            }
        } else {
            // Image par défaut si aucune image
            try {
                mainImage.setImage(new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream("/images/default.png"))));
            } catch (Exception e) {
                System.out.println("Image par défaut non trouvée");
            }
        }
    }

    @FXML
    private void retourListe() {
        loadView("/views/Publications.fxml");
    }

    @FXML
    private void modifierPublication() {
        loadView("/views/ModifierPublication.fxml");
    }

    @FXML
    private void supprimerPublication() {
        if (selectedPublication != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation de suppression");
            confirm.setHeaderText(null);
            confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette publication ?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        servicePublication.supprimer(selectedPublication.getId());
                        showAlert("Succès", "Publication supprimée avec succès !");
                        loadView("/views/Publications.fxml");
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    // Méthode utilitaire pour charger une vue
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) titreLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}