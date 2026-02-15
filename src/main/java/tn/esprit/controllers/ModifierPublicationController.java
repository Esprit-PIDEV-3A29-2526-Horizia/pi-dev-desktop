package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.esprit.entities.Publication;
import tn.esprit.services.ServicePublication;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ModifierPublicationController implements Initializable {

    @FXML private TextField titreField;
    @FXML private TextField lieuField;
    @FXML private TextField tarifField;
    @FXML private TextArea contenuField;
    @FXML private TextField imageField;
    @FXML private CheckBox publieCheck;

    private ServicePublication service = new ServicePublication();
    private Publication selected;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        selected = DashboardController.getInstance().getSelectedPublication();

        if (selected != null) {
            titreField.setText(selected.getTitre());
            lieuField.setText(selected.getLieu());
            tarifField.setText(String.valueOf(selected.getTarif()));
            contenuField.setText(selected.getContenu());
            imageField.setText(selected.getImage());
            publieCheck.setSelected(selected.isEstPublie());
        } else {
            showAlert("Erreur", "Aucune publication sélectionnée !");
        }
    }

    @FXML
    private void modifier() {

        if (selected == null) {
            showAlert("Erreur", "Aucune publication sélectionnée !");
            return;
        }

        float tarifValue;
        try {
            tarifValue = Float.parseFloat(tarifField.getText());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le tarif doit être un nombre valide !");
            return;
        }

        selected.setTitre(titreField.getText());
        selected.setLieu(lieuField.getText());
        selected.setTarif(tarifValue);
        selected.setContenu(contenuField.getText());
        selected.setImage(imageField.getText());
        selected.setEstPublie(publieCheck.isSelected());

        try {
            service.modifier(selected);
            showAlert("Succès", "Publication modifiée avec succès !");
            DashboardController.getInstance().loadView("/views/publication.fxml");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void annuler() {
        DashboardController.getInstance().loadView("/views/publication.fxml");
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}