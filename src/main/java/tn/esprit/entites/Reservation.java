package tn.esprit.entites;

import java.sql.Timestamp;

public class Reservation {

    private int id;
    private Timestamp date_reservation;
    private int nbr_personnes;
    private String statut;
    private int idVoyage;
    private int idUser;
    private String imageUrl;

    // champ pour affichage (join avec voyage)
    private String destination;

    // Constructeurs
    public Reservation() {}

    // Pour ajout (sans id ni date)
    public Reservation(int nbr_personnes, String statut, int idVoyage, int idUser) {
        this.nbr_personnes = nbr_personnes;
        this.statut = statut;
        this.idVoyage = idVoyage;
        this.idUser = idUser;
    }

    // Complet
    public Reservation(int id, Timestamp date_reservation, int nbr_personnes, String statut, int idVoyage, int idUser) {
        this.id = id;
        this.date_reservation = date_reservation;
        this.nbr_personnes = nbr_personnes;
        this.statut = statut;
        this.idVoyage = idVoyage;
        this.idUser = idUser;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Timestamp getDate_reservation() { return date_reservation; }
    public void setDate_reservation(Timestamp date_reservation) { this.date_reservation = date_reservation; }

    public int getNbr_personnes() { return nbr_personnes; }
    public void setNbr_personnes(int nbr_personnes) { this.nbr_personnes = nbr_personnes; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getIdVoyage() { return idVoyage; }
    public void setIdVoyage(int idVoyage) { this.idVoyage = idVoyage; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", date_reservation=" + date_reservation +
                ", nbr_personnes=" + nbr_personnes +
                ", statut='" + statut + '\'' +
                ", idVoyage=" + idVoyage +
                ", idUser=" + idUser +
                ", destination='" + destination + '\'' +
                '}';
    }
}
