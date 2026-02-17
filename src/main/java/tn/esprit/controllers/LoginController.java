package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private Button btnAdmin; // Doit correspondre au fx:id dans Scene Builder
    @FXML private Button btnUser;  // Doit correspondre au fx:id dans Scene Builder

    @FXML
    void handleAdminAccess(ActionEvent event) { // Ajoutez 'ActionEvent event' en paramètre
        try {
            System.out.println("Accès Admin : Chargement du catalogue voyage...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionVoyage.fxml"));
            Parent root = loader.load();

            // On récupère la fenêtre (Stage) directement via l'événement du clic
            // Cela fonctionne même si fx:id="btnAdmin" est mal configuré
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Horizia - Dashboard Administration");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur de chargement du catalogue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleUserAccess() {
        changerScene("/CatalogueUser.fxml", "Horizia - Catalogue Client");
    }

    private void changerScene(String fxmlPath, String titre) {
        try {
            // Utilisation de getClass().getResource() avec le chemin correct
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // On utilise n'importe quel bouton injecté pour récupérer la fenêtre
            Stage stage = (Stage) btnAdmin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titre);
            stage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Erreur : Impossible de trouver le fichier " + fxmlPath);
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Erreur : btnAdmin n'est pas lié au FXML !");
        }
    }
}