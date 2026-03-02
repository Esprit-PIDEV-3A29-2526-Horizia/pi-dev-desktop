package tn.esprit.frontend.common;  // ✅ Fixed package

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import tn.esprit.backend.entities.Commentaire;  // ✅ Added
import tn.esprit.backend.entities.Publication;  // ✅ Added
import tn.esprit.backend.services.CommentaireService;  // ✅ Added
import tn.esprit.backend.services.PublicationService;  // ✅ Added

public class PublicationsController implements Initializable {

    @FXML private FlowPane publicationsContainer;
    @FXML private Button ajouterBtn;
    @FXML private TextField rechercheField;
    @FXML private ComboBox<String> triComboBox;

    private PublicationService publicationService = new PublicationService();
    private CommentaireService commentaireService = new CommentaireService();

    private Map<Publication, VBox> commentaireSectionsMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        triComboBox.getItems().addAll("Plus récentes", "Plus anciennes", "A-Z", "Z-A", "Plus aimées");
        triComboBox.setValue("Plus récentes");

        loadPublications();

        ajouterBtn.setOnAction(e -> DashboardController.loadViewStatic("/views/common/AjouterPublication.fxml"));  // ✅ Fixed
        rechercheField.setOnKeyReleased(e -> rechercher());
        triComboBox.setOnAction(e -> trier());
    }

    public void loadPublications() {
        publicationsContainer.getChildren().clear();
        commentaireSectionsMap.clear();

        // ✅ Fixed: getAll() doesn't take parameter, sorting done here
        List<Publication> list = publicationService.getAll();
        list = sortPublications(list, triComboBox.getValue());

        System.out.println("📊 Chargement de " + list.size() + " publications...");

        for (Publication p : list) {
            VBox card = creerCartePublication(p);
            publicationsContainer.getChildren().add(card);
        }
    }

    // ✅ Added: Sorting method
    private List<Publication> sortPublications(List<Publication> list, String sortType) {
        if (sortType == null) return list;

        switch (sortType) {
            case "Plus anciennes":
                return list.stream()
                        .sorted((p1, p2) -> p1.getDateCreation().compareTo(p2.getDateCreation()))
                        .collect(Collectors.toList());
            case "A-Z":
                return list.stream()
                        .sorted((p1, p2) -> p1.getTitre().compareToIgnoreCase(p2.getTitre()))
                        .collect(Collectors.toList());
            case "Z-A":
                return list.stream()
                        .sorted((p1, p2) -> p2.getTitre().compareToIgnoreCase(p1.getTitre()))
                        .collect(Collectors.toList());
            case "Plus aimées":
                return list.stream()
                        .sorted((p1, p2) -> Integer.compare(p2.getLikes(), p1.getLikes()))
                        .collect(Collectors.toList());
            case "Plus récentes":
            default:
                return list.stream()
                        .sorted((p1, p2) -> p2.getDateCreation().compareTo(p1.getDateCreation()))
                        .collect(Collectors.toList());
        }
    }

    private VBox creerCartePublication(Publication p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(400);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color: #3498db; -fx-background-radius: 50%;");
        avatar.setMinSize(40, 40);
        Label avatarText = new Label("U");
        avatarText.setStyle("-fx-font-size: 18; -fx-text-fill: white;");
        avatar.getChildren().add(avatarText);

        VBox info = new VBox(2);
        Label auteurLabel = new Label("Utilisateur " + p.getUtilisateurId());
        auteurLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        // ✅ Fixed: getDatePublication() -> getDateCreation()
        Label dateLabel = new Label(p.getDateCreation().format(DateTimeFormatter.ofPattern("dd MMM yyyy à HH:mm")));
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: gray;");
        info.getChildren().addAll(auteurLabel, dateLabel);

        header.getChildren().addAll(avatar, info);

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 8; -fx-min-height: 150;");

        if (p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                String imgPath = p.getImage().replace("\\", "/");
                InputStream is = getClass().getResourceAsStream(imgPath);
                ImageView imageView = new ImageView();
                imageView.setFitWidth(370);
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);

                if (is != null) {
                    imageView.setImage(new Image(is));
                } else {
                    imageView.setImage(new Image("file:" + p.getImage(), 370, 150, true, true));
                }
                imageContainer.getChildren().add(imageView);
            } catch (Exception e) {
                Label noImage = new Label("🖼️ Pas d'image");
                imageContainer.getChildren().add(noImage);
            }
        } else {
            Label noImage = new Label("🖼️ Pas d'image");
            imageContainer.getChildren().add(noImage);
        }

        // Titre et description
        Label titreLabel = new Label(p.getTitre());
        titreLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        titreLabel.setWrapText(true);

        String desc = p.getDescription();
        if (desc != null && desc.length() > 100) {
            desc = desc.substring(0, 100) + "...";
        }
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #555;");
        descLabel.setWrapText(true);

        // Stats
        HBox stats = new HBox(15);
        Button likeBtn = new Button("❤️ " + p.getLikes());
        likeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c;");
        likeBtn.setOnAction(e -> {
            publicationService.incrementerLikes(p.getId());
            p.setLikes(p.getLikes() + 1);
            likeBtn.setText("❤️ " + p.getLikes());
        });

        int commentCount = commentaireService.getByPublication(p.getId()).size();
        Button commentBtn = new Button("💬 " + commentCount + " commentaires");
        commentBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db;");

        // Section commentaires (cachée par défaut)
        VBox commentairesSection = new VBox(10);
        commentairesSection.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 8;");
        commentairesSection.setVisible(false);
        commentairesSection.setManaged(false);

        // Liste des commentaires
        VBox listeCommentaires = new VBox(8);
        chargerCommentairesDansSection(p, listeCommentaires);

        // Formulaire ajout commentaire
        HBox ajoutCommentaireBox = new HBox(10);
        TextField nouveauCommentaireField = new TextField();
        nouveauCommentaireField.setPromptText("Écrire un commentaire...");
        HBox.setHgrow(nouveauCommentaireField, Priority.ALWAYS);

        Button envoyerBtn = new Button("➤");
        envoyerBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        envoyerBtn.setOnAction(e -> {
            String contenu = nouveauCommentaireField.getText().trim();
            if (!contenu.isEmpty()) {
                ajouterCommentaire(p, contenu, listeCommentaires, commentBtn, nouveauCommentaireField);
            }
        });

        ajoutCommentaireBox.getChildren().addAll(nouveauCommentaireField, envoyerBtn);
        commentairesSection.getChildren().addAll(listeCommentaires, ajoutCommentaireBox);

        // Toggle commentaires
        commentBtn.setOnAction(e -> {
            boolean visible = !commentairesSection.isVisible();
            commentairesSection.setVisible(visible);
            commentairesSection.setManaged(visible);
            if (visible) {
                chargerCommentairesDansSection(p, listeCommentaires);
            }
        });

        stats.getChildren().addAll(likeBtn, commentBtn);

        // Boutons admin
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button modifBtn = new Button("✏️ Modifier");
        modifBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        modifBtn.setOnAction(e -> {
            DashboardController.setSelectedPublication(p);  // ✅ Fixed
            DashboardController.loadViewStatic("/views/common/ModifierPublication.fxml");  // ✅ Fixed
        });

        Button supprBtn = new Button("🗑️ Supprimer");
        supprBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        supprBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setContentText("Supprimer '" + p.getTitre() + "' ?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    try {
                        publicationService.supprimer(p.getId());
                        loadPublications();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });

        buttons.getChildren().addAll(modifBtn, supprBtn);

        // Assembler
        card.getChildren().addAll(header, imageContainer, titreLabel, descLabel, stats, buttons, commentairesSection);
        return card;
    }

    private void chargerCommentairesDansSection(Publication p, VBox container) {
        container.getChildren().clear();
        List<Commentaire> commentaires = commentaireService.getByPublication(p.getId());

        if (commentaires.isEmpty()) {
            Label emptyLabel = new Label("Aucun commentaire. Soyez le premier ! 💬");
            emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            container.getChildren().add(emptyLabel);
        } else {
            for (Commentaire c : commentaires) {
                VBox commentCard = creerCarteCommentaire(p, c, container);
                container.getChildren().add(commentCard);
            }
        }
    }

    private VBox creerCarteCommentaire(Publication p, Commentaire c, VBox parentContainer) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

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
        editBtn.setOnAction(e -> {
            card.getChildren().clear();
            TextField editField = new TextField(c.getContenu());
            editField.setStyle("-fx-background-radius: 5;");

            HBox editButtons = new HBox(5);
            Button saveBtn = new Button("💾 Enregistrer");
            saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            saveBtn.setOnAction(ev -> {
                String nouveau = editField.getText().trim();
                if (!nouveau.isEmpty()) {
                    modifierCommentaire(p, c, nouveau, parentContainer);
                }
            });

            Button cancelBtn = new Button("❌ Annuler");
            cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
            cancelBtn.setOnAction(ev -> chargerCommentairesDansSection(p, parentContainer));

            editButtons.getChildren().addAll(saveBtn, cancelBtn);
            card.getChildren().addAll(editField, editButtons);
        });

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 10; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setContentText("Supprimer ce commentaire ?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    supprimerCommentaire(p, c, parentContainer);
                }
            });
        });

        header.getChildren().addAll(nomLabel, dateLabel, spacer, editBtn, deleteBtn);

        Label contenuLabel = new Label(c.getContenu());
        contenuLabel.setWrapText(true);
        contenuLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 13;");

        card.getChildren().addAll(header, contenuLabel);
        return card;
    }

    private void ajouterCommentaire(Publication p, String contenu, VBox container, Button commentBtn, TextField field) {
        if (contenu.length() < 2) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Le commentaire doit contenir au moins 2 caractères !");
            alert.showAndWait();
            return;
        }

        Commentaire c = new Commentaire();
        c.setPublicationId(p.getId());
        c.setAuteur("Moi");
        c.setContenu(contenu);

        commentaireService.ajouter(c);
        field.clear();
        chargerCommentairesDansSection(p, container);

        int newCount = commentaireService.getByPublication(p.getId()).size();
        commentBtn.setText("💬 " + newCount + " commentaires");
    }

    private void modifierCommentaire(Publication p, Commentaire c, String nouveauContenu, VBox container) {
        c.setContenu(nouveauContenu);
        commentaireService.modifier(c);
        chargerCommentairesDansSection(p, container);
    }

    private void supprimerCommentaire(Publication p, Commentaire c, VBox container) {
        commentaireService.supprimer(c.getId());
        chargerCommentairesDansSection(p, container);
    }

    public void refresh() {
        loadPublications();
    }

    private void rechercher() {
        String keyword = rechercheField.getText().trim();
        if (keyword.isEmpty()) {
            loadPublications();
            return;
        }

        publicationsContainer.getChildren().clear();
        // ✅ Fixed: Filter locally since rechercherParTitre doesn't exist
        List<Publication> list = publicationService.getAll().stream()
                .filter(p -> p.getTitre().toLowerCase().contains(keyword.toLowerCase()) ||
                        p.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        for (Publication p : list) {
            VBox card = creerCartePublication(p);
            publicationsContainer.getChildren().add(card);
        }
    }

    private void trier() {
        loadPublications();
    }
}