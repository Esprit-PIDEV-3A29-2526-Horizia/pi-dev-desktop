package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entites.Categorie;
import tn.esprit.entites.Voyage;
import tn.esprit.services.CategorieService;
import tn.esprit.services.VoyageService;

import java.sql.Date;
import java.util.Optional;

public class GestionVoyageController {

    // --- Champs de saisie ---
    @FXML private TextField tfDestination;
    @FXML private TextField tfPrix;
    @FXML private TextField tfImage;
    @FXML private TextArea taDescription;
    @FXML private DatePicker dpDepart;
    @FXML private DatePicker dpRetour;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private TextField tfRecherche;

    // --- TableView ---
    @FXML private TableView<Voyage> tableVoyages;
    @FXML private TableColumn<Voyage, String> colDest;
    @FXML private TableColumn<Voyage, Double> colPrix;
    @FXML private TableColumn<Voyage, Date> colDepart;
    @FXML private TableColumn<Voyage, Date> colRetour;
    @FXML private TableColumn<Voyage, Integer> colCat;

    // --- Services & Listes ---
    private final VoyageService vs = new VoyageService();
    private final CategorieService cs = new CategorieService();
    private final ObservableList<Voyage> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Charger les catégories dans la ComboBox
        cbCategorie.setItems(FXCollections.observableArrayList(cs.afficher()));

        // 2. Configurer les colonnes de la table
        colDest.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colDepart.setCellValueFactory(new PropertyValueFactory<>("dateDepart"));
        colRetour.setCellValueFactory(new PropertyValueFactory<>("dateRetour"));
        colCat.setCellValueFactory(new PropertyValueFactory<>("idCategorie"));

        // 3. Charger les données et configurer la recherche dynamique
        loadData();
        setupSearch();

        // 4. Ecouter la sélection dans la table pour remplir le formulaire (pour modif)
        tableVoyages.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                remplirFormulaire(newSelection);
            }
        });
    }

    private void loadData() {
        masterData.setAll(vs.afficher());
        tableVoyages.setItems(masterData);
    }

    private void setupSearch() {
        FilteredList<Voyage> filteredData = new FilteredList<>(masterData, p -> true);

        tfRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(voyage -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String filter = newValue.toLowerCase();

                if (voyage.getDestination().toLowerCase().contains(filter)) return true;
                if (String.valueOf(voyage.getPrix()).contains(filter)) return true;
                return false;
            });
        });

        SortedList<Voyage> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableVoyages.comparatorProperty());
        tableVoyages.setItems(sortedData);
    }

    @FXML
    void handleAjouter() {
        if (validerSaisie()) {
            Voyage v = new Voyage(
                    tfDestination.getText(),
                    taDescription.getText(),
                    Double.parseDouble(tfPrix.getText()),
                    Date.valueOf(dpDepart.getValue()),
                    Date.valueOf(dpRetour.getValue()),
                    tfImage.getText(),
                    cbCategorie.getValue().getId()
            );
            vs.ajouter(v);
            loadData();
            clearForm();
        }
    }

    @FXML
    void handleModifier() {
        Voyage selectionne = tableVoyages.getSelectionModel().getSelectedItem();
        if (selectionne != null && validerSaisie()) {
            selectionne.setDestination(tfDestination.getText());
            selectionne.setPrix(Double.parseDouble(tfPrix.getText()));
            selectionne.setDateDepart(Date.valueOf(dpDepart.getValue()));
            selectionne.setDateRetour(Date.valueOf(dpRetour.getValue()));
            selectionne.setIdCategorie(cbCategorie.getValue().getId());

            vs.modifier(selectionne);
            loadData();
            showAlert("Succès", "Voyage modifié avec succès !");
        } else {
            showAlert("Erreur", "Veuillez sélectionner un voyage à modifier.");
        }
    }

    @FXML
    void handleSupprimer() {
        Voyage selectionne = tableVoyages.getSelectionModel().getSelectedItem();
        if (selectionne != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Voulez-vous vraiment supprimer ce voyage ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                vs.supprimer(selectionne.getId());
                loadData();
                clearForm();
            }
        }
    }

    private void remplirFormulaire(Voyage v) {
        tfDestination.setText(v.getDestination());
        tfPrix.setText(String.valueOf(v.getPrix()));
        dpDepart.setValue(v.getDateDepart().toLocalDate());
        dpRetour.setValue(v.getDateRetour().toLocalDate());
        taDescription.setText(v.getDescription());
        // Sélectionner la bonne catégorie dans la ComboBox
        cbCategorie.getItems().stream()
                .filter(c -> c.getId() == v.getIdCategorie())
                .findFirst()
                .ifPresent(c -> cbCategorie.setValue(c));
    }

    private boolean validerSaisie() {
        if (tfDestination.getText().isEmpty() || tfPrix.getText().isEmpty() ||
                dpDepart.getValue() == null || cbCategorie.getValue() == null) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }
        try {
            Double.parseDouble(tfPrix.getText());
        } catch (NumberFormatException e) {
            showAlert("Format incorrect", "Le prix doit être un nombre valide.");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearForm() {
        tfDestination.clear();
        tfPrix.clear();
        taDescription.clear();
        dpDepart.setValue(null);
        dpRetour.setValue(null);
        cbCategorie.setValue(null);
    }
}