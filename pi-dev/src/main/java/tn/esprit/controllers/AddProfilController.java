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
    @FXML private Label lblTitre;
    @FXML private Button btnSave;
    @FXML private Button btnReset;
    @FXML private Button btnBack;

    private ServiceProfil serviceProfil = new ServiceProfil();
    private AdminDashboardController dashboardController;
    private boolean isStandaloneWindow = false;

    private Profil profilToEdit;
    private boolean isEditMode = false;

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
        this.isStandaloneWindow = (controller == null);
    }

    public void setProfilToEdit(Profil profil) {
        this.profilToEdit = profil;
        this.isEditMode = (profil != null);

        if (isEditMode) {
            cbType.setValue(profil.getType());
            cbStatut.setValue(profil.getStatut());

            if (lblTitre != null) {
                lblTitre.setText("✏️ Modifier un Profil");
            }
        }
    }

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation AddProfilController ===");

        cbType.getItems().addAll("ADMIN", "AGENT", "CLIENT");
        cbStatut.getItems().addAll("ACTIF", "INACTIF", "BLOQUE");

        if (!isEditMode) {
            cbType.setValue("CLIENT");
            cbStatut.setValue("ACTIF");
        }
    }

    @FXML
    private void saveProfil() {
        System.out.println("=== Sauvegarde du profil ===");

        String type = cbType.getValue();
        String statut = cbStatut.getValue();

        if (type == null || statut == null) {
            showMessage("Veuillez sélectionner un type et un statut", "error");
            return;
        }

        try {
            if (isEditMode && profilToEdit != null) {
                profilToEdit.setType(type);
                profilToEdit.setStatut(statut);
                serviceProfil.modifier(profilToEdit);
                showMessage("✅ Profil modifié avec succès!", "success");
            } else {
                Profil profil = new Profil(type, statut);
                serviceProfil.ajouter(profil);
                showMessage("✅ Profil ajouté avec succès!", "success");
            }

            // Retour après succès
            goBackAfterDelay();

        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("❌ Erreur: " + e.getMessage(), "error");
        }
    }

    private void goBackAfterDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                javafx.application.Platform.runLater(() -> {
                    goBack();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void resetForm() {
        if (isEditMode && profilToEdit != null) {
            cbType.setValue(profilToEdit.getType());
            cbStatut.setValue(profilToEdit.getStatut());
        } else {
            cbType.setValue("CLIENT");
            cbStatut.setValue("ACTIF");
        }
    }

    @FXML
    private void goBack() {
        System.out.println("=== Retour à la liste des profils ===");

        if (isStandaloneWindow) {
            // Mode FENÊTRE AUTONOME
            try {
                Stage stage = (Stage) lblMessage.getScene().getWindow();
                stage.close();
                System.out.println("✅ Fenêtre fermée");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Mode DASHBOARD
            if (dashboardController != null) {
                dashboardController.showProfils(); // Important : cette méthode doit exister
                System.out.println("✅ Retour au dashboard - Liste des profils");
            }
        }
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        if ("error".equals(type)) {
            lblMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        } else {
            lblMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        }
    }
}