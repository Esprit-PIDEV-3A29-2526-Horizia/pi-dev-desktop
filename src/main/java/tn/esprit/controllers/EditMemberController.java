package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.entities.User;
import tn.esprit.entities.Profil;
import tn.esprit.services.Serviceuser;
import tn.esprit.services.ServiceProfil;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class EditMemberController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Label lblMessage;
    @FXML private Label lblId; // Pour stocker l'ID caché

    private Serviceuser userService = new Serviceuser();
    private ServiceProfil profilService = new ServiceProfil();
    private AdminDashboardController dashboardController;

    private User userToEdit;
    private List<Profil> allProfils;

    @FXML
    public void initialize() {
        chargerProfils();

        // Listener pour mettre à jour les statuts quand le type change
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

        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("❌ Erreur de chargement des statuts", "error");
        }
    }

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
    }

    public void setUserToEdit(User user) {
        this.userToEdit = user;
        remplirFormulaire(user);
    }

    private void remplirFormulaire(User user) {
        lblId.setText(String.valueOf(user.getId()));
        txtNom.setText(user.getNom());
        txtPrenom.setText(user.getPrenom());
        txtEmail.setText(user.getEmail());
        txtPassword.setText(""); // Ne pas afficher le mot de passe existant
        txtTelephone.setText(user.getTelephone());
        txtAdresse.setText(user.getAddresse());

        if (user.getProfil() != null) {
            cbType.setValue(user.getProfil().getType());
            cbStatut.setValue(user.getProfil().getStatut());
        }
    }

    @FXML
    private void goBack() {
        System.out.println("=== Retour à la liste ===");
        if (dashboardController != null) {
            dashboardController.showUsers(); // Ou dashboardController.loadPage("/fxml/MemberList.fxml");
        }
    }

    @FXML
    private void updateMember() {
        // Validation
        if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || cbType.getValue() == null ||
                cbStatut.getValue() == null) {
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

            // Mettre à jour l'utilisateur
            userToEdit.setNom(txtNom.getText().trim());
            userToEdit.setPrenom(txtPrenom.getText().trim());
            userToEdit.setEmail(txtEmail.getText().trim());

            // Ne mettre à jour le mot de passe que s'il a été modifié
            if (!txtPassword.getText().isEmpty()) {
                userToEdit.setPassword(txtPassword.getText());
            }

            userToEdit.setTelephone(txtTelephone.getText().trim());
            userToEdit.setAddresse(txtAdresse.getText().trim());
            userToEdit.setProfil(selectedProfil);

            // Sauvegarder
            userService.modifier(userToEdit);

            showMessage("✅ Membre modifié avec succès!", "success");

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
        if (userToEdit != null) {
            remplirFormulaire(userToEdit);
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