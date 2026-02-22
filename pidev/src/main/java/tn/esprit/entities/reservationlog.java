package tn.esprit.entities;

import java.util.Date;

public class reservationlog {
    private int id;
    private int id_l;
    private int idc;
    private Date date_debut;
    private Date date_fin;
    private float montant;
    private Status status;  // Changed from Enum to Status
    private String modalite;
    private Date dateLimitePaiement;

    public Date getDateLimitePaiement() { return dateLimitePaiement; }
    public void setDateLimitePaiement(Date dateLimitePaiement) { this.dateLimitePaiement = dateLimitePaiement; }
    // Constructor
    public reservationlog(int id_l, int idc, Date date_debut, Date date_fin, float montant, Status status, String modalite) {
        this.id_l = id_l;
        this.idc = idc;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.montant = montant;
        this.status = status;
        this.modalite = modalite;
    }

    // Default constructor
    public reservationlog() {
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_l() {
        return id_l;
    }

    public void setId_l(int id_l) {
        this.id_l = id_l;
    }

    public int getIdc() {
        return idc;
    }

    public void setIdc(int idc) {
        this.idc = idc;
    }

    public Date getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(Date date_debut) {
        this.date_debut = date_debut;
    }

    public Date getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(Date date_fin) {
        this.date_fin = date_fin;
    }

    public float getMontant() {
        return montant;
    }

    public void setMontant(float montant) {
        this.montant = montant;
    }

    public Status getStatus() {  // Changed return type to Status
        return status;
    }

    public void setStatus(Status status) {  // Changed parameter type to Status
        this.status = status;
    }

    public String getModalite() {
        return modalite;
    }

    public void setModalite(String modalite) {
        this.modalite = modalite;
    }

    @Override
    public String toString() {
        return "reservationlog{" +
                "id=" + id +
                ", id_l=" + id_l +
                ", idc=" + idc +
                ", date_debut=" + date_debut +
                ", date_fin=" + date_fin +
                ", montant=" + montant +
                ", status=" + status +
                ", modalite='" + modalite + '\'' +
                '}';
    }
}