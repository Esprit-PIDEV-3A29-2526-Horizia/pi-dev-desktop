package org.example;

import tn.esprit.entities.logement;
import tn.esprit.entities.reservationlog;
import tn.esprit.entities.Status;  // Import the Status enum
import tn.esprit.services.IService;
import tn.esprit.services.Servicelogement;
import tn.esprit.services.Servicereservationlog;

import java.sql.SQLException;
import java.util.Date;  // For dates
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Servicelogement sl = new Servicelogement();
        Servicereservationlog srl = new Servicereservationlog();  // Instance for reservationlog service

        try {
            // Test Servicelogement: Add a logement
            //sl.ajouter(new logement("hotel yassine", "Tunis", 5, "connexion,piscine", 1000, true));
          //  System.out.println("Logement ajouté");

            // Optionally, modify or delete a logement (uncomment if needed)
            // sl.modifier(new logement(1, "hotel Syfax", "Sfax", 12, "connexion,piscine", 1000, true));  // Assuming ID 1
            // System.out.println("Logement modifié");
            // sl.supprimer(1);  // Assuming ID 1 exists
            // System.out.println("Logement supprimé");

            // Display logements
            //System.out.println("Liste des logements : " + sl.afficher());

            // Test Servicereservationlog: Add a reservation (assuming logement ID 1 exists from above)
            Date dateDebut = new Date();  // Current date as start
            Date dateFin = new Date(System.currentTimeMillis() + 86400000);  // +1 day as end (adjust as needed)
            reservationlog res = new reservationlog( 2, 1, dateDebut, dateFin, 1500.0f, Status.terminée, "Paiement en ligne");  // ID 0 for auto-increment, id_l=1 (logement), idc=1 (client)
            srl.ajouter(res);
            System.out.println("Réservation ajoutée");

            // Optionally, modify the reservation (uncomment if needed, assuming ID is set after add)
            // res.setId(1);  // Set the ID if known (e.g., from DB)
            // res.setStatus(Status.CONFIRMED);
            // srl.modifier(res);
            // System.out.println("Réservation modifiée");

            // Optionally, delete a reservation (uncomment if needed)
            // srl.supprimer(1);  // Assuming ID 1
            // System.out.println("Réservation supprimée");

            // Display reservations
            System.out.println("Liste des réservations : " + srl.afficher());

            List<reservationlog> reservationsEnAttente = srl.rechercherParAttribut("status", Status.terminée);
            System.out.println("Liste des réservations : " + reservationsEnAttente);
            for (reservationlog r : reservationsEnAttente) {

                System.out.println("      - Réservation ID: " + r.getId() + ", Montant: " + r.getMontant());
            }
        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        }
    }
}