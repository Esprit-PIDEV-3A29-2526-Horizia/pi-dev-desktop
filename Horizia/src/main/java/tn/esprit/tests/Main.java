package tn.esprit.tests;

import tn.esprit.entities.Publication;
import tn.esprit.services.ServicePublication;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        ServicePublication sp = new ServicePublication();

        try {
            sp.ajouter(new Publication(
                    "Voyage à Paris",
                    "Découverte de Paris",
                    "France",
                    LocalDate.now()
            ));

            System.out.println("Publications:");
            System.out.println(sp.afficher());

            System.out.println("Recherche France:");
            System.out.println(sp.rechercherParDestination("France"));

            System.out.println("Tri par date:");
            System.out.println(sp.trierParDate());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
