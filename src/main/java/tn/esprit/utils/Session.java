package tn.esprit.utils;

import tn.esprit.entities.utilisateur;

public class Session {
    private static utilisateur utilisateurConnecte;

    public static void setUtilisateurConnecte(utilisateur u) {
        utilisateurConnecte = u;
    }

    public static utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public static void deconnecter() {
        utilisateurConnecte = null;
    }

    public static boolean estConnecte() {
        return utilisateurConnecte != null;
    }
}