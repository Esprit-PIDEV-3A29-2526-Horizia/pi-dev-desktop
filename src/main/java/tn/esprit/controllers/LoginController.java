package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML
    private void handleAdminAccess(ActionEvent event) {
        changerScene(event, "/GestionVoyage.fxml", "Horizia - Dashboard Admin");
    }

    @FXML
    private void handleUserAccess(ActionEvent event) {
        // Le nom du fichier doit correspondre EXACTEMENT (Majuscules incluses)
        changerScene(event, "/CatalogueUser.fxml", "Horizia - Catalogue Client");
    }

    private void changerScene(ActionEvent event, String fxmlPath, String title) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("ERREUR : Le fichier FXML est introuvable au chemin : " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement de la scène : " + e.getMessage());
            e.printStackTrace();
        }
    }
}