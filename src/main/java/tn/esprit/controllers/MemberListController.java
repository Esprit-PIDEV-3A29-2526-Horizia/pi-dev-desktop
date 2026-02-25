package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.Serviceuser;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MemberListController {

    @FXML private TableView<User> tableUsers;
    // La colonne ID a été supprimée
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colPrenom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colTelephone;
    @FXML private TableColumn<User, String> colType;
    @FXML private TableColumn<User, String> colStatut;
    @FXML private TableColumn<User, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private ComboBox<String> filterStatutCombo;
    @FXML private ComboBox<String> sortByCombo;
    @FXML private RadioButton rbAscending;
    @FXML private RadioButton rbDescending;
    @FXML private ToggleGroup sortOrderGroup;
    @FXML private Button applySortBtn;
    @FXML private Label totalMembresLabel;
    @FXML private Label lblMessage;
    @FXML private Button btnAdd;

    private final Serviceuser serviceUser = new Serviceuser();
    private ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;
    private SortedList<User> sortedData;

    private AdminDashboardController dashboardController;

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation MemberListController ===");

        serviceUser.testConnexion();

        setupTableColumns();
        setupActionsColumn();
        setupFilterControls();
        setupSortingControls();
        loadUsers();
        setupDynamicSearch();
    }

    private void setupTableColumns() {
        System.out.println("Configuration des colonnes...");

        // L'ID n'est plus affiché mais on peut toujours l'utiliser en interne
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        colType.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getType();
            return new javafx.beans.property.SimpleStringProperty(type != null ? type : "");
        });

        colStatut.setCellValueFactory(cellData -> {
            String statut = cellData.getValue().getStatut();
            return new javafx.beans.property.SimpleStringProperty(statut != null ? statut : "");
        });

        // Styliser la colonne type
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

    private void setupFilterControls() {
        // Initialiser les filtres
        filterTypeCombo.setItems(FXCollections.observableArrayList(
                "Tous", "ADMIN", "AGENT", "CLIENT"
        ));
        filterTypeCombo.setValue("Tous");

        filterStatutCombo.setItems(FXCollections.observableArrayList(
                "Tous", "ACTIF", "BLOQUE"
        ));
        filterStatutCombo.setValue("Tous");

        // Ajouter les écouteurs pour les filtres
        filterTypeCombo.setOnAction(event -> applyFilters());
        filterStatutCombo.setOnAction(event -> applyFilters());
    }

    private void setupSortingControls() {
        // Initialiser les options de tri (sans l'ID)
        sortByCombo.setItems(FXCollections.observableArrayList(
                "Nom", "Prénom", "Email", "Téléphone", "Type", "Statut"
        ));
        sortByCombo.setValue("Nom");

        // Action du bouton Appliquer
        applySortBtn.setOnAction(event -> applySort());

        // Optionnel : tri automatique
        sortByCombo.setOnAction(event -> applySort());
        sortOrderGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> applySort());
    }

    private void applyFilters() {
        if (filteredData == null) return;

        String selectedType = filterTypeCombo.getValue();
        String selectedStatut = filterStatutCombo.getValue();

        filteredData.setPredicate(user -> {
            // Filtre par texte (recherche)
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                if (!searchInUser(user, searchText.toLowerCase())) {
                    return false;
                }
            }

            // Filtre par type
            if (selectedType != null && !"Tous".equals(selectedType)) {
                if (!selectedType.equals(user.getType())) {
                    return false;
                }
            }

            // Filtre par statut
            if (selectedStatut != null && !"Tous".equals(selectedStatut)) {
                if (!selectedStatut.equals(user.getStatut())) {
                    return false;
                }
            }

            return true;
        });

        updateStats();
        applySort(); // Réappliquer le tri après filtrage
    }

    private void applySort() {
        if (sortedData == null || filteredData == null) return;

        String selectedField = sortByCombo.getValue();
        if (selectedField == null) return;

        Comparator<User> comparator = null;

        switch (selectedField) {
            case "Nom":
                comparator = Comparator.comparing(User::getNom, Comparator.nullsLast(String::compareTo));
                break;
            case "Prénom":
                comparator = Comparator.comparing(User::getPrenom, Comparator.nullsLast(String::compareTo));
                break;
            case "Email":
                comparator = Comparator.comparing(User::getEmail, Comparator.nullsLast(String::compareTo));
                break;
            case "Téléphone":
                comparator = Comparator.comparing(User::getTelephone, Comparator.nullsLast(String::compareTo));
                break;
            case "Type":
                comparator = Comparator.comparing(u -> u.getType() != null ? u.getType() : "", String::compareTo);
                break;
            case "Statut":
                comparator = Comparator.comparing(u -> u.getStatut() != null ? u.getStatut() : "", String::compareTo);
                break;
        }

        if (comparator != null) {
            // Appliquer l'ordre (croissant/décroissant)
            if (rbDescending.isSelected()) {
                comparator = comparator.reversed();
            }

            sortedData.setComparator(comparator);
            tableUsers.refresh();
        }
    }

    private void setupDynamicSearch() {
        filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(); // Les filtres incluent maintenant la recherche
        });

        sortedData = new SortedList<>(filteredData);
        tableUsers.setItems(sortedData);
    }

    private boolean searchInUser(User user, String keyword) {
        if (user.getNom() != null && user.getNom().toLowerCase().contains(keyword)) return true;
        if (user.getPrenom() != null && user.getPrenom().toLowerCase().contains(keyword)) return true;
        if (user.getEmail() != null && user.getEmail().toLowerCase().contains(keyword)) return true;
        if (user.getTelephone() != null && user.getTelephone().toLowerCase().contains(keyword)) return true;
        if (user.getType() != null && user.getType().toLowerCase().contains(keyword)) return true;
        if (user.getStatut() != null && user.getStatut().toLowerCase().contains(keyword)) return true;
        // L'ID est toujours recherchable mais pas affiché
        if (String.valueOf(user.getId()).contains(keyword)) return true;
        return false;
    }

    private void loadUsers() {
        try {
            masterData.clear();
            List<User> users = serviceUser.afficher();

            if (users != null && !users.isEmpty()) {
                masterData.addAll(users);
            }

            updateStats();
            applyFilters(); // Appliquer les filtres après chargement
            applySort(); // Appliquer le tri après chargement

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Erreur de chargement: " + e.getMessage(), "error");
        }
    }

    private void updateStats() {
        if (filteredData != null) {
            int filteredCount = filteredData.size();
            int totalCount = masterData.size();

            if (filteredCount == totalCount) {
                totalMembresLabel.setText(String.valueOf(totalCount));
            } else {
                totalMembresLabel.setText(filteredCount + " / " + totalCount);
            }
        } else {
            totalMembresLabel.setText(String.valueOf(masterData.size()));
        }
    }

    @FXML
    private void openAddForm() {
        try {
            if (dashboardController != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddMember.fxml"));
                Parent root = loader.load();

                AddMemberController controller = loader.getController();
                controller.setDashboardController(dashboardController);
                dashboardController.setContent(root);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddMember.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Ajouter un membre");
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Erreur d'ouverture du formulaire", "error");
        }
    }

    private void openEditForm(User user) {
        try {
            if (dashboardController != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditMember.fxml"));
                Parent root = loader.load();

                EditMemberController controller = loader.getController();
                controller.setDashboardController(dashboardController);
                controller.setUserToEdit(user);
                dashboardController.setContent(root);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditMember.fxml"));
                Parent root = loader.load();

                EditMemberController controller = loader.getController();
                controller.setUserToEdit(user);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Modifier " + user.getNom() + " " + user.getPrenom());
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Erreur d'ouverture du formulaire", "error");
        }
    }

    private void deleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer " + user.getNom() + " " + user.getPrenom() + " ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceUser.supprimer(user.getId()); // On utilise l'ID en interne
                showMessage("✅ Membre supprimé avec succès", "success");
                loadUsers();
            } catch (SQLException e) {
                e.printStackTrace();
                showMessage("❌ Erreur: " + e.getMessage(), "error");
            }
        }
    }

    public void refreshList() {
        loadUsers();
        searchField.clear();
        filterTypeCombo.setValue("Tous");
        filterStatutCombo.setValue("Tous");
        sortByCombo.setValue("Nom");
        rbAscending.setSelected(true);
    }

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
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

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() ->
                        lblMessage.setText("")
                );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}