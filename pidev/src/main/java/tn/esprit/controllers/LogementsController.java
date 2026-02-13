package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import tn.esprit.entities.logement; // Votre entité (minuscule)
import tn.esprit.services.Servicelogement; // Votre service

import java.net.URL;
import java.sql.SQLException; // Pour gérer les exceptions SQL
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class LogementsController implements Initializable {

    @FXML
    private FlowPane logementsFlowPane;

    private Servicelogement servicelogement = new Servicelogement(); // Instance de votre service

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            loadLogements();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            // Ici, vous pouvez afficher une alerte à l'utilisateur si la DB échoue
        }
    }

    private void loadLogements() throws SQLException {
        List<logement> logements = servicelogement.afficher(); // Récupère tous les logements via afficher()

        for (logement logement : logements) {
            // Créer une carte pour chaque logement
            VBox card = createLogementCard(logement);
            logementsFlowPane.getChildren().add(card);
        }
    }

    private VBox createLogementCard(logement logement) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(250);

        // Image du logement
        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        try {
            String imagePath = logement.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("http") || imagePath.startsWith("https")) {
                    // URL externe : Chargez directement
                    imageView.setImage(new Image(imagePath));
                } else {
                    // Chemin relatif/local : Utilisez getResource (ex: "@image/logement1.jpg")
                    imageView.setImage(new Image(getClass().getResource(imagePath).toExternalForm()));
                }
            } else {
                // Image par défaut si null ou vide
                imageView.setImage(new Image(getClass().getResource("@image/default.jpg").toExternalForm()));
            }
        } catch (Exception e) {
            // Gestion d'erreur : Image par défaut en cas d'échec (ex: URL invalide, fichier manquant)
            System.err.println("Erreur de chargement de l'image pour " + logement.getNom() + " : " + e.getMessage());
            try {
                imageView.setImage(new Image(getClass().getResource("@image/default.jpg").toExternalForm()));
            } catch (Exception ex) {
                // Si même l'image par défaut échoue, laissez vide ou affichez un placeholder
                System.err.println("Image par défaut introuvable : " + ex.getMessage());
            }
        }

        // Label pour le nom
        Label nomLabel = new Label(logement.getNom());
        nomLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Label pour l'adresse
        Label adresseLabel = new Label("Adresse: " + logement.getAdresse());
        adresseLabel.setStyle("-fx-font-size: 12; -fx-text-fill: gray;");
        adresseLabel.setWrapText(true);

        // Label pour la capacité
        Label capaciteLabel = new Label("Capacité: " + logement.getCapacite() + " personnes");
        capaciteLabel.setStyle("-fx-font-size: 12; -fx-text-fill: gray;");

        // Label pour le tarif par nuit
        Label tarifLabel = new Label("Tarif/nuit: " + logement.getTarif_nuit() + " €");
        tarifLabel.setStyle("-fx-font-size: 14; -fx-text-fill: green;");

        // Label pour la disponibilité
        Label dispoLabel = new Label(logement.isDisponibilite() ? "Disponible" : "Non disponible");
        dispoLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + (logement.isDisponibilite() ? "green" : "red") + ";");

        // Bouton "Voir plus" (ou "Réserver")
        Button voirPlusBtn = new Button("Voir plus");
        voirPlusBtn.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-background-radius: 5;");
        voirPlusBtn.setOnAction(e -> {
            // Action pour voir les détails (ex: ouvrir une nouvelle fenêtre ou changer de vue)
            System.out.println("Voir détails de " + logement.getNom());
            // Ici, implémentez la navigation vers une vue de détails, ou ouvrez un popup avec plus d'infos
        });

        // Optionnel : Boutons pour CRUD (Modifier/Supprimer)
        Button modifierBtn = new Button("Modifier");
        modifierBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-background-radius: 5;");
        modifierBtn.setOnAction(e -> {
            // Ouvrir un formulaire de modification (vous devrez créer une nouvelle vue/fenêtre)
            System.out.println("Modifier " + logement.getNom());
        });

        Button supprimerBtn = new Button("Supprimer");
        supprimerBtn.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-background-radius: 5;");
        supprimerBtn.setOnAction(e -> {
            try {
                servicelogement.supprimer(logement.getId());
                // Rafraîchir la vue après suppression
                logementsFlowPane.getChildren().clear();
                loadLogements();
            } catch (SQLException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();                // Afficher une alerte d'erreur
            }
        });

        card.getChildren().addAll(imageView, nomLabel, adresseLabel, capaciteLabel, tarifLabel, dispoLabel, voirPlusBtn, modifierBtn, supprimerBtn);
        return card;
    }
}