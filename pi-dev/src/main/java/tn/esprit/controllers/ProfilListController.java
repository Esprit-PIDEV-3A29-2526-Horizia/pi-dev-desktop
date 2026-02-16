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
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.Profil;
import tn.esprit.services.ServiceProfil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Optional;

public class ProfilListController {

    @FXML private TableView<Profil> tableProfils;
    @FXML private TableColumn<Profil, String> colType;
    @FXML private TableColumn<Profil, String> colStatut;
    @FXML private TableColumn<Profil, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private ComboBox<String> filterStatutCombo;

    @FXML private ComboBox<String> sortByCombo;
    @FXML private ToggleGroup sortOrderGroup;
    @FXML private RadioButton rbAscending;
    @FXML private RadioButton rbDescending;

    @FXML private Label totalProfilsLabel;
    @FXML private Label lblMessage;

    private ServiceProfil serviceProfil = new ServiceProfil();
    private ObservableList<Profil> masterData = FXCollections.observableArrayList();
    private FilteredList<Profil> filteredData;
    private SortedList<Profil> sortedData;

    private AdminDashboardController dashboardController;

    @FXML
    public void initialize() {
        System.out.println("=== Initialisation ProfilListController ===");

        setupTableColumns();
        setupFilters();
        setupSortOptions();
        loadProfils();
        setupSearch();
    }

