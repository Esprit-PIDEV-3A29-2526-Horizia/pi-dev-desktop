package tn.esprit.backend.utils;

import tn.esprit.backend.entities.Utilisateur;
import tn.esprit.backend.entities.Role;  // ✅ Added import

public class TestData {

    public static void connecterAdminTest() {
        Utilisateur admin = new Utilisateur();
        admin.setId(1);
        admin.setNom("Ben Ahmed");
        admin.setPrenom("Khalil");
        admin.setEmail("khalil@horizia.com");
        admin.setRole(Role.ADMIN);  // ✅ Fixed: Utilisateur.Role.ADMIN → Role.ADMIN

        Session.connecter(admin);
        Session.setAdminMode(true);

        System.out.println(" Mode ADMIN activé: " + admin.getNomComplet());
    }

    public static void connecterUserTest() {
        Utilisateur user = new Utilisateur();
        user.setId(2);
        user.setNom("Ben Ali");
        user.setPrenom("Sarra");
        user.setEmail("sarra@horizia.com");
        user.setRole(Role.USER);  // ✅ Fixed: Utilisateur.Role.USER → Role.USER

        Session.connecter(user);
        Session.setAdminMode(false);

        System.out.println(" Mode USER activé: " + user.getNomComplet());
    }

    public static void deconnecter() {
        Session.deconnecter();
        System.out.println(" Mode anonyme");
    }
}

