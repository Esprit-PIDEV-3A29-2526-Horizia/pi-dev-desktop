package tn.esprit.controllers.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import tn.esprit.entities.Location;
import tn.esprit.entities.Vehicule;
import tn.esprit.services.PlanningService;
import tn.esprit.services.VehiculeService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ClientPlanningController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private ComboBox<Vehicule> comboVehicule;
    @FXML private Label lblMoisAnnee;
    @FXML private GridPane gridCalendrier;
    @FXML private VBox vboxLegende;
    @FXML private Label lblMessage;

    private VehiculeService vehiculeService;
    private PlanningService planningService;
    private List<Location> locations;
    private int moisCourant;
    private int anneeCourante;
    private Vehicule vehiculeSelectionne;

    @FXML
    public void initialize() {
        vehiculeService = new VehiculeService();
        planningService = new PlanningService();

        LocalDate now = LocalDate.now();
        moisCourant = now.getMonthValue();
        anneeCourante = now.getYear();

        chargerVehicules();
        configurerCombo();
        afficherCalendrier();
    }

    private void chargerVehicules() {
        List<Vehicule> vehicules = vehiculeService.getAllVehicules();
        comboVehicule.getItems().clear();
        comboVehicule.getItems().addAll(vehicules);

        if (!vehicules.isEmpty()) {
            comboVehicule.setValue(vehicules.get(0));
            vehiculeSelectionne = vehicules.get(0);
        }
    }
    @FXML
    private void allerCatalogue() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client/CatalogueVoitures.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) comboVehicule.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 900));
            stage.setTitle("Horizia - Catalogue");
        } catch (IOException e) {
            System.err.println("Erreur navigation vers Catalogue : " + e.getMessage());
        }
    }

    @FXML
    private void allerMesReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client/MesReservations.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) comboVehicule.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 900));
            stage.setTitle("Horizia - Mes Réservations");
        } catch (IOException e) {
            System.err.println("Erreur navigation vers Mes Réservations : " + e.getMessage());
        }
    }
    private void configurerCombo() {
        comboVehicule.setCellFactory(lv -> new ListCell<Vehicule>() {
            @Override
            protected void updateItem(Vehicule v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                } else {
                    setText(v.getImmatriculation() + " - " +
                            (v.getPhoto() != null ? "🚗" : "") + " " +
                            String.format("%.0f TND/jour", v.getPrixParJour()));
                }
            }
        });

        comboVehicule.setButtonCell(new ListCell<Vehicule>() {
            @Override
            protected void updateItem(Vehicule v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                } else {
                    setText(v.getImmatriculation() + " - " +
                            String.format("%.0f TND/jour", v.getPrixParJour()));
                }
            }
        });

        comboVehicule.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                vehiculeSelectionne = newVal;
                chargerLocationsVehicule();
                afficherCalendrier();
            }
        });
    }

    private void chargerLocationsVehicule() {
        if (vehiculeSelectionne == null) return;

        // Charger toutes les locations du mois pour ce véhicule
        locations = planningService.getLocationsDuMois(anneeCourante, moisCourant).stream()
                .filter(l -> l.getIdVehicule() == vehiculeSelectionne.getIdVehicule())
                .filter(l -> !"annulée".equals(l.getStatut()) && !"no_show".equals(l.getStatut()))
                .toList();
    }

    private void afficherCalendrier() {
        if (gridCalendrier == null) return;

        gridCalendrier.getChildren().clear();
        gridCalendrier.getColumnConstraints().clear();
        gridCalendrier.getRowConstraints().clear();

        // En-têtes des jours
        String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            gridCalendrier.getColumnConstraints().add(cc);

            Label header = new Label(jours[i]);
            header.setMaxWidth(Double.MAX_VALUE);
            header.setAlignment(Pos.CENTER);
            header.setFont(Font.font("System", FontWeight.BOLD, 13));
            header.setPadding(new Insets(8));
            header.setStyle("-fx-background-color: " + (i >= 5 ? "#e8f4f8" : "#2c3e50") +
                    "; -fx-text-fill: " + (i >= 5 ? "#3498db" : "white") +
                    "; -fx-background-radius: 5;");
            gridCalendrier.add(header, i, 0);
        }

        YearMonth ym = YearMonth.of(anneeCourante, moisCourant);
        int nbJours = ym.lengthOfMonth();
        LocalDate premierJour = LocalDate.of(anneeCourante, moisCourant, 1);
        int debutSemaine = premierJour.getDayOfWeek().getValue() - 1;

        int row = 1;
        int col = debutSemaine;

        for (int jour = 1; jour <= nbJours; jour++) {
            LocalDate dateJour = LocalDate.of(anneeCourante, moisCourant, jour);
            VBox cellule = creerCelluleJour(jour, dateJour);

            gridCalendrier.add(cellule, col, row);
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }

        // Ajuster les lignes
        for (int r = 0; r <= row; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(r == 0 ? 35 : 70);
            rc.setPrefHeight(r == 0 ? 35 : 70);
            gridCalendrier.getRowConstraints().add(rc);
        }

        lblMoisAnnee.setText(YearMonth.of(anneeCourante, moisCourant)
                .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)).toUpperCase());
    }

    private VBox creerCelluleJour(int jour, LocalDate date) {
        VBox cell = new VBox(3);
        cell.setPadding(new Insets(5));
        cell.setMinHeight(70);
        cell.setAlignment(Pos.TOP_CENTER);

        boolean estAujourdhui = date.equals(LocalDate.now());
        boolean estWeekend = date.getDayOfWeek().getValue() >= 6;

        // Vérifier si le véhicule est occupé ce jour
        boolean estOccupe = false;
        String statutOccupe = "";
        String clientNom = "";

        if (locations != null) {
            for (Location loc : locations) {
                if (loc.getDateDebut() == null || loc.getDateFinPrev() == null) continue;
                LocalDate debut = loc.getDateDebut().toLocalDateTime().toLocalDate();
                LocalDate fin = loc.getDateFinPrev().toLocalDateTime().toLocalDate();

                if ((date.isEqual(debut) || date.isAfter(debut)) &&
                        (date.isEqual(fin) || date.isBefore(fin))) {
                    estOccupe = true;
                    statutOccupe = loc.getStatut();
                    clientNom = loc.getClientNomComplet() != null ?
                            loc.getClientNomComplet().split(" ")[0] : "Client";
                    break;
                }
            }
        }

        // Style de la cellule
        String bgColor = "white";
        String borderColor = "#dee2e6";

        if (estOccupe) {
            bgColor = "#fee2e2"; // rouge clair pour occupé
            borderColor = "#ef4444";
        } else if (estAujourdhui) {
            bgColor = "#fff3cd";
            borderColor = "#f59e0b";
        } else if (estWeekend) {
            bgColor = "#f8f9fa";
        }

        cell.setStyle("-fx-background-color:" + bgColor +
                "; -fx-border-color:" + borderColor +
                "; -fx-border-width:1; -fx-background-radius:5; -fx-border-radius:5;");

        // Numéro du jour
        Label lblJour = new Label(String.valueOf(jour));
        lblJour.setFont(Font.font("System", estAujourdhui ? FontWeight.BOLD : FontWeight.NORMAL, 12));
        String textColor = estOccupe ? "#ef4444" : (estAujourdhui ? "#f59e0b" : (estWeekend ? "#3498db" : "#2c3e50"));
        lblJour.setStyle("-fx-text-fill:" + textColor + ";");
        cell.getChildren().add(lblJour);

        // Badge si occupé
        if (estOccupe) {
            Label badge = new Label("🚗 " + clientNom);
            badge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;" +
                    "-fx-font-size: 8px; -fx-padding: 2 4; -fx-background-radius: 3;" +
                    "-fx-font-weight: bold;");
            cell.getChildren().add(badge);
        } else {
            Label dispo = new Label("✓ Libre");
            dispo.setStyle("-fx-font-size: 8px; -fx-text-fill: #10b981; -fx-font-weight: bold;");
            cell.getChildren().add(dispo);
        }

        // Tooltip
        if (estOccupe) {
            Tooltip tooltip = new Tooltip("Véhicule occupé ce jour");
            Tooltip.install(cell, tooltip);
        }

        return cell;
    }

    @FXML
    private void moisPrecedent() {
        moisCourant--;
        if (moisCourant < 1) {
            moisCourant = 12;
            anneeCourante--;
        }
        chargerLocationsVehicule();
        afficherCalendrier();
    }

    @FXML
    private void moisSuivant() {
        moisCourant++;
        if (moisCourant > 12) {
            moisCourant = 1;
            anneeCourante++;
        }
        chargerLocationsVehicule();
        afficherCalendrier();
    }

    @FXML
    private void revenirAujourdhui() {
        LocalDate now = LocalDate.now();
        moisCourant = now.getMonthValue();
        anneeCourante = now.getYear();
        chargerLocationsVehicule();
        afficherCalendrier();
    }

    /**
     * ✅ Méthode complète pour retourner à l'accueil client
     */
    @FXML
    public void retourAccueil(ActionEvent actionEvent) {
        try {
            // Charger le fichier FXML de l'accueil client
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/client/AccueilClient.fxml"));
            Parent root = loader.load();

            // Récupérer la stage actuelle
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Créer la nouvelle scène et l'appliquer
            Scene scene = new Scene(root, 1400, 800);
            stage.setScene(scene);
            stage.setTitle("Horizia - Location de Voitures");
            stage.centerOnScreen(); // Centrer la fenêtre

            System.out.println("✓ Retour à l'accueil client");

        } catch (IOException e) {
            System.err.println("[ClientPlanning] Erreur retour accueil: " + e.getMessage());
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de retourner à l'accueil.");
        } catch (Exception e) {
            System.err.println("[ClientPlanning] Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            afficherAlerte("Erreur", "Une erreur inattendue est survenue.");
        }
    }

    /**
     * Méthode utilitaire pour afficher une alerte
     */
    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Styliser l'alerte pour correspondre au thème sombre
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #0a0f1e;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait();
    }
}