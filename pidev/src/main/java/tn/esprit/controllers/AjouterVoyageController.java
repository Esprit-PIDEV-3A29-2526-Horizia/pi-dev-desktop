package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import java.io.IOException;
import java.sql.Date;
import java.util.List;

public class AjouterVoyageController {

    @FXML private TextField tfTitre;
    @FXML private TextField tfDestination;
    @FXML private TextField tfPrix;
    @FXML private TextField tfPlacesTotal;
    @FXML private TextField tfPlacesRestantes;

    @FXML private DatePicker dpDepart;
    @FXML private DatePicker dpRetour;

    @FXML private ImageView imgPreview;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private Button btnEnregistrer;

    private final VoyageService vs = new VoyageService();
    private final CategorieService cs = new CategorieService();

    private String imagePath = "";
    private int idVoyageAModifier = -1;

    // Callback pour rafraîchir la liste après ajout/modification
    private Runnable onVoyageAjouteCallback;

    // Référence au contrôleur parent
    private GestionVoyageController parentController;

    @FXML
    public void initialize() {
        chargerCategories();

        dpDepart.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && dpRetour.getValue() != null && dpRetour.getValue().isBefore(newV)) {
                dpRetour.setValue(null);
            }
        });
    }

    /**
     * Définit le contrôleur parent
     */
    public void setParentController(GestionVoyageController controller) {
        this.parentController = controller;
    }

    /**
     * Définit le callback à appeler après l'ajout/modification d'un voyage
     */
    public void setOnVoyageAjouteCallback(Runnable callback) {
        this.onVoyageAjouteCallback = callback;
    }

    public void prepareModif(Voyage v) {
        if (v == null) return;
        idVoyageAModifier = v.getId();
        tfTitre.setText(nvl(v.getTitre()));
        tfDestination.setText(nvl(v.getDestination()));
        tfPrix.setText(String.valueOf(v.getPrix()));
        taDescription.setText(nvl(v.getDescription()));
        if (v.getDate_depart() != null) dpDepart.setValue(v.getDate_depart().toLocalDate());
        if (v.getDate_retour() != null) dpRetour.setValue(v.getDate_retour().toLocalDate());
        tfPlacesTotal.setText(String.valueOf(v.getPlaces_total()));
        tfPlacesRestantes.setText(String.valueOf(v.getPlaces_restantes()));
        if (v.getImage_url() != null && !v.getImage_url().isBlank()) {
            imagePath = v.getImage_url();
            try { imgPreview.setImage(new Image(imagePath, true)); }
            catch (Exception e) { imgPreview.setImage(null); }
        } else {
            imagePath = "";
            imgPreview.setImage(null);
        }
        for (Categorie c : cbCategorie.getItems()) {
            if (c.getId() == v.getId_categorie()) {
                cbCategorie.setValue(c);
                break;
            }
        }
        if (btnEnregistrer != null) btnEnregistrer.setText("Mettre à jour");
    }

    @FXML
    void enregistrer() {
        try {
            if (isBlank(tfTitre) || isBlank(tfDestination) || isBlank(tfPrix)
                    || isBlank(tfPlacesTotal)
                    || cbCategorie.getValue() == null
                    || dpDepart.getValue() == null || dpRetour.getValue() == null) {
                afficherAlerte("Champs manquants", "Veuillez remplir tous les champs obligatoires.");
                return;
            }
            if (dpRetour.getValue().isBefore(dpDepart.getValue())) {
                afficherAlerte("Dates invalides", "La date retour doit être après la date départ.");
                return;
            }
            double prix = Double.parseDouble(tfPrix.getText().trim());
            int pTotal = Integer.parseInt(tfPlacesTotal.getText().trim());
            if (prix <= 0) {
                afficherAlerte("Prix invalide", "Le prix doit être > 0.");
                return;
            }
            if (pTotal < 0) {
                afficherAlerte("Places invalides", "Le total doit être positif.");
                return;
            }
            int pRest;
            if (idVoyageAModifier == -1) {
                if (tfPlacesRestantes == null || tfPlacesRestantes.getText() == null || tfPlacesRestantes.getText().trim().isEmpty()) {
                    pRest = pTotal;
                } else {
                    pRest = Integer.parseInt(tfPlacesRestantes.getText().trim());
                }
            } else {
                if (isBlank(tfPlacesRestantes)) {
                    afficherAlerte("Champs manquants", "Veuillez saisir les places restantes (mode modification).");
                    return;
                }
                pRest = Integer.parseInt(tfPlacesRestantes.getText().trim());
            }
            if (pRest < 0) {
                afficherAlerte("Places invalides", "Les places restantes doivent être positives.");
                return;
            }
            if (pRest > pTotal) {
                afficherAlerte("Incohérence", "Les places restantes ne peuvent pas dépasser le total.");
                return;
            }
            Date dateD = Date.valueOf(dpDepart.getValue());
            Date dateR = Date.valueOf(dpRetour.getValue());
            Voyage v = new Voyage();
            v.setTitre(tfTitre.getText().trim());
            v.setDestination(tfDestination.getText().trim());
            v.setDescription(taDescription.getText() == null ? "" : taDescription.getText().trim());
            v.setPrix(prix);
            v.setDate_depart(dateD);
            v.setDate_retour(dateR);
            v.setImage_url(imagePath);
            v.setId_categorie(cbCategorie.getValue().getId());
            v.setPlaces_total(pTotal);
            v.setPlaces_restantes(pRest);

            if (idVoyageAModifier == -1) {
                vs.ajouter(v);
                afficherAlerteSucces("Succès", "Voyage ajouté avec succès !");
            } else {
                v.setId(idVoyageAModifier);
                vs.modifier(v);
                afficherAlerteSucces("Succès", "Voyage mis à jour !");
            }

            // Appeler le callback pour rafraîchir la liste
            if (onVoyageAjouteCallback != null) {
                onVoyageAjouteCallback.run();
            }

            // Retourner à la liste via le parentController (sans recharger tout le fichier)
            if (parentController != null) {
                parentController.retourALaListe();
            }

        } catch (NumberFormatException e) {
            afficherAlerte("Erreur", "Prix / places doivent être des nombres valides.");
        } catch (Exception e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    @FXML
    void handleUpload() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        if (tfDestination != null && tfDestination.getScene() != null && tfDestination.getScene().getWindow() != null) {
            Stage stage = (Stage) tfDestination.getScene().getWindow();
            File selected = fc.showOpenDialog(stage);
            if (selected != null) {
                imagePath = selected.toURI().toString();
                try {
                    imgPreview.setImage(new Image(imagePath, true));
                } catch (Exception e) {
                    imgPreview.setImage(null);
                    afficherAlerte("Image", "Impossible de charger cette image.");
                }
            }
        } else {
            File selected = fc.showOpenDialog(null);
            if (selected != null) {
                imagePath = selected.toURI().toString();
                try {
                    imgPreview.setImage(new Image(imagePath, true));
                } catch (Exception e) {
                    imgPreview.setImage(null);
                    afficherAlerte("Image", "Impossible de charger cette image.");
                }
            }
        }
    }

    @FXML
    void handleVider() {
        tfTitre.clear();
        tfDestination.clear();
        tfPrix.clear();
        tfPlacesTotal.clear();
        tfPlacesRestantes.clear();
        dpDepart.setValue(null);
        dpRetour.setValue(null);
        cbCategorie.setValue(null);
        taDescription.clear();
        imgPreview.setImage(null);
        imagePath = "";
    }

    @FXML
    void annuler() {
        if (parentController != null) {
            parentController.retourALaListe();
        }
    }
    private void chargerCategories() {
        List<Categorie> liste = cs.afficher();
        cbCategorie.setItems(FXCollections.observableArrayList(liste));
        cbCategorie.setConverter(new StringConverter<>() {
            @Override
            public String toString(Categorie c) {
                return (c == null) ? "" : c.getNom();
            }
            @Override
            public Categorie fromString(String s) {
                return null;
            }
        });

        if (!liste.isEmpty() && idVoyageAModifier == -1) {
            cbCategorie.setValue(liste.get(0));
        }
    }

    private void afficherAlerteSucces(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isBlank(TextField tf) {
        return tf == null || tf.getText() == null || tf.getText().trim().isEmpty();
    }

    private String nvl(String s) {
        return (s == null) ? "" : s;
    }
}