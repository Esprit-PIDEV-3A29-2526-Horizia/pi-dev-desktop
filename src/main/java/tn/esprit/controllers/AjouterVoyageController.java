package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.entites.Categorie;
import tn.esprit.entites.Voyage;
import tn.esprit.services.CategorieService;
import tn.esprit.services.VoyageService;

import java.io.File;
import java.sql.Date;
import java.util.List;

public class AjouterVoyageController {

    @FXML private TextField tfDestination,tfTitre, tfPrix, tfPlacesTotal, tfPlacesRestantes;
    @FXML private DatePicker dpDepart, dpRetour;
    @FXML private ImageView imgPreview;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private Button btnEnregistrer; // Pour changer le texte (Ajouter/Modifier)

    private final VoyageService vs = new VoyageService();
    private final CategorieService cs = new CategorieService();
    private String imagePath = "";

    // --- VARIABLE POUR LA MODIFICATION ---
    private int idVoyageAModifier = -1; // -1 = Ajout, sinon = Modification

    @FXML
    public void initialize() {
        chargerCategories();
    }

    /**
     * Cette méthode remplit les champs lors d'une modification
     * Elle corrige l'erreur "Cannot resolve method prepareModif"
     */
    public void prepareModif(Voyage v) {
        this.idVoyageAModifier = v.getId(); // On stocke l'ID original
        tfTitre.setText(v.getDestination());
        tfDestination.setText(v.getDestination());
        tfPrix.setText(String.valueOf(v.getPrix()));
        taDescription.setText(v.getDescription());

        // Conversion des dates SQL en LocalDate pour le DatePicker
        dpDepart.setValue(v.getDate_depart().toLocalDate());
        dpRetour.setValue(v.getDate_retour().toLocalDate());

        tfPlacesTotal.setText(String.valueOf(v.getPlaces_total()));
        tfPlacesRestantes.setText(String.valueOf(v.getPlaces_restantes()));

        // Prévisualisation de l'image
        if (v.getImage_url() != null && !v.getImage_url().isEmpty()) {
            imagePath = v.getImage_url();
            imgPreview.setImage(new Image(imagePath));
        }

        // Sélectionner la bonne catégorie dans le ComboBox
        for (Categorie c : cbCategorie.getItems()) {
            if (c.getId() == v.getId_categorie()) {
                cbCategorie.setValue(c);
                break;
            }
        }

        if(btnEnregistrer != null) btnEnregistrer.setText("Mettre à jour");
    }

    @FXML
    void handleVider() {
        // Vérifiez que tous ces fx:id correspondent bien à vos déclarations @FXML en haut de la classe
        if (tfDestination != null) tfDestination.clear();
        if (tfPrix != null) tfPrix.clear();
        if (tfPlacesTotal != null) tfPlacesTotal.clear();
        if (tfPlacesRestantes != null) tfPlacesRestantes.clear();
        if (dpDepart != null) dpDepart.setValue(null);
        if (dpRetour != null) dpRetour.setValue(null);
        if (cbCategorie != null) cbCategorie.setValue(null);
        if (imgPreview != null) imgPreview.setImage(null);
        if (taDescription != null) taDescription.setText("");
    }
    private void chargerCategories() {
        List<Categorie> liste = cs.afficher();
        cbCategorie.setItems(FXCollections.observableArrayList(liste));
        cbCategorie.setConverter(new StringConverter<Categorie>() {
            @Override
            public String toString(Categorie object) { return (object != null) ? object.getNom() : ""; }
            @Override
            public Categorie fromString(String string) { return null; }
        });
    }

    @FXML
    void enregistrer() {
        try {
            // Validations de base
            Categorie selectedCat = cbCategorie.getValue();
            if (selectedCat == null || dpDepart.getValue() == null || dpRetour.getValue() == null) {
                afficherAlerte("Champs Manquants", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            Date dateD = Date.valueOf(dpDepart.getValue());
            Date dateR = Date.valueOf(dpRetour.getValue());
            double prix = Double.parseDouble(tfPrix.getText());
            int pTotal = Integer.parseInt(tfPlacesTotal.getText());
            int pRestantes = Integer.parseInt(tfPlacesRestantes.getText());

            // Validation de la cohérence des places
            if (pRestantes > pTotal) {
                afficherAlerte("Incohérence", "Les places restantes ne peuvent pas dépasser le total.");
                return;
            }

            // Création de l'objet Voyage
            Voyage v = new Voyage(
                    tfDestination.getText(),
                    taDescription.getText(),
                    prix,
                    dateD,
                    dateR,
                    imagePath,
                    selectedCat.getId(),
                    pTotal,
                    pRestantes
            );

            // --- LOGIQUE AJOUT vs MODIFICATION ---
            if (idVoyageAModifier == -1) {
                vs.ajouter(v);
                afficherAlerteSucces("Succès", "Voyage ajouté avec succès !");
            } else {
                v.setId(idVoyageAModifier); // On garde l'ID existant pour le UPDATE
                vs.modifier(v); // Appelle ta méthode UPDATE SQL
                afficherAlerteSucces("Succès", "Voyage mis à jour !");
            }

            annuler();

        } catch (NumberFormatException e) {
            afficherAlerte("Erreur", "Le prix et les places doivent être des nombres.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afficherAlerteSucces(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setTitle(titre);
        a.showAndWait();
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            imagePath = selectedFile.toURI().toString();
            imgPreview.setImage(new Image(imagePath));
        }
    }

    @FXML
    void annuler() {
        Stage stage = (Stage) tfDestination.getScene().getWindow();
        stage.close();
    }
}