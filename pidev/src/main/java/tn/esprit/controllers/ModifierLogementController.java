package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import tn.esprit.entities.logement;
import tn.esprit.services.Servicelogement;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ModifierLogementController implements Initializable {

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextField nomField;

    @FXML
    private TextField adresseField;

    @FXML
    private Spinner<Integer> capaciteSpinner;

    @FXML
    private TextField tarifField;

    @FXML
    private TextField equipementField;

    @FXML
    private TextField imageField;

    @FXML
    private Label disponibiliteLabel;

    @FXML
    private HBox switchBackground;

    @FXML
    private Circle switchCircle;

    @FXML
    private ToggleButton disponibiliteToggle;

    @FXML
    private Button modifierBtn;

    @FXML
    private Button annulerBtn;

    private Servicelogement servicelogement = new Servicelogement();
    private logement selectedLogement;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Récupérer le logement sélectionné
        selectedLogement = AdminDashboardController.getSelectedLogement();
        if (selectedLogement != null) {
            // Pré-remplir les champs avec les données existantes
            typeComboBox.setValue(selectedLogement.getType());
            nomField.setText(selectedLogement.getNom());
            adresseField.setText(selectedLogement.getAdresse());
            capaciteSpinner.getValueFactory().setValue(selectedLogement.getCapacite());
            tarifField.setText(String.valueOf(selectedLogement.getTarif_nuit()));
            equipementField.setText(selectedLogement.getEquipement());
            imageField.setText(selectedLogement.getImage());
            disponibiliteToggle.setSelected(selectedLogement.isDisponibilite());
            updateSwitchUI(); // Mettre à jour l'UI du switch
        } else {
            showAlert("Erreur", "Aucun logement sélectionné pour la modification.");
        }

        // Actions des boutons
        modifierBtn.setOnAction(e -> modifierLogement());
        annulerBtn.setOnAction(e ->AdminDashboardController.loadPage("/fxml/Logements.fxml"));
    }

    @FXML
    private void handleSwitchClick() {
        // Inverser l'état du toggle
        disponibiliteToggle.setSelected(!disponibiliteToggle.isSelected());
        updateSwitchUI();
    }

    private void updateSwitchUI() {
        boolean isSelected = disponibiliteToggle.isSelected();
        if (isSelected) {
            switchBackground.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 13; -fx-padding: 2;");
            switchCircle.setTranslateX(24); // Déplacer le cercle à droite
            disponibiliteLabel.setText("Disponible");
            disponibiliteLabel.setTextFill(Color.web("#2ecc71"));
        } else {
            switchBackground.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 13; -fx-padding: 2;");
            switchCircle.setTranslateX(0); // Remettre à gauche
            disponibiliteLabel.setText("Non disponible");
            disponibiliteLabel.setTextFill(Color.web("#e74c3c"));
        }
    }

    private void modifierLogement() {
        if (selectedLogement == null) return;

        // Validation basique
        if (nomField.getText().isEmpty() || adresseField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Mettre à jour l'objet logement avec les nouvelles valeurs
        selectedLogement.setType(typeComboBox.getValue());
        selectedLogement.setNom(nomField.getText());
        selectedLogement.setAdresse(adresseField.getText());
        selectedLogement.setCapacite(capaciteSpinner.getValue());
        selectedLogement.setTarif_nuit(Float.parseFloat(tarifField.getText()));
        selectedLogement.setEquipement(equipementField.getText());
        selectedLogement.setImage(imageField.getText());
        selectedLogement.setDisponibilite(disponibiliteToggle.isSelected());

        try {
            servicelogement.modifier(selectedLogement);
            showAlert("Succès", "Logement modifié avec succès.");
            AdminDashboardController.loadPage("/fxml/Logements.fxml"); // Revenir à la liste
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}