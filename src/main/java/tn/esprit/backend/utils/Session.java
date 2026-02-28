package tn.esprit.backend.utils;

import tn.esprit.backend.entities.Utilisateur;

public class Session {
    private static Utilisateur utilisateurConnecte;
    private static boolean adminMode = false;

    public static void connecter(Utilisateur u) {
        utilisateurConnecte = u;
        adminMode = (u != null && u.isAdmin());
    }

    public static void deconnecter() {
        utilisateurConnecte = null;
        adminMode = false;
    }

    public static Utilisateur getUtilisateur() {
        return utilisateurConnecte;
    }

    public static boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public static boolean estAdmin() {
        return estConnecte() && utilisateurConnecte.isAdmin();
    }

    public static boolean isAdminMode() {
        return adminMode;
    }

    public static void setAdminMode(boolean mode) {
        adminMode = mode;
    }
}