    private void setupTableColumns() {
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colType.setStyle("-fx-alignment: CENTER;");
        colStatut.setStyle("-fx-alignment: CENTER;");

        // Styliser les cellules
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
                            setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-font-size: 14px;");
                            break;
                        case "AGENT":
                            setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-font-size: 14px;");
                            break;
                        case "CLIENT":
                            setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-font-size: 14px;");
                            break;
                        default:
                            setStyle("-fx-alignment: CENTER; -fx-font-size: 14px;");
                    }
                }
            }
        });

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
                        setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-font-size: 14px;");
                    } else if ("BLOQUE".equals(item)) {
                        setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-font-size: 14px;");
                    } else if ("INACTIF".equals(item)) {
                        setStyle("-fx-text-fill: #6B7280; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-font-size: 14px;");
                    } else {
                        setStyle("-fx-alignment: CENTER; -fx-font-size: 14px;");
                    }
                }
            }
        });

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("✏️ Modifier");
            private final Button btnSupprimer = new Button("🗑️ Supprimer");
            private final HBox pane = new HBox(10, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
                btnSupprimer.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");

                btnModifier.setTooltip(new Tooltip("Modifier ce profil"));
                btnSupprimer.setTooltip(new Tooltip("Supprimer ce profil"));

                btnModifier.setOnAction(event -> {
                    Profil profil = getTableView().getItems().get(getIndex());
                    openEditForm(profil);
                });

                btnSupprimer.setOnAction(event -> {
                    Profil profil = getTableView().getItems().get(getIndex());
                    deleteProfil(profil);
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

    private void setupFilters() {
        filterTypeCombo.getItems().addAll("Tous", "ADMIN", "AGENT", "CLIENT");
        filterStatutCombo.getItems().addAll("Tous", "ACTIF", "INACTIF", "BLOQUE");

        filterTypeCombo.setValue("Tous");
        filterStatutCombo.setValue("Tous");

        filterTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterStatutCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupSortOptions() {
        sortByCombo.getItems().addAll("Type", "Statut");
        sortByCombo.setValue("Type");

        rbAscending.setSelected(true);

        sortByCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySort());
        sortOrderGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> applySort());
    }

    private void applySort() {
        if (sortedData == null) return;

        String sortBy = sortByCombo.getValue();
        boolean ascending = rbAscending.isSelected();

        Comparator<Profil> comparator = null;

        switch (sortBy) {
            case "Type":
                comparator = Comparator.comparing(Profil::getType, Comparator.nullsLast(String::compareTo));
                break;
            case "Statut":
                comparator = Comparator.comparing(Profil::getStatut, Comparator.nullsLast(String::compareTo));
                break;
            default:
                comparator = Comparator.comparing(Profil::getType);
        }

        if (comparator != null) {
            if (!ascending) {
                comparator = comparator.reversed();
            }
            sortedData.setComparator(comparator);
            tableProfils.refresh();
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        if (filteredData == null) return;

        String searchText = searchField.getText().toLowerCase();
        String typeFilter = filterTypeCombo.getValue();
        String statutFilter = filterStatutCombo.getValue();

        filteredData.setPredicate(profil -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    profil.getType().toLowerCase().contains(searchText) ||
                    profil.getStatut().toLowerCase().contains(searchText);

            boolean matchesType = typeFilter.equals("Tous") || profil.getType().equals(typeFilter);
            boolean matchesStatut = statutFilter.equals("Tous") || profil.getStatut().equals(statutFilter);

            return matchesSearch && matchesType && matchesStatut;
        });

        updateStats();
    }

    private void loadProfils() {
        try {
            masterData.clear();
            masterData.addAll(serviceProfil.afficher());

            filteredData = new FilteredList<>(masterData, p -> true);
            sortedData = new SortedList<>(filteredData);

            sortedData.setComparator(Comparator.comparing(Profil::getType));

            tableProfils.setItems(sortedData);
            updateStats();

            System.out.println("✅ " + masterData.size() + " profils chargés");

        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Erreur de chargement des profils", "error");
        }
    }

    private void updateStats() {
        if (filteredData != null) {
            int total = masterData.size();
            int filtered = filteredData.size();

            if (filtered == total) {
                totalProfilsLabel.setText(String.valueOf(total));
            } else {
                totalProfilsLabel.setText(filtered + " / " + total);
            }

            long admins = filteredData.stream().filter(p -> "ADMIN".equals(p.getType())).count();
            long agents = filteredData.stream().filter(p -> "AGENT".equals(p.getType())).count();
            long clients = filteredData.stream().filter(p -> "CLIENT".equals(p.getType())).count();

            System.out.println("📊 Filtre: " + filtered + " profils | Admins: " + admins + " | Agents: " + agents + " | Clients: " + clients);
        } else {
            totalProfilsLabel.setText(String.valueOf(masterData.size()));
        }
    }

    public void setDashboardController(AdminDashboardController controller) {
        this.dashboardController = controller;
        System.out.println("✅ DashboardController passé à ProfilListController");
    }

    @FXML
    private void handleAddProfil() {
        System.out.println("=== handleAddProfil appelé ===");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddProfil.fxml"));
            Parent root = loader.load();

            AddProfilController controller = loader.getController();
            controller.setDashboardController(dashboardController);

            if (dashboardController != null) {
                // Mode DASHBOARD
                dashboardController.setContent(root);
            } else {
                // Mode FENÊTRE
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Ajouter un profil");
                stage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Erreur ouverture formulaire", "error");
        }
    }

    private void openEditForm(Profil profil) {
        System.out.println("=== openEditForm appelé pour: " + profil.getType() + " ===");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddProfil.fxml"));
            Parent root = loader.load();

            AddProfilController controller = loader.getController();
            controller.setDashboardController(dashboardController);
            controller.setProfilToEdit(profil);

            if (dashboardController != null) {
                // Mode DASHBOARD
                dashboardController.setContent(root);
            } else {
                // Mode FENÊTRE
                Stage stage = new Stage();
                stage.setTitle("Modifier profil - " + profil.getType());
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
                refreshList(); // Rafraîchir après fermeture
            }

        } catch (IOException e) {
            showMessage("Erreur ouverture formulaire", "error");
            e.printStackTrace();
        }
    }

    private void deleteProfil(Profil profil) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le profil " + profil.getType() + " ?");
        alert.setContentText("Cette action est irréversible. Les utilisateurs avec ce profil seront affectés.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceProfil.supprimer(profil.getId());
                showMessage("✅ Profil supprimé avec succès", "success");
                refreshList();
            } catch (SQLException e) {
                e.printStackTrace();
                showMessage("❌ Erreur: " + e.getMessage(), "error");
            }
        }
    }

    public void refreshList() {
        loadProfils();
        searchField.clear();
        filterTypeCombo.setValue("Tous");
        filterStatutCombo.setValue("Tous");
        sortByCombo.setValue("Type");
        rbAscending.setSelected(true);
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        if ("error".equals(type)) {
            lblMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        } else {
            lblMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
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