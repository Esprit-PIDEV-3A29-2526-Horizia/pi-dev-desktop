package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import tn.esprit.entities.Publication;
import tn.esprit.services.PublicationService;
import java.io.InputStream;  // ← AJOUTER CETTE LIGNE

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PublicationCardController implements Initializable {

    @FXML private StackPane imageContainer;
    @FXML private ImageView imageView;
    @FXML private Label dateLabel;
    @FXML private Label titreLabel;
    @FXML private Label descriptionLabel;
    @FXML private Button voirDetailsBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;

    private Publication publication;
    private PublicationsController parentController;
    private PublicationService publicationService = new PublicationService();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisation vide, les données sont injectées via setPublication
    }
    public void setPublication(Publication publication) {
        this.publication = publication;

        System.out.println("=== DEBUG CARTE ===");
        System.out.println("Titre: " + publication.getTitre());
        System.out.println("Image path: [" + publication.getImage() + "]");

        titreLabel.setText(publication.getTitre());

        String desc = publication.getDescription();
        if (desc != null && desc.length() > 100) {
            desc = desc.substring(0, 100) + "...";
        }
        descriptionLabel.setText(desc);

        dateLabel.setText(publication.getDatePublication().format(dateFormatter));

        // ========== IMAGE CORRIGÉE ==========
        if (publication.getImage() != null && !publication.getImage().isEmpty()) {
            try {
                String imgPath = publication.getImage().replace("\\", "/");
                System.out.println("Tentative chargement: " + imgPath);

                // Méthode 1 : Charger depuis les ressources (src/main/resources)
                Image img = null;

                // Essayer d'abord comme ressource
                try {
                    InputStream is = getClass().getResourceAsStream(imgPath);
                    if (is != null) {
                        System.out.println("✅ Trouvé dans les ressources");
                        img = new Image(is, 250, 150, true, true);
                    }
                } catch (Exception e) {
                    System.out.println("❌ Pas trouvé dans les ressources: " + e.getMessage());
                }

                // Si échec, essayer comme fichier
                if (img == null) {
                    String projectPath = System.getProperty("user.dir");
                    String fullPath = projectPath + "/src/main/resources" + imgPath;
                    System.out.println("Essai chemin fichier: " + fullPath);
                    img = new Image("file:" + fullPath, 250, 150, true, true);
                }

                if (img.isError()) {
                    System.out.println("❌ ERREUR Image: " + img.getException());
                } else {
                    System.out.println("✅ Image chargée: " + img.getWidth() + "x" + img.getHeight());
                }

                imageView.setImage(img);

            } catch (Exception e) {
                System.out.println("❌ EXCEPTION: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ Pas d'image (null ou vide)");
        }
        System.out.println("===================");



        modifierBtn.setOnAction(e -> {
            Dashboard.setSelectedPublication(publication);
            Dashboard.loadView("/ModifierPublication.fxml");
        });

        supprimerBtn.setOnAction(e -> supprimerPublication());
    }
    private void supprimerPublication() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la publication");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer '" + publication.getTitre() + "' ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                publicationService.supprimer(publication.getId());
                if (parentController != null) {
                    parentController.refresh();
                }
            }
        });
    }

    public void setParentController(PublicationsController parent) {
        this.parentController = parent;
    }

}
