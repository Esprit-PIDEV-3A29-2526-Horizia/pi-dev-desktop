package tn.esprit.frontend.user;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class UserFavorisController implements Initializable {

    @FXML private Button btnExplorer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Le bouton a déjà un onAction dans le FXML, mais on peut aussi le lier ici
        btnExplorer.setOnAction(e -> explorer());
    }

    @FXML
    private void explorer() {
        // Redirige vers la page d'exploration
        UserMainController.getInstance().showExplorer();
    }
}