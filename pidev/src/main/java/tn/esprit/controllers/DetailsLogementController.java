package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.entities.logement;
import tn.esprit.services.Servicelogement;

import java.net.URL;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

public class DetailsLogementController implements Initializable {

    @FXML
    private Label nomLabel;

    @FXML
    private Label prixLabel;

    @FXML
    private Label dispoBadge;

    @FXML
    private Button modifierBtn;

    @FXML
    private Button supprimerBtn;

    @FXML
    private ImageView mainImage;

    @FXML
    private Label descriptionLabel; // Correspond à equipement dans l'entité

    @FXML
    private Label typeLabel;

    @FXML
    private Label adresseLabel;

    @FXML
    private Label capaciteLabel;

    @FXML
    private Label equipementLabel;

    private Servicelogement servicelogement = new Servicelogement();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Récupérer le logement sélectionné depuis Dashboard
        logement selectedLogement = Dashboard.getSelectedLogement();
        if (selectedLogement != null) {
            // Remplir les labels avec les données du logement
            nomLabel.setText(selectedLogement.getNom());
            prixLabel.setText("Prix/Nuit : " + selectedLogement.getTarif_nuit() + " DT");
            dispoBadge.setText(selectedLogement.isDisponibilite() ? "Disponible" : "Non disponible");
            dispoBadge.setStyle(selectedLogement.isDisponibilite()
                    ? "-fx-background-color: #28A745; -fx-text-fill: white; -fx-padding: 10 25; -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 14;"
                    : "-fx-background-color: #DC3545; -fx-text-fill: white; -fx-padding: 10 25; -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 14;");

            typeLabel.setText("🏠 Type : " + selectedLogement.getType());
            adresseLabel.setText("📍 Adresse : " + selectedLogement.getAdresse());
            capaciteLabel.setText("👥 Capacité : " + selectedLogement.getCapacite());
            equipementLabel.setText("✨ Équipements : " + selectedLogement.getEquipement());
            descriptionLabel.setText(selectedLogement.getEquipement()); // Utilise equipement comme description

            // Charger l'image
            String imagePath = selectedLogement.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    if (imagePath.startsWith("http")) {
                        mainImage.setImage(new Image(imagePath));
                    } else {
                        mainImage.setImage(new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm()));
                    }
                } catch (Exception e) {
                    System.err.println("Erreur chargement image : " + e.getMessage());
                    // Optionnel : définir une image par défaut
                }
            }
        } else {
            // Gérer le cas où aucun logement n'est sélectionné
            nomLabel.setText("Aucun logement sélectionné");
            showAlert("Erreur", "Aucun logement sélectionné.");
        }
    }

    @FXML
    private void retourListe() {
        // Revenir à la vue des logements
        Dashboard.loadView("/Logements.fxml");
    }

    @FXML
    private void modifierLogement() {
        // Pour l'instant, afficher une alerte. Vous pouvez étendre pour charger une vue de modification.
        showAlert("Modifier", "Fonctionnalité de modification à implémenter.");
        // Exemple d'extension : Dashboard.setSelectedLogement(selectedLogement); Dashboard.loadView("/modifierLogement.fxml");
    }

    @FXML
    private void supprimerLogement() {
        logement selectedLogement = Dashboard.getSelectedLogement();
        if (selectedLogement != null) {
            // Confirmation avant suppression
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de suppression");
            confirmation.setHeaderText("Supprimer le logement : " + selectedLogement.getNom());
            confirmation.setContentText("Cette action est irréversible. Voulez-vous continuer ?");
            confirmation.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    try {
                        servicelogement.supprimer(selectedLogement.getId());
                        showAlert("Succès", "Logement supprimé avec succès.");
                        // Recharger la liste des logements
                        Dashboard.loadView("/Logements.fxml");
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
                    }
                }
            });
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}