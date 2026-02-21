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

    private Dashboard dashboardInstance;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Récupérer l'instance Dashboard via singleton
        dashboardInstance = Dashboard.getInstance();

        if (dashboardInstance == null) {
            System.err.println("❌ Impossible de récupérer l'instance du Dashboard !");
            showAlert("Erreur", "Problème d'accès au Dashboard.");
            return;
        }

        // Récupérer le logement sélectionné
        logement selectedLogement = dashboardInstance.getSelectedLogement();

        if (selectedLogement != null) {
            remplirDetails(selectedLogement);
        } else {
            nomLabel.setText("Aucun logement sélectionné");
            showAlert("Erreur", "Aucun logement sélectionné.");
        }
    }
    private void remplirDetails(logement selectedLogement) {
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
    }

    @FXML
    private void retourListe() {
        if (dashboardInstance != null) {
            dashboardInstance.loadView("/fxml/Logements.fxml");
        }
    }

    @FXML
    private void modifierLogement() {
        if (dashboardInstance != null) {
            dashboardInstance.loadView("/fxml/modifierLogement.fxml");
        }
    }

    @FXML
    private void supprimerLogement() {
        if (dashboardInstance == null) return;

        logement selectedLogement = dashboardInstance.getSelectedLogement();
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
                        dashboardInstance.loadView("/fxml/Logements.fxml");
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
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}