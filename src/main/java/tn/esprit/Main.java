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
            int choix = sc.nextInt();
            sc.nextLine(); // Nettoyer le buffer

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
                    System.out.print("ID de la catégorie choisie : ");
                    int idCat = sc.nextInt();

                    vs.ajouter(new Voyage(dest, "Description voyage", prix, d1, d2, "http://image.url", idCat));
                    break;

                case 3:
                    System.out.println("--- Liste des voyages ---");
                    vs.afficher().forEach(v -> System.out.println("ID: " + v.getId() + " | Destination: " + v.getDestination()));

                    System.out.print("ID du voyage : ");
                    int idV = sc.nextInt();
                    System.out.print("Nombre de personnes : ");
                    int nb = sc.nextInt();
                    // On suppose l'utilisateur ID 1 pour le test
                    rs.ajouter(new Reservation(nb, "En attente", idV, 1));
                    break;

                case 4:
                    System.out.println("\n--- CONTENU DE LA BASE DE DONNÉES ---");
                    System.out.println("CATÉGORIES :"); cs.afficher().forEach(System.out::println);
                    System.out.println("VOYAGES :"); vs.afficher().forEach(System.out::println);
                    System.out.println("RÉSERVATIONS :"); rs.afficher().forEach(System.out::println);
                    break;

                case 0:
                    System.exit(0);
            }
        }
    }
}