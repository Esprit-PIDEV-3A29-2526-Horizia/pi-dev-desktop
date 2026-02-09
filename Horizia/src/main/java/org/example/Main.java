package org.example;

import org.example.entities.*;
import org.example.services.*;

import java.sql.Timestamp;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final MarqueService marqueService = new MarqueService();
    private static final ModeleService modeleService = new ModeleService();
    private static final VehiculeService vehiculeService = new VehiculeService();
    private static final LocationService locationService = new LocationService();

    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("   BIENVENUE DANS HORIZIA - Gestion de Location 🚗");
        System.out.println("======================================\n");

        while (true) {
            afficherMenuPrincipal();
            String choixStr = scanner.nextLine().trim();

            int choix;
            try {
                choix = Integer.parseInt(choixStr);
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Choix invalide. Entrez un nombre.");
                continue;
            }

            if (choix == 0) {
                System.out.println("\n👋 Au revoir Adem ! À bientôt champion !");
                break;
            }

            switch (choix) {
                case 1 -> gererMarques();
                case 2 -> gererModeles();
                case 3 -> gererVehicules();
                case 4 -> gererLocations();
                default -> System.out.println("⚠️ Choix invalide, réessayez.");
            }
        }

        scanner.close();
    }

    private static void afficherMenuPrincipal() {
        System.out.println("\nQue voulez-vous faire ?");
        System.out.println("1. Gestion des Marques");
        System.out.println("2. Gestion des Modèles");
        System.out.println("3. Gestion des Véhicules");
        System.out.println("4. Gestion des Locations");
        System.out.println("0. Quitter");
        System.out.print("Votre choix : ");
    }

    // ───────────────────────────────────────────────
    // GESTION MARQUES
    // ───────────────────────────────────────────────
    private static void gererMarques() {
        while (true) {
            System.out.println("\n--- Gestion des Marques ---");
            System.out.println("1. Lister toutes les marques");
            System.out.println("2. Ajouter une marque");
            System.out.println("3. Rechercher une marque par nom");
            System.out.println("0. Retour au menu principal");
            System.out.print("Choix : ");

            int choix = lireEntier();
            if (choix == 0) return;

            switch (choix) {
                case 1:
                    System.out.println("\nListe des marques (A → Z) :");
                    marqueService.getAllMarquesAlphabetique().forEach(System.out::println);
                    break;
                case 2:
                    System.out.print("Nom de la nouvelle marque : ");
                    String nom = scanner.nextLine().trim();
                    if (nom.isEmpty()) {
                        System.out.println("Nom vide → annulé.");
                        break;
                    }
                    Marque m = new Marque(nom);
                    if (marqueService.ajouterMarque(m)) {
                        System.out.println("✅ Marque ajoutée ! ID = " + m.getIdMarque());
                    } else {
                        System.out.println("❌ Échec ajout (déjà existante ou erreur).");
                    }
                    break;
                case 3:
                    System.out.print("Mot-clé pour rechercher : ");
                    String recherche = scanner.nextLine().trim();
                    if (recherche.isEmpty()) break;
                    System.out.println("Résultats pour '" + recherche + "' :");
                    marqueService.rechercherMarqueParNom(recherche).forEach(System.out::println);
                    break;
                default:
                    System.out.println("Choix invalide.");
            }
        }
    }

    // ───────────────────────────────────────────────
    // GESTION MODÈLES
    // ───────────────────────────────────────────────
    private static void gererModeles() {
        while (true) {
            System.out.println("\n--- Gestion des Modèles ---");
            System.out.println("1. Lister tous les modèles");
            System.out.println("2. Ajouter un modèle");
            System.out.println("3. Rechercher modèles par nom de marque");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            int choix = lireEntier();
            if (choix == 0) return;

            switch (choix) {
                case 1:
                    System.out.println("\nListe des modèles :");
                    modeleService.getAllModeles();
                    break;
                case 2:
                    System.out.print("ID de la marque : ");
                    int idMarque = lireEntier();
                    System.out.print("Nom du modèle : ");
                    String nomModele = scanner.nextLine().trim();
                    if (idMarque <= 0 || nomModele.isEmpty()) {
                        System.out.println("Données invalides.");
                        break;
                    }
                    Modele m = new Modele(idMarque, nomModele);
                    if (modeleService.ajouterModele(m)) {
                        System.out.println("✅ Modèle ajouté ! ID = " + m.getIdModele());
                    } else {
                        System.out.println("❌ Échec ajout.");
                    }
                    break;
                case 3:
                    System.out.print("Nom de la marque à rechercher : ");
                    String nomMarque = scanner.nextLine().trim();
                    if (nomMarque.isEmpty()) break;
                    System.out.println("Modèles trouvés pour '" + nomMarque + "' :");
                    modeleService.rechercherModelesParNomMarque(nomMarque).forEach(System.out::println);
                    break;
                default:
                    System.out.println("Choix invalide.");
            }
        }
    }

    // ───────────────────────────────────────────────
    // GESTION VÉHICULES (CRUD complet)
    // ───────────────────────────────────────────────
    private static void gererVehicules() {
        while (true) {
            System.out.println("\n--- Gestion des Véhicules ---");
            System.out.println("1. Lister tous les véhicules");
            System.out.println("2. Ajouter un véhicule");
            System.out.println("3. Modifier un véhicule");
            System.out.println("4. Supprimer un véhicule");
            System.out.println("5. Rechercher par immatriculation");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            int choix = lireEntier();
            if (choix == 0) return;

            switch (choix) {
                case 1:
                    vehiculeService.getAllVehicules().forEach(System.out::println);
                    break;
                case 2:
                    Vehicule v = creerVehiculeInteractif();
                    if (v != null && vehiculeService.ajouterVehicule(v)) {
                        System.out.println("✅ Véhicule ajouté ! ID = " + v.getIdVehicule());
                    }
                    break;
                case 3:
                    System.out.print("ID du véhicule à modifier : ");
                    int idModif = lireEntier();
                    Vehicule vehicule = vehiculeService.getVehiculeById(idModif);
                    if (vehicule != null) {
                        vehicule = modifierVehiculeInteractif(vehicule);
                        if (vehiculeService.modifierVehicule(vehicule)) {
                            System.out.println("✅ Véhicule modifié !");
                        }
                    }
                    break;
                case 4:
                    System.out.print("ID du véhicule à supprimer : ");
                    int idSuppr = lireEntier();
                    if (vehiculeService.supprimerVehicule(idSuppr)) {
                        System.out.println("🗑️ Véhicule supprimé.");
                    } else {
                        System.out.println("Impossible de supprimer (peut-être des locations en cours ?)");
                    }
                    break;
                case 5:
                    System.out.print("Immatriculation (ou partie) : ");
                    String immat = scanner.nextLine().trim();
                    vehiculeService.rechercherParImmatriculation(immat).forEach(System.out::println);
                    break;
                default:
                    System.out.println("Choix invalide.");
            }
        }
    }

    // ───────────────────────────────────────────────
    // GESTION LOCATIONS (CRUD complet)
    // ───────────────────────────────────────────────
    private static void gererLocations() {
        while (true) {
            System.out.println("\n--- Gestion des Locations ---");
            System.out.println("1. Lister toutes les locations");
            System.out.println("2. Ajouter une location");
            System.out.println("3. Modifier une location (ex: retour)");
            System.out.println("4. Supprimer une location");
            System.out.println("5. Rechercher par nom client");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            int choix = lireEntier();
            if (choix == 0) return;

            switch (choix) {
                case 1:
                    locationService.getAllLocations().forEach(System.out::println);
                    break;
                case 2:
                    Location l = creerLocationInteractif();
                    if (l != null) {
                        if (vehiculeService.getVehiculeById(l.getIdVehicule()) == null) {
                            System.out.println("Erreur : Ce véhicule n'existe pas.");
                        } else if (locationService.ajouterLocation(l)) {
                            System.out.println("✅ Location ajoutée ! ID = " + l.getIdLocation());
                        }
                    }
                    break;
                case 3:
                    System.out.print("ID de la location à modifier : ");
                    int idLoc = lireEntier();
                    Location loc = locationService.getLocationById(idLoc);
                    if (loc != null) {
                        loc = modifierLocationInteractif(loc);
                        if (locationService.modifierLocation(loc)) {
                            System.out.println("✅ Location modifiée !");
                        }
                    }
                    break;
                case 4:
                    System.out.print("ID de la location à supprimer : ");
                    int idDel = lireEntier();
                    if (locationService.supprimerLocation(idDel)) {
                        System.out.println("🗑️ Location supprimée.");
                    } else {
                        System.out.println("Impossible de supprimer.");
                    }
                    break;
                case 5:
                    System.out.print("Nom ou téléphone du client : ");
                    String recherche = scanner.nextLine().trim();
                    locationService.rechercherParClient(recherche).forEach(System.out::println);
                    break;
                default:
                    System.out.println("Choix invalide.");
            }
        }
    }

    // ───────────────────────────────────────────────
    // Helpers interactifs
    // ───────────────────────────────────────────────

    private static int lireEntier() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Entrez un nombre valide.");
            return -1;
        }
    }

    private static Vehicule creerVehiculeInteractif() {
        Vehicule v = new Vehicule();
        System.out.print("Immatriculation : ");
        v.setImmatriculation(scanner.nextLine().trim());
        System.out.print("ID Modèle : ");
        v.setIdModele(lireEntier());
        System.out.print("Année : ");
        v.setAnnee(lireEntier());
        System.out.print("Carburant : ");
        v.setCarburant(scanner.nextLine().trim());
        System.out.print("Couleur : ");
        v.setCouleur(scanner.nextLine().trim());
        System.out.print("Kilométrage : ");
        v.setKilometrage(lireEntier());
        System.out.print("État (disponible/louee/en_maintenance/indisponible) : ");
        v.setEtat(scanner.nextLine().trim());
        System.out.print("Prix par jour : ");
        v.setPrixParJour(Double.parseDouble(scanner.nextLine().trim()));
        System.out.print("Photo (URL ou chemin, vide pour aucun) : ");
        v.setPhoto(scanner.nextLine().trim());
        return v;
    }

    private static Vehicule modifierVehiculeInteractif(Vehicule v) {
        System.out.println("Laissez vide pour garder la valeur actuelle.");
        System.out.print("Nouvelle immatriculation (" + v.getImmatriculation() + ") : ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) v.setImmatriculation(input);

        // Ajoute ici les autres champs à modifier (année, prix, état...)
        // Exemple rapide pour l'état :
        System.out.print("Nouvel état (" + v.getEtat() + ") : ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) v.setEtat(input);

        return v;
    }

    private static Location creerLocationInteractif() {
        Location l = new Location();
        System.out.print("ID Véhicule : ");
        int idVeh = lireEntier();
        if (vehiculeService.getVehiculeById(idVeh) == null) {
            System.out.println("Véhicule inexistant → annulé.");
            return null;
        }
        l.setIdVehicule(idVeh);

        System.out.print("Nom complet client : ");
        l.setClientNomComplet(scanner.nextLine().trim());
        System.out.print("Téléphone client : ");
        l.setClientTelephone(scanner.nextLine().trim());
        System.out.print("CIN client : ");
        l.setClientCin(scanner.nextLine().trim());
        System.out.print("Date début (ex: 2025-05-05 18:00:00) : ");
        l.setDateDebut(Timestamp.valueOf(scanner.nextLine().trim()));
        System.out.print("Date fin prévue (ex: 2025-05-10 18:00:00) : ");
        l.setDateFinPrev(Timestamp.valueOf(scanner.nextLine().trim()));
        System.out.print("Kilométrage début : ");
        l.setKilometrageDebut(lireEntier());
        System.out.print("Prix par jour : ");
        l.setPrixParJour(Double.parseDouble(scanner.nextLine().trim()));
        System.out.print("Montant total : ");
        l.setMontantTotal(Double.parseDouble(scanner.nextLine().trim()));
        System.out.print("Statut (réservée/en_cours/terminée/annulée) : ");
        l.setStatut(scanner.nextLine().trim());

        return l;
    }

    private static Location modifierLocationInteractif(Location l) {
        System.out.println("Laissez vide pour garder.");
        System.out.print("Nouveau statut (" + l.getStatut() + ") : ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) l.setStatut(input);

        System.out.print("Date fin réelle (ex: 2025-05-08 17:00:00) : ");
        input = scanner.nextLine().trim();
        if (!input.isEmpty()) l.setDateFinReelle(Timestamp.valueOf(input));

        // Ajoute ici d'autres champs à modifier si besoin
        return l;
    }
}