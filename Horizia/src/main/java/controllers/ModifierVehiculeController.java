package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.entities.Marque;
import org.example.entities.Modele;
import org.example.entities.Vehicule;
import org.example.services.MarqueService;
import org.example.services.ModeleService;
import org.example.services.VehiculeService;

import java.util.List;

public class ModifierVehiculeController {

    @FXML private TextField txtRechercheImmat;
    @FXML private Label lblMessageRecherche;
    @FXML private javafx.scene.layout.VBox vboxFormulaire;
    @FXML private Label lblVehiculeTrouve;

    @FXML private ComboBox<Marque> comboMarque;
    @FXML private ComboBox<Modele> comboModele;
    @FXML private TextField txtAnnee;
    @FXML private ComboBox<String> comboCarburant;
    @FXML private TextField txtCouleur;
    @FXML private TextField txtKilometrage;
    @FXML private ComboBox<String> comboEtat;
    @FXML private TextField txtPrixParJour;
    @FXML private TextField txtPhoto;
    @FXML private Label lblMessage;

    private VehiculeService vehiculeService;
    private MarqueService marqueService;
    private ModeleService modeleService;
    private Vehicule vehiculeCourant;

    @FXML
    public void initialize() {
        vehiculeService = new VehiculeService();
        marqueService = new MarqueService();
        modeleService = new ModeleService();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Modifier Véhicule - Chargée");
        System.out.println("═══════════════════════════════════════════════");

        initialiserComboBoxes();
        txtRechercheImmat.requestFocus();
    }

    private void initialiserComboBoxes() {
        comboCarburant.getItems().addAll("Essence", "Diesel", "Hybride", "Electrique");
        comboEtat.getItems().addAll("disponible", "louee", "en_maintenance", "indisponible");
    }

    @FXML
    private void rechercherVehicule() {
        String immat = txtRechercheImmat.getText();

        if (immat == null || immat.trim().isEmpty()) {
            afficherErreurRecherche("⚠️ Veuillez saisir une immatriculation !");
            return;
        }

        System.out.println("→ Recherche du véhicule : " + immat);

        List<Vehicule> vehicules = vehiculeService.rechercherParImmatriculation(immat.trim());

        if (vehicules.isEmpty()) {
            afficherErreurRecherche("✗ Aucun véhicule trouvé avec cette immatriculation !");
            cacherFormulaire();
            return;
        }

        vehiculeCourant = vehicules.get(0);
        System.out.println("✓ Véhicule trouvé : " + vehiculeCourant.getImmatriculation());

        afficherSuccesRecherche("✓ Véhicule trouvé : " + vehiculeCourant.getImmatriculation());
        preremplirFormulaire();
        afficherFormulaire();
    }

    private void preremplirFormulaire() {
        // Charger marque et modèle
        Modele modele = modeleService.getModeleById(vehiculeCourant.getIdModele());
        if (modele != null) {
            Marque marque = marqueService.getMarqueById(modele.getIdMarque());

            comboMarque.getItems().clear();
            comboMarque.getItems().add(marque);
            comboMarque.setValue(marque);

            comboModele.getItems().clear();
            comboModele.getItems().add(modele);
            comboModele.setValue(modele);

            lblVehiculeTrouve.setText("Véhicule : " + marque.getNomMarque() + " " + modele.getNomModele() +
                    " - " + vehiculeCourant.getImmatriculation());
        }

        txtAnnee.setText(String.valueOf(vehiculeCourant.getAnnee()));
        comboCarburant.setValue(vehiculeCourant.getCarburant());
        txtCouleur.setText(vehiculeCourant.getCouleur());
        txtKilometrage.setText(String.valueOf(vehiculeCourant.getKilometrage()));
        comboEtat.setValue(vehiculeCourant.getEtat());
        txtPrixParJour.setText(String.valueOf(vehiculeCourant.getPrixParJour()));
        txtPhoto.setText(vehiculeCourant.getPhoto());

        System.out.println("✓ Formulaire pré-rempli");
    }

