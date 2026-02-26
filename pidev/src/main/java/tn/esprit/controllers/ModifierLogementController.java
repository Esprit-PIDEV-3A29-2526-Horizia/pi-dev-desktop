package tn.esprit.controllers;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import tn.esprit.entities.logement;
import tn.esprit.services.Servicelogement;
import tn.esprit.utils.AudioRecorder;
import tn.esprit.utils.VoskService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ModifierLogementController implements Initializable {

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextField nomField;

    @FXML
    private TextField adresseField;

    @FXML
    private Spinner<Integer> capaciteSpinner;

    @FXML
    private TextField tarifField;

    @FXML
    private TextField equipementField;

    @FXML
    private TextField imageField;

    @FXML
    private Label disponibiliteLabel;

    @FXML
    private HBox switchBackground;

    @FXML
    private Circle switchCircle;

    @FXML
    private ToggleButton disponibiliteToggle;

    @FXML
    private Button modifierBtn;

    @FXML
    private Button annulerBtn;

    // Boutons pour la reconnaissance vocale
    @FXML
    private Button nomMicroBtn;
    @FXML
    private Button imageMicroBtn;
    @FXML
    private Button adresseMicroBtn;
    @FXML
    private Button equipementMicroBtn;
    @FXML
    private Button tarifMicroBtn;

    private Servicelogement servicelogement = new Servicelogement();
    private logement selectedLogement;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation du modèle Vosk (dans un thread séparé)
        new Thread(() -> {
            try {
                String userDir = System.getProperty("user.dir");
                String modelPath = userDir + File.separator + "models" + File.separator + "vosk-model-fr-0.6-linto-2.2.0";
                File modelDir = new File(modelPath);
                if (!modelDir.exists() || !modelDir.isDirectory()) {
                    throw new IOException("Dossier modèle introuvable : " + modelPath);
                }
                VoskService.initModel(modelPath);
                System.out.println("✅ Modèle Vosk initialisé avec succès (modification)");
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Erreur Vosk", e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();

        // Récupérer le logement sélectionné
        selectedLogement = AdminDashboardController.getSelectedLogement();
        if (selectedLogement != null) {
            // Pré-remplir les champs avec les données existantes
            typeComboBox.setValue(selectedLogement.getType());
            nomField.setText(selectedLogement.getNom());
            adresseField.setText(selectedLogement.getAdresse());
            capaciteSpinner.getValueFactory().setValue(selectedLogement.getCapacite());
            tarifField.setText(String.valueOf(selectedLogement.getTarif_nuit()));
            equipementField.setText(selectedLogement.getEquipement());
            imageField.setText(selectedLogement.getImage());
            disponibiliteToggle.setSelected(selectedLogement.isDisponibilite());
            updateSwitchUI(); // Mettre à jour l'UI du switch
        } else {
            showAlert("Erreur", "Aucun logement sélectionné pour la modification.", Alert.AlertType.WARNING);
        }

        // Actions des boutons micro
        nomMicroBtn.setOnAction(e -> handleMicro(nomField, nomMicroBtn));
        imageMicroBtn.setOnAction(e -> handleMicro(imageField, imageMicroBtn));
        adresseMicroBtn.setOnAction(e -> handleMicro(adresseField, adresseMicroBtn));
        equipementMicroBtn.setOnAction(e -> handleMicro(equipementField, equipementMicroBtn));
        tarifMicroBtn.setOnAction(e -> handleMicro(tarifField, tarifMicroBtn));

        // Actions des boutons principaux
        modifierBtn.setOnAction(e -> modifierLogement());
        annulerBtn.setOnAction(e -> AdminDashboardController.loadPage("/fxml/Logements.fxml"));
    }

    @FXML
    private void handleSwitchClick() {
        disponibiliteToggle.setSelected(!disponibiliteToggle.isSelected());
        updateSwitchUI();
    }

    private void updateSwitchUI() {
        boolean isSelected = disponibiliteToggle.isSelected();
        if (isSelected) {
            switchBackground.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 13; -fx-padding: 2;");
            switchCircle.setTranslateX(24);
            disponibiliteLabel.setText("Disponible");
            disponibiliteLabel.setTextFill(Color.web("#2ecc71"));
        } else {
            switchBackground.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 13; -fx-padding: 2;");
            switchCircle.setTranslateX(0);
            disponibiliteLabel.setText("Non disponible");
            disponibiliteLabel.setTextFill(Color.web("#e74c3c"));
        }
    }

    /**
     * Gère l'enregistrement vocal et la transcription pour un champ donné.
     */
    private void handleMicro(TextField field, Button microBtn) {
        if (!VoskService.isInitialized()) {
            showAlert("Modèle non prêt", "Veuillez réessayer dans quelques instants.", Alert.AlertType.WARNING);
            return;
        }

        RotateTransition rotate = new RotateTransition(Duration.seconds(2), microBtn);
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.play();
        microBtn.setDisable(true);

        new Thread(() -> {
            try {
                byte[] audioData = AudioRecorder.recordAudio(5);
                String recognizedText = VoskService.recognize(audioData, 16000);

                Platform.runLater(() -> {
                    String resultat;
                    if (field == tarifField) {
                        Integer nombre = convertirMotsEnNombre(recognizedText);
                        if (nombre != null) {
                            resultat = nombre.toString();
                            field.setText(resultat);
                        } else {
                            showAlert("Conversion impossible",
                                    "La valeur reconnue n'a pas pu être convertie en nombre. Veuillez réessayer ou saisir manuellement.",
                                    Alert.AlertType.WARNING);
                        }
                    } else {
                        resultat = formatFirstLetterCapital(recognizedText);
                        field.setText(resultat);
                    }
                    rotate.stop();
                    microBtn.setRotate(0);
                    microBtn.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Erreur", "Reconnaissance échouée : " + e.getMessage(), Alert.AlertType.ERROR);
                    rotate.stop();
                    microBtn.setDisable(false);
                });
            }
        }).start();
    }
    private Integer convertirMotsEnNombre(String mots) {
        if (mots == null || mots.trim().isEmpty()) return null;

        java.util.Map<String, Integer> nombres = new java.util.HashMap<>();
        nombres.put("zéro", 0);
        nombres.put("un", 1); nombres.put("deux", 2); nombres.put("trois", 3);
        nombres.put("quatre", 4); nombres.put("cinq", 5); nombres.put("six", 6);
        nombres.put("sept", 7); nombres.put("huit", 8); nombres.put("neuf", 9);
        nombres.put("dix", 10); nombres.put("onze", 11); nombres.put("douze", 12);
        nombres.put("treize", 13); nombres.put("quatorze", 14); nombres.put("quinze", 15);
        nombres.put("seize", 16); nombres.put("dix-sept", 17); nombres.put("dix-huit", 18);
        nombres.put("dix-neuf", 19);

        java.util.Map<String, Integer> dizaines = new java.util.HashMap<>();
        dizaines.put("vingt", 20); dizaines.put("trente", 30); dizaines.put("quarante", 40);
        dizaines.put("cinquante", 50); dizaines.put("soixante", 60); dizaines.put("soixante-dix", 70);
        dizaines.put("quatre-vingt", 80); dizaines.put("quatre-vingt-dix", 90);

        String[] motsArray = mots.toLowerCase().trim().split("\\s+");
        int total = 0;
        int current = 0;

        for (String mot : motsArray) {
            if (mot.equals("cent")) {
                current = (current == 0) ? 100 : current * 100;
            } else if (mot.equals("mille")) {
                current = (current == 0) ? 1000 : current * 1000;
                total += current;
                current = 0;
            } else if (nombres.containsKey(mot)) {
                current += nombres.get(mot);
            } else if (dizaines.containsKey(mot)) {
                current += dizaines.get(mot);
            } else if (mot.matches("\\d+")) {
                current += Integer.parseInt(mot);
            }
            // Gérer les cas comme "vingt et un" (simplifié)
        }
        total += current;
        return total;
    }

    /**
     * Met la première lettre en majuscule et le reste en minuscules.
     */
    private String formatFirstLetterCapital(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void modifierLogement() {
        if (selectedLogement == null) return;

        // Validation des champs obligatoires
        String nom = nomField.getText().trim();
        String adresse = adresseField.getText().trim();
        String tarifStr = tarifField.getText().trim();

        if (nom.isEmpty()) {
            showAlert("Erreur", "Le nom est obligatoire.", Alert.AlertType.ERROR);
            return;
        }
        if (adresse.isEmpty()) {
            showAlert("Erreur", "L'adresse est obligatoire.", Alert.AlertType.ERROR);
            return;
        }
        if (tarifStr.isEmpty()) {
            showAlert("Erreur", "Le tarif est obligatoire.", Alert.AlertType.ERROR);
            return;
        }

        float tarif;
        try {
            tarif = Float.parseFloat(tarifStr);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le tarif doit être un nombre valide.", Alert.AlertType.ERROR);
            return;
        }

        // Mettre à jour l'objet logement avec capitalisation correcte
        selectedLogement.setType(typeComboBox.getValue());
        selectedLogement.setNom(formatFirstLetterCapital(nom));
        selectedLogement.setAdresse(formatFirstLetterCapital(adresse));
        selectedLogement.setCapacite(capaciteSpinner.getValue());
        selectedLogement.setTarif_nuit(tarif);
        selectedLogement.setEquipement(formatFirstLetterCapital(equipementField.getText().trim()));
        selectedLogement.setImage(imageField.getText().trim()); // L'image n'est pas capitalisée (peut être un chemin)
        selectedLogement.setDisponibilite(disponibiliteToggle.isSelected());

        try {
            servicelogement.modifier(selectedLogement);
            showAlert("Succès", "Logement modifié avec succès.", Alert.AlertType.INFORMATION);
            AdminDashboardController.loadPage("/fxml/Logements.fxml");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}