package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entites.Voyage;
import tn.esprit.services.VoyageService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class CatalogueUserController implements Initializable {

    @FXML private GridPane voyageGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboTri;

    // chips
    @FXML private Button btnTous;
    @FXML private Button btnTunisie;
    @FXML private Button btnEurope;
    @FXML private Button btnPromos;

    private final VoyageService vs = new VoyageService();
    private List<Voyage> listeOriginale = new ArrayList<>();

    private static final int NB_COLONNES = 3; // mets 4 si tu veux 4 cartes / ligne

    // filtre actif
    private enum Filtre { TOUS, TUNISIE, EUROPE, PROMOS }
    private Filtre filtreActif = Filtre.TOUS;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        listeOriginale = vs.afficher();
        if (listeOriginale == null) listeOriginale = new ArrayList<>();

        if (comboTri != null) {
            comboTri.getItems().setAll(
                    "Prix : Croissant",
                    "Prix : Décroissant",
                    "Date : Plus proche",
                    "Destination : A-Z",
                    "Places restantes : Desc"
            );
            comboTri.setValue("Prix : Croissant");
            comboTri.setOnAction(e -> appliquerTout());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> appliquerTout());
        }

        setChipActive(btnTous);
        appliquerTout();
    }

    // ======= FILTRES (buttons) =======

    @FXML
    private void filtrerTous() {
        filtreActif = Filtre.TOUS;
        setChipActive(btnTous);
        appliquerTout();
    }

    @FXML
    private void filtrerTunisie() {
        filtreActif = Filtre.TUNISIE;
        setChipActive(btnTunisie);
        appliquerTout();
    }

    @FXML
    private void filtrerEurope() {
        filtreActif = Filtre.EUROPE;
        setChipActive(btnEurope);
        appliquerTout();
    }

    @FXML
    private void filtrerPromos() {
        filtreActif = Filtre.PROMOS;
        setChipActive(btnPromos);
        appliquerTout();
    }

    private void setChipActive(Button active) {
        // style actif/inactif (simple)
        Button[] all = {btnTous, btnTunisie, btnEurope, btnPromos};
        for (Button b : all) {
            if (b == null) continue;
            if (b == active) {
                b.setStyle("-fx-background-color: #E8B156; -fx-text-fill: white; -fx-font-weight: bold;" +
                        "-fx-background-radius: 18; -fx-padding: 6 16; -fx-cursor: hand;");
            } else {
                b.setStyle("-fx-background-color: #DACEB6; -fx-text-fill: #1F2937; -fx-font-weight: bold;" +
                        "-fx-background-radius: 18; -fx-padding: 6 16; -fx-cursor: hand;");
            }
        }
    }

    // ======= APPLIQUER filtres + recherche + tri =======

    private void appliquerTout() {
        String keyword = safeLower(searchField != null ? searchField.getText() : "");

        List<Voyage> resultats = listeOriginale.stream()

                // 1) filtre chips
                .filter(this::matchFiltreActif)

                // 2) recherche text (titre ou destination)
                .filter(v -> keyword.isBlank()
                        || safeLower(v.getDestination()).contains(keyword)
                        || safeLower(v.getTitre()).contains(keyword))

                .collect(Collectors.toList());

        // 3) tri
        String tri = comboTri != null ? comboTri.getValue() : null;
        if (tri != null) {
            resultats = trierStreams(resultats, tri);
        }

        chargerVoyages(resultats);
    }

    private boolean matchFiltreActif(Voyage v) {
        if (v == null) return false;

        String dest = safeLower(v.getDestination());

        return switch (filtreActif) {
            case TOUS -> true;

            case TUNISIE -> containsAny(dest,
                    "tozeur", "tunis", "sousse", "sfax", "djerba", "hammamet", "monastir", "bizerte");

            case EUROPE -> containsAny(dest,
                    // Espagne
                    "barcelone", "madrid", "valence", "seville", "séville",
                    // France
                    "paris", "lyon", "marseille", "nice",
                    // Italie
                    "rome", "milan", "venise", "florence",
                    // Royaume-Uni
                    "londres", "london", "manchester",
                    // Allemagne
                    "berlin", "munich", "munchen",
                    // Portugal
                    "lisbonne", "lisbon", "porto",
                    // Pays-Bas
                    "amsterdam", "rotterdam",
                    // Suisse
                    "geneve", "genève", "zurich", "suisse",
                    // Belgique
                    "bruxelles", "brussels",
                    // Autres
                    "vienna", "vienne", "prague", "budapest", "athenes", "athènes"
            );

            //exemple promo: prix <= 2000 (modifie selon ton besoin)
            case PROMOS -> v.getPrix() <= 2000;
        };
    }

    private boolean containsAny(String text, String... keys) {
        if (text == null) return false;
        for (String k : keys) {
            if (k != null && !k.isBlank() && text.contains(k)) return true;
        }
        return false;
    }

    private List<Voyage> trierStreams(List<Voyage> base, String tri) {
        return switch (tri) {
            case "Prix : Croissant" -> base.stream()
                    .sorted(Comparator.comparingDouble(Voyage::getPrix))
                    .collect(Collectors.toList());

            case "Prix : Décroissant" -> base.stream()
                    .sorted(Comparator.comparingDouble(Voyage::getPrix).reversed())
                    .collect(Collectors.toList());

            case "Date : Plus proche" -> base.stream()
                    .sorted(Comparator.comparing(this::dateDepartAsLocalDate))
                    .collect(Collectors.toList());

            case "Destination : A-Z" -> base.stream()
                    .sorted(Comparator.comparing(v -> safeLower(v.getDestination())))
                    .collect(Collectors.toList());

            case "Places restantes : Desc" -> base.stream()
                    .sorted(Comparator.comparingInt(Voyage::getPlaces_restantes).reversed())
                    .collect(Collectors.toList());

            default -> base;
        };
    }

    private LocalDate dateDepartAsLocalDate(Voyage v) {
        Date d = (v == null) ? null : v.getDate_depart();
        return (d == null) ? LocalDate.MAX : d.toLocalDate();
    }

    private String safeLower(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }

    // ======= AFFICHAGE grid =======

    public void chargerVoyages(List<Voyage> voyages) {
        if (voyageGrid == null) return;

        voyageGrid.getChildren().clear();
        if (voyages == null) voyages = List.of();

        int column = 0;
        int row = 0;

        for (Voyage v : voyages) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/VoyageCardUser.fxml"));
                VBox card = loader.load();

                VoyageCardUserController controller = loader.getController();
                if (controller != null) controller.setData(v);

                voyageGrid.add(card, column, row);

                column++;
                if (column == NB_COLONNES) {
                    column = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ======= NAVIGATION =======

    @FXML
    private void handleRecherche() {
        appliquerTout();
    }

    @FXML
    private void afficherCatalogue() {
        if (searchField != null) searchField.clear();
        if (comboTri != null) comboTri.setValue("Prix : Croissant");
        filtrerTous();
    }

    @FXML
    private void afficherHistorique(ActionEvent event) {
        changerScene(event, "/MesReservations.fxml", "Mes Réservations");
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        changerScene(event, "/Login.fxml", "Connexion");
    }

    private void changerScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}