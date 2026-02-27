package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.entities.User;
import tn.esprit.entities.Profil;
import tn.esprit.services.ServiceUser;
import tn.esprit.services.ServiceProfil;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AddMemberController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label lblMessage;

    private ServiceUser userService = new ServiceUser();
    private ServiceProfil profilService = new ServiceProfil();
    private AdminDashboardController dashboardController;

    private List<Profil> allProfils;

    @FXML
    public void initialize() {
        chargerProfils();

        cbType.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mettreAJourStatuts(newVal);
            }
        });
    }

    private void chargerProfils() {
        try {
            allProfils = profilService.afficher();

            List<String> types = allProfils.stream()
                    .map(Profil::getType)
                    .distinct()
                    .collect(Collectors.toList());

            cbType.setItems(FXCollections.observableArrayList(types));

            if (!types.isEmpty()) {
                cbType.setValue(types.get(0));
                mettreAJourStatuts(types.get(0));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("❌ Erreur de chargement des profils", "error");
        }
    }

    private void mettreAJourStatuts(String type) {
        try {
            List<Profil> profilsDuType = profilService.rechercherParType(type);

            List<String> statuts = profilsDuType.stream()
                    .map(Profil::getStatut)
                    .collect(Collectors.toList());

            cbStatut.setItems(FXCollections.observableArrayList(statuts));

            if (!statuts.isEmpty()) {
                cbStatut.setValue(statuts.get(0));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("❌ Erreur de chargement des statuts", "error");
        }
    }

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
    }

    @FXML
    private void goBack() {
        System.out.println("=== Retour à la liste (AddMember) ===");
        if (dashboardController != null) {
            dashboardController.showUsers(); // Retourne à la liste des membres
        }
    }

    @FXML
    private void saveMember() {
        // Validation
        if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || txtPassword.getText().isEmpty() ||
                cbType.getValue() == null || cbStatut.getValue() == null) {
            showMessage("❌ Veuillez remplir tous les champs obligatoires", "error");
            return;
        }

        try {
            // Trouver le profil correspondant
            Profil selectedProfil = trouverProfil(cbType.getValue(), cbStatut.getValue());

            if (selectedProfil == null) {
                showMessage("❌ Profil non trouvé", "error");
                return;
            }

            // Créer le nouvel utilisateur
            User user = new User();
            user.setNom(txtNom.getText().trim());
            user.setPrenom(txtPrenom.getText().trim());
            user.setEmail(txtEmail.getText().trim());
            user.setPassword(txtPassword.getText());
            user.setTelephone(txtTelephone.getText().trim());
            user.setAddresse(txtAdresse.getText().trim());
            user.setProfil(selectedProfil);

            // Ajouter à la base
            userService.ajouter(user);

            showMessage("✅ Membre ajouté avec succès!", "success");

            // Retour à la liste après un délai
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        if (dashboardController != null) {
                            dashboardController.showUsers();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("❌ Erreur: " + e.getMessage(), "error");
        }
    }

    private Profil trouverProfil(String type, String statut) {
        if (allProfils != null) {
            for (Profil p : allProfils) {
                if (p.getType().equals(type) && p.getStatut().equals(statut)) {
                    return p;
                }
            }
        }
        return null;
    }

    @FXML
    private void resetForm() {
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtTelephone.clear();
        txtAdresse.clear();

        if (!cbType.getItems().isEmpty()) {
            cbType.setValue(cbType.getItems().get(0));
        }
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        if (type.equals("error")) {
            lblMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        } else {
            lblMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        }
    }
}