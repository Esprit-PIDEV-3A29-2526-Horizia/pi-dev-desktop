package controllers;

import org.example.entities.Location;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.services.ContratService;
import org.example.services.EmailService;
import org.example.services.LocationService;
import org.example.services.QRCodeService;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de la page Documents & Facturation
 * CORRIGÉ :
 *   - imports org.example.*
 *   - loc.getIdLocation()         (pas getId())
 *   - loc.getClientNomComplet()   (pas getNomClient())
 *   - loc.getClientTelephone()    (pas getTelClient())
 *   - loc.getDateFinPrev()        (pas getDateFin())
 *   - loc.getDateDebut().toLocalDateTime().toLocalDate()  (Timestamp !)
 *   - loc.getPrixParJour()        retourne double (pas BigDecimal)
 *   - loc.getAvance()             (pas getMontantAvance())
 *   - loc.getIdVehicule()         (pas getVehicule() qui n'existe pas)
 *   - getEmailClient()            n'existe pas → champ email saisi manuellement
 */
public class DocumentsController implements Initializable, MainLayoutController.ControllerAvecLayout {

    // ✅ FIX 3 : Référence au MainLayoutController pour garder la sidebar
    private MainLayoutController mainLayoutController;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─── FXML Components ─────────────────────────────────────────
    @FXML private TabPane tabPane;
    @FXML private Tab tabContrats;
    @FXML private Tab tabFactures;

    // Onglet Contrats
    @FXML private ComboBox<String> cmbLocationContrat;
    @FXML private Label lblClientContrat;
    @FXML private Label lblVehiculeContrat;
    @FXML private Label lblPeriodeContrat;
    @FXML private Label lblPrixJourContrat;
    @FXML private CheckBox chkGPS;
    @FXML private CheckBox chkSiegeBebe;
    @FXML private CheckBox chkAssurance;
    @FXML private CheckBox chkChauffeur;
    @FXML private CheckBox chkCarburant;
    @FXML private Label lblMontantBase;
    @FXML private Label lblMontantExtras;
    @FXML private Label lblMontantTotal;
    @FXML private Label lblAvance;
    @FXML private Label lblSolde;
    @FXML private TextArea txtApercuContrat;
    @FXML private TextField txtEmailClient;
    @FXML private ImageView imgQRCode;
    @FXML private Label lblStatutEmail;

    // Onglet Factures (retour)
    @FXML private ComboBox<String> cmbLocationFacture;
    @FXML private Label lblClientFacture;
    @FXML private Label lblVehiculeFacture;
    @FXML private Spinner<Integer> spnHeuresRetard;
    @FXML private CheckBox chkCarburantManquant;
    @FXML private CheckBox chkDommagesLegers;
    @FXML private CheckBox chkDommagesGraves;
    @FXML private Label lblPenalites;
    @FXML private Label lblTotalFacture;
    @FXML private Label lblSoldeFacture;
    @FXML private TextArea txtNotesRetour;
    @FXML private TextArea txtApercuFacture;
    @FXML private TextField txtEmailFacture;
    @FXML private Label lblStatutEmailFacture;

    // ─── Data ─────────────────────────────────────────────────────
    private List<Location> locations;

    @Override
    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }
    private Location locationSelectionneeContrat;
    private Location locationSelectionneeFacture;

    // ─────────────────────────────────────────────────────────────
    // INITIALISATION
    // ─────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerLocations();
        configurerSpinner();
        configurerListeners();
    }

    private void chargerLocations() {
        LocationService service = new LocationService();
        locations = service.getAllLocations();

        cmbLocationContrat.getItems().clear();
        cmbLocationFacture.getItems().clear();

        for (Location loc : locations) {
            // FIX : getIdLocation() + getClientNomComplet()
            String label = "#" + loc.getIdLocation() + " - " + loc.getClientNomComplet() +
                    " (" + (loc.getStatut() != null ? loc.getStatut() : "?") + ")";
            cmbLocationContrat.getItems().add(label);
            if ("en_cours".equals(loc.getStatut()) || "terminée".equals(loc.getStatut())) {
                cmbLocationFacture.getItems().add(label);
            }
        }
    }

    private void configurerSpinner() {
        SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 72, 0);
        if (spnHeuresRetard != null) spnHeuresRetard.setValueFactory(factory);
    }

    private void configurerListeners() {
        if (chkGPS != null)       chkGPS.setOnAction(e -> recalculerMontantContrat());
        if (chkSiegeBebe != null)  chkSiegeBebe.setOnAction(e -> recalculerMontantContrat());
        if (chkAssurance != null)  chkAssurance.setOnAction(e -> recalculerMontantContrat());
        if (chkChauffeur != null)  chkChauffeur.setOnAction(e -> recalculerMontantContrat());
        if (chkCarburant != null)  chkCarburant.setOnAction(e -> recalculerMontantContrat());

        if (spnHeuresRetard != null)       spnHeuresRetard.valueProperty().addListener((o, ov, nv) -> recalculerPenalites());
        if (chkCarburantManquant != null)   chkCarburantManquant.setOnAction(e -> recalculerPenalites());
        if (chkDommagesLegers != null)      chkDommagesLegers.setOnAction(e -> recalculerPenalites());
        if (chkDommagesGraves != null)      chkDommagesGraves.setOnAction(e -> recalculerPenalites());
    }

    // ─────────────────────────────────────────────────────────────
    // ONGLET CONTRATS
    // ─────────────────────────────────────────────────────────────

    @FXML
    private void onLocationContratChanged() {
        int idx = cmbLocationContrat.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= locations.size()) return;

        locationSelectionneeContrat = locations.get(idx);
        afficherDetailsContrat(locationSelectionneeContrat);
        recalculerMontantContrat();
        genererApercuContrat();
        genererQRCode();
    }

    private void afficherDetailsContrat(Location loc) {
        // FIX : getClientNomComplet() (pas getNomClient())
        if (lblClientContrat != null)
            lblClientContrat.setText(loc.getClientNomComplet() != null ? loc.getClientNomComplet() : "—");
        // FIX : getIdVehicule() — pas de getVehicule() dans Location.java
        if (lblVehiculeContrat != null)
            lblVehiculeContrat.setText("Véhicule #" + loc.getIdVehicule());
        // FIX : getDateFinPrev() + Timestamp.toLocalDateTime().toLocalDate()
        if (lblPeriodeContrat != null) {
            String debut = loc.getDateDebut() != null ?
                    loc.getDateDebut().toLocalDateTime().toLocalDate().format(FMT) : "?";
            String fin = loc.getDateFinPrev() != null ?
                    loc.getDateFinPrev().toLocalDateTime().toLocalDate().format(FMT) : "?";
            lblPeriodeContrat.setText(debut + " → " + fin);
        }
        // FIX : getPrixParJour() retourne double (pas BigDecimal), pas de .doubleValue()
        if (lblPrixJourContrat != null)
            lblPrixJourContrat.setText(ContratService.formaterMontant(loc.getPrixParJour()));
        // FIX : getEmailClient() n'existe pas → ne pas préremplir (l'utilisateur saisit l'email)
        // txtEmailClient reste vide (l'agent le saisit manuellement)
    }

    private void recalculerMontantContrat() {
        if (locationSelectionneeContrat == null) return;

        // FIX : Timestamp → .toLocalDateTime().toLocalDate()
        LocalDate debut = locationSelectionneeContrat.getDateDebut() != null ?
                locationSelectionneeContrat.getDateDebut().toLocalDateTime().toLocalDate() : LocalDate.now();
        // FIX : getDateFinPrev() (pas getDateFin())
        LocalDate fin = locationSelectionneeContrat.getDateFinPrev() != null ?
                locationSelectionneeContrat.getDateFinPrev().toLocalDateTime().toLocalDate() : LocalDate.now().plusDays(1);
        // FIX : getPrixParJour() retourne double directement
        double prixJour = locationSelectionneeContrat.getPrixParJour();

        long nbJours       = ContratService.calculerNbJours(debut, fin);
        double montantBase = ContratService.calculerMontantBase(prixJour, debut, fin);
        double extras      = ContratService.calculerMontantExtras(nbJours,
                chkGPS != null && chkGPS.isSelected(),
                chkSiegeBebe != null && chkSiegeBebe.isSelected(),
                chkAssurance != null && chkAssurance.isSelected(),
                chkChauffeur != null && chkChauffeur.isSelected(),
                chkCarburant != null && chkCarburant.isSelected());
        double total = montantBase + extras;
        // FIX : getAvance() (pas getMontantAvance().doubleValue())
        double avance = locationSelectionneeContrat.getAvance();
        double solde  = ContratService.calculerSolde(total, avance);

        if (lblMontantBase   != null) lblMontantBase.setText(ContratService.formaterMontant(montantBase));
        if (lblMontantExtras != null) lblMontantExtras.setText(ContratService.formaterMontant(extras));
        if (lblMontantTotal  != null) lblMontantTotal.setText(ContratService.formaterMontant(total));
        if (lblAvance        != null) lblAvance.setText(ContratService.formaterMontant(avance));
        if (lblSolde         != null) {
            lblSolde.setText(ContratService.formaterMontant(solde));
            lblSolde.setStyle("-fx-font-weight:bold; -fx-font-size:16px; -fx-text-fill:" +
                    (solde > 0 ? "#e74c3c" : "#27ae60") + ";");
        }
    }

    @FXML
    private void genererApercuContrat() {
        if (locationSelectionneeContrat == null) return;
        String contrat = ContratService.genererContratTexte(
                locationSelectionneeContrat,
                chkGPS != null && chkGPS.isSelected(),
                chkSiegeBebe != null && chkSiegeBebe.isSelected(),
                chkAssurance != null && chkAssurance.isSelected(),
                chkChauffeur != null && chkChauffeur.isSelected(),
                chkCarburant != null && chkCarburant.isSelected()
        );
        if (txtApercuContrat != null) txtApercuContrat.setText(contrat);
    }

    private void genererQRCode() {
        if (locationSelectionneeContrat == null) return;
        Location loc = locationSelectionneeContrat;
        // FIX : getIdLocation(), getClientNomComplet(), getIdVehicule(), getDateFinPrev()
        String dateDebut = loc.getDateDebut() != null ?
                loc.getDateDebut().toLocalDateTime().toLocalDate().format(FMT) : "—";
        String dateFin = loc.getDateFinPrev() != null ?
                loc.getDateFinPrev().toLocalDateTime().toLocalDate().format(FMT) : "—";

        Image qr = QRCodeService.genererQRCodeLocation(
                loc.getIdLocation(),
                loc.getClientNomComplet() != null ? loc.getClientNomComplet() : "—",
                "Véhicule #" + loc.getIdVehicule(),
                dateDebut,
                dateFin
        );
        if (imgQRCode != null && qr != null) imgQRCode.setImage(qr);
    }

    @FXML
    private void sauvegarderContrat() {
        if (locationSelectionneeContrat == null) {
            showAlert(Alert.AlertType.WARNING, "Sélectionnez une location d'abord.");
            return;
        }
        String contenu = txtApercuContrat != null ? txtApercuContrat.getText() : "";
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir le dossier de sauvegarde");
        File dir = chooser.showDialog(null);
        if (dir != null) {
            // FIX : getIdLocation()
            String chemin = ContratService.sauvegarderContrat(
                    contenu, locationSelectionneeContrat.getIdLocation(), dir.getAbsolutePath());
            if (chemin != null)
                showAlert(Alert.AlertType.INFORMATION, "Contrat sauvegardé :\n" + chemin);
            else
                showAlert(Alert.AlertType.ERROR, "Erreur lors de la sauvegarde.");
        }
    }

    @FXML
    private void sauvegarderQRCode() {
        if (locationSelectionneeContrat == null) {
            showAlert(Alert.AlertType.WARNING, "Sélectionnez une location d'abord.");
            return;
        }
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir le dossier de sauvegarde du QR Code");
        File dir = chooser.showDialog(null);
        if (dir != null) {
            Location loc = locationSelectionneeContrat;
            String dateDebut = loc.getDateDebut() != null ?
                    loc.getDateDebut().toLocalDateTime().toLocalDate().format(FMT) : "—";
            String dateFin = loc.getDateFinPrev() != null ?
                    loc.getDateFinPrev().toLocalDateTime().toLocalDate().format(FMT) : "—";

            String chemin = QRCodeService.sauvegarderQRCode(
                    loc.getIdLocation(),
                    loc.getClientNomComplet() != null ? loc.getClientNomComplet() : "—",
                    "Véhicule #" + loc.getIdVehicule(),
                    dateDebut, dateFin,
                    dir.getAbsolutePath()
            );
            if (chemin != null)
                showAlert(Alert.AlertType.INFORMATION, "QR Code sauvegardé :\n" + chemin);
            else
                showAlert(Alert.AlertType.ERROR, "Erreur lors de la sauvegarde du QR Code.");
        }
    }

    @FXML
    private void envoyerEmailContrat() {
        if (locationSelectionneeContrat == null) {
            showAlert(Alert.AlertType.WARNING, "Sélectionnez une location d'abord.");
            return;
        }
        String email = txtEmailClient != null ? txtEmailClient.getText().trim() : "";
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Entrez l'adresse email du client.");
            return;
        }
        if (lblStatutEmail != null) {
            lblStatutEmail.setText("📤 Envoi en cours...");
            lblStatutEmail.setStyle("-fx-text-fill: #f39c12;");
        }
        new Thread(() -> {
            EmailService.ResultatEmail resultat = EmailService.envoyerContrat(
                    locationSelectionneeContrat, email, null);
            Platform.runLater(() -> {
                if (lblStatutEmail != null) {
                    lblStatutEmail.setText(resultat.succes() ? "✅ " + resultat.message() : "❌ " + resultat.message());
                    lblStatutEmail.setStyle("-fx-text-fill: " + (resultat.succes() ? "#27ae60" : "#e74c3c") + ";");
                }
            });
        }).start();
    }

    // ─────────────────────────────────────────────────────────────
    // ONGLET FACTURES
    // ─────────────────────────────────────────────────────────────

    @FXML
    private void onLocationFactureChanged() {
        int idx = cmbLocationFacture.getSelectionModel().getSelectedIndex();
        List<Location> locsFiltrees = locations.stream()
                .filter(l -> "en_cours".equals(l.getStatut()) || "terminée".equals(l.getStatut()))
                .toList();
        if (idx < 0 || idx >= locsFiltrees.size()) return;

        locationSelectionneeFacture = locsFiltrees.get(idx);
        afficherDetailsFacture(locationSelectionneeFacture);
        recalculerPenalites();
    }

    private void afficherDetailsFacture(Location loc) {
        if (lblClientFacture != null)
            lblClientFacture.setText(loc.getClientNomComplet() != null ? loc.getClientNomComplet() : "—");
        if (lblVehiculeFacture != null)
            lblVehiculeFacture.setText("Véhicule #" + loc.getIdVehicule());
        // email non stocké dans Location → l'agent saisit manuellement
    }

    private void recalculerPenalites() {
        if (locationSelectionneeFacture == null) return;

        int heures = spnHeuresRetard != null ? spnHeuresRetard.getValue() : 0;
        double penalites = ContratService.calculerPenaliteRetard(
                heures,
                chkCarburantManquant != null && chkCarburantManquant.isSelected(),
                chkDommagesLegers    != null && chkDommagesLegers.isSelected(),
                chkDommagesGraves    != null && chkDommagesGraves.isSelected()
        );

        LocalDate debut = locationSelectionneeFacture.getDateDebut() != null ?
                locationSelectionneeFacture.getDateDebut().toLocalDateTime().toLocalDate() : LocalDate.now();
        LocalDate fin   = locationSelectionneeFacture.getDateFinPrev() != null ?
                locationSelectionneeFacture.getDateFinPrev().toLocalDateTime().toLocalDate() : LocalDate.now();
        double montantBase = ContratService.calculerMontantBase(locationSelectionneeFacture.getPrixParJour(), debut, fin);
        double total   = montantBase + penalites;
        double avance  = locationSelectionneeFacture.getAvance();
        double solde   = ContratService.calculerSolde(total, avance);

        if (lblPenalites    != null) lblPenalites.setText(ContratService.formaterMontant(penalites));
        if (lblTotalFacture != null) lblTotalFacture.setText(ContratService.formaterMontant(total));
        if (lblSoldeFacture != null) {
            lblSoldeFacture.setText(ContratService.formaterMontant(solde));
            lblSoldeFacture.setStyle("-fx-font-weight:bold; -fx-font-size:16px; -fx-text-fill:" +
                    (solde > 0 ? "#e74c3c" : "#27ae60") + ";");
        }
        genererApercuFacture(montantBase, avance, penalites);
    }

    @FXML
    private void genererApercuFacture() {
        if (locationSelectionneeFacture == null) return;
        double avance = locationSelectionneeFacture.getAvance();
        LocalDate debut = locationSelectionneeFacture.getDateDebut() != null ?
                locationSelectionneeFacture.getDateDebut().toLocalDateTime().toLocalDate() : LocalDate.now();
        LocalDate fin   = locationSelectionneeFacture.getDateFinPrev() != null ?
                locationSelectionneeFacture.getDateFinPrev().toLocalDateTime().toLocalDate() : LocalDate.now();
        double montantBase = ContratService.calculerMontantBase(locationSelectionneeFacture.getPrixParJour(), debut, fin);

        int heures = spnHeuresRetard != null ? spnHeuresRetard.getValue() : 0;
        double penalites = ContratService.calculerPenaliteRetard(
                heures,
                chkCarburantManquant != null && chkCarburantManquant.isSelected(),
                chkDommagesLegers    != null && chkDommagesLegers.isSelected(),
                chkDommagesGraves    != null && chkDommagesGraves.isSelected()
        );
        genererApercuFacture(montantBase, avance, penalites);
    }

    private void genererApercuFacture(double montantBase, double avance, double penalites) {
        if (locationSelectionneeFacture == null) return;
        String notes = txtNotesRetour != null ? txtNotesRetour.getText() : "";
        String facture = ContratService.genererFactureTexte(
                locationSelectionneeFacture, montantBase, avance, penalites, notes);
        if (txtApercuFacture != null) txtApercuFacture.setText(facture);
    }

    @FXML
    private void sauvegarderFacture() {
        if (locationSelectionneeFacture == null) {
            showAlert(Alert.AlertType.WARNING, "Sélectionnez une location d'abord.");
            return;
        }
        String contenu = txtApercuFacture != null ? txtApercuFacture.getText() : "";
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir le dossier de sauvegarde");
        File dir = chooser.showDialog(null);
        if (dir != null) {
            String chemin = ContratService.sauvegarderContrat(
                    contenu, locationSelectionneeFacture.getIdLocation(), dir.getAbsolutePath());
            if (chemin != null)
                showAlert(Alert.AlertType.INFORMATION, "Facture sauvegardée :\n" + chemin);
            else
                showAlert(Alert.AlertType.ERROR, "Erreur lors de la sauvegarde.");
        }
    }

    @FXML
    private void envoyerEmailFacture() {
        if (locationSelectionneeFacture == null) {
            showAlert(Alert.AlertType.WARNING, "Sélectionnez une location d'abord.");
            return;
        }
        String email = txtEmailFacture != null ? txtEmailFacture.getText().trim() : "";
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Entrez l'adresse email du client.");
            return;
        }
        if (lblStatutEmailFacture != null) {
            lblStatutEmailFacture.setText("📤 Envoi en cours...");
            lblStatutEmailFacture.setStyle("-fx-text-fill: #f39c12;");
        }

        LocalDate debut = locationSelectionneeFacture.getDateDebut() != null ?
                locationSelectionneeFacture.getDateDebut().toLocalDateTime().toLocalDate() : LocalDate.now();
        LocalDate fin   = locationSelectionneeFacture.getDateFinPrev() != null ?
                locationSelectionneeFacture.getDateFinPrev().toLocalDateTime().toLocalDate() : LocalDate.now();
        double montantBase = ContratService.calculerMontantBase(locationSelectionneeFacture.getPrixParJour(), debut, fin);
        double avance = locationSelectionneeFacture.getAvance();
        int heures = spnHeuresRetard != null ? spnHeuresRetard.getValue() : 0;
        double penalites = ContratService.calculerPenaliteRetard(heures,
                chkCarburantManquant != null && chkCarburantManquant.isSelected(),
                chkDommagesLegers    != null && chkDommagesLegers.isSelected(),
                chkDommagesGraves    != null && chkDommagesGraves.isSelected());
        final double total = montantBase + penalites;
        final double solde = ContratService.calculerSolde(total, avance);

        new Thread(() -> {
            EmailService.ResultatEmail resultat = EmailService.envoyerFactureFinale(
                    locationSelectionneeFacture, email, total, avance, solde);
            Platform.runLater(() -> {
                if (lblStatutEmailFacture != null) {
                    lblStatutEmailFacture.setText(resultat.succes() ? "✅ " + resultat.message() : "❌ " + resultat.message());
                    lblStatutEmailFacture.setStyle("-fx-text-fill: " + (resultat.succes() ? "#27ae60" : "#e74c3c") + ";");
                }
            });
        }).start();
    }

    // ─────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────

    @FXML
    private void retourDashboard() {
        // ✅ FIX 3 : Naviguer via MainLayoutController pour garder la sidebar
        if (mainLayoutController != null) {
            mainLayoutController.naviguerVers("/views/Dashboardview.fxml");
        } else {
            // Fallback si le contrôleur parent n'est pas disponible
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/views/MainLayout.fxml"));
                Stage stage = (Stage) tabPane.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception e) {
                System.err.println("[DocumentsController] Erreur retour: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle("Horizia - Documents");
        alert.showAndWait();
    }
}