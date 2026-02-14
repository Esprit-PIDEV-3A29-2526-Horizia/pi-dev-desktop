//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.sql.SQLException;
import java.sql.Timestamp;
import tn.esprit.entities.Events;
import tn.esprit.services.ServiceEvent;
import tn.esprit.services.ServiceParticipation;

public class Main {
    public static void main(String[] args) {
        ServiceEvent se = new ServiceEvent();
        ServiceParticipation sep = new ServiceParticipation();

        try {
            Events e = new Events("The 1975 Concert", "A highly theatrical, two-act performance designed as a commentary on modern masculinity and tech consumption", "Culturel", "Paris", Timestamp.valueOf("2026-03-20 20:30:00"), Timestamp.valueOf("2026-03-21 04:30:00"), 90.00F, 400, 200, "https://i.pinimg.com/736x/b0/90/dc/b090dc4039434bb6ebf9ae34c87a58dd.jpg", "valide", 1);
            se.ajouter(e);
            System.out.println("Participation ajoutee");
            System.out.println(sep.afficher());
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }
}
