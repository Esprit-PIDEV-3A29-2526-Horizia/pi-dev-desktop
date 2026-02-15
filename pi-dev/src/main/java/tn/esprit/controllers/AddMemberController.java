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

    // Stocker tous les profils pour référence
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
            // Charger tous les profils
            allProfils = profilService.afficher();

            // Extraire les types uniques
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
            showMessage("❌ Erreur de chargement des profils: " + e.getMessage(), "error");
        }
    }
    private User userToEdit; // Pour stocker l'utilisateur à modifier
    private boolean isEditMode = false; // Pour savoir si on est en mode édition

    // ... votre code existant ...

    /**
     * Méthode appelée quand on veut modifier un utilisateur existant
     */
    public void setUserToEdit(User user) {
        this.userToEdit = user;
        this.isEditMode = (user != null);

        if (isEditMode) {
            // Remplir le formulaire avec les données de l'utilisateur
            remplirFormulaire(user);

            // Changer le titre et le bouton
            // (si vous avez un Label pour le titre)
            // lblTitre.setText("Modifier un Membre");
        }
    }
    private void remplirFormulaire(User user) {
        txtNom.setText(user.getNom());
        txtPrenom.setText(user.getPrenom());
        txtEmail.setText(user.getEmail());
        txtPassword.setText(user.getPassword()); // Attention: en production, ne pas pré-remplir le mot de passe
        txtTelephone.setText(user.getTelephone());
        txtAdresse.setText(user.getAddresse());

        if (user.getProfil() != null) {
            cbType.setValue(user.getProfil().getType());
            cbStatut.setValue(user.getProfil().getStatut());
        }
    }

    private void mettreAJourStatuts(String type) {
        try {
            // Récupérer les profils du type sélectionné
            List<Profil> profilsDuType = profilService.rechercherParType(type);

            // Extraire les statuts
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
        if (dashboardController != null) {
            dashboardController.showUsers();
        }
    }

    @FXML
    private void saveMember() {
        // Validation des champs
        if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() ||
                txtEmail.getText().isEmpty() ||
                (txtPassword.getText().isEmpty() && !isEditMode) || // Mot de passe requis seulement en ajout
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

            if (isEditMode && userToEdit != null) {
                // Mode ÉDITION
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

                userService.modifier(userToEdit);
                showMessage("✅ Membre modifié avec succès!", "success");

            } else {
                // Mode AJOUT
                User newUser = new User();
                newUser.setNom(txtNom.getText().trim());
                newUser.setPrenom(txtPrenom.getText().trim());
                newUser.setEmail(txtEmail.getText().trim());
                newUser.setPassword(txtPassword.getText());
                newUser.setTelephone(txtTelephone.getText().trim());
                newUser.setAddresse(txtAdresse.getText().trim());
                newUser.setProfil(selectedProfil);

                userService.ajouter(newUser);
                showMessage("✅ Membre ajouté avec succès!", "success");
            }

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
            lblMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else {
            lblMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 14px;");
        }
    }


    @FXML
    private Label lblTitre; // Ajoutez ce Label dans votre FXML si vous voulez changer le titre

    // Optionnel: Ajouter cette méthode pour réinitialiser le mode
    public void resetMode() {
        this.isEditMode = false;
        this.userToEdit = null;
        resetForm();
        // if (lblTitre != null) lblTitre.setText("Ajouter un Membre");
    }
}