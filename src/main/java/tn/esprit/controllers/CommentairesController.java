package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Commentaire;
import tn.esprit.entities.Publication;
import tn.esprit.services.CommentaireService;

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

    private CommentaireService commentaireService = new CommentaireService();
    private Publication publication;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ajouterBtn.setOnAction(e -> ajouterCommentaire());
        retourBtn.setOnAction(e -> retourPublications());
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
        publicationInfoLabel.setText("Publication: " + publication.getTitre());
        loadCommentaires();
    }

    private void loadCommentaires() {
        commentairesContainer.getChildren().clear();

        List<Commentaire> commentaires = commentaireService.getByPublication(publication.getId());

        for (Commentaire c : commentaires) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CommentaireCard.fxml"));
                VBox card = loader.load();

                CommentaireCardController controller = loader.getController();
                controller.setCommentaire(c);
                controller.setParentController(this);

                commentairesContainer.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void ajouterCommentaire() {
        String auteur = auteurField.getText().trim();
        String contenu = contenuField.getText().trim();

        if (auteur.isEmpty() || contenu.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs manquants");
            alert.setContentText("Veuillez remplir tous les champs");
            alert.showAndWait();
            return;
        }

        Commentaire c = new Commentaire(publication.getId(), auteur, contenu);
        commentaireService.ajouter(c);

        auteurField.clear();
        contenuField.clear();
        loadCommentaires();
    }

    /**
     * Modifier un commentaire existant.
     * @param commentaire le commentaire à modifier
     */
    public void modifierCommentaire(Commentaire commentaire) {
        // Créer une boîte de dialogue personnalisée
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText("Modification du commentaire");

        ButtonType modifierBtn = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        ButtonType annulerBtn = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(modifierBtn, annulerBtn);

        // Créer les champs pré-remplis
        TextField auteurField = new TextField(commentaire.getAuteur());
        auteurField.setPromptText("Auteur");

        TextArea contenuArea = new TextArea(commentaire.getContenu());
        contenuArea.setPromptText("Contenu");
        contenuArea.setPrefRowCount(4);

        VBox vbox = new VBox(10,
                new Label("Auteur:"), auteurField,
                new Label("Contenu:"), contenuArea);
        vbox.setPrefWidth(400);

        dialog.getDialogPane().setContent(vbox);

        // Traitement du résultat
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == modifierBtn) {
            String newAuteur = auteurField.getText().trim();
            String newContenu = contenuArea.getText().trim();

            if (newAuteur.isEmpty() || newContenu.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Champs vides");
                alert.setContentText("L'auteur et le contenu ne peuvent pas être vides.");
                alert.showAndWait();
                return;
            }

            commentaire.setAuteur(newAuteur);
            commentaire.setContenu(newContenu);
            commentaireService.modifier(commentaire);
            loadCommentaires();
        }
    }

    /**
     * Supprimer un commentaire après confirmation.
     * @param commentaire le commentaire à supprimer
     */
    public void supprimerCommentaire(Commentaire commentaire) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le commentaire");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce commentaire ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            commentaireService.supprimer(commentaire.getId());
            loadCommentaires();
        }
    }

    public void refresh() {
        loadCommentaires();
    }

    /**
     * Retourne à la liste des publications.
     */
    private void retourPublications() {
        try {
            // Charger le fichier FXML de la liste des publications
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Publications.fxml"));
            Parent root = loader.load();

            // Récupérer la scène actuelle et remplacer sa racine
            Stage stage = (Stage) retourBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du retour aux publications : " + e.getMessage());
        }
    }
}