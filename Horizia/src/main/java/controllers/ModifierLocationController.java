package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.entities.Location;
import org.example.services.LocationService;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

public class ModifierLocationController {

    @FXML private TextField txtRechercheClient;
    @FXML private Label lblMessageRecherche;
    @FXML private VBox vboxFormulaire;
    @FXML private Label lblLocationInfo;
    @FXML private DatePicker dateFinReelle;
    @FXML private TextField txtKilometrageRetour;
    @FXML private TextField txtAvance;
    @FXML private ComboBox<String> comboStatut;
    @FXML private TextArea txtNotes;
    @FXML private Label lblMessage;

    private LocationService locationService;
    private Location locationCourante;

    @FXML
    public void initialize() {
        locationService = new LocationService();
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Modifier Location - Chargée");
        System.out.println("═══════════════════════════════════════════════");

        comboStatut.getItems().addAll("réservée", "en_cours", "terminée", "annulée", "no_show");
    }

    @FXML
    private void rechercherLocation() {
        String client = txtRechercheClient.getText();
        if (client == null || client.trim().isEmpty()) {
            afficherErreurRecherche("⚠️ Veuillez saisir un nom de client !");
            return;
        }

        List<Location> locations = locationService.rechercherParClient(client.trim());
        if (locations.isEmpty()) {
            afficherErreurRecherche("✗ Aucune location trouvée pour ce client !");
            vboxFormulaire.setVisible(false);
            vboxFormulaire.setManaged(false);
            return;
        }

        locationCourante = locations.get(0);
        afficherSuccesRecherche("✓ Location trouvée !");
        preremplirFormulaire();
        vboxFormulaire.setVisible(true);
        vboxFormulaire.setManaged(true);
    }

    private void preremplirFormulaire() {
        lblLocationInfo.setText("Client : " + locationCourante.getClientNomComplet() + " | Montant : " +
                String.format("%.3f TND", locationCourante.getMontantTotal()));

        if (locationCourante.getDateFinReelle() != null) {
            dateFinReelle.setValue(locationCourante.getDateFinReelle().toLocalDateTime().toLocalDate());
        }

        if (locationCourante.getKilometrageRetour() != null) {
            txtKilometrageRetour.setText(String.valueOf(locationCourante.getKilometrageRetour()));
        }

        txtAvance.setText(String.valueOf(locationCourante.getAvance()));
        comboStatut.setValue(locationCourante.getStatut());
        txtNotes.setText(locationCourante.getNotes());
    }

    @FXML
    private void enregistrerModifications() {
        if (locationCourante == null) return;

        if (dateFinReelle.getValue() != null) {
            locationCourante.setDateFinReelle(Timestamp.valueOf(dateFinReelle.getValue().atStartOfDay()));
        }

        if (txtKilometrageRetour.getText() != null && !txtKilometrageRetour.getText().isEmpty()) {
            try {
                locationCourante.setKilometrageRetour(Integer.parseInt(txtKilometrageRetour.getText()));
            } catch (NumberFormatException e) {
                afficherErreur("⚠️ Kilométrage invalide !");
                return;
            }
        }

        try {
            locationCourante.setAvance(Double.parseDouble(txtAvance.getText()));
        } catch (NumberFormatException e) {
            afficherErreur("⚠️ Avance invalide !");
            return;
        }

        locationCourante.setStatut(comboStatut.getValue());
        locationCourante.setNotes(txtNotes.getText());

        boolean succes = locationService.modifierLocation(locationCourante);
        if (succes) {
            afficherSucces("✓ Modifications enregistrées !");
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        Stage stage = (Stage) txtRechercheClient.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            afficherErreur("✗ Échec de la modification !");
        }
    }

    @FXML
    private void annuler() {
        Stage stage = (Stage) txtRechercheClient.getScene().getWindow();
        stage.close();
    }

    private void afficherSuccesRecherche(String msg) {
        lblMessageRecherche.setText(msg);
        lblMessageRecherche.setStyle("-fx-text-fill: #27ae60; -fx-background-color: #d5f4e6; -fx-font-weight: bold;");
        lblMessageRecherche.setVisible(true);
        lblMessageRecherche.setManaged(true);
    }

    private void afficherErreurRecherche(String msg) {
        lblMessageRecherche.setText(msg);
        lblMessageRecherche.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fadbd8; -fx-font-weight: bold;");
        lblMessageRecherche.setVisible(true);
        lblMessageRecherche.setManaged(true);
    }

    private void afficherSucces(String msg) {
        lblMessage.setText(msg);
        lblMessage.setStyle("-fx-text-fill: #27ae60; -fx-background-color: #d5f4e6; -fx-font-weight: bold; -fx-border-color: #27ae60; -fx-border-width: 2; -fx-border-radius: 8;");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void afficherErreur(String msg) {
        lblMessage.setText(msg);
        lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fadbd8; -fx-font-weight: bold; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }
}