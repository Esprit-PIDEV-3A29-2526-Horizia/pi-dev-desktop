package tn.esprit.entites;

import java.sql.Timestamp;

public class Reservation {

    private int id;
    private Timestamp date_reservation;
    private String statut;
    private int id_voyage;
    private int id_user;
    private int nbr_personnes;
    private int nb_adultes;
    private int nb_enfants;
    private double prix_total;
    private String payment_status;

    // Champs supplémentaires pour affichage
    private String imageUrl;
    private String destination;
    private String qrCode;

    // Constructeur par défaut
    public Reservation() {}

    // Constructeur complet
    public Reservation(int id, Timestamp date_reservation, String statut, int id_voyage, int id_user,
                       int nbr_personnes, int nb_adultes, int nb_enfants, double prix_total, String payment_status) {
        this.id = id;
        this.date_reservation = date_reservation;
        this.statut = statut;
        this.id_voyage = id_voyage;
        this.id_user = id_user;
        this.nbr_personnes = nbr_personnes;
        this.nb_adultes = nb_adultes;
        this.nb_enfants = nb_enfants;
        this.prix_total = prix_total;
        this.payment_status = payment_status;
    }

    // Constructeur pour ajout (sans id ni date)
    public Reservation(String statut, int id_voyage, int id_user, int nbr_personnes, int nb_adultes, int nb_enfants, double prix_total, String payment_status) {
        this.statut = statut;
        this.id_voyage = id_voyage;
        this.id_user = id_user;
        this.nbr_personnes = nbr_personnes;
        this.nb_adultes = nb_adultes;
        this.nb_enfants = nb_enfants;
        this.prix_total = prix_total;
        this.payment_status = payment_status;
    }

    // Constructeur simplifié (pour compatibilité avec l'ancien code)
    public Reservation(int nbr_personnes, String statut, int id_voyage, int id_user) {
        this.nbr_personnes = nbr_personnes;
        this.statut = statut;
        this.id_voyage = id_voyage;
        this.id_user = id_user;
        this.nb_adultes = nbr_personnes;
        this.nb_enfants = 0;
        this.prix_total = 0;
        this.payment_status = "NON_PAYEE";
    }

    // Constructeur pour Réservation (id, nbr_personnes, statut, id_voyage, id_user) - utilisé dans votre code
    public Reservation(int id, int nbr_personnes, String statut, int id_voyage, int id_user) {
        this.id = id;
        this.nbr_personnes = nbr_personnes;
        this.statut = statut;
        this.id_voyage = id_voyage;
        this.id_user = id_user;
        this.nb_adultes = nbr_personnes;
        this.nb_enfants = 0;
        this.prix_total = 0;
        this.payment_status = "NON_PAYEE";
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Timestamp getDate_reservation() { return date_reservation; }
    public void setDate_reservation(Timestamp date_reservation) { this.date_reservation = date_reservation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getId_voyage() { return id_voyage; }
    public void setId_voyage(int id_voyage) { this.id_voyage = id_voyage; }

    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }

    public int getNbr_personnes() { return nbr_personnes; }
    public void setNbr_personnes(int nbr_personnes) { this.nbr_personnes = nbr_personnes; }

    public int getNb_adultes() { return nb_adultes; }
    public void setNb_adultes(int nb_adultes) { this.nb_adultes = nb_adultes; }

    public int getNb_enfants() { return nb_enfants; }
    public void setNb_enfants(int nb_enfants) { this.nb_enfants = nb_enfants; }

    public double getPrix_total() { return prix_total; }
    public void setPrix_total(double prix_total) { this.prix_total = prix_total; }

    public String getPayment_status() { return payment_status; }
    public void setPayment_status(String payment_status) { this.payment_status = payment_status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", date_reservation=" + date_reservation +
                ", statut='" + statut + '\'' +
                ", id_voyage=" + id_voyage +
                ", id_user=" + id_user +
                ", nbr_personnes=" + nbr_personnes +
                ", nb_adultes=" + nb_adultes +
                ", nb_enfants=" + nb_enfants +
                ", prix_total=" + prix_total +
                ", payment_status='" + payment_status + '\'' +
                '}';
    }
}