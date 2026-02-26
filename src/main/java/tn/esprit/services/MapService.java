package tn.esprit.services;

import javafx.scene.web.WebView;
import tn.esprit.entities.Events;

public class MapService {
    public static void loadMap(WebView webView, Events event) {
        if (event.getLocation() == null || event.getLocation().trim().isEmpty()) {
            webView.getEngine().loadContent("<html><body style='text-align:center;padding:50px;'>📍 Lieu non spécifié</body></html>");
            return;
        }

        // Pour l'utilisateur, on affiche juste l'adresse (pas de carte)
        String html = "<html><body style='font-family:Arial;text-align:center;padding:50px;'>" +
                "<h2>📍 " + event.getLocation() + "</h2>" +
                "<p>Carte disponible prochainement</p>" +
                "</body></html>";

        webView.getEngine().loadContent(html);
    }
}