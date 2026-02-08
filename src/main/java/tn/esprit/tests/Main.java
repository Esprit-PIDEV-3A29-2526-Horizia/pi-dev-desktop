package tn.esprit.tests;

import tn.esprit.entities.User;
import tn.esprit.entities.Profil;
import tn.esprit.services.ServiceUser;
import tn.esprit.services.ServiceProfil;
import tn.esprit.tests.*;

import java.awt.desktop.SystemSleepEvent;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        ServiceUser su = new ServiceUser();
        ServiceProfil sp = new ServiceProfil();
        Scanner sc = new Scanner(System.in);

        int choixPrincipal;
        int choixSecondaire;

        do {
            System.out.println("\n======= MENU PRINCIPAL =======");
            System.out.println("1. Gestion des Users");
            System.out.println("2. Gestion des Profils");
            System.out.println("3. Quitter");
            System.out.print("Votre choix : ");

            choixPrincipal = sc.nextInt();
            sc.nextLine();

            switch (choixPrincipal) {


                case 1:
                    do {
                        System.out.println("\n--- Gestion des Users ---");
                        System.out.println("1. Ajouter un User");
                        System.out.println("2. Afficher les Users");
                        System.out.println("3. Modifier un User");
                        System.out.println("4. Rechercher User par nom");
                        System.out.println("5. Trier Users par nom");
                        System.out.println("6. Trier par ID");
                        System.out.println("7. Supprimer un User");
                        System.out.println("8. Retour");
                        System.out.print("Votre choix : ");

                        choixSecondaire = sc.nextInt();
                        sc.nextLine();

                        try {
                            switch (choixSecondaire) {

                                case 1:
                                    User u = new User();

                                    System.out.print("Nom : ");
                                    u.setNom(sc.nextLine());

                                    System.out.print("Prénom : ");
                                    u.setPrenom(sc.nextLine());

                                    System.out.print("Email : ");
                                    u.setEmail(sc.nextLine());

                                    System.out.print("Adresse : ");
                                    u.setAdresse(sc.nextLine());

                                    System.out.print("Téléphone : ");
                                    u.setTelephone(sc.nextLine());

                                    System.out.print("Mot de passe : ");
                                    u.setPassword(sc.nextLine());

                                    su.ajouter(u);
                                    System.out.println("User ajouté avec succès ✅");
                                    break;

                                case 2:
                                    System.out.println("\nListe des Users :");
                                    for (User user : su.afficher()) {
                                        System.out.println(user);
                                    }
                                    break;

                                case 3:
                                    User uMod = new User();

                                    System.out.print("ID du User à modifier : ");
                                    uMod.setId(sc.nextInt());
                                    sc.nextLine();

                                    System.out.print("Nouveau nom : ");
                                    uMod.setNom(sc.nextLine());

                                    System.out.print("Nouveau prénom : ");
                                    uMod.setPrenom(sc.nextLine());

                                    System.out.print("Nouvel email : ");
                                    uMod.setEmail(sc.nextLine());

                                    System.out.print("Nouvelle adresse : ");
                                    uMod.setAdresse(sc.nextLine());

                                    System.out.print("Nouveau téléphone : ");
                                    uMod.setTelephone(sc.nextLine());

                                    System.out.print("Nouveau mot de passe : ");
                                    uMod.setPassword(sc.nextLine());

                                    su.modifier(uMod);
                                    System.out.println("User modifié avec succès ✏️");
                                    break;
                                case 4:
                                    System.out.print("Nom à chercher : ");
                                    String nom = sc.nextLine();
                                    su.rechercherParNom(nom).forEach(System.out::println);
                                    break;

                                case 5:
                                    su.trierParNom().forEach(System.out::println);
                                    break;

                                case 6:
                                    su.trierParIdDesc().forEach(System.out::println);

                                case 7:
                                    System.out.print("ID du User à supprimer : ");
                                    su.supprimer(sc.nextInt());
                                    sc.nextLine();
                                    System.out.println("User supprimé avec succès 🗑️");
                                    break;

                                case 8:
                                    break;

                                default:
                                    System.out.println("❌ Choix invalide");
                            }

                        } catch (SQLException e) {
                            System.out.println("Erreur : " + e.getMessage());
                        }

                    } while (choixSecondaire != 8);
                    break;


                case 2:
                    do {
                        System.out.println("\n--- Gestion des Profils ---");
                        System.out.println("1. Ajouter un Profil");
                        System.out.println("2. Afficher les Profils");
                        System.out.println("3. Modifier un Profil");
                        System.out.println("4. Supprimer un Profil");
                        System.out.println("5. Chercher un profile par type");
                        System.out.println("6. trie Profil par type");
                        System.out.println("7. Retour");
                        System.out.print("Votre choix : ");

                        choixSecondaire = sc.nextInt();
                        sc.nextLine();

                        try {
                            switch (choixSecondaire) {

                                case 1:
                                    System.out.print("Type (CLIENT / ADMIN / AGENT) : ");
                                    String type = sc.nextLine();

                                    System.out.print("Statut (ACTIF / BLOQUE) : ");
                                    String statut = sc.nextLine();

                                    sp.ajouter(new Profil(type, statut));
                                    System.out.println("Profil ajouté avec succès ✅");
                                    break;

                                case 2:
                                    System.out.println("\nListe des Profils :");
                                    for (Profil p : sp.afficher()) {
                                        System.out.println(p);
                                    }
                                    break;

                                case 3:
                                    System.out.print("ID du profil à modifier : ");
                                    int idMod = sc.nextInt();
                                    sc.nextLine();

                                    System.out.print("Nouveau type : ");
                                    String newType = sc.nextLine();

                                    System.out.print("Nouveau statut : ");
                                    String newStatut = sc.nextLine();

                                    sp.modifier(new Profil(idMod, newType, newStatut));
                                    System.out.println("Profil modifié avec succès ✏️");
                                    break;

                                case 4:
                                    System.out.print("ID du profil à supprimer : ");
                                    sp.supprimer(sc.nextInt());
                                    sc.nextLine();
                                    System.out.println("Profil supprimé avec succès 🗑️");
                                    break;
                                case 5:
                                    System.out.print("Type du profil à chercher : ");
                                    String typ = sc.nextLine();
                                    sp.rechercherParType(typ).forEach(System.out::println);
                                    break;

                                case 6:
                                    sp.trierParType().forEach(System.out::println);
                                    break;


                                case 7:
                                    break;

                                default:
                                    System.out.println("❌ Choix invalide");
                            }

                        } catch (SQLException e) {
                            System.out.println("Erreur : " + e.getMessage());
                        }

                    } while (choixSecondaire != 7);
                    break;

                case 3:
                    System.out.println("Au revoir 👋");
                    break;

                default:
                    System.out.println("❌ Choix invalide");
            }

        } while (choixPrincipal != 3);

        sc.close();
    }
}
