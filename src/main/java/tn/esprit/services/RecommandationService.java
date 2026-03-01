package tn.esprit.services;

import tn.esprit.entities.Events;
import tn.esprit.entities.Participation;

import java.util.*;
import java.util.stream.Collectors;

public class RecommandationService {

    private ServiceEvent serviceEvent;
    private ServiceParticipation serviceParticipation;

    public RecommandationService() {
        this.serviceEvent = new ServiceEvent();
        this.serviceParticipation = new ServiceParticipation();
    }

    /**
     * NIVEAU 1 - Recommandation simple par catégorie
     */
    public List<Events> recommanderParCategorie(Events event, List<Events> tousLesEvents) {
        return tousLesEvents.stream()
                .filter(e -> e.getId_event() != event.getId_event())
                .filter(e -> e.getCategorie().equalsIgnoreCase(event.getCategorie()))
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * NIVEAU 2 - Recommandation par score de similarité (catégorie + prix + lieu)
     */
    public List<Events> recommanderParSimilarite(Events event, List<Events> tousLesEvents) {
        return tousLesEvents.stream()
                .filter(e -> e.getId_event() != event.getId_event())
                .sorted((e1, e2) -> {
                    double score1 = calculerScoreSimilarite(e1, event);
                    double score2 = calculerScoreSimilarite(e2, event);
                    return Double.compare(score2, score1);
                })
                .limit(3)
                .collect(Collectors.toList());
    }

    private double calculerScoreSimilarite(Events e, Events reference) {
        double score = 0;

        // 1. Même catégorie = +50 points
        if (e.getCategorie().equalsIgnoreCase(reference.getCategorie())) {
            score += 50;
        }

        // 2. Prix similaire (écart < 20%) = +30 points
        double ecartPrix = Math.abs(e.getPrix() - reference.getPrix()) / reference.getPrix();
        if (ecartPrix < 0.2) {
            score += 30;
        } else if (ecartPrix < 0.5) {
            score += 15; // Écart moyen
        }

        // 3. Même ville/lieu = +20 points
        if (reference.getLocation() != null && e.getLocation() != null) {
            String villeRef = extraireVille(reference.getLocation());
            String villeE = extraireVille(e.getLocation());
            if (villeRef.equals(villeE)) {
                score += 20;
            }
        }

        return score;
    }

    /**
     * NIVEAU 3 - Recommandation collaborative (basée sur l'historique des réservations)
     */
    public List<Events> recommenderCollaboratif(Events event, List<Events> tousLesEvents) {
        try {
            List<Participation> toutesParticipations = serviceParticipation.afficher();

            // Trouver les utilisateurs qui ont réservé CET événement
            Set<Integer> usersQuiOntReserve = toutesParticipations.stream()
                    .filter(p -> p.getId_event() == event.getId_event())
                    .map(Participation::getId_participation) // Utilise l'ID de participation comme proxy
                    .collect(Collectors.toSet());

            // Trouver les autres événements réservés par CES utilisateurs
            Map<Integer, Integer> compteurEvents = new HashMap<>();

            for (Participation p : toutesParticipations) {
                if (usersQuiOntReserve.contains(p.getId_participation())) {
                    if (p.getId_event() != event.getId_event()) {
                        compteurEvents.put(p.getId_event(),
                                compteurEvents.getOrDefault(p.getId_event(), 0) + 1);
                    }
                }
            }

            // Trier par nombre de réservations et retourner les 3 premiers
            return compteurEvents.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                    .limit(3)
                    .map(entry -> trouverEventParId(entry.getKey(), tousLesEvents))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Méthode utilitaire pour extraire la ville d'une adresse
     */
    private String extraireVille(String adresse) {
        if (adresse == null) return "";
        // Extrait le dernier mot (grossièrement la ville)
        String[] parties = adresse.split(",");
        return parties[parties.length - 1].trim();
    }

    private Events trouverEventParId(int id, List<Events> tousLesEvents) {
        return tousLesEvents.stream()
                .filter(e -> e.getId_event() == id)
                .findFirst()
                .orElse(null);
    }
}