    @FXML
    private void enregistrerModifications() {
        if (vehiculeCourant == null) {
            afficherErreur("⚠️ Aucun véhicule sélectionné !");
            return;
        }

        // Validation
        try {
            int annee = Integer.parseInt(txtAnnee.getText().trim());
            if (annee < 1900 || annee > 2050) {
                afficherErreur("⚠️ L'année doit être entre 1900 et 2050 !");
                return;
            }
            vehiculeCourant.setAnnee(annee);
        } catch (NumberFormatException e) {
            afficherErreur("⚠️ L'année doit être un nombre valide !");
            return;
        }

        try {
            int kilometrage = Integer.parseInt(txtKilometrage.getText().trim());
            if (kilometrage < 0) {
                afficherErreur("⚠️ Le kilométrage ne peut pas être négatif !");
                return;
            }
            vehiculeCourant.setKilometrage(kilometrage);
        } catch (NumberFormatException e) {
            afficherErreur("⚠️ Le kilométrage doit être un nombre valide !");
            return;
        }

        try {
            double prix = Double.parseDouble(txtPrixParJour.getText().trim());
            if (prix <= 0) {
                afficherErreur("⚠️ Le prix doit être supérieur à 0 !");
                return;
            }
            vehiculeCourant.setPrixParJour(prix);
        } catch (NumberFormatException e) {
            afficherErreur("⚠️ Le prix doit être un nombre valide !");
            return;
        }

        vehiculeCourant.setCarburant(comboCarburant.getValue());
        vehiculeCourant.setCouleur(txtCouleur.getText().trim());
        vehiculeCourant.setEtat(comboEtat.getValue());
        vehiculeCourant.setPhoto(txtPhoto.getText().trim());

        System.out.println("→ Enregistrement des modifications...");

        boolean succes = vehiculeService.modifierVehicule(vehiculeCourant);

        if (succes) {
            System.out.println("✓ Véhicule modifié avec succès");
            afficherSucces("✓ Les modifications ont été enregistrées avec succès !");

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::reinitialiser);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            System.err.println("✗ Échec de la modification");
            afficherErreur("✗ Erreur lors de l'enregistrement des modifications !");
        }
    }

    private void afficherFormulaire() {
        vboxFormulaire.setVisible(true);
        vboxFormulaire.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.millis(400), vboxFormulaire);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void cacherFormulaire() {
        vboxFormulaire.setVisible(false);
        vboxFormulaire.setManaged(false);
    }

    private void reinitialiser() {
        txtRechercheImmat.clear();
        cacherFormulaire();
        cacherMessageRecherche();
        cacherMessage();
        vehiculeCourant = null;
        txtRechercheImmat.requestFocus();
        System.out.println("🔄 Interface réinitialisée");
    }

    @FXML
    private void annuler() {
        System.out.println("✖ Annulation de la modification");
        Stage stage = (Stage) txtRechercheImmat.getScene().getWindow();
        stage.close();
    }

    private void afficherSuccesRecherche(String message) {
        lblMessageRecherche.setText(message);
        lblMessageRecherche.setStyle("-fx-text-fill: #27ae60; -fx-background-color: #d5f4e6; -fx-font-weight: bold;");
        lblMessageRecherche.setVisible(true);
        lblMessageRecherche.setManaged(true);
    }

    private void afficherErreurRecherche(String message) {
        lblMessageRecherche.setText(message);
        lblMessageRecherche.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fadbd8; -fx-font-weight: bold;");
        lblMessageRecherche.setVisible(true);
        lblMessageRecherche.setManaged(true);
    }

    private void cacherMessageRecherche() {
        lblMessageRecherche.setVisible(false);
        lblMessageRecherche.setManaged(false);
    }

    private void afficherSucces(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #27ae60; -fx-background-color: #d5f4e6; -fx-font-weight: bold; -fx-border-color: #27ae60; -fx-border-width: 2; -fx-border-radius: 8;");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void afficherErreur(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fadbd8; -fx-font-weight: bold; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void cacherMessage() {
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }
}