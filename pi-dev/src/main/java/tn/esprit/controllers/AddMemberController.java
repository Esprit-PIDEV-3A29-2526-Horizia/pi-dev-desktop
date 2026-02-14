package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Profil;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceUser;
import tn.esprit.services.ServiceProfil;  // ← N'OUBLIEZ PAS D'IMPORTER

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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

    private final ServiceUser serviceUser = new ServiceUser();
    private User userToEdit;
    private MemberListController previousController;

    @FXML
    public void initialize() {
        // Remplir les ComboBox
        cbType.getItems().addAll("CLIENT", "AGENT", "ADMIN");
        cbStatut.getItems().addAll("ACTIF", "BLOQUE", "EN ATTENTE");

        // Optionnel: Vérifier les profils existants au démarrage
        checkExistingProfils();
    }

    // 🔹 MÉTHODE À AJOUTER (1)
    private int getOrCreateProfil(String type, String statut) throws SQLException {
        ServiceProfil serviceProfil = new ServiceProfil();

        // Chercher si un profil avec ce type/statut existe déjà
        List<Profil> profils = serviceProfil.rechercherParType(type);
        for (Profil p : profils) {
            if (p.getStatut().equals(statut)) {
                System.out.println("🔍 Profil existant trouvé: ID=" + p.getId() +
                        ", Type=" + p.getType() + ", Statut=" + p.getStatut());
                return p.getId();  // Profil existant trouvé
            }
        }

        // Créer un nouveau profil
        Profil nouveauProfil = new Profil(type, statut);
        int newId = serviceProfil.ajouter(nouveauProfil);
        System.out.println("✅ Nouveau profil créé avec ID: " + newId);
        return newId;
    }

    // 🔹 MÉTHODE À AJOUTER (2) - Pour déboguer
    private void checkExistingProfils() {
        try {
            ServiceProfil serviceProfil = new ServiceProfil();
            List<Profil> profils = serviceProfil.afficher();
            System.out.println("📊 Profils existants dans la base:");
            if (profils.isEmpty()) {
                System.out.println("  Aucun profil trouvé");
            } else {
                for (Profil p : profils) {
                    System.out.println("  ID: " + p.getId() + " | " + p.getType() + " | " + p.getStatut());
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification des profils:");
            e.printStackTrace();
        }
    }

    public void setUserToEdit(User user) {
        this.userToEdit = user;
        if (user != null) {
            // Remplir le formulaire avec les données de l'utilisateur
            txtNom.setText(user.getNom());
            txtPrenom.setText(user.getPrenom());
            txtEmail.setText(user.getEmail());
            txtPassword.setText(user.getPassword());
            txtTelephone.setText(user.getTelephone());
            txtAdresse.setText(user.getAddresse());

            if (user.getProfil() != null) {
                cbType.setValue(user.getProfil().getType());
                cbStatut.setValue(user.getProfil().getStatut());
            }
        }
    }

    public void setPreviousController(MemberListController controller) {
        this.previousController = controller;
    }

    @FXML
    private void saveMember() {
        if (!validateForm()) {
            return;
        }

        try {
            // 🔹 UTILISATION DE LA MÉTHODE getOrCreateProfil
            int profilId = getOrCreateProfil(cbType.getValue(), cbStatut.getValue());

            // Créer l'objet Profil avec l'ID obtenu
            Profil profil = new Profil();
            profil.setId(profilId);
            profil.setType(cbType.getValue());
            profil.setStatut(cbStatut.getValue());

            if (userToEdit == null) {
                // Ajout d'un nouveau membre
                User user = new User(
                        txtNom.getText(),
                        txtPrenom.getText(),
                        txtEmail.getText(),
                        txtPassword.getText(),
                        txtTelephone.getText(),
                        txtAdresse.getText()
                );
                user.setProfil(profil);

                serviceUser.ajouter(user);
                showMessage("✅ Membre ajouté avec succès! (Profil ID: " + profilId + ")", "success");
            } else {
                // Modification d'un membre existant
                userToEdit.setNom(txtNom.getText());
                userToEdit.setPrenom(txtPrenom.getText());
                userToEdit.setEmail(txtEmail.getText());
                userToEdit.setPassword(txtPassword.getText());
                userToEdit.setTelephone(txtTelephone.getText());
                userToEdit.setAddresse(txtAdresse.getText());
                userToEdit.setProfil(profil);

                serviceUser.modifier(userToEdit);
                showMessage("✏️ Membre modifié avec succès! (Profil ID: " + profilId + ")", "success");
            }

            // Retour à la liste après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::goBack);
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
        if (txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || txtPassword.getText().isEmpty() ||
                txtTelephone.getText().isEmpty() || txtAdresse.getText().isEmpty() ||
                cbType.getValue() == null || cbStatut.getValue() == null) {

            showMessage("❌ Tous les champs sont obligatoires!", "error");
            return false;
        }
        return true;
    }

    @FXML
    private void resetForm() {
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        cbType.setValue(null);
        cbStatut.setValue(null);
        lblMessage.setText("");
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MemberList.fxml"));
            Parent root = loader.load();

            if (previousController != null) {
                MemberListController controller = loader.getController();
                controller.refreshList();
            }

            Stage stage = (Stage) txtNom.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des membres");

        } catch (IOException e) {
            e.printStackTrace();
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