package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Publication;

import java.util.function.Consumer;

public class PublicationCardController {

    @FXML private Label titreLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label auteurLabel;
    @FXML private Label dateLabel;
    @FXML private Label likesLabel;
    @FXML private Label categorieLabel;
    @FXML private ImageView imageView;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private Publication publication;
    private Consumer<Publication> onEditCallback;
    private Consumer<Publication> onDeleteCallback;

    public void setAdminMode(boolean isAdmin) {
        if (btnEdit != null) {
            btnEdit.setVisible(isAdmin);
            btnEdit.setManaged(isAdmin);
        }
        if (btnDelete != null) {
            btnDelete.setVisible(isAdmin);
            btnDelete.setManaged(isAdmin);
        }
    }

    // ✅ Méthode alternative pour compatibilité
    public void setAdminBackendMode() {
        setAdminMode(true);
    }

    public void setPublication(Publication p) {
        this.publication = p;

        if (titreLabel != null) titreLabel.setText(p.getTitre());
        if (descriptionLabel != null) {
            String desc = p.getDescription();
            if (desc != null && desc.length() > 100) {
                desc = desc.substring(0, 100) + "...";
            }
            descriptionLabel.setText(desc);
        }
        if (auteurLabel != null) auteurLabel.setText("Par: " + p.getAuteur());
        if (dateLabel != null && p.getDateCreation() != null) {
            dateLabel.setText(p.getDateCreation().toLocalDate().toString());
        }
        if (likesLabel != null) likesLabel.setText("❤️ " + p.getLikes());
        if (categorieLabel != null && p.getCategorie() != null) {
            categorieLabel.setText(p.getCategorie().getLabel());
        }

        if (imageView != null && p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                String imagePath = p.getImage();
                if (!imagePath.startsWith("/")) {
                    imagePath = "/" + imagePath;
                }
                Image img = new Image(getClass().getResourceAsStream(imagePath));
                if (img != null && !img.isError()) {
                    imageView.setImage(img);
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement image: " + e.getMessage());
            }
        }
    }

    public void setOnEditCallback(Consumer<Publication> callback) {
        this.onEditCallback = callback;
        if (btnEdit != null) {
            btnEdit.setOnAction(e -> {
                if (onEditCallback != null && publication != null) {
                    onEditCallback.accept(publication);
                }
            });
        }
    }

    public void setOnDeleteCallback(Consumer<Publication> callback) {
        this.onDeleteCallback = callback;
        if (btnDelete != null) {
            btnDelete.setOnAction(e -> {
                if (onDeleteCallback != null && publication != null) {
                    onDeleteCallback.accept(publication);
                }
            });
        }
    }
}