package tn.esprit.entites;

import java.sql.Timestamp;

public class Reservation {
    private int id;
    private Timestamp dateReservation; // Utilisation de Timestamp pour avoir date + heure
    private int nb_places;
    private String statut; // "En attente", "Confirmée", "Annulée"
    private int idVoyage; // Clé étrangère vers Voyage
    private int idUtilisateur;
    private String destination;// Clé étrangère vers Utilisateur

    // Constructeurs
    public Reservation() {}

    // Constructeur pour l'ajout (sans ID ni date, car gérés par la BDD)
    public Reservation(int nb_places, String statut, int idVoyage, int idUtilisateur) {
        this.nb_places = nb_places;
        this.statut = statut;
        this.idVoyage = idVoyage;
        this.idUtilisateur = idUtilisateur;
    }

    // Constructeur complet
    public Reservation(int id, Timestamp dateReservation, int nb_places, String statut, int idVoyage, int idUtilisateur) {
        this.id = id;
        this.dateReservation = dateReservation;
        this.nb_places = nb_places;
        this.statut = statut;
        this.idVoyage = idVoyage;
        this.idUtilisateur = idUtilisateur;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Timestamp getDateReservation() { return dateReservation; }
    public void setDateReservation(Timestamp dateReservation) { this.dateReservation = dateReservation; }
    public int getNb_places() { return nb_places; }
    public void setNb_places(int nb_places) { this.nb_places = nb_places; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public int getIdVoyage() { return idVoyage; }
    public void setIdVoyage(int idVoyage) { this.idVoyage = idVoyage; }
    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", dateReservation=" + dateReservation +
                ", nb_places=" + nb_places +
                ", statut='" + statut + '\'' +
                ", idVoyage=" + idVoyage +
                ", idUtilisateur=" + idUtilisateur +
                '}';
    }
}