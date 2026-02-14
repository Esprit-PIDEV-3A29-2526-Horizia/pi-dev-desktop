package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceUser;
import java.util.stream.Collectors;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MemberListController {

    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colPrenom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colTelephone;
    @FXML private TableColumn<User, String> colType;      // Viendra du profil
    @FXML private TableColumn<User, String> colStatut;    // Viendra du profil
    @FXML private TableColumn<User, Void> colActions;

    @FXML private TextField searchField;
    @FXML private Label totalMembresLabel;
    @FXML private Label lblMessage;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ObservableList<User> usersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation MemberListController ===");

        // Tester la connexion d'abord
        serviceUser.testConnexion();

        // Configurer les colonnes
        setupTableColumns();
        setupActionsColumn();

        // Charger les données
        loadUsers();
    }

    private void setupTableColumns() {
        System.out.println("Configuration des colonnes...");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        // Pour type et statut, on utilise une cellule personnalisée
        colType.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getType();
            return new javafx.beans.property.SimpleStringProperty(type != null ? type : "");
        });

        colStatut.setCellValueFactory(cellData -> {
            String statut = cellData.getValue().getStatut();
            return new javafx.beans.property.SimpleStringProperty(statut != null ? statut : "");
        });

        // Styliser la colonne type avec des couleurs
        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "ADMIN":
                            setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                            break;
                        case "AGENT":
                            setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                            break;
                        case "CLIENT":
                            setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #6B7280;");
                    }
                }
            }
        });

        // Styliser la colonne statut
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("ACTIF".equals(item)) {
                        setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                    } else if ("BLOQUE".equals(item)) {
                        setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #6B7280;");
                    }
                }
            }
        });

        // Centrer certaines colonnes
        colId.setStyle("-fx-alignment: CENTER;");
        colType.setStyle("-fx-alignment: CENTER;");
        colStatut.setStyle("-fx-alignment: CENTER;");

        System.out.println("✅ Colonnes configurées");
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("✏️ Modifier");
            private final Button btnSupprimer = new Button("🗑️ Supprimer");
            private final HBox pane = new HBox(10, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");
                btnSupprimer.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3; -fx-cursor: hand;");

                btnModifier.setTooltip(new Tooltip("Modifier ce membre"));
                btnSupprimer.setTooltip(new Tooltip("Supprimer ce membre"));

                btnModifier.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    openEditForm(user);
                });

                btnSupprimer.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        colActions.setPrefWidth(200);
    }

    private void loadUsers() {
        System.out.println("=== Chargement des utilisateurs ===");

        try {
            usersList.clear();

            List<User> users = serviceUser.afficher();

            if (users == null || users.isEmpty()) {
                System.out.println("⚠️ Aucun utilisateur trouvé");
                showMessage("Aucun membre trouvé", "info");
            } else {
                System.out.println("📊 " + users.size() + " utilisateur(s) trouvé(s)");

                // Afficher les 3 premiers pour debug
                for (int i = 0; i < Math.min(3, users.size()); i++) {
                    User u = users.get(i);
                    System.out.println("  " + (i+1) + ". " + u.getNom() + " " + u.getPrenom() +
                            " | Type: " + u.getType() +
                            " | Statut: " + u.getStatut());
                }

                usersList.addAll(users);
            }

            tableUsers.setItems(usersList);
            tableUsers.refresh();
            updateStats();

            System.out.println("✅ Table mise à jour avec " + usersList.size() + " éléments");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement:");
            e.printStackTrace();
            showMessage("Erreur de chargement: " + e.getMessage(), "error");
        }
    }

    private void updateStats() {
        int total = usersList.size();
        totalMembresLabel.setText(String.valueOf(total));

        // Compter par type
        long admins = usersList.stream().filter(u -> "ADMIN".equals(u.getType())).count();
        long agents = usersList.stream().filter(u -> "AGENT".equals(u.getType())).count();
        long clients = usersList.stream().filter(u -> "CLIENT".equals(u.getType())).count();

        // Compter par statut
        long actifs = usersList.stream().filter(u -> "ACTIF".equals(u.getStatut())).count();
        long bloques = usersList.stream().filter(u -> "BLOQUE".equals(u.getStatut())).count();

        System.out.println("📊 Stats: Total=" + total +
                " | Admins=" + admins +
                " | Agents=" + agents +
                " | Clients=" + clients +
                " | Actifs=" + actifs +
                " | Bloqués=" + bloques);
    }

    @FXML
    private void searchMembers() {
        String searchTerm = searchField.getText().toLowerCase().trim();

        if (searchTerm.isEmpty()) {
            tableUsers.setItems(usersList);
            totalMembresLabel.setText(String.valueOf(usersList.size()));
            return;
        }

        ObservableList<User> filteredList = FXCollections.observableArrayList();
        for (User user : usersList) {
            if (user.getNom().toLowerCase().contains(searchTerm) ||
                    user.getPrenom().toLowerCase().contains(searchTerm) ||
                    user.getEmail().toLowerCase().contains(searchTerm) ||
                    (user.getType() != null && user.getType().toLowerCase().contains(searchTerm))) {
                filteredList.add(user);
            }
        }

        tableUsers.setItems(filteredList);
        totalMembresLabel.setText(filteredList.size() + " / " + usersList.size());
        System.out.println("🔍 Recherche '" + searchTerm + "': " + filteredList.size() + " résultat(s)");
    }

    @FXML
    private void openAddForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddMember.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tableUsers.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un membre");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Erreur d'ouverture du formulaire", "error");
        }
    }

    private void openEditForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddMember.fxml"));
            Parent root = loader.load();

            AddMemberController controller = loader.getController();
            controller.setUserToEdit(user);
            controller.setPreviousController(this);

            Stage stage = (Stage) tableUsers.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier " + user.getNom() + " " + user.getPrenom());
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Erreur d'ouverture du formulaire", "error");
        }
    }

    private void deleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer " + user.getNom() + " " + user.getPrenom() + " ?");
        alert.setContentText("Cette action supprimera aussi son profil associé.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("🗑️ Tentative de suppression de l'utilisateur ID: " + user.getId());

            try {
                serviceUser.supprimer(user.getId());
                System.out.println("✅ Suppression réussie en base");

                loadUsers();
                System.out.println("✅ Liste rechargée");

                showMessage("✅ Membre supprimé avec succès", "success");

            } catch (SQLException e) {
                System.err.println("❌ Échec de la suppression:");
                e.printStackTrace();
                showMessage("❌ Erreur: " + e.getMessage(), "error");
            }
        }
    }
    public void refreshList() {
        loadUsers();
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        switch (type) {
            case "error":
                lblMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                break;
            case "success":
                lblMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                break;
            default:
                lblMessage.setStyle("-fx-text-fill: #3B82F6; -fx-font-weight: bold;");
                break;
        }
    }

    public void filterByProfil(int profilId) {
        try {
            System.out.println("🔍 Filtrage par profil ID: " + profilId);

            // Récupérer tous les utilisateurs
            List<User> allUsers = serviceUser.afficher();

            // Utiliser Stream pour filtrer
            List<User> filteredUsers = allUsers.stream()
                    .filter(user -> user.getProfil() != null)  // Éviter NullPointerException
                    .filter(user -> user.getProfil().getId() == profilId)
                    .collect(Collectors.toList());

            // Optionnel: Récupérer le type du premier élément pour affichage
            String profilType = filteredUsers.stream()
                    .findFirst()
                    .map(User::getType)
                    .orElse("Inconnu");

            // Mettre à jour la liste observable
            usersList.clear();
            usersList.addAll(filteredUsers);

            // Mettre à jour le tableau
            tableUsers.setItems(usersList);
            tableUsers.refresh();

            // Mettre à jour les stats
            updateStats();

            // Afficher un message
            long count = filteredUsers.size();
            if (count > 0) {
                showMessage(String.format("✅ %d membre(s) de type %s", count, profilType), "success");
            } else {
                showMessage("⚠️ Aucun membre trouvé pour ce profil", "info");
            }

            System.out.println("✅ " + count + " membre(s) trouvé(s)");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du filtrage:");
            e.printStackTrace();
            showMessage("Erreur de filtrage", "error");
        }
    }
}