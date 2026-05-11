package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Commentaire;
import tn.esprit.entities.Publication;
import tn.esprit.services.CommentaireService;
import tn.esprit.utils.NavigationManager;
import tn.esprit.utils.SelectedItem;
import tn.esprit.utils.SessionManager;
import tn.esprit.utils.SimpleTTS;
import tn.esprit.utils.VoskSpeechService;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CommentairesController implements Initializable {

    // Navbar partagée
    @FXML private NavbarController navbarController;

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
    private boolean isAdminMode = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("📝 Initialisation de CommentairesController");

        // Mettre à jour la navbar
        if (navbarController != null) {
            navbarController.updateUserInfo();
        }

        publication = SelectedItem.getCurrentPublication();
        if (publication == null) {
            System.err.println("❌ Aucune publication sélectionnée !");
            showAlert("Erreur", "Aucune publication sélectionnée.");
            retour();
            return;
        }

        System.out.println("✅ Publication chargée : " + publication.getTitre() + " (ID " + publication.getId() + ")");
        publicationInfoLabel.setText("📄 " + publication.getTitre());

        // Vérifier si l'utilisateur est connecté
        if (SessionManager.isLoggedIn()) {
            System.out.println("👤 Utilisateur connecté : " + SessionManager.getCurrentUser().getEmail());
            String nomComplet = SessionManager.getCurrentUser().getPrenom() + " " + SessionManager.getCurrentUser().getNom();
            auteurField.setText(nomComplet);
            auteurField.setEditable(false);
            auteurField.setStyle("-fx-background-color: #f0f0f0;");
        } else {
            System.out.println("⚠️ Utilisateur non connecté, le champ auteur est libre.");
            auteurField.setPromptText("Votre nom");
        }

        // Vérifier si c'est le mode admin (pour la navigation retour)
        isAdminMode = SessionManager.isAdmin();

        langueCombo.getItems().addAll("Français", "English", "العربية");
        langueCombo.setValue("Français");

        ajouterBtn.setOnAction(e -> ajouterCommentaire());
        retourBtn.setOnAction(e -> retour());

        voiceInputBtn.setOnAction(e -> {
            voiceInputBtn.setDisable(true);
            voiceInputBtn.setText("🔴");

            VoskSpeechService.recognize(5).thenAccept(texte -> {
                javafx.application.Platform.runLater(() -> {
                    voiceInputBtn.setDisable(false);
                    voiceInputBtn.setText("🎤");
                    if (!texte.isEmpty()) {
                        String currentText = contenuField.getText();
                        contenuField.setText(currentText + (currentText.isEmpty() ? "" : " ") + texte);
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
        if (navbarController != null) {
            navbarController.setActiveNosLogements();
        }
        loadCommentaires();
    }

    private void loadCommentaires() {
        commentairesContainer.getChildren().clear();
        List<Commentaire> commentaires = commentaireService.getByPublication(publication.getId());
        System.out.println("📋 " + commentaires.size() + " commentaires chargés.");

        if (commentaires.isEmpty()) {
            Label emptyLabel = new Label("Aucun commentaire pour le moment. Soyez le premier à commenter !");
            emptyLabel.setStyle("-fx-text-fill: #666; -fx-padding: 20; -fx-alignment: center;");
            commentairesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Commentaire c : commentaires) {
            VBox card = creerCarteCommentaire(c);
            commentairesContainer.getChildren().add(card);
        }
    }

    private VBox creerCarteCommentaire(Commentaire c) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setPadding(new Insets(12));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label auteur = new Label(c.getAuteur());
        auteur.setStyle("-fx-font-weight: bold; -fx-text-fill: #23779C;");

        Label date = new Label(c.getDateCreation().toString());
        date.setStyle("-fx-text-fill: gray; -fx-font-size: 11;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean peutAgir = SessionManager.isLoggedIn() &&
                (SessionManager.getCurrentUser().getId() == c.getUtilisateurId() || SessionManager.isAdmin());

        Button playBtn = new Button("🔊");
        playBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 10; -fx-cursor: hand; -fx-background-radius: 15; -fx-padding: 4 8;");
        playBtn.setOnAction(e -> SimpleTTS.speak(c.getContenu()));

        Button modifierBtn = new Button("✏️");
        modifierBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10; -fx-cursor: hand; -fx-background-radius: 15; -fx-padding: 4 8;");
        modifierBtn.setVisible(peutAgir);
        modifierBtn.setOnAction(e -> modifierCommentaire(c));

        Button supprimerBtn = new Button("🗑️");
        supprimerBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10; -fx-cursor: hand; -fx-background-radius: 15; -fx-padding: 4 8;");
        supprimerBtn.setVisible(peutAgir);
        supprimerBtn.setOnAction(e -> supprimerCommentaire(c));

        header.getChildren().addAll(auteur, date, spacer, playBtn, modifierBtn, supprimerBtn);

        Label contenu = new Label(c.getContenu());
        contenu.setWrapText(true);
        contenu.setStyle("-fx-text-fill: #333; -fx-font-size: 13px;");

        card.getChildren().addAll(header, contenu);
        return card;
    }

    private void ajouterCommentaire() {
        System.out.println("➡️ Tentative d'ajout de commentaire");

        if (!SessionManager.isLoggedIn()) {
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
        c.setUtilisateurId(SessionManager.getCurrentUser().getId());

        String nomComplet = SessionManager.getCurrentUser().getPrenom() + " " + SessionManager.getCurrentUser().getNom();
        c.setAuteur(nomComplet);
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

    private void modifierCommentaire(Commentaire commentaire) {
        if (!peutModifierOuSupprimer(commentaire)) return;

        TextInputDialog dialog = new TextInputDialog(commentaire.getContenu());
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText(null);
        dialog.setContentText("Nouveau contenu:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            commentaire.setContenu(result.get().trim());
            commentaireService.modifier(commentaire);
            loadCommentaires();
            showAlert("Succès", "Commentaire modifié avec succès !");
        }
    }

    private void supprimerCommentaire(Commentaire commentaire) {
        if (!peutModifierOuSupprimer(commentaire)) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer ce commentaire ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            commentaireService.supprimer(commentaire.getId());
            loadCommentaires();
            showAlert("Succès", "Commentaire supprimé avec succès !");
        }
    }

    private boolean peutModifierOuSupprimer(Commentaire c) {
        return SessionManager.isLoggedIn() &&
                (SessionManager.getCurrentUser().getId() == c.getUtilisateurId() || SessionManager.isAdmin());
    }

    private void retour() {
        if (isAdminMode) {
            // Mode Admin - retour vers GestionPublications
            NavigationManager.loadView("/GestionPublications.fxml", "Gestion des publications");
        } else {
            // Mode User - retour vers UserExplorer
            NavigationManager.loadView("/fxml/UserExplorer.fxml", "Explorer");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}