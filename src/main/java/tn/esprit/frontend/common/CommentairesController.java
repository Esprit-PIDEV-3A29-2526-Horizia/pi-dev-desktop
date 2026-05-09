package tn.esprit.frontend.common;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.backend.entities.Commentaire;
import tn.esprit.backend.entities.Publication;
import tn.esprit.backend.services.CommentaireService;
import tn.esprit.backend.utils.SelectedItem;
import tn.esprit.backend.utils.Session;
import tn.esprit.backend.utils.SimpleTTS;
import tn.esprit.backend.utils.VoskSpeechService;
import tn.esprit.frontend.admin.AdminDashboardController;
import tn.esprit.frontend.user.UserMainController;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CommentairesController implements Initializable {

    @FXML private Label publicationInfoLabel;
    @FXML private TextField auteurField;
    @FXML private TextArea contenuField;
    @FXML private Button ajouterBtn;
    @FXML private VBox commentairesContainer;
    @FXML private Button retourBtn;
    @FXML private ComboBox<String> langueCombo;
    @FXML private Button voiceInputBtn;

    private CommentaireService commentaireService = new CommentaireService();
    private Publication publication;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("📝 Initialisation de CommentairesController");
        publication = SelectedItem.getCurrentPublication();
        if (publication == null) {
            System.err.println("❌ Aucune publication sélectionnée !");
            showAlert("Erreur", "Aucune publication sélectionnée.");
            retour();
            return;
        }
        System.out.println("✅ Publication chargée : " + publication.getTitre() + " (ID " + publication.getId() + ")");
        publicationInfoLabel.setText("Publication: " + publication.getTitre());

        if (Session.estConnecte()) {
            System.out.println("👤 Utilisateur connecté : " + Session.getUtilisateur().getNomComplet());
            auteurField.setText(Session.getUtilisateur().getNomComplet());
            auteurField.setEditable(false);
        } else {
            System.out.println("⚠️ Utilisateur non connecté, le champ auteur est libre.");
        }

        langueCombo.getItems().addAll("Français", "English", "العربية");
        langueCombo.setValue("Français");

        ajouterBtn.setOnAction(e -> ajouterCommentaire());
        retourBtn.setOnAction(e -> retour());

        voiceInputBtn.setOnAction(e -> {
            voiceInputBtn.setDisable(true);
            voiceInputBtn.setText("🔴 Enregistrement (5s)... parlez !");

            VoskSpeechService.recognize(5).thenAccept(texte -> {
                javafx.application.Platform.runLater(() -> {
                    voiceInputBtn.setDisable(false);
                    voiceInputBtn.setText("🎤");
                    if (!texte.isEmpty()) {
                        contenuField.setText(contenuField.getText() + " " + texte);
                    } else {
                        showAlert("Reconnaissance vocale", "Aucune parole reconnue. Réessayez.");
                    }
                });
            }).exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> {
                    voiceInputBtn.setDisable(false);
                    voiceInputBtn.setText("🎤");
                    showAlert("Erreur", "Échec de la reconnaissance : " + ex.getMessage());
                });
                return null;
            });
        });

        loadCommentaires();
    }

    private void loadCommentaires() {
        commentairesContainer.getChildren().clear();
        List<Commentaire> commentaires = commentaireService.getByPublication(publication.getId());
        System.out.println("📋 " + commentaires.size() + " commentaires chargés.");

        for (Commentaire c : commentaires) {
            VBox card = creerCarteCommentaire(c);
            commentairesContainer.getChildren().add(card);
        }
    }

    private VBox creerCarteCommentaire(Commentaire c) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 10; -fx-background-radius: 5;");

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label auteur = new Label(c.getAuteur());
        auteur.setStyle("-fx-font-weight: bold;");

        Label date = new Label(c.getDateCreation().toString());
        date.setStyle("-fx-text-fill: gray; -fx-font-size: 11;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        boolean peutAgir = Session.estConnecte() &&
                (Session.getUtilisateur().getId() == c.getUtilisateurId() || Session.estAdmin());

        Button modifierBtn = new Button("Modifier");
        modifierBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10; -fx-cursor: hand;");
        modifierBtn.setVisible(peutAgir);
        modifierBtn.setOnAction(e -> modifierCommentaire(c));

        Button supprimerBtn = new Button("Supprimer");
        supprimerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10; -fx-cursor: hand;");
        supprimerBtn.setVisible(peutAgir);
        supprimerBtn.setOnAction(e -> supprimerCommentaire(c));

        Button playBtn = new Button("🔊");
        playBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 10; -fx-cursor: hand;");
        playBtn.setOnAction(e -> SimpleTTS.speak(c.getContenu()));

        header.getChildren().addAll(auteur, date, spacer, playBtn, modifierBtn, supprimerBtn);

        Label contenu = new Label(c.getContenu());
        contenu.setWrapText(true);

        card.getChildren().addAll(header, contenu);
        return card;
    }

    private void ajouterCommentaire() {
        System.out.println("➡️ Tentative d'ajout de commentaire");
        if (!Session.estConnecte()) {
            showAlert("Connexion requise", "Vous devez être connecté pour commenter.");
            return;
        }
        String contenu = contenuField.getText().trim();
        if (contenu.isEmpty()) {
            showAlert("Erreur", "Le commentaire ne peut pas être vide.");
            return;
        }

        Commentaire c = new Commentaire();
        c.setPublicationId(publication.getId());
        c.setUtilisateurId(Session.getUtilisateur().getId());
        c.setAuteur(Session.getUtilisateur().getNomComplet());
        c.setContenu(contenu);

        try {
            commentaireService.ajouter(c);
            System.out.println("✅ Commentaire ajouté avec succès !");
            contenuField.clear();
            loadCommentaires();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ajout du commentaire : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ajouter le commentaire : " + e.getMessage());
        }
    }

    public void modifierCommentaire(Commentaire commentaire) {
        if (!peutModifierOuSupprimer(commentaire)) return;

        TextInputDialog dialog = new TextInputDialog(commentaire.getContenu());
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText(null);
        dialog.setContentText("Nouveau contenu:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            commentaire.setContenu(result.get());
            commentaireService.modifier(commentaire);
            loadCommentaires();
        }
    }

    public void supprimerCommentaire(Commentaire commentaire) {
        if (!peutModifierOuSupprimer(commentaire)) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Supprimer ce commentaire ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            commentaireService.supprimer(commentaire.getId());
            loadCommentaires();
        }
    }

    private boolean peutModifierOuSupprimer(Commentaire c) {
        return Session.estConnecte() &&
                (Session.getUtilisateur().getId() == c.getUtilisateurId() || Session.estAdmin());
    }

    private void retour() {
        if (UserMainController.getInstance() != null) {
            UserMainController.getInstance().loadView("/views/user/UserExplorer.fxml");
        } else if (AdminDashboardController.getInstance() != null) {
            // ✅ CORRIGÉ : GestionPublications.fxml au lieu de Gestionpublication.fxml
            AdminDashboardController.getInstance().loadView("/views/admin/GestionPublications.fxml");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}