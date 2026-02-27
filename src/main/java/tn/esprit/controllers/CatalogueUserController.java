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
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.api.chatbot.ChatbotController;
import tn.esprit.api.weather.OpenWeatherService;
import tn.esprit.api.weather.WeatherInfo;
import tn.esprit.entites.Voyage;
import tn.esprit.services.VoyageService;
import tn.esprit.utils.Config;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CatalogueUserController implements Initializable {

    @FXML private GridPane voyageGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> comboTri;

    @FXML private Button btnTous;
    @FXML private Button btnTunisie;
    @FXML private Button btnEurope;
    @FXML private Button btnPromos;

    @FXML private AnchorPane rootPane; // On va ajouter ce champ

    private final VoyageService vs = new VoyageService();
    private List<Voyage> listeOriginale = new ArrayList<>();
    private static final int NB_COLONNES = 3;

    // NOUVEAU : Chatbot
    private ChatbotController chatbotController;

    private enum Filtre { TOUS, TUNISIE, EUROPE, PROMOS }
    private Filtre filtreActif = Filtre.TOUS;

    private final OpenWeatherService meteoService =
            new OpenWeatherService(Config.get("openweather.apiKey"));

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final ConcurrentHashMap<String, WeatherInfo> cacheMeteo = new ConcurrentHashMap<>();

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
        javafx.application.Platform.runLater(() -> {
            initialiserChatbot();
        });
    }
    private void initialiserChatbot() {
        try {
            chatbotController = new ChatbotController();
            Scene scene = voyageGrid.getScene();
            if (scene != null) {
                Parent ancienneRacine = scene.getRoot();
                StackPane nouvelleRacine = new StackPane();
                nouvelleRacine.getChildren().add(ancienneRacine);
                nouvelleRacine.getChildren().add(chatbotController.createChatbot());
                scene.setRoot(nouvelleRacine);
                System.out.println("Chatbot ajouté avec succès !");
            } else {
                System.err.println("Scene non disponible, attente...");
                attendreSceneEtAjouterChatbot();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du chatbot: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void attendreSceneEtAjouterChatbot() {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    Scene scene = voyageGrid.getScene();
                    if (scene != null) {
                        Parent ancienneRacine = scene.getRoot();
                        StackPane nouvelleRacine = new StackPane();
                        nouvelleRacine.getChildren().add(ancienneRacine);
                        nouvelleRacine.getChildren().add(chatbotController.createChatbot());
                        scene.setRoot(nouvelleRacine);
                        System.out.println("Chatbot ajouté (après attente)");
                        timer.cancel();
                    }
                });
            }
        }, 0, 500);
    }


    @FXML private void filtrerTous()   { filtreActif = Filtre.TOUS;   setChipActive(btnTous);   appliquerTout(); }
    @FXML private void filtrerTunisie(){ filtreActif = Filtre.TUNISIE;setChipActive(btnTunisie);appliquerTout(); }
    @FXML private void filtrerEurope() { filtreActif = Filtre.EUROPE; setChipActive(btnEurope); appliquerTout(); }
    @FXML private void filtrerPromos() { filtreActif = Filtre.PROMOS; setChipActive(btnPromos); appliquerTout(); }

    private void setChipActive(Button active) {
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

    private void appliquerTout() {
        String keyword = safeLower(searchField != null ? searchField.getText() : "");
        List<Voyage> resultats = listeOriginale.stream()
                .filter(Objects::nonNull)
                .filter(this::matchFiltreActif)
                .filter(v -> keyword.isBlank()
                        || safeLower(v.getDestination()).contains(keyword)
                        || safeLower(v.getTitre()).contains(keyword))
                .collect(Collectors.toList());
        String tri = comboTri != null ? comboTri.getValue() : null;
        if (tri != null) resultats = trierStreams(resultats, tri);
        chargerVoyages(resultats);
    }

    private boolean matchFiltreActif(Voyage v) {
        String dest = safeLower(v.getDestination());
        String titre = safeLower(v.getTitre());
        return switch (filtreActif) {
            case TOUS -> true;

            case TUNISIE -> containsAny(dest,
                    "tunis", "sfax", "sousse", "kairouan", "bizerte", "gabès", "ariana",
                    "gafsa", "kasserine", "médnine", "ben arous", "monastir", "mahdia",
                    "hammamet", "nabeul", "djerba", "tozeur", "douz", "tabarka", "ain draham",
                    "sidi bou said", "carthage", "la marsa", "gammarth", "el jem", "dougga",
                    "korbous", "kelibia", "haouaria", "rafraf", "zarzis", "midoun", "ajim",
                    "kerkennah", "karkannah", "port el kantaoui", "yasmine hammamet",
                    "nefta", "degache", "chott el jerid", "kebili", "tataouine", "beja",
                    "jendouba", "siliana", "zaghouan", "seliana"
            )
                    || containsAny(titre, "tunisie", "tunisia", "djerba", "hammamet", "sousse",
                    "tozeur", "monastir", "bizerte", "tabarka", "nabeul", "carthage");

            case EUROPE -> containsAny(dest,
                    // FRANCE
                    "paris", "lyon", "marseille", "toulouse", "nice", "strasbourg", "bordeaux",
                    "cannes", "antibes", "saint-tropez", "avignon", "biarritz", "carcassonne",
                    "annecy", "corse", "ajaccio", "bastia",

                    // ESPAGNE
                    "madrid", "barcelone", "valence", "séville", "malaga", "majorque", "ibiza",
                    "tenerife", "lanzarote", "costa brava", "costa del sol",

                    // ITALIE
                    "rome", "milan", "venise", "florence", "naples", "turin", "pise", "sienne",
                    "pompei", "sorrente", "amalfi", "cinque terre", "sicile", "sardaigne",

                    // ROYAUME-UNI
                    "londres", "london", "manchester", "liverpool", "edimbourg", "glasgow",
                    "cambridge", "oxford", "bath", "belfast",

                    // ALLEMAGNE
                    "berlin", "hambourg", "munich", "cologne", "francfort", "stuttgart",
                    "dresde", "heidelberg", "fribourg",

                    // PORTUGAL
                    "lisbonne", "porto", "faro", "madeira", "madère", "albufeira", "sintra",

                    // PAYS-BAS
                    "amsterdam", "rotterdam", "la haye", "maastricht", "delft", "giethoorn",

                    // BELGIQUE
                    "bruxelles", "anvers", "gand", "bruges", "liège", "namur", "waterloo",

                    // SUISSE
                    "zurich", "genève", "bâle", "berne", "lausanne", "interlaken", "zermatt",
                    "lugano", "lucerne", "montreux","suisse",

                    // AUTRICHE
                    "vienne", "salzbourg", "innsbruck", "graz", "hallstatt", "zell am see",

                    // GRÈCE
                    "athènes", "thessalonique", "crète", "chania", "mykonos", "santorin",
                    "rhodes", "corfou", "paros", "naxos", "zakynthos", "olympia", "delphes","gréce",

                    // CROATIE
                    "zagreb", "split", "dubrovnik", "zadar", "pula", "hvar", "korcula",
                    "rovinj", "porec",

                    // RÉP. TCHÈQUE
                    "prague", "brno", "cesky krumlov", "karlovy vary",

                    // HONGRIE
                    "budapest", "debrecen", "pecs", "eger", "balaton", "siofok", "szentendre",

                    // POLOGNE
                    "varsovie", "cracovie", "wroclaw", "gdansk", "zakopane", "auschwitz",

                    // SUÈDE
                    "stockholm", "gothenburg", "malmö", "uppsala", "kiruna", "gotland",

                    // DANEMARK
                    "copenhague", "aarhus", "odense", "roskilde", "bornholm",

                    // FINLANDE
                    "helsinki", "tampere", "turku", "rovaniemi", "laponie",

                    // NORVÈGE
                    "oslo", "bergen", "stavanger", "tromsø", "lofoten", "geiranger", "fjord",

                    // IRLANDE
                    "dublin", "cork", "galway", "killarney", "falaises de moher",

                    // ISLANDE
                    "reykjavik", "blue lagoon", "geysir", "gullfoss", "jökulsárlón",

                    // MALTE
                    "malte", "valletta", "gozo", "mdina", "blue lagoon malta",

                    // AUTRES PAYS
                    "luxembourg", "monaco", "monte-carlo", "andorre", "vaduz", "saint-marin",
                    "san marino", "liechtenstein", "lituanie", "lettonie", "estonie",
                    "slovaquie", "slovénie", "bulgarie", "roumanie", "serbie", "croatie",
                    "bosnie", "monténégro", "albanie", "macédoine", "kosovo"
            )
                    || titre.contains("europe") || titre.contains("europ")
                    || titre.contains("europe") || titre.contains("europ");

            case PROMOS -> v.getPrix() <= 2000;
        };
    }

    private boolean containsAny(String text, String... keys) {
        if (text == null) return false;
        for (String k : keys) {
            if (k != null && !k.isBlank() && text.contains(k.toLowerCase())) return true;
        }
        return false;
    }

    private List<Voyage> trierStreams(List<Voyage> base, String tri) {
        return switch (tri) {
            case "Prix : Croissant" -> base.stream().sorted(Comparator.comparingDouble(Voyage::getPrix)).collect(Collectors.toList());
            case "Prix : Décroissant" -> base.stream().sorted(Comparator.comparingDouble(Voyage::getPrix).reversed()).collect(Collectors.toList());
            case "Date : Plus proche" -> base.stream().sorted(Comparator.comparing(this::dateDepartAsLocalDate)).collect(Collectors.toList());
            case "Destination : A-Z" -> base.stream().sorted(Comparator.comparing(v -> safeLower(v.getDestination()))).collect(Collectors.toList());
            case "Places restantes : Desc" -> base.stream().sorted(Comparator.comparingInt(Voyage::getPlaces_restantes).reversed()).collect(Collectors.toList());
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
                if (controller != null) {
                    controller.setMeteo(meteoService, cacheMeteo, executor);
                    controller.setData(v);
                }
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

    @FXML private void handleRecherche() { appliquerTout(); }

    @FXML
    private void afficherCatalogue() {
        if (searchField != null) searchField.clear();
        if (comboTri != null) comboTri.setValue("Prix : Croissant");
        filtrerTous();
    }

    @FXML
    private void afficherHistorique(ActionEvent event) {
        // Arrêter proprement le chatbot avant de changer de scène
        if (chatbotController != null) {
            // Si vous avez une méthode pour nettoyer, appelez-la ici
        }
        changerScene(event, "/MesReservations.fxml", "Mes Réservations");
    }

    @FXML
    private void handleDeconnexion(ActionEvent event) {
        executor.shutdownNow();
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