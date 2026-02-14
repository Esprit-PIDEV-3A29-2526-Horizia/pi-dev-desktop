package tn.esprit.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.Profil;
import tn.esprit.services.ServiceProfil;
import tn.esprit.services.ServiceUser;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ProfilListController {

    @FXML private TableView<Profil> tableProfils;
    @FXML private TableColumn<Profil, Integer> colId;
    @FXML private TableColumn<Profil, String> colType;
    @FXML private TableColumn<Profil, String> colStatut;
    @FXML private TableColumn<Profil, Integer> colNbUsers;
    @FXML private TableColumn<Profil, Void> colActions;

    @FXML private TextField searchField;
    @FXML private Label totalProfilsLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalAgentsLabel;
    @FXML private Label totalClientsLabel;
    @FXML private Label lblMessage;

    private final ServiceProfil serviceProfil = new ServiceProfil();
    private final ServiceUser serviceUser = new ServiceUser();
    private final ObservableList<Profil> profilList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation ProfilListController ===");
        setupTableColumns();
        setupActionsColumn();
        loadProfils();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Colonne pour le nombre d'utilisateurs avec ce profil
        colNbUsers.setCellValueFactory(cellData -> {
            try {
                int count = serviceUser.compterParProfil(cellData.getValue().getId());
                return new SimpleIntegerProperty(count).asObject();
            } catch (SQLException e) {
                e.printStackTrace();
                return new SimpleIntegerProperty(0).asObject();
            }
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
                        setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colId.setStyle("-fx-alignment: CENTER;");
        colNbUsers.setStyle("-fx-alignment: CENTER;");
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("✏️");
            private final Button btnSupprimer = new Button("🗑️");
            private final Button btnVoirMembres = new Button("👥");
            private final HBox pane = new HBox(10, btnModifier, btnSupprimer, btnVoirMembres);

            {
                btnModifier.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
                btnSupprimer.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
                btnVoirMembres.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");

                btnModifier.setTooltip(new Tooltip("Modifier ce profil"));
                btnSupprimer.setTooltip(new Tooltip("Supprimer ce profil"));
                btnVoirMembres.setTooltip(new Tooltip("Voir les membres avec ce profil"));

                btnModifier.setOnAction(event -> {
                    Profil profil = getTableView().getItems().get(getIndex());
                    openEditForm(profil);
                });

                btnSupprimer.setOnAction(event -> {
                    Profil profil = getTableView().getItems().get(getIndex());
                    deleteProfil(profil);
                });

                btnVoirMembres.setOnAction(event -> {
                    Profil profil = getTableView().getItems().get(getIndex());
                    showMembersWithProfil(profil);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadProfils() {
        try {
            profilList.clear();
            profilList.addAll(serviceProfil.afficher());  // Utilise votre serviceProfil.afficher()
            tableProfils.setItems(profilList);
            updateStats();
            System.out.println("✅ " + profilList.size() + " profils chargés");
        } catch (SQLException e) {
            showMessage("Erreur chargement: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    private void updateStats() {
        int total = profilList.size();
        totalProfilsLabel.setText(String.valueOf(total));

        // Compter par type
        long admins = profilList.stream().filter(p -> "ADMIN".equals(p.getType())).count();
        long agents = profilList.stream().filter(p -> "AGENT".equals(p.getType())).count();
        long clients = profilList.stream().filter(p -> "CLIENT".equals(p.getType())).count();

        totalAdminsLabel.setText(String.valueOf(admins));
        totalAgentsLabel.setText(String.valueOf(agents));
        totalClientsLabel.setText(String.valueOf(clients));
    }

    @FXML
    private void handleSearch() {
        try {
            String searchTerm = searchField.getText().toLowerCase().trim();

            if (searchTerm.isEmpty()) {
                tableProfils.setItems(profilList);
                return;
            }

            // Utilise votre serviceProfil.rechercherParType() si disponible
            ObservableList<Profil> filteredList = FXCollections.observableArrayList();
            for (Profil profil : profilList) {
                if (profil.getType().toLowerCase().contains(searchTerm) ||
                        profil.getStatut().toLowerCase().contains(searchTerm)) {
                    filteredList.add(profil);
                }
            }
            tableProfils.setItems(filteredList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshList() {
        loadProfils();
        searchField.clear();
    }

    @FXML
    private void handleAddProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddProfil.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un profil");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Rafraîchir la liste après fermeture
            refreshList();

        } catch (IOException e) {
            showMessage("Erreur ouverture formulaire", "error");
            e.printStackTrace();
        }
    }

    private void openEditForm(Profil profil) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddProfil.fxml"));
            Parent root = loader.load();

            AddProfilController controller = loader.getController();
            controller.setProfilToEdit(profil);
            controller.setPreviousController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier profil - " + profil.getType());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshList();

        } catch (IOException e) {
            showMessage("Erreur ouverture formulaire", "error");
            e.printStackTrace();
        }
    }

    private void deleteProfil(Profil profil) {
        try {
            // Vérifier si des utilisateurs utilisent ce profil
            int nbUsers = serviceUser.compterParProfil(profil.getId());

            if (nbUsers > 0) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Ce profil est utilisé par " + nbUsers + " membre(s)");
                alert.setContentText("La suppression du profil entraînera la perte du profil pour ces membres. Continuer ?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    serviceProfil.supprimer(profil.getId());  // Utilise votre serviceProfil.supprimer()
                    refreshList();
                    showMessage("Profil supprimé avec succès", "success");
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Supprimer " + profil.getType() + " ?");
                alert.setContentText("Cette action est irréversible.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    serviceProfil.supprimer(profil.getId());  // Utilise votre serviceProfil.supprimer()
                    refreshList();
                    showMessage("Profil supprimé avec succès", "success");
                }
            }
        } catch (SQLException e) {
            showMessage("Erreur suppression: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    private void showMembersWithProfil(Profil profil) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MemberList.fxml"));
            Parent root = loader.load();

            MemberListController controller = loader.getController();
            controller.filterByProfil(profil.getId());

            Stage stage = new Stage();
            stage.setTitle("Membres - " + profil.getType());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            showMessage("Erreur ouverture liste membres", "error");
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