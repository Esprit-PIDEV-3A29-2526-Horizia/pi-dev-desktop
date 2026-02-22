package tn.esprit.controllers;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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

public class AjoutLogementController implements Initializable {

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextField nomField;

    @FXML
    private TextField imageField;

    @FXML
    private TextField adresseField;

    @FXML
    private TextField equipementField;

    @FXML
    private TextField tarifField;

    @FXML
    private Spinner<Integer> capaciteSpinner;

    @FXML
    private ToggleButton disponibiliteToggle;

    @FXML
    private Label disponibiliteLabel;

    @FXML
    private HBox switchBackground;

    @FXML
    private Circle switchCircle;

    @FXML
    private Button ajouterBtn;

    @FXML
    private Button annulerBtn;

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

    // CONSTANTES AJUSTÉES pour switch 50x26
    private static final double CIRCLE_TRANSLATE_OFF = 0;
    private static final double CIRCLE_TRANSLATE_ON = 24;
    private static final String COLOR_OFF = "#e74c3c";
    private static final String COLOR_ON = "#2ecc71";

    public AjoutLogementController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        typeComboBox.setPromptText("Sélectionner un type");

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2);
        capaciteSpinner.setValueFactory(valueFactory);

        nomMicroBtn.setOnAction(e -> handleMicro(nomField, nomMicroBtn));
        imageMicroBtn.setOnAction(e -> handleMicro(imageField, imageMicroBtn));
        adresseMicroBtn.setOnAction(e -> handleMicro(adresseField, adresseMicroBtn));
        equipementMicroBtn.setOnAction(e -> handleMicro(equipementField, equipementMicroBtn));
        tarifMicroBtn.setOnAction(e -> handleMicro(tarifField, tarifMicroBtn));

        ajouterBtn.setOnAction(e -> ajouterLogement());
        annulerBtn.setOnAction(e -> retourListe());

        initialiserSwitch();

        // Initialisation de Vosk (thread séparé)
        new Thread(() -> {
            try {
                String userDir = System.getProperty("user.dir");
                String modelPath = userDir + File.separator + "models" + File.separator + "vosk-model-fr-0.6-linto-2.2.0";
                File modelDir = new File(modelPath);
                if (!modelDir.exists() || !modelDir.isDirectory()) {
                    throw new IOException("Dossier modèle introuvable : " + modelPath);
                }

                VoskService.initModel(modelPath);
                System.out.println("✅ Modèle Vosk initialisé avec succès");
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur Vosk", e.getMessage()));
            }
        }).start();
    }
    private void initialiserSwitch() {
        disponibiliteToggle.setSelected(false);
        mettreAJourSwitchUI();
    }

    @FXML
    private void handleSwitchClick(MouseEvent event) {
        boolean nouveauEtat = !disponibiliteToggle.isSelected();
        disponibiliteToggle.setSelected(nouveauEtat);
        animerSwitch(nouveauEtat);
        mettreAJourLabelDisponibilite(nouveauEtat);
    }

    private void animerSwitch(boolean actif) {
        double targetTranslate = actif ? CIRCLE_TRANSLATE_ON : CIRCLE_TRANSLATE_OFF;
        String targetColor = actif ? COLOR_ON : COLOR_OFF;

        TranslateTransition translate = new TranslateTransition(Duration.millis(200), switchCircle);
        translate.setToX(targetTranslate);
        translate.play();

        switchBackground.setStyle(
                "-fx-background-color: " + targetColor + "; " +
                        "-fx-background-radius: 13; " +
                        "-fx-padding: 2;"
        );
    }

    private void mettreAJourSwitchUI() {
        boolean estDisponible = disponibiliteToggle.isSelected();
        double translateX = estDisponible ? CIRCLE_TRANSLATE_ON : CIRCLE_TRANSLATE_OFF;
        String color = estDisponible ? COLOR_ON : COLOR_OFF;

        switchCircle.setTranslateX(translateX);
        switchBackground.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-background-radius: 13; " +
                        "-fx-padding: 2;"
        );

        mettreAJourLabelDisponibilite(estDisponible);
    }

    private void mettreAJourLabelDisponibilite(boolean estDisponible) {
        if (estDisponible) {
            disponibiliteLabel.setText("Disponible");
            disponibiliteLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");
        } else {
            disponibiliteLabel.setText("Non disponible");
            disponibiliteLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        }
    }

    private void handleMicro(TextField field, Button microBtn) {
        if (!VoskService.isInitialized()) {
            showAlert(Alert.AlertType.WARNING, "Modèle non prêt",
                    "Le modèle de reconnaissance vocale est en cours de chargement. Veuillez réessayer dans quelques instants.");
            return;
        }

        RotateTransition rotate = new RotateTransition(Duration.seconds(2), microBtn);
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.play();
        microBtn.setDisable(true);

        new Thread(() -> {
            try {
                byte[] audioData = AudioRecorder.recordAudio(5); // 5 secondes
                String recognizedText = VoskService.recognize(audioData, 16000);

                Platform.runLater(() -> {
                    String formatted = formatFirstLetterCapital(recognizedText);
                    field.setText(formatted);
                    rotate.stop();
                    microBtn.setRotate(0);
                    microBtn.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Reconnaissance échouée : " + e.getMessage());
                    rotate.stop();
                    microBtn.setDisable(false);
                });
            }
        }).start();
    }
    private void ajouterLogement() {
        StringBuilder erreurs = new StringBuilder();

        if (typeComboBox.getValue() == null || typeComboBox.getValue().trim().isEmpty()) {
            erreurs.append("- Sélectionnez un type de logement.\n");
        }

        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            erreurs.append("- Le nom est obligatoire.\n");
        } else if (nom.length() > 255) {
            erreurs.append("- Le nom ne doit pas dépasser 255 caractères.\n");
        } else if (!Character.isUpperCase(nom.charAt(0))) {
            erreurs.append("- La première lettre du nom doit être en majuscule.\n");
        }

        String adresse = adresseField.getText().trim();
        if (adresse.isEmpty()) {
            erreurs.append("- L'adresse est obligatoire.\n");
        } else if (adresse.length() > 255) {
            erreurs.append("- L'adresse ne doit pas dépasser 255 caractères.\n");
        } else if (!Character.isUpperCase(adresse.charAt(0))) {
            erreurs.append("- La première lettre de l'adresse doit être en majuscule.\n");
        }

        int capacite = capaciteSpinner.getValue();
        if (capacite <= 2 || capacite > 20) {
            erreurs.append("- La capacité doit être supérieure à 2 et inférieure ou égale à 20.\n");
        }

        String equipement = equipementField.getText().trim();
        if (equipement.length() > 255) {
            erreurs.append("- Les équipements ne doivent pas dépasser 255 caractères.\n");
        } else if (!equipement.isEmpty() && !Character.isUpperCase(equipement.charAt(0))) {
            erreurs.append("- La première lettre des équipements doit être en majuscule.\n");
        }

        String tarifStr = tarifField.getText().trim();
        float tarif = 0;
        if (tarifStr.isEmpty()) {
            erreurs.append("- Le tarif est obligatoire.\n");
        } else {
            try {
                tarif = Float.parseFloat(tarifStr);
                if (tarif <= 50 || tarif > 3000) {
                    erreurs.append("- Le tarif doit être supérieur à 50 et inférieur ou égal à 3000.\n");
                }
            } catch (NumberFormatException e) {
                erreurs.append("- Le tarif doit être un nombre valide.\n");
            }
        }

        if (erreurs.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreurs de validation", erreurs.toString());
            return;
        }

        try {
            logement l = new logement();
            l.setType(typeComboBox.getValue());
            l.setNom(nom.toUpperCase());
            l.setImage(imageField.getText().trim());
            l.setAdresse(adresse.toUpperCase());
            l.setCapacite(capacite);
            l.setEquipement(equipement.toUpperCase());
            l.setTarif_nuit(tarif);
            l.setDisponibilite(disponibiliteToggle.isSelected());

            servicelogement.ajouter(l);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Logement ajouté avec succès !");
            retourListe();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    private void retourListe() {
        // CORRECTION: Utiliser AdminDashboardController au lieu de Dashboard
        AdminDashboardController.loadPage("/fxml/Logements.fxml");
    }
    private String formatFirstLetterCapital(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}