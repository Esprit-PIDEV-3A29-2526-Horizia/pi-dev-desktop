package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entites.Categorie;
import tn.esprit.services.CategorieService;

import java.io.IOException;
import java.util.Optional;

public class GestionCategorieController {

    @FXML private ListView<Categorie> listCategories;
    @FXML private TextField tfNom, tfRecherche;
    @FXML private TextArea taDescription;

    private final CategorieService cs = new CategorieService();
    private final ObservableList<Categorie> data = FXCollections.observableArrayList();
    private Categorie categorieSelectionnee;

    @FXML
    public void initialize() {
        listCategories.setCellFactory(lv -> new ListCell<Categorie>() {
            @Override
            protected void updateItem(Categorie item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox card = new VBox(5);
                    card.setPadding(new javafx.geometry.Insets(15));
                    card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #DACEB6; -fx-border-radius: 10;");

                    Label nameLabel = new Label(item.getNom());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #23779C;");

                    Label descLabel = new Label(item.getDescription());
                    descLabel.setStyle("-fx-text-fill: #555;");
                    descLabel.setWrapText(true);

                    card.getChildren().addAll(nameLabel, descLabel);
                    setGraphic(card);

                    selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                        if (isNowSelected) {
                            card.setStyle("-fx-background-color: #3D94CA; -fx-background-radius: 10;");
                            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
                            descLabel.setStyle("-fx-text-fill: white;");
                        } else {
                            card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #DACEB6; -fx-border-radius: 10;");
                            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #23779C;");
                            descLabel.setStyle("-fx-text-fill: #555;");
                        }
                    });
                }
            }
        });

        chargerDonnees();

        listCategories.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                categorieSelectionnee = newSelection;
                tfNom.setText(newSelection.getNom());
                taDescription.setText(newSelection.getDescription());
            }
        });

        setupRecherche();
    }

    private void chargerDonnees() {
        data.setAll(cs.afficher());
        listCategories.setItems(data);
    }

    private void setupRecherche() {
        FilteredList<Categorie> filteredData = new FilteredList<>(data, p -> true);
        tfRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(categorie -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return categorie.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        categorie.getDescription().toLowerCase().contains(lowerCaseFilter);
            });
        });
        listCategories.setItems(filteredData);
    }

    // --- NAVIGATION ---
    @FXML void naviguerVoyages(ActionEvent event) { changerScene("/GestionVoyage.fxml", event); }
    @FXML void naviguerCategories(ActionEvent event) { changerScene("/GestionCategorie.fxml", event); }
    @FXML void naviguerReservations(ActionEvent event) { changerScene("/GestionReservation.fxml", event); }

    private void changerScene(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Erreur de chargement FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- CRUD ---
    @FXML
    void handleEnregistrer() {
        if (tfNom.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Le nom est obligatoire.").show();
            return;
        }
        if (categorieSelectionnee == null) {
            cs.ajouter(new Categorie(tfNom.getText(), taDescription.getText()));
        } else {
            categorieSelectionnee.setNom(tfNom.getText());
            categorieSelectionnee.setDescription(taDescription.getText());
            cs.modifier(categorieSelectionnee);
        }
        chargerDonnees();
        handleViderFormulaire();
    }

    @FXML
    void handleSupprimer() {
        if (categorieSelectionnee != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous supprimer : " + categorieSelectionnee.getNom() + " ?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    cs.supprimer(categorieSelectionnee.getId());
                    chargerDonnees();
                    handleViderFormulaire();
                }
            });
        }
    }

    @FXML
    void handleViderFormulaire() {
        categorieSelectionnee = null;
        tfNom.clear();
        taDescription.clear();
        listCategories.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        changerScene("/Login.fxml", event);
    }

}