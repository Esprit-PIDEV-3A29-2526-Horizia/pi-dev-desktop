package controllers;

import org.example.entities.Location;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.services.PlanningService;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.util.*;

/**
 * Contrôleur de la page Planning & Calendrier
 * CORRIGÉ :
 *   - imports org.example.*
 *   - loc.getClientNomComplet()     (pas getNomClient())
 *   - loc.getClientTelephone()      (pas getTelClient())
 *   - loc.getIdVehicule()           (pas getVehicule() qui n'existe pas)
 *   - loc.getDateFinPrev()          (pas getDateFin())
 *   - Timestamp → .toLocalDateTime().toLocalDate()
 */
public class PlanningController implements Initializable, MainLayoutController.ControllerAvecLayout {

    // ✅ FIX 3 : Référence au MainLayoutController pour garder la sidebar
    private MainLayoutController mainLayoutController;

    @Override
    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─── FXML Components ─────────────────────────────────────────
    @FXML private Label lblMoisAnnee;
    @FXML private GridPane gridCalendrier;
    @FXML private VBox vboxAlertesRetour;
    @FXML private Label lblTauxOccupation;
    @FXML private Label lblCAMois;
    @FXML private Label lblNbLocations;
    @FXML private Label lblNbConflits;
    @FXML private VBox vboxStatuts;
    @FXML private ScrollPane scrollCalendrier;

    // ─── Data ─────────────────────────────────────────────────────
    private int moisCourant;
    private int anneeCourante;
    private List<Location> locationsDuMois;

    // ─────────────────────────────────────────────────────────────
    // INITIALISATION
    // ─────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LocalDate now = LocalDate.now();
        moisCourant   = now.getMonthValue();
        anneeCourante = now.getYear();
        chargerMois();
    }

    private void chargerMois() {
        locationsDuMois = PlanningService.getLocationsDuMois(anneeCourante, moisCourant);

        if (lblMoisAnnee != null) {
            lblMoisAnnee.setText(PlanningService.getNomMois(moisCourant, anneeCourante).toUpperCase());
        }

        afficherCalendrier();
        afficherStatistiques();
        afficherAlertesRetour();
    }

    // ─────────────────────────────────────────────────────────────
    // CALENDRIER
    // ─────────────────────────────────────────────────────────────

    private void afficherCalendrier() {
        if (gridCalendrier == null) return;
        gridCalendrier.getChildren().clear();
        gridCalendrier.getColumnConstraints().clear();
        gridCalendrier.getRowConstraints().clear();

        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            gridCalendrier.getColumnConstraints().add(cc);
        }

        String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            Label header = creerHeaderJour(jours[i], i >= 5);
            gridCalendrier.add(header, i, 0);
        }

        YearMonth ym = YearMonth.of(anneeCourante, moisCourant);
        int nbJours  = ym.lengthOfMonth();
        LocalDate premierJour = LocalDate.of(anneeCourante, moisCourant, 1);
        int debutSemaine = premierJour.getDayOfWeek().getValue() - 1;

        int row = 1;
        int col = debutSemaine;

        for (int jour = 1; jour <= nbJours; jour++) {
            LocalDate dateJour = LocalDate.of(anneeCourante, moisCourant, jour);
            List<Location> locsJour = getLocationsParDate(dateJour);
            VBox cellule = creerCelluleJour(jour, dateJour, locsJour);
            gridCalendrier.add(cellule, col, row);
            col++;
            if (col == 7) { col = 0; row++; }
        }

        for (int r = 0; r <= row; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(r == 0 ? 35 : 90);
            rc.setPrefHeight(r == 0 ? 35 : 90);
            gridCalendrier.getRowConstraints().add(rc);
        }
    }

    private Label creerHeaderJour(String nomJour, boolean weekend) {
        Label lbl = new Label(nomJour);
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        lbl.setPadding(new Insets(8));
        lbl.setStyle("-fx-background-color: " + (weekend ? "#e8f4f8" : "#2c3e50") +
                "; -fx-text-fill: " + (weekend ? "#3498db" : "white") +
                "; -fx-background-radius: 5;");
        return lbl;
    }

    private VBox creerCelluleJour(int jour, LocalDate date, List<Location> locs) {
        VBox cell = new VBox(3);
        cell.setPadding(new Insets(5));
        cell.setMinHeight(90);

        boolean estAujourdhui = date.equals(LocalDate.now());
        boolean estWeekend    = date.getDayOfWeek().getValue() >= 6;

        String bgColor     = estAujourdhui ? "#fff3cd" : (estWeekend ? "#f8f9fa" : "white");
        String borderColor = estAujourdhui ? "#f39c12" : "#dee2e6";
        cell.setStyle("-fx-background-color:" + bgColor +
                "; -fx-border-color:" + borderColor +
                "; -fx-border-width:1; -fx-background-radius:5; -fx-border-radius:5;");

        Label lblJour = new Label(String.valueOf(jour));
        lblJour.setFont(Font.font("System", estAujourdhui ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        lblJour.setStyle("-fx-text-fill:" + (estAujourdhui ? "#e67e22" : (estWeekend ? "#3498db" : "#2c3e50")) + ";");
        cell.getChildren().add(lblJour);

        int count = 0;
        for (Location loc : locs) {
            if (count >= 2) {
                Label plusLbl = new Label("+" + (locs.size() - 2) + " autres");
                plusLbl.setStyle("-fx-font-size:9px; -fx-text-fill:#95a5a6; -fx-font-style:italic;");
                cell.getChildren().add(plusLbl);
                break;
            }
            cell.getChildren().add(creerBadgeLocation(loc));
            count++;
        }

        // Tooltip
        if (!locs.isEmpty()) {
            StringBuilder tt = new StringBuilder();
            for (Location loc : locs) {
                // FIX : getClientNomComplet() + getIdVehicule()
                tt.append(PlanningService.getEmojiStatut(loc.getStatut()))
                        .append(" ").append(loc.getClientNomComplet() != null ? loc.getClientNomComplet() : "?")
                        .append(" | Véhicule #").append(loc.getIdVehicule())
                        .append("\n");
            }
            Tooltip tooltip = new Tooltip(tt.toString().trim());
            Tooltip.install(cell, tooltip);
        }

        return cell;
    }

    private Label creerBadgeLocation(Location loc) {
        String couleur = PlanningService.getCouleurStatut(loc.getStatut());
        String emoji   = PlanningService.getEmojiStatut(loc.getStatut());
        // FIX : getClientNomComplet() (pas getNomClient())
        String client  = loc.getClientNomComplet() != null ? loc.getClientNomComplet() : "?";
        String nom = client.length() > 10 ? client.substring(0, 10) + "…" : client;

        Label badge = new Label(emoji + " " + nom);
        badge.setMaxWidth(Double.MAX_VALUE);
        badge.setWrapText(false);
        badge.setStyle("-fx-background-color:" + couleur +
                "; -fx-text-fill:white; -fx-font-size:9px;" +
                " -fx-padding:2 5; -fx-background-radius:3; -fx-font-weight:bold;");
        return badge;
    }

    /**
     * Filtre les locations actives pour une date donnée
     * FIX : Timestamp → .toLocalDateTime().toLocalDate(), getDateFinPrev()
     */
    private List<Location> getLocationsParDate(LocalDate date) {
        List<Location> result = new ArrayList<>();
        for (Location loc : locationsDuMois) {
            // FIX : getDateFinPrev(), Timestamp → .toLocalDateTime().toLocalDate()
            if (loc.getDateDebut() == null || loc.getDateFinPrev() == null) continue;
            LocalDate debut = loc.getDateDebut().toLocalDateTime().toLocalDate();
            LocalDate fin   = loc.getDateFinPrev().toLocalDateTime().toLocalDate();
            if (!date.isBefore(debut) && !date.isAfter(fin)) {
                result.add(loc);
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    // STATISTIQUES
    // ─────────────────────────────────────────────────────────────

    private void afficherStatistiques() {
        double taux = PlanningService.calculerTauxOccupation(anneeCourante, moisCourant);
        if (lblTauxOccupation != null) {
            lblTauxOccupation.setText(String.format("%.1f%%", taux));
            String couleur = taux >= 70 ? "#27ae60" : (taux >= 40 ? "#f39c12" : "#e74c3c");
            lblTauxOccupation.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:" + couleur + ";");
        }

        double ca = PlanningService.calculerCADuMois(anneeCourante, moisCourant);
        if (lblCAMois != null) lblCAMois.setText(String.format("%.3f TND", ca));

        if (lblNbLocations != null) lblNbLocations.setText(String.valueOf(locationsDuMois.size()));

        Map<Integer, List<Location>> conflits = PlanningService.detecterConflitsDuMois(anneeCourante, moisCourant);
        if (lblNbConflits != null) {
            int nb = conflits.size();
            lblNbConflits.setText(String.valueOf(nb));
            lblNbConflits.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:" +
                    (nb > 0 ? "#e74c3c" : "#27ae60") + ";");
        }

        if (vboxStatuts != null) {
            vboxStatuts.getChildren().clear();
            Map<String, Integer> statuts = PlanningService.getStatutsParMois(anneeCourante, moisCourant);
            for (Map.Entry<String, Integer> entry : statuts.entrySet()) {
                HBox ligne = creerLigneStatut(entry.getKey(), entry.getValue(),
                        locationsDuMois.isEmpty() ? 0 : (entry.getValue() * 100.0 / locationsDuMois.size()));
                vboxStatuts.getChildren().add(ligne);
            }
        }
    }

    private HBox creerLigneStatut(String statut, int nb, double pct) {
        HBox ligne = new HBox(10);
        ligne.setAlignment(Pos.CENTER_LEFT);

        String couleur = PlanningService.getCouleurStatut(statut);
        String emoji   = PlanningService.getEmojiStatut(statut);

        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill:" + couleur + "; -fx-font-size:14px;");

        Label lblStatut = new Label(emoji + " " + statut.replace("_", " "));
        lblStatut.setMinWidth(120);
        lblStatut.setStyle("-fx-font-size:12px; -fx-text-fill:#2c3e50;");

        StackPane barre = new StackPane();
        barre.setPrefHeight(14);
        barre.setPrefWidth(150);
        barre.setStyle("-fx-background-color:#f0f0f0; -fx-background-radius:7;");

        Region fill = new Region();
        fill.setPrefHeight(14);
        fill.setPrefWidth(Math.min(pct * 1.5, 150));
        fill.setStyle("-fx-background-color:" + couleur + "; -fx-background-radius:7;");
        barre.getChildren().add(fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);

        Label lblNb = new Label(nb + " (" + String.format("%.0f%%", pct) + ")");
        lblNb.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + couleur + ";");

        ligne.getChildren().addAll(dot, lblStatut, barre, lblNb);
        return ligne;
    }

    // ─────────────────────────────────────────────────────────────
    // ALERTES RETOUR
    // ─────────────────────────────────────────────────────────────

    private void afficherAlertesRetour() {
        if (vboxAlertesRetour == null) return;
        vboxAlertesRetour.getChildren().clear();

        List<Location> retoursBientot = PlanningService.getLocationsQuiTerminentBientot(3);

        if (retoursBientot.isEmpty()) {
            Label lblVide = new Label("✅ Aucun retour prévu dans les 3 prochains jours");
            lblVide.setStyle("-fx-font-size:13px; -fx-text-fill:#27ae60; -fx-font-style:italic;");
            vboxAlertesRetour.getChildren().add(lblVide);
            return;
        }

        for (Location loc : retoursBientot) {
            vboxAlertesRetour.getChildren().add(creerAlerteRetour(loc));
        }
    }

    private HBox creerAlerteRetour(Location loc) {
        HBox alerte = new HBox(12);
        alerte.setAlignment(Pos.CENTER_LEFT);
        alerte.setPadding(new Insets(10, 15, 10, 15));
        alerte.setStyle("-fx-background-color:#fff3cd; -fx-background-radius:8;" +
                " -fx-border-color:#f39c12; -fx-border-radius:8; -fx-border-width:1;");

        Label icone = new Label("⚠️");
        icone.setStyle("-fx-font-size:18px;");

        VBox infos = new VBox(3);
        // FIX : getClientNomComplet() (pas getNomClient())
        Label lblNom = new Label(loc.getClientNomComplet() != null ? loc.getClientNomComplet() : "—");
        lblNom.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:#2c3e50;");

        // FIX : getIdVehicule() + getDateFinPrev() + Timestamp.toLocalDateTime().toLocalDate()
        String dateRetour = loc.getDateFinPrev() != null ?
                loc.getDateFinPrev().toLocalDateTime().toLocalDate().format(FMT) : "?";
        Label lblDetails = new Label("Véhicule #" + loc.getIdVehicule() + " → Retour : " + dateRetour);
        lblDetails.setStyle("-fx-font-size:11px; -fx-text-fill:#7f8c8d;");

        infos.getChildren().addAll(lblNom, lblDetails);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // FIX : getClientTelephone() (pas getTelClient())
        Label tel = new Label(loc.getClientTelephone() != null ? "📞 " + loc.getClientTelephone() : "");
        tel.setStyle("-fx-font-size:12px; -fx-text-fill:#3498db;");

        alerte.getChildren().addAll(icone, infos, spacer, tel);
        return alerte;
    }

    // ─────────────────────────────────────────────────────────────
    // NAVIGATION MOIS
    // ─────────────────────────────────────────────────────────────

    @FXML
    private void moisPrecedent() {
        moisCourant--;
        if (moisCourant < 1) { moisCourant = 12; anneeCourante--; }
        chargerMois();
    }

    @FXML
    private void moisSuivant() {
        moisCourant++;
        if (moisCourant > 12) { moisCourant = 1; anneeCourante++; }
        chargerMois();
    }

    @FXML private void allerAujourdhui() {
        LocalDate now = LocalDate.now();
        moisCourant   = now.getMonthValue();
        anneeCourante = now.getYear();
        chargerMois();
    }

    @FXML private void rafraichir() { chargerMois(); }

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
                Stage stage = (Stage) gridCalendrier.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception e) {
                System.err.println("[PlanningController] Erreur retour: " + e.getMessage());
            }
        }
    }
}