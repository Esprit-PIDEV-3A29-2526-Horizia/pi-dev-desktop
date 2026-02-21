package tn.esprit.utils;

import tn.esprit.entities.reservationlog;
import tn.esprit.entities.User;  // Import de l'entité user
import tn.esprit.entities.logement;  // Import de l'entité logement

public class SessionManager {
    private static User currentUser = null;  // Utilisateur connecté
    private static logement selectedLogement = null;  // Logement sélectionné pour réservation

    // Méthode pour définir l'utilisateur connecté (appelée après login réussi)
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Méthode pour récupérer l'utilisateur connecté
    public static User getCurrentUser() {
        return currentUser;
    }

    // Méthode pour déconnecter l'utilisateur
    public static void logout() {
        currentUser = null;
        selectedLogement = null;  // Optionnel : réinitialiser le logement sélectionné
    }

    // Méthode pour vérifier si un utilisateur est connecté
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // Méthode pour vérifier si l'utilisateur connecté est un admin (profil_id = 1, ajustez si nécessaire)
    public static boolean isAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getProfil_id() == 1;  // Assumant que 1 = admin
    }

    // Méthode pour définir le logement sélectionné (pour réservation)
    public static void setSelectedLogement(logement logement) {
        selectedLogement = logement;
    }

    // Méthode pour récupérer le logement sélectionné
    public static logement getSelectedLogement() {
        return selectedLogement;
    }

    // Pour les tests : définir un utilisateur fictif (à supprimer après intégration)
    public static void setTestUser() {
        User testUser = new User();
        testUser.setId(1);
        testUser.setNom("Test");
        testUser.setPrenom("User");
        testUser.setEmail("test@example.com");
        testUser.setProfil_id(2);  // Profil non-admin
        setCurrentUser(testUser);
    }

    // Pour les tests : définir un utilisateur admin fictif (à supprimer après intégration)
    public static void setTestAdminUser() {
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setNom("Admin");
        adminUser.setPrenom("Test");
        adminUser.setEmail("admin@example.com");
        adminUser.setProfil_id(1);  // Profil admin
        setCurrentUser(adminUser);
    }
    private static reservationlog editingReservation;

    public static reservationlog getEditingReservation() {
        return editingReservation;
    }

    public static void setEditingReservation(reservationlog reservation) {
        editingReservation = reservation;
    }

    public static void clearEditingReservation() {
        editingReservation = null;
    }
}