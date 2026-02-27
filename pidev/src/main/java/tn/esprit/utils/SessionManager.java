package tn.esprit.utils;

import tn.esprit.entities.reservationlog;
import tn.esprit.entities.User;
import tn.esprit.entities.logement;

public class SessionManager {
    private static User currentUser = null;
    private static logement selectedLogement = null;
    private static reservationlog editingReservation = null;

    // ===== GESTION UTILISATEUR =====

    public static void setCurrentUser(User user) {
        currentUser = user;
        System.out.println("Utilisateur connecté: " + (user != null ? user.getEmail() : "null"));
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        System.out.println("Déconnexion de: " + (currentUser != null ? currentUser.getEmail() : "personne"));
        currentUser = null;
        selectedLogement = null;
        editingReservation = null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getProfil_id() == 1;
    }

    // ===== GESTION LOGEMENT SÉLECTIONNÉ =====

    public static void setSelectedLogement(logement logement) {
        selectedLogement = logement;
        System.out.println("Logement sélectionné: " + (logement != null ? logement.getNom() : "null"));
    }

    public static logement getSelectedLogement() {
        return selectedLogement;
    }

    public static void clearSelectedLogement() {
        selectedLogement = null;
        System.out.println("Logement sélectionné effacé");
    }

    // ===== GESTION RÉSERVATION EN COURS D'ÉDITION =====

    public static reservationlog getEditingReservation() {
        return editingReservation;
    }

    public static void setEditingReservation(reservationlog reservation) {
        editingReservation = reservation;
        System.out.println("Réservation en édition: " + (reservation != null ? reservation.getId() : "null"));
    }

    public static void clearEditingReservation() {
        editingReservation = null;
        System.out.println("Réservation en édition effacée");
    }

    // ===== NETTOYAGE COMPLET =====

    public static void clearAll() {
        currentUser = null;
        selectedLogement = null;
        editingReservation = null;
        System.out.println("Session complètement effacée");
    }

    // ===== MÉTHODES DE TEST =====

    public static void setTestUser() {
        User testUser = new User();
        testUser.setId(1);
        testUser.setNom("Test");
        testUser.setPrenom("User");
        testUser.setEmail("test@example.com");
        testUser.setProfil_id(2);
        setCurrentUser(testUser);
        System.out.println("🧪 Utilisateur de test connecté: " + testUser.getEmail());
    }

    public static void setTestAdminUser() {
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setNom("Admin");
        adminUser.setPrenom("Test");
        adminUser.setEmail("admin@example.com");
        adminUser.setProfil_id(1);
        setCurrentUser(adminUser);
        System.out.println("🧪 Admin de test connecté: " + adminUser.getEmail());
    }

    // ===== MÉTHODES UTILITAIRES =====

    public static void printSessionStatus() {
        System.out.println("\n=== ÉTAT DE LA SESSION ===");
        System.out.println("Utilisateur connecté: " + (currentUser != null ? currentUser.getEmail() : "non connecté"));
        System.out.println("Logement sélectionné: " + (selectedLogement != null ? selectedLogement.getNom() : "aucun"));
        System.out.println("Réservation en édition: " + (editingReservation != null ? editingReservation.getId() : "aucune"));
        System.out.println("==========================\n");
    }
}