package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import java.net.URL;
import java.util.ResourceBundle;

public class MapPickerController implements Initializable {

    @FXML private WebView mapView;
    @FXML private TextField addressField;
    @FXML private TextField latField;
    @FXML private TextField lngField;
    @FXML private ButtonType saveButtonType;
    @FXML private ButtonType cancelButtonType;

    private WebEngine webEngine;
    private double selectedLat = 36.8065;
    private double selectedLng = 10.1815;
    private String selectedAddress = "Tunis, Tunisie";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = mapView.getEngine();

        addressField.setText(selectedAddress);
        latField.setText(String.valueOf(selectedLat));
        lngField.setText(String.valueOf(selectedLng));

        // Carte statique OpenStreetMap (image simple)
        updateMap();
    }

    @FXML
    private void updateMap() {
        try {
            selectedLat = Double.parseDouble(latField.getText());
            selectedLng = Double.parseDouble(lngField.getText());
            selectedAddress = addressField.getText();

            // Image statique de la carte (pas de clic, juste une image)
            String html = "<html><body style='margin:0;padding:0;text-align:center;background:#f0f0f0;'>" +
                    "<img src='https://staticmap.openstreetmap.de/staticmap.php?center=" +
                    selectedLat + "," + selectedLng +
                    "&zoom=13&size=800x400&maptype=mapnik&markers=" +
                    selectedLat + "," + selectedLng + ",lightblue1' " +
                    "style='width:100%;height:100%;object-fit:contain;'/>" +
                    "</body></html>";

            webEngine.loadContent(html);

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Coordonnées invalides");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public double getSelectedLat() { return selectedLat; }
    public double getSelectedLng() { return selectedLng; }
    public String getSelectedAddress() { return selectedAddress; }
}