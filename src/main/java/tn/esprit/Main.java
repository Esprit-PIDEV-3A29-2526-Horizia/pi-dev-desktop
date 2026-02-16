package tn.esprit;

import tn.esprit.entites.*;
import tn.esprit.services.*;
import java.sql.Date;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        CategorieService cs = new CategorieService();
        VoyageService vs = new VoyageService();
        ReservationService rs = new ReservationService();

        System.out.println("=== BIENVENUE DANS LE GESTIONNAIRE VOYAGE & LOISIRS ===");

        while (true) {
            System.out.println("\n--- MENU PRINCIPAL ---");
            System.out.println("1. Ajouter une Catégorie");
            System.out.println("2. Ajouter un Voyage");
            System.out.println("3. Faire une Réservation");
            System.out.println("4. Afficher tout (Check BDD)");
            System.out.println("0. Quitter");
            System.out.print("Choix : ");

            if (!sc.hasNextInt()) { sc.next(); continue; } // Sécurité saisie
            int choix = sc.nextInt();
            sc.nextLine();

            switch (choix) {
                case 1:
                    System.out.print("Nom catégorie (ex: Luxe) : ");
                    String nomCat = sc.nextLine();
                    System.out.print("Description : ");
                    String descCat = sc.nextLine();
                    cs.ajouter(new Categorie(nomCat, descCat));
                    break;

                case 2:
                    System.out.println("--- Liste des catégories disponibles ---");
                    cs.afficher().forEach(c -> System.out.println("ID: " + c.getId() + " | Nom: " + c.getNom()));

                    System.out.print("Destination : ");
                    String dest = sc.nextLine();
                    System.out.print("Prix : ");
                    double prix = sc.nextDouble();
                    sc.nextLine();
                    System.out.print("Date départ (YYYY-MM-DD) : ");
                    Date d1 = Date.valueOf(sc.nextLine());
                    System.out.print("Date retour (YYYY-MM-DD) : ");
                    Date d2 = Date.valueOf(sc.nextLine());

                    // --- AJOUT DES SAISIES POUR LES PLACES ---
                    System.out.print("Nombre de places totales : ");
                    int pTotal = sc.nextInt();
                    System.out.print("Nombre de places restantes : ");
                    int pRestantes = sc.nextInt();

                    System.out.print("ID de la catégorie choisie : ");
                    int idCat = sc.nextInt();
                    sc.nextLine(); // Nettoyer après le nextInt

                    // Vérification de la contrainte avant l'envoi
                    if (pRestantes > pTotal) {
                        System.out.println("Erreur : Les places restantes ne peuvent pas dépasser le total !");
                    } else {
                        // APPEL AU CONSTRUCTEUR MIS À JOUR (9 ARGUMENTS)
                        vs.ajouter(new Voyage(dest, "Description voyage", prix, d1, d2, "http://image.url", idCat, pTotal, pRestantes));
                        System.out.println("Voyage ajouté avec succès !");
                    }
                    break;

                case 3:
                    System.out.println("--- Liste des voyages ---");
                    vs.afficher().forEach(v -> System.out.println("ID: " + v.getId() + " | Destination: " + v.getDestination() + " (" + v.getPlaces_restantes() + " places libres)"));

                    System.out.print("ID du voyage : ");
                    int idV = sc.nextInt();
                    System.out.print("Nombre de personnes : ");
                    int nb = sc.nextInt();
                    rs.ajouter(new Reservation(nb, "En attente", idV, 1));
                    System.out.println("Réservation enregistrée !");
                    break;

                case 4:
                    System.out.println("\n--- CONTENU DE LA BASE DE DONNÉES ---");
                    System.out.println("CATÉGORIES :"); cs.afficher().forEach(System.out::println);
                    System.out.println("VOYAGES :"); vs.afficher().forEach(System.out::println);
                    System.out.println("RÉSERVATIONS :"); rs.afficher().forEach(System.out::println);
                    break;

                case 0:
                    System.out.println("Au revoir !");
                    System.exit(0);
            }
        }
    }
}