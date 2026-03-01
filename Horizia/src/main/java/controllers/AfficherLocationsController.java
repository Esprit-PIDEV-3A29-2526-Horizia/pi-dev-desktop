package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entities.Location;
import org.example.entities.Vehicule;
import org.example.services.LocationService;
import org.example.services.VehiculeService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class AfficherLocationsController {

    @FXML private TextField txtRecherche;
    @FXML private FlowPane flowPaneLocations;
    @FXML private Label lblNbResultats;

    private LocationService locationService;
    private VehiculeService vehiculeService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private List<Location> locationsActuelles;

    @FXML
    public void initialize() {
        locationService = new LocationService();
        vehiculeService = new VehiculeService();

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Interface Afficher Locations (Cards) - Chargée");
        System.out.println("═══════════════════════════════════════════════");

        chargerLocations();
    }

    private void chargerLocations() {
        locationsActuelles = locationService.getAllLocations();
        afficherLocations(locationsActuelles);
        lblNbResultats.setText(locationsActuelles.size() + " location(s)");
        System.out.println("✓ " + locationsActuelles.size() + " location(s) chargée(s)");
    }

    private void afficherLocations(List<Location> locations) {
        flowPaneLocations.getChildren().clear();

        if (locations.isEmpty()) {
            // Afficher un message si aucune location
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setStyle("-fx-background-color: white; -fx-padding: 60; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);");

            Label iconLabel = new Label("📭");
            iconLabel.setStyle("-fx-font-size: 48px;");

            Label messageLabel = new Label("Aucune location trouvée");
            messageLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");

            Label hintLabel = new Label("Essayez de modifier vos critères de recherche");
            hintLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6;");

            emptyBox.getChildren().addAll(iconLabel, messageLabel, hintLabel);
            flowPaneLocations.getChildren().add(emptyBox);
            return;
        }

        // Créer une carte pour chaque location
        for (Location location : locations) {
            VBox card = creerCarteLocation(location);
            flowPaneLocations.getChildren().add(card);
        }
    }

    private VBox creerCarteLocation(Location location) {
        VBox card = new VBox(15);
        card.setPrefWidth(330);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20));

        // Style de base de la carte
        String couleurStatut = getCouleurStatut(location.getStatut());
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 3); " +
                        "-fx-border-color: " + couleurStatut + "; " +
                        "-fx-border-width: 0 0 0 5; " +
                        "-fx-border-radius: 12; " +
                        "-fx-cursor: hand;"
        );

        // Effet hover
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5); " +
                        "-fx-border-color: " + couleurStatut + "; " +
                        "-fx-border-width: 0 0 0 5; " +
                        "-fx-border-radius: 12; " +
                        "-fx-cursor: hand;"
        ));

        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 3); " +
                        "-fx-border-color: " + couleurStatut + "; " +
                        "-fx-border-width: 0 0 0 5; " +
                        "-fx-border-radius: 12; " +
                        "-fx-cursor: hand;"
        ));

        // Header de la carte avec le statut
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label statutLabel = new Label(formatStatut(location.getStatut()));
        statutLabel.setStyle(
                "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + couleurStatut + "; " +
                        "-fx-background-color: " + getCouleurFondStatut(location.getStatut()) + "; " +
                        "-fx-padding: 5 12; " +
                        "-fx-background-radius: 12;"
        );

        header.getChildren().add(statutLabel);

        // Nom du client (gros et en gras)
        Label clientLabel = new Label("👤 " + location.getClientNomComplet());
        clientLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        clientLabel.setWrapText(true);

        // Téléphone
        Label telLabel = new Label("📞 " + location.getClientTelephone());
        telLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        // Véhicule
        Vehicule vehicule = vehiculeService.getVehiculeById(location.getIdVehicule());
        String vehiculeInfo = vehicule != null ? vehicule.getImmatriculation() : "Véhicule indisponible";
        Label vehiculeLabel = new Label("🚗 " + vehiculeInfo);
        vehiculeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e; -fx-font-weight: bold;");

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #e0e0e0;");

        // Dates
        Label dateDebutLabel = new Label("📅 Du : " + dateFormat.format(location.getDateDebut()));
        dateDebutLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        Label dateFinLabel = new Label("📅 Au : " + dateFormat.format(location.getDateFinPrev()));
        dateFinLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #e0e0e0;");

        // Montant
        Label montantLabel = new Label("💰 " + String.format("%.3f TND", location.getMontantTotal()));
        montantLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        // Bouton détails
        Button btnDetails = new Button("📄 Voir Détails");
        btnDetails.setPrefWidth(290);
        btnDetails.setStyle(
                "-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 0; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        btnDetails.setOnAction(e -> afficherDetailsLocation(location));

        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(
                header,
                clientLabel,
                telLabel,
                vehiculeLabel,
                sep1,
                dateDebutLabel,
                dateFinLabel,
                sep2,
                montantLabel,
                btnDetails
        );

        return card;
    }

    private String getCouleurStatut(String statut) {
        switch (statut.toLowerCase()) {
            case "réservée": return "#2980b9";
            case "en_cours": return "#f39c12";
            case "terminée": return "#27ae60";
            case "annulée": return "#e74c3c";
            case "no_show": return "#95a5a6";
            default: return "#7f8c8d";
        }
    }

    private String getCouleurFondStatut(String statut) {
        switch (statut.toLowerCase()) {
            case "réservée": return "#e8f4f8";
            case "en_cours": return "#fff9e6";
            case "terminée": return "#d5f4e6";
            case "annulée": return "#fadbd8";
            case "no_show": return "#f5f5f5";
            default: return "#f8f9fa";
        }
    }

    private String formatStatut(String statut) {
        switch (statut.toLowerCase()) {
            case "réservée": return "🔵 RÉSERVÉE";
            case "en_cours": return "🟡 EN COURS";
            case "terminée": return "🟢 TERMINÉE";
            case "annulée": return "🔴 ANNULÉE";
            case "no_show": return "⚪ NO SHOW";
            default: return statut.toUpperCase();
        }
    }

    @FXML
    private void rechercherLocations() {
        String recherche = txtRecherche.getText();

        if (recherche == null || recherche.trim().isEmpty()) {
            afficherTout();
            return;
        }

        System.out.println("→ Recherche : " + recherche);

        List<Location> resultats = locationService.rechercherParClient(recherche.trim());
        afficherLocations(resultats);
        locationsActuelles = resultats;

        lblNbResultats.setText(resultats.size() + " location(s)");
        System.out.println("✓ " + resultats.size() + " résultat(s)");
    }

    @FXML
    private void afficherTout() {
        txtRecherche.clear();
        chargerLocations();
    }

    @FXML
    private void actualiser() {
        System.out.println("🔄 Actualisation des données...");
        chargerLocations();
    }

    @FXML
    private void retour() {
        System.out.println("🔙 Retour au menu principal");
        Stage stage = (Stage) flowPaneLocations.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void exporterCSV() {
        if (locationsActuelles == null || locationsActuelles.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Export CSV");
            alert.setHeaderText("Aucune donnée à exporter");
            alert.setContentText("La liste des locations est vide.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les locations en CSV");
        fileChooser.setInitialFileName("locations_" + System.currentTimeMillis() + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );

        Stage stage = (Stage) flowPaneLocations.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // En-têtes
                writer.write("Client,Téléphone,CIN,Véhicule,Date Début,Date Fin Prévue,Montant Total,Statut\n");

                // Données
                for (Location loc : locationsActuelles) {
                    Vehicule veh = vehiculeService.getVehiculeById(loc.getIdVehicule());
                    String vehiculeInfo = veh != null ? veh.getImmatriculation() : "N/A";

                    writer.write(String.format("%s,%s,%s,%s,%s,%s,%.3f,%s\n",
                            loc.getClientNomComplet(),
                            loc.getClientTelephone(),
                            loc.getClientCin() != null ? loc.getClientCin() : "",
                            vehiculeInfo,
                            dateFormat.format(loc.getDateDebut()),
                            dateFormat.format(loc.getDateFinPrev()),
                            loc.getMontantTotal(),
                            loc.getStatut()
                    ));
                }

                System.out.println("✓ Export CSV réussi : " + file.getAbsolutePath());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText("Les données ont été exportées");
                alert.setContentText("Fichier : " + file.getName());
                alert.showAndWait();

            } catch (IOException e) {
                System.err.println("✗ Erreur export CSV : " + e.getMessage());

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur d'export");
                alert.setHeaderText("Impossible d'exporter les données");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void afficherDetailsLocation(Location location) {
        Vehicule vehicule = vehiculeService.getVehiculeById(location.getIdVehicule());
        String vehiculeInfo = vehicule != null ? vehicule.getImmatriculation() : "Véhicule indisponible";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la Location");
        alert.setHeaderText("📋 Location de " + location.getClientNomComplet());

        String details = String.format(
                "👤 INFORMATIONS CLIENT\n" +
                        "   Nom : %s\n" +
                        "   Téléphone : %s\n" +
                        "   CIN : %s\n\n" +
                        "🚗 VÉHICULE\n" +
                        "   %s\n\n" +
                        "📅 DATES\n" +
                        "   Début : %s\n" +
                        "   Fin prévue : %s\n" +
                        "   Fin réelle : %s\n\n" +
                        "📊 KILOMÉTRAGE\n" +
                        "   Départ : %d km\n" +
                        "   Retour : %s\n\n" +
                        "💰 FINANCES\n" +
                        "   Prix par jour : %.3f TND\n" +
                        "   Montant total : %.3f TND\n" +
                        "   Avance : %.3f TND\n" +
                        "   Reste à payer : %.3f TND\n\n" +
                        "ℹ️ STATUT : %s\n" +
                        "📝 Notes : %s",
                location.getClientNomComplet(),
                location.getClientTelephone(),
                location.getClientCin() != null ? location.getClientCin() : "Non renseigné",
                vehiculeInfo,
                dateFormat.format(location.getDateDebut()),
                dateFormat.format(location.getDateFinPrev()),
                location.getDateFinReelle() != null ? dateFormat.format(location.getDateFinReelle()) : "Non terminée",
                location.getKilometrageDebut(),
                location.getKilometrageRetour() != null ? location.getKilometrageRetour() + " km" : "Non renseigné",
                location.getPrixParJour(),
                location.getMontantTotal(),
                location.getAvance(),
                location.getMontantTotal() - location.getAvance(),
                formatStatut(location.getStatut()),
                location.getNotes() != null ? location.getNotes() : "Aucune note"
        );

        alert.setContentText(details);
        alert.showAndWait();
    }
}