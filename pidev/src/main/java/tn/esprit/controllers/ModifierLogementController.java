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

    // ⚠️ On utilise maintenant l'instance de Dashboard
    private Dashboard dashboardInstance;
    private logement selectedLogement;

    public void setDashboardInstance(Dashboard dashboard) {
        this.dashboardInstance = dashboard;
        this.selectedLogement = dashboard.getSelectedLogement();
        remplirChamps();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Les boutons et switch
        modifierBtn.setOnAction(e -> modifierLogement());
        annulerBtn.setOnAction(e -> {
            if (dashboardInstance != null) {
                dashboardInstance.loadView("/fxml/Logements.fxml");
            }
        });

        switchBackground.setOnMouseClicked(e -> {
            disponibiliteToggle.setSelected(!disponibiliteToggle.isSelected());
            updateSwitchUI();
        });
    }

    private void remplirChamps() {
        if (selectedLogement != null) {
            typeComboBox.setValue(selectedLogement.getType());
            nomField.setText(selectedLogement.getNom());
            adresseField.setText(selectedLogement.getAdresse());
            capaciteSpinner.getValueFactory().setValue(selectedLogement.getCapacite());
            tarifField.setText(String.valueOf(selectedLogement.getTarif_nuit()));
            equipementField.setText(selectedLogement.getEquipement());
            imageField.setText(selectedLogement.getImage());
            disponibiliteToggle.setSelected(selectedLogement.isDisponibilite());
            updateSwitchUI();
        } else {
            showAlert("Erreur", "Aucun logement sélectionné pour la modification.");
        }
    }

    private void updateSwitchUI() {
        boolean isSelected = disponibiliteToggle.isSelected();
        if (isSelected) {
            switchBackground.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 13; -fx-padding: 2;");
            switchCircle.setTranslateX(24);
            disponibiliteLabel.setText("Disponible");
            disponibiliteLabel.setTextFill(Color.web("#2ecc71"));
        } else {
            switchBackground.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 13; -fx-padding: 2;");
            switchCircle.setTranslateX(0);
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

        // Mise à jour des données
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
            if (dashboardInstance != null) {
                dashboardInstance.loadView("/fxml/Logements.fxml");
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage());
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