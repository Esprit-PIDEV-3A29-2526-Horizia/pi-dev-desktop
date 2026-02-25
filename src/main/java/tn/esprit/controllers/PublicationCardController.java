package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Commentaire;
import tn.esprit.entities.Publication;
import tn.esprit.services.CommentaireService;
import tn.esprit.services.PublicationService;

import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PublicationCardController implements Initializable {

    @FXML private Label auteurLabel;
    @FXML private Label dateLabel;
    @FXML private ImageView imageView;
    @FXML private Label titreLabel;
    @FXML private Label descriptionLabel;
    @FXML private HBox tagsContainer;

    @FXML private Button likeBtn;
    @FXML private Button commentToggleBtn;
    @FXML private Button shareBtn;

    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;

    @FXML private VBox commentairesSection;
    @FXML private VBox listeCommentaires;
    @FXML private TextField nouveauCommentaireField;
    @FXML private Button publierCommentaireBtn;

    private Publication publication;
    private PublicationsController parentController;
    private CommentaireService commentaireService = new CommentaireService();
    private PublicationService publicationService = new PublicationService();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy à HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        likeBtn.setOnAction(e -> likerPublication());
        commentToggleBtn.setOnAction(e -> toggleCommentaires());
        shareBtn.setOnAction(e -> partagerPublication());

        modifierBtn.setOnAction(e -> modifierPublication());
        supprimerBtn.setOnAction(e -> supprimerPublication());

        publierCommentaireBtn.setOnAction(e -> ajouterCommentaire());
    }

    public void setPublication(Publication publication) {
        this.publication = publication;

        auteurLabel.setText("Utilisateur " + publication.getUtilisateurId());
        dateLabel.setText(publication.getDatePublication().format(dateFormatter));
        titreLabel.setText(publication.getTitre());
        descriptionLabel.setText(publication.getDescription());

        likeBtn.setText("❤️ " + publication.getLikes());
        updateCommentCount();

        loadImage();
        chargerCommentaires();
    }

    private void loadImage() {
        if (publication.getImage() != null && !publication.getImage().isEmpty()) {
            try {
                String imgPath = publication.getImage().replace("\\", "/");
                InputStream is = getClass().getResourceAsStream(imgPath);
                if (is != null) {
                    Image img = new Image(is, 370, 200, true, true);
                    imageView.setImage(img);
                } else {
                    Image img = new Image("file:" + publication.getImage(), 370, 200, true, true);
                    if (!img.isError()) {
                        imageView.setImage(img);
                    }
                }
            } catch (Exception e) {
                System.out.println("Image non chargée: " + e.getMessage());
            }
        }
    }

    private void toggleCommentaires() {
        boolean visible = commentairesSection.isVisible();
        commentairesSection.setVisible(!visible);
        commentairesSection.setManaged(!visible);

        if (!visible) {
            chargerCommentaires();
        }
    }

    private void chargerCommentaires() {
        listeCommentaires.getChildren().clear();
        List<Commentaire> commentaires = commentaireService.getByPublication(publication.getId());

        if (commentaires.isEmpty()) {
            Label emptyLabel = new Label("Aucun commentaire. Soyez le premier ! 💬");
            emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            listeCommentaires.getChildren().add(emptyLabel);
        } else {
            for (Commentaire c : commentaires) {
                listeCommentaires.getChildren().add(creerCarteCommentaire(c));
            }
        }

        updateCommentCount();
    }

    private VBox creerCarteCommentaire(Commentaire c) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 8;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nomLabel = new Label(c.getAuteur());
        nomLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 12;");

        Label dateLabel = new Label(c.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
        dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #95a5a6;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("✏️");
        editBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 10; -fx-cursor: hand;");
        editBtn.setOnAction(e -> modifierCommentaire(c, card));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 10; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> supprimerCommentaire(c));

        header.getChildren().addAll(nomLabel, dateLabel, spacer, editBtn, deleteBtn);

        Label contenuLabel = new Label(c.getContenu());
        contenuLabel.setWrapText(true);
        contenuLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 13;");

        card.getChildren().addAll(header, contenuLabel);
        return card;
    }

    private void ajouterCommentaire() {
        String contenu = nouveauCommentaireField.getText().trim();
        if (contenu.isEmpty()) {
            showAlert("Erreur", "Le commentaire ne peut pas être vide !");
            return;
        }
        if (contenu.length() < 2) {
            showAlert("Erreur", "Minimum 2 caractères !");
            return;
        }

        Commentaire c = new Commentaire();
        c.setPublicationId(publication.getId());
        c.setAuteur("Moi");
        c.setContenu(contenu);

        commentaireService.ajouter(c);
        nouveauCommentaireField.clear();
        chargerCommentaires();
    }

    private void modifierCommentaire(Commentaire c, VBox card) {
        card.getChildren().clear();

        TextField editField = new TextField(c.getContenu());
        editField.setStyle("-fx-background-radius: 5;");

        HBox buttons = new HBox(5);
        Button saveBtn = new Button("💾");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> {
            String nouveau = editField.getText().trim();
            if (!nouveau.isEmpty()) {
                c.setContenu(nouveau);
                commentaireService.modifier(c);
                chargerCommentaires();
            }
        });

        Button cancelBtn = new Button("❌");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        cancelBtn.setOnAction(e -> chargerCommentaires());

        buttons.getChildren().addAll(saveBtn, cancelBtn);
        card.getChildren().addAll(editField, buttons);
    }

    private void supprimerCommentaire(Commentaire c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Supprimer ce commentaire ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                commentaireService.supprimer(c.getId());
                chargerCommentaires();
            }
        });
    }

    private void updateCommentCount() {
        int count = commentaireService.getByPublication(publication.getId()).size();
        commentToggleBtn.setText("💬 " + count + " commentaire" + (count > 1 ? "s" : ""));
    }

    private void likerPublication() {
        publicationService.incrementerLikes(publication.getId());
        publication.setLikes(publication.getLikes() + 1);
        likeBtn.setText("❤️ " + publication.getLikes());
    }

    private void partagerPublication() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString("Publication: " + publication.getTitre());
        clipboard.setContent(content);

        showAlert("Partagé", "Lien copié dans le presse-papiers !");
    }

    private void modifierPublication() {
        Dashboard.setSelectedPublication(publication);
        // ← SANS /views/
        Dashboard.loadView("/resources/ModifierPublication.fxml");
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}