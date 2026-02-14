package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Profil;
import tn.esprit.services.ServiceProfil;

import java.sql.SQLException;

public class AddProfilController {

    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label lblMessage;

    private final ServiceProfil serviceProfil = new ServiceProfil();
    private Profil profilToEdit;
    private ProfilListController previousController;

    @FXML
    public void initialize() {
        // Les ComboBox sont déjà remplies dans le FXML
    }

    public void setProfilToEdit(Profil profil) {
        this.profilToEdit = profil;
        if (profil != null) {
            cbType.setValue(profil.getType());
            cbStatut.setValue(profil.getStatut());
        }
    }

    public void setPreviousController(ProfilListController controller) {
        this.previousController = controller;
    }

    @FXML
    private void saveProfil() {
        if (!validateForm()) {
            return;
        }

        try {
            if (profilToEdit == null) {
                // Ajout
                Profil profil = new Profil(cbType.getValue(), cbStatut.getValue());
                serviceProfil.ajouter(profil);  // Utilise votre serviceProfil.ajouter()
                showMessage("✅ Profil ajouté avec succès", "success");
            } else {
                // Modification
                profilToEdit.setType(cbType.getValue());
                profilToEdit.setStatut(cbStatut.getValue());
                serviceProfil.modifier(profilToEdit);  // Utilise votre serviceProfil.modifier()
                showMessage("✅ Profil modifié avec succès", "success");
            }

            // Fermer après 1.5 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(this::cancel);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            showMessage("❌ Erreur: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        if (cbType.getValue() == null || cbStatut.getValue() == null) {
            showMessage("Tous les champs sont obligatoires", "error");
            return false;
        }
        return true;
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) cbType.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        if ("error".equals(type)) {
            lblMessage.setStyle("-fx-text-fill: #EF4444;");
        } else {
            lblMessage.setStyle("-fx-text-fill: #10B981;");
        }
    }
}