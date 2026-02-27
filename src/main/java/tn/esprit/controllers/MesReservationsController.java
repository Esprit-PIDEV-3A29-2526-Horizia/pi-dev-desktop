package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import tn.esprit.entites.Reservation;
import tn.esprit.services.ReservationService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MesReservationsController {

    @FXML private FlowPane flowReservations;
    @FXML private Label emptyMessage;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> cbStatut;
    @FXML private Button btnAccueil;
    @FXML private Button btnNosVoyages;
    @FXML private Button btnMesReservations;
    @FXML private Button btnContact;
    private final ReservationService rs = new ReservationService();
    private List<Reservation> listeOriginale = new ArrayList<>();

    @FXML
    public void initialize() {
        activerBouton(btnMesReservations);
        if (cbStatut != null) {
            cbStatut.setItems(FXCollections.observableArrayList(
                    "Tous", "Confirmée", "En attente", "Annulée"
            ));
            cbStatut.getSelectionModel().select("Tous");
            cbStatut.valueProperty().addListener((obs, o, n) -> appliquerFiltresEtAfficher());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> appliquerFiltresEtAfficher());
        }
        chargerDonnees();
        appliquerFiltresEtAfficher();
    }


    private void chargerDonnees() {
        int idUser = 1;
        List<Reservation> list = rs.getReservationsParUtilisateur(idUser);
        listeOriginale = (list == null) ? new ArrayList<>() : list;
    }

    @FXML
    private void onRechercher() {
        appliquerFiltresEtAfficher();
    }
    @FXML
    private void onReset() {
        if (searchField != null) searchField.clear();
        if (cbStatut != null) cbStatut.getSelectionModel().select("Tous");
        afficher(listeOriginale);
    }
    private void appliquerFiltresEtAfficher() {
        String q = (searchField != null && searchField.getText() != null)
                ? searchField.getText().trim().toLowerCase(Locale.ROOT)
                : "";
        String statutChoisi = (cbStatut != null && cbStatut.getValue() != null)
                ? cbStatut.getValue()
                : "Tous";
        List<Reservation> resultats = listeOriginale.stream()
                .filter(r -> matchRecherche(r, q))
                .filter(r -> matchStatut(r, statutChoisi))
                .collect(Collectors.toList());
        afficher(resultats);
    }

    private boolean matchRecherche(Reservation r, String q) {
        if (q.isEmpty()) return true;
        String dest = (r.getDestination() != null) ? r.getDestination() : "";
        return dest.toLowerCase(Locale.ROOT).contains(q);
    }
    private boolean matchStatut(Reservation r, String statutChoisi) {
        if (statutChoisi == null || statutChoisi.equalsIgnoreCase("Tous")) return true;
        String st = (r.getStatut() == null || r.getStatut().isBlank())
                ? "EN_ATTENTE"
                : r.getStatut().trim().toUpperCase(Locale.ROOT);
        String attendu = switch (statutChoisi) {
            case "Confirmée" -> "CONFIRMEE";
            case "En attente" -> "EN_ATTENTE";
            case "Annulée" -> "ANNULEE";
            default -> "";
        };
        return attendu.isEmpty() || st.equals(attendu);
    }

    private void afficher(List<Reservation> list) {
        flowReservations.getChildren().clear();
        boolean empty = (list == null || list.isEmpty());
        emptyMessage.setVisible(empty);
        emptyMessage.setManaged(empty);
        if (empty) return;
        for (Reservation r : list) {
            flowReservations.getChildren().add(creerCardReservation(r));
        }
    }

    private VBox creerCardReservation(Reservation res) {

        VBox card = new VBox(10);
        card.setPrefWidth(320);
        card.setPadding(new Insets(16));
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 14, 0, 0, 4);
                """);
        ImageView img = new ImageView();
        img.setFitHeight(140);
        img.setFitWidth(288);
        img.setPreserveRatio(false);
        img.setSmooth(true);
        Image loaded = chargerImage(res.getImageUrl());
        img.setImage(loaded);
        Rectangle clip = new Rectangle(288, 140);
        clip.setArcWidth(18);
        clip.setArcHeight(18);
        img.setClip(clip);
        Label lblDest = new Label(res.getDestination() != null ? res.getDestination() : "Voyage");
        lblDest.setWrapText(true);
        lblDest.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1A3C5A;");
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lblPers = new Label("👥 " + res.getNbr_personnes() + " personnes");
        lblPers.setStyle("-fx-text-fill: #334155; -fx-font-size: 13;");
        String statut = (res.getStatut() != null && !res.getStatut().isBlank()) ? res.getStatut() : "En attente";
        Label lblStatut = new Label(statut);
        lblStatut.setStyle(styleStatut(statut));
        lblStatut.setPadding(new Insets(5, 12, 5, 12));
        row.getChildren().addAll(lblPers, lblStatut);
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        Button btnDetails = new Button("Détails");
        btnDetails.setStyle("""
                -fx-background-color: #3D94CA;
                -fx-text-fill: white;
                -fx-background-radius: 14;
                -fx-padding: 8 14;
                -fx-cursor: hand;
                """);
        btnDetails.setOnAction(e -> voirDetails(res));
        if (statut.toUpperCase(Locale.ROOT).contains("CONFIRM")) {
            Label ok = new Label("✓ Validée");
            ok.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");
            actions.getChildren().addAll(btnDetails, ok);
        } else {
            Button btnAnnuler = new Button("Annuler");
            btnAnnuler.setStyle("""
            -fx-background-color: #DACEB6;
            -fx-text-fill: #1A3C5A;
            -fx-background-radius: 14;
            -fx-padding: 8 14;
            -fx-cursor: hand;
            -fx-font-weight: bold;
            """);
            btnAnnuler.setOnAction(e -> handleAnnulation(res));
            actions.getChildren().addAll(btnDetails, btnAnnuler);
        }
        card.getChildren().addAll(img, lblDest, row, actions);
        return card;
    }
    private void voirDetails(Reservation res) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la réservation");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(600);
        VBox root = new VBox(14);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #F6F7FB;");
        ImageView img = new ImageView(chargerImage(res.getImageUrl()));
        img.setFitWidth(520);
        img.setFitHeight(260);
        img.setPreserveRatio(false);
        img.setSmooth(true);
        Rectangle clip = new Rectangle(520, 260);
        clip.setArcWidth(22);
        clip.setArcHeight(22);
        img.setClip(clip);
        String destination = (res.getDestination() != null && !res.getDestination().isBlank())
                ? res.getDestination()
                : "Voyage";
        Label title = new Label(destination.toUpperCase());
        title.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #1A3C5A;");
        String statut = (res.getStatut() != null && !res.getStatut().isBlank()) ? res.getStatut() : "En attente";
        Label chip = new Label(statut);
        chip.setStyle(styleStatut(statut));
        chip.setPadding(new Insets(6, 14, 6, 14));
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        Label l1 = new Label("👥 Nombre de personnes :");
        l1.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label v1 = new Label(String.valueOf(res.getNbr_personnes()));
        v1.setStyle("-fx-text-fill: #0F172A;");
        Label l2 = new Label("📌 Statut :");
        l2.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label v2 = new Label(statut);
        v2.setStyle("-fx-text-fill: #0F172A;");
        grid.addRow(0, l1, v1);
        grid.addRow(1, l2, v2);
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button btnOk = new Button("OK");
        btnOk.setStyle("""
            -fx-background-color: #3D94CA;
            -fx-text-fill: white;
            -fx-background-radius: 14;
            -fx-padding: 8 16;
            -fx-cursor: hand;
            """);
        btnOk.setOnAction(e -> dialog.close());
        actions.getChildren().addAll(btnOk);
        VBox card = new VBox(12, title, chip, grid, actions);
        card.setPadding(new Insets(16));
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 18;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 14, 0, 0, 4);
            """);
        root.getChildren().addAll(img, card);
        dialog.getDialogPane().setContent(root);
        dialog.showAndWait();
    }
    private void handleAnnulation(Reservation res) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation d'annulation");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir annuler votre réservation pour " + res.getDestination() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            rs.annulerReservation(res.getId());
            chargerDonnees();
            appliquerFiltresEtAfficher();
        }
    }

    private Image chargerImage(String url) {
        if (url == null || url.isBlank()) return chargerPlaceholderLocal();
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                Image img = new Image(url, true);
                if (!img.isError()) return img;
                return chargerPlaceholderLocal();
            }
            if (url.startsWith("file:/") || url.matches("^[a-zA-Z]:\\\\.*") || url.startsWith("/Users/") || url.startsWith("/home/")) {
                File f = url.startsWith("file:/") ? new File(new URL(url).toURI()) : new File(url);
                if (f.exists()) return new Image(f.toURI().toString(), true);
                return chargerPlaceholderLocal();
            }
            String path = url.startsWith("/") ? url : "/" + url;
            URL resUrl = getClass().getResource(path);
            if (resUrl != null) {
                Image img = new Image(resUrl.toExternalForm(), true);
                if (!img.isError()) return img;
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + url + " => " + e.getMessage());
        }
        return chargerPlaceholderLocal();
    }

    private Image chargerPlaceholderLocal() {
        URL ph = getClass().getResource("/images/placeholder.png");
        if (ph != null) {
            Image img = new Image(ph.toExternalForm(), true);
            if (!img.isError()) return img;
        }
        return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR4nGNgYGD4DwABBAEAH9lY1QAAAABJRU5ErkJggg==");
    }


    private String styleStatut(String statut) {
        if (statut == null) statut = "";
        String s = statut.toLowerCase(Locale.ROOT);

        if (s.contains("attente") || s.isBlank()) {
            return "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-background-radius: 20; -fx-font-weight: bold;";
        }
        if (s.contains("confirm")) {
            return "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-background-radius: 20; -fx-font-weight: bold;";
        }
        if (s.contains("annul")) {
            return "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-background-radius: 20; -fx-font-weight: bold;";
        }
        return "-fx-background-color: #E2E8F0; -fx-text-fill: #334155; -fx-background-radius: 20; -fx-font-weight: bold;";
    }


    private void activerBouton(Button actif) {
        Button[] buttons = {btnAccueil, btnNosVoyages, btnMesReservations, btnContact};
        for (Button b : buttons) {
            if (b != null) b.getStyleClass().remove("nav-active");
        }
        if (actif != null && !actif.getStyleClass().contains("nav-active")) {
            actif.getStyleClass().add("nav-active");
        }
    }

    @FXML
    private void allerAuCatalogue() {
        changerRoot("/CatalogueUser.fxml");
    }

    @FXML
    private void deconnexion() {
        changerRoot("/Login.fxml");
    }

    private void changerRoot(String fxml) {
        try {
            URL loc = getClass().getResource(fxml);
            if (loc == null) {
                System.err.println("FXML introuvable : " + fxml);
                return;
            }
            Parent root = FXMLLoader.load(loc);

            if (flowReservations != null && flowReservations.getScene() != null) {
                flowReservations.getScene().setRoot(root);
            } else if (searchField != null && searchField.getScene() != null) {
                searchField.getScene().setRoot(root);
            } else {
                System.err.println("Scene introuvable (flowReservations/searchField null?)");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String nz(String s) {
        return (s == null) ? "" : s;
    }

}