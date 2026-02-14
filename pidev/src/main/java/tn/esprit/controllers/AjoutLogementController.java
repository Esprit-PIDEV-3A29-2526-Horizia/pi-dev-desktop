package tn.esprit.controllers;

import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.logement;
import tn.esprit.services.Servicelogement;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

public class AjoutLogementController {

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextField nomField, imageField, adresseField, equipementField, tarifField;

    @FXML
    private Spinner<Integer> capaciteSpinner;

    @FXML
    private CheckBox disponibiliteCheckBox;

    @FXML
    private Button ajouterBtn, annulerBtn, nomMicroBtn, imageMicroBtn, adresseMicroBtn, equipementMicroBtn, tarifMicroBtn; // Ajout de tarifMicroBtn

    private Servicelogement servicelogement = new Servicelogement();

    @FXML
    public void initialize() {
        // Initialiser les actions des boutons micro
        nomMicroBtn.setOnAction(e -> handleMicro(nomField, nomMicroBtn));
        imageMicroBtn.setOnAction(e -> handleMicro(imageField, imageMicroBtn));
        adresseMicroBtn.setOnAction(e -> handleMicro(adresseField, adresseMicroBtn));
        equipementMicroBtn.setOnAction(e -> handleMicro(equipementField, equipementMicroBtn));
        tarifMicroBtn.setOnAction(e -> handleMicro(tarifField, tarifMicroBtn)); // Ajout pour tarif

        // Action pour ajouter
        ajouterBtn.setOnAction(e -> ajouterLogement());

        // Action pour annuler (retour à la liste via Dashboard)
        annulerBtn.setOnAction(e -> retourListe());
    }

    private void handleMicro(TextField field, Button microBtn) {
        // Animation : rotation du bouton micro pendant l'enregistrement
        RotateTransition rotate = new RotateTransition(Duration.seconds(2), microBtn);
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.play();

        // Simuler l'enregistrement vocal (remplacer par une vraie implémentation speech-to-text)
        // Ici, on suppose une bibliothèque comme CMU Sphinx ou une API (e.g., Google Speech API)
        // Pour cet exemple, on simule avec un délai et un texte fictif
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Simuler 3 secondes d'enregistrement
                String recognizedText = "Texte reconnu depuis la voix"; // Remplacer par le vrai texte reconnu
                javafx.application.Platform.runLater(() -> {
                    field.setText(recognizedText.toUpperCase()); // Convertir en majuscules
                    rotate.stop(); // Arrêter l'animation
                    microBtn.setRotate(0); // Remettre à l'état initial
                });
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void ajouterLogement() {
        // Validation des champs
        StringBuilder erreurs = new StringBuilder();

        // Type : obligatoire
        if (typeComboBox.getValue() == null || typeComboBox.getValue().trim().isEmpty()) {
            erreurs.append("- Sélectionnez un type de logement.\n");
        }

        // Nom : obligatoire, non vide, max 255 caractères
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            erreurs.append("- Le nom est obligatoire.\n");
        } else if (nom.length() > 255) {
            erreurs.append("- Le nom ne doit pas dépasser 255 caractères.\n");
        }

        // Image : obligatoire, non vide, vérifier validité
        String image = imageField.getText().trim();
        if (image.isEmpty()) {
            erreurs.append("- L'image est obligatoire.\n");
        } else if (!isValidImagePath(image)) {
            erreurs.append("- L'URL ou le chemin de l'image n'est pas valide.\n");
        }

        // Adresse : obligatoire, non vide, max 255 caractères
        String adresse = adresseField.getText().trim();
        if (adresse.isEmpty()) {
            erreurs.append("- L'adresse est obligatoire.\n");
        } else if (adresse.length() > 255) {
            erreurs.append("- L'adresse ne doit pas dépasser 255 caractères.\n");
        }

        // Capacité : déjà contrôlée par le Spinner (min 1, max 20)

        // Équipement : optionnel, max 255 caractères
        String equipement = equipementField.getText().trim();
        if (equipement.length() > 255) {
            erreurs.append("- Les équipements ne doivent pas dépasser 255 caractères.\n");
        }

        // Tarif : obligatoire, nombre flottant positif
        String tarifStr = tarifField.getText().trim();
        float tarif = 0;
        if (tarifStr.isEmpty()) {
            erreurs.append("- Le tarif est obligatoire.\n");
        } else {
            try {
                tarif = Float.parseFloat(tarifStr);
                if (tarif <= 0) {
                    erreurs.append("- Le tarif doit être un nombre positif.\n");
                }
            } catch (NumberFormatException e) {
                erreurs.append("- Le tarif doit être un nombre valide.\n");
            }
        }

        // Si erreurs, afficher et arrêter
        if (erreurs.length() > 0) {
            showAlert("Erreurs de validation", erreurs.toString());
            return;
        }

        try {
            // Créer l'objet logement (convertir les chaînes en majuscules)
            logement l = new logement();
            l.setType(typeComboBox.getValue());
            l.setNom(nom.toUpperCase());
            l.setImage(image);
            l.setAdresse(adresse.toUpperCase());
            l.setCapacite(capaciteSpinner.getValue());
            l.setEquipement(equipement.toUpperCase());
            l.setTarif_nuit(tarif);
            l.setDisponibilite(disponibiliteCheckBox.isSelected());

            // Ajouter via service (déjà implémenté)
            servicelogement.ajouter(l);

            showAlert("Succès", "Logement ajouté avec succès !");
            retourListe(); // Retour à la liste après ajout
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    // Méthode utilitaire pour valider le chemin/URL de l'image
    private boolean isValidImagePath(String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            try {
                new URL(path);
                return true;
            } catch (MalformedURLException e) {
                return false;
            }
        } else {
            // Pour les chemins locaux, on peut vérifier l'extension (simple check)
            return path.toLowerCase().endsWith(".jpg") || path.toLowerCase().endsWith(".png") || path.toLowerCase().endsWith(".jpeg") || path.toLowerCase().endsWith(".gif");
        }
    }

    private void retourListe() {
        // Charger la vue Logements dans le dashboard
        Dashboard.loadView("/Logements.fxml");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}