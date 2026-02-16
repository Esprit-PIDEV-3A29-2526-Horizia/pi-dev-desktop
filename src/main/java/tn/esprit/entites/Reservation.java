package tn.esprit.entites;

import java.sql.Timestamp;

public class Reservation {
    private int id;
    private Timestamp dateReservation; // Utilisation de Timestamp pour avoir date + heure
    private int nbPersonnes;
    private String statut; // "En attente", "Confirmée", "Annulée"
    private int idVoyage; // Clé étrangère vers Voyage
    private int idUtilisateur; // Clé étrangère vers Utilisateur

    // Constructeurs
    public Reservation() {}

    // Constructeur pour l'ajout (sans ID ni date, car gérés par la BDD)
    public Reservation(int nbPersonnes, String statut, int idVoyage, int idUtilisateur) {
        this.nbPersonnes = nbPersonnes;
        this.statut = statut;
        this.idVoyage = idVoyage;
        this.idUtilisateur = idUtilisateur;
    }

    // Constructeur complet
    public Reservation(int id, Timestamp dateReservation, int nbPersonnes, String statut, int idVoyage, int idUtilisateur) {
        this.id = id;
        this.dateReservation = dateReservation;
        this.nbPersonnes = nbPersonnes;
        this.statut = statut;
        this.idVoyage = idVoyage;
        this.idUtilisateur = idUtilisateur;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Timestamp getDateReservation() { return dateReservation; }
    public void setDateReservation(Timestamp dateReservation) { this.dateReservation = dateReservation; }
    public int getNbPersonnes() { return nbPersonnes; }
    public void setNbPersonnes(int nbPersonnes) { this.nbPersonnes = nbPersonnes; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public int getIdVoyage() { return idVoyage; }
    public void setIdVoyage(int idVoyage) { this.idVoyage = idVoyage; }
    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", dateReservation=" + dateReservation +
                ", nbPersonnes=" + nbPersonnes +
                ", statut='" + statut + '\'' +
                ", idVoyage=" + idVoyage +
                ", idUtilisateur=" + idUtilisateur +
                '}';
    }
}