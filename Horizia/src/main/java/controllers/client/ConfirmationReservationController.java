package controllers.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entities.Location;
import org.example.entities.Marque;
import org.example.entities.Modele;
import org.example.entities.Vehicule;
import org.example.services.EmailService;
import org.example.services.MarqueService;
import org.example.services.ModeleService;
import org.example.services.QRCodeService;
import org.example.services.VehiculeService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la page de confirmation de réservation.
 *
 * fx:id alignés avec ConfirmationReservation.fxml :
 *   imgQRCode, lblQRLoading, lblNumeroReservation,
 *   lblClient, lblVehicule, lblPeriode, lblMontant, lblAvance,
 *   txtEmail, lblStatutEmail
 *
 * onAction alignés :
 *   #sauvegarderQRCode, #envoyerEmail,
 *   #ouvrirMesReservations, #retourCatalogue
 */
public class ConfirmationReservationController implements Initializable {

    // ─── FXML — noms exacts du ConfirmationReservation.fxml ──────
    @FXML private ImageView imgQRCode;
    @FXML private Label     lblQRLoading;
    @FXML private Label     lblNumeroReservation;
    @FXML private Label     lblClient;
    @FXML private Label     lblVehicule;
    @FXML private Label     lblPeriode;
    @FXML private Label     lblMontant;
    @FXML private Label     lblAvance;
    @FXML private TextField txtEmail;
    @FXML private Label     lblStatutEmail;

    // ─── Services ────────────────────────────────────────────────
    private final VehiculeService vehiculeService = new VehiculeService();
    private final ModeleService   modeleService   = new ModeleService();
    private final MarqueService   marqueService   = new MarqueService();

    // ─── État ────────────────────────────────────────────────────
    private Location location;
    private String   vehiculeInfo = "";

    // ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // initialisation via setLocation()
    }

    /**
     * Injecté depuis ReservationVoitureController après le chargement FXML.
     */
    public void setLocation(Location location) {
        this.location = location;
        afficherDetails();
        genererQRCode();
    }

    // ─────────────────────────────────────────────────────────────
    // AFFICHAGE DÉTAILS
    // ─────────────────────────────────────────────────────────────

    private void afficherDetails() {
        if (location == null) return;

        // Numéro réservation
        if (lblNumeroReservation != null)
            lblNumeroReservation.setText("Réservation #" + location.getIdLocation());

        // Client
        if (lblClient != null)
            lblClient.setText(location.getClientNomComplet() != null
                    ? location.getClientNomComplet() : "—");

        // Montant et avance
        if (lblMontant != null)
            lblMontant.setText(String.format("%.3f TND", location.getMontantTotal()));
        if (lblAvance != null)
            lblAvance.setText(String.format("%.3f TND  (30%%)", location.getAvance()));

        // Période
        if (lblPeriode != null) {
            String debut = location.getDateDebut() != null
                    ? location.getDateDebut().toLocalDateTime().toLocalDate().toString() : "—";
            String fin = location.getDateFinPrev() != null
                    ? location.getDateFinPrev().toLocalDateTime().toLocalDate().toString() : "—";
            lblPeriode.setText(debut + "  →  " + fin);
        }

        // Véhicule (chargement en arrière-plan)
        new Thread(() -> {
            try {
                Vehicule v = vehiculeService.getVehiculeById(location.getIdVehicule());
                String info = "Véhicule #" + location.getIdVehicule();
                if (v != null) {
                    Modele m = modeleService.getModeleById(v.getIdModele());
                    String marqueNom = "";
                    String modeleNom = "";
                    if (m != null) {
                        modeleNom = m.getNomModele();
                        Marque marque = marqueService.getMarqueById(m.getIdMarque());
                        if (marque != null) marqueNom = marque.getNomMarque();
                    }
                    info = marqueNom + " " + modeleNom + " (" + v.getImmatriculation() + ")";
                }
                vehiculeInfo = info;
                final String finalInfo = info;
                Platform.runLater(() -> {
                    if (lblVehicule != null) lblVehicule.setText(finalInfo);
                });
            } catch (Exception e) {
                vehiculeInfo = "Véhicule #" + location.getIdVehicule();
                Platform.runLater(() -> {
                    if (lblVehicule != null) lblVehicule.setText(vehiculeInfo);
                });
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────
    // GÉNÉRATION QR CODE
    // ─────────────────────────────────────────────────────────────

    private void genererQRCode() {
        if (location == null) return;
        if (lblQRLoading != null) lblQRLoading.setVisible(true);
        if (imgQRCode   != null) imgQRCode.setVisible(false);

        new Thread(() -> {
            try {
                String dateDebut = location.getDateDebut() != null
                        ? location.getDateDebut().toLocalDateTime().toLocalDate().toString() : "—";
                String dateFin = location.getDateFinPrev() != null
                        ? location.getDateFinPrev().toLocalDateTime().toLocalDate().toString() : "—";

                // Attendre vehiculeInfo (max 3 s)
                int wait = 0;
                while (vehiculeInfo.isEmpty() && wait < 30) {
                    Thread.sleep(100);
                    wait++;
                }

                Image img = QRCodeService.genererQRCodeLocation(
                        location.getIdLocation(),
                        location.getClientNomComplet(),
                        vehiculeInfo,
                        dateDebut,
                        dateFin
                );

                Platform.runLater(() -> {
                    if (img != null && imgQRCode != null) {
                        imgQRCode.setImage(img);
                        imgQRCode.setVisible(true);
                    }
                    if (lblQRLoading != null) lblQRLoading.setVisible(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (lblQRLoading != null) lblQRLoading.setText("Erreur QR Code");
                });
                System.err.println("[ConfirmationCtrl] Erreur QR : " + e.getMessage());
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────
    // ACTIONS FXML
    // ─────────────────────────────────────────────────────────────

    @FXML
    private void envoyerEmail() {
        if (location == null) return;

        String email = txtEmail != null ? txtEmail.getText().trim() : "";
        if (email.isEmpty() || !email.contains("@")) {
            setStatutEmail("⚠️ Veuillez saisir une adresse email valide.", false);
            return;
        }

        setStatutEmail("📤 Envoi en cours...", null);

        new Thread(() -> {
            EmailService.ResultatEmail resultat =
                    EmailService.envoyerConfirmationLocation(location, email);
            Platform.runLater(() -> {
                if (resultat.succes()) {
                    setStatutEmail("✅ Email envoyé à " + email + " !", true);
                } else {
                    setStatutEmail("❌ Erreur : " + resultat.message(), false);
                }
            });
        }).start();
    }

    @FXML
    private void sauvegarderQRCode() {
        if (location == null || imgQRCode == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Sauvegarder le QR Code");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image PNG", "*.png"));
        fc.setInitialFileName("QR_Location_" + location.getIdLocation() + ".png");

        Stage stage = (Stage) imgQRCode.getScene().getWindow();
        java.io.File file = fc.showSaveDialog(stage);

        if (file != null) {
            String dateDebut = location.getDateDebut() != null
                    ? location.getDateDebut().toLocalDateTime().toLocalDate().toString() : "—";
            String dateFin = location.getDateFinPrev() != null
                    ? location.getDateFinPrev().toLocalDateTime().toLocalDate().toString() : "—";

            String chemin = QRCodeService.sauvegarderQRCode(
                    location.getIdLocation(),
                    location.getClientNomComplet(),
                    vehiculeInfo,
                    dateDebut,
                    dateFin,
                    file.getParent()
            );
            if (chemin != null) {
                setStatutEmail("✅ QR Code sauvegardé : " + file.getName(), true);
            } else {
                setStatutEmail("❌ Erreur lors de la sauvegarde.", false);
            }
        }
    }

    /** onAction="#ouvrirMesReservations" dans le FXML */
    @FXML
    private void ouvrirMesReservations() {
        naviguerVers("/views/client/MesReservations.fxml", "Horizia - Mes Réservations", 1400, 800);
    }

    /** onAction="#retourCatalogue" dans le FXML */
    @FXML
    private void retourCatalogue() {
        naviguerVers("/views/client/CatalogueVoitures.fxml", "Horizia - Catalogue", 1400, 800);
    }

    // ─────────────────────────────────────────────────────────────
    // UTILITAIRES
    // ─────────────────────────────────────────────────────────────

    private void setStatutEmail(String message, Boolean succes) {
        if (lblStatutEmail == null) return;
        lblStatutEmail.setText(message);
        if (succes == null) {
            lblStatutEmail.setStyle("-fx-text-fill: #f39c12;");
        } else if (succes) {
            lblStatutEmail.setStyle("-fx-text-fill: #27ae60;");
        } else {
            lblStatutEmail.setStyle("-fx-text-fill: #e74c3c;");
        }
        lblStatutEmail.setVisible(true);
    }

    private void naviguerVers(String fxmlPath, String titre, double w, double h) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Trouver le Stage depuis n'importe quel composant disponible
            Stage stage = null;
            if (imgQRCode != null && imgQRCode.getScene() != null)
                stage = (Stage) imgQRCode.getScene().getWindow();
            else if (lblClient != null && lblClient.getScene() != null)
                stage = (Stage) lblClient.getScene().getWindow();

            if (stage == null) {
                System.err.println("[ConfirmationCtrl] Stage null, navigation impossible.");
                return;
            }
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(titre);
        } catch (IOException e) {
            System.err.println("[ConfirmationCtrl] Erreur navigation : " + e.getMessage());
        }
    }
}