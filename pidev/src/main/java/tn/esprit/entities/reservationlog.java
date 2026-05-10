package tn.esprit.entities;

import java.util.Date;

public class reservationlog {
    private int id;
    private int id_l;
    private int idc;
    private Date date_debut;
    private Date date_fin;
    private float montant;
    private Status status;
    private String modalite;
    private Date dateLimitePaiement;
    private String trackingId;
    private int adultes;
    private int enfants;
    private int nombreChambres;
    private String modeReservation;
    private String repartitionChambres;
    private Date createdAt;

    // Constructeur complet
    public reservationlog(int id_l, int idc, Date date_debut, Date date_fin, float montant, Status status, String modalite) {
        this.id_l = id_l;
        this.idc = idc;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.montant = montant;
        this.status = status;
        this.modalite = modalite;
    }

    // Constructeur par défaut
    public reservationlog() {
    }

    // Getters et Setters
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getModalite() {
        return modalite;
    }

    public void setModalite(String modalite) {
        this.modalite = modalite;
    }

    public Date getDateLimitePaiement() {
        return dateLimitePaiement;
    }

    public void setDateLimitePaiement(Date dateLimitePaiement) {
        this.dateLimitePaiement = dateLimitePaiement;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public int getAdultes() {
        return adultes;
    }

    public void setAdultes(int adultes) {
        this.adultes = adultes;
    }

    public int getEnfants() {
        return enfants;
    }

    public void setEnfants(int enfants) {
        this.enfants = enfants;
    }

    public int getNombreChambres() {
        return nombreChambres;
    }

    public void setNombreChambres(int nombreChambres) {
        this.nombreChambres = nombreChambres;
    }

    public String getModeReservation() {
        return modeReservation;
    }

    public void setModeReservation(String modeReservation) {
        this.modeReservation = modeReservation;
    }

    public String getRepartitionChambres() {
        return repartitionChambres;
    }

    public void setRepartitionChambres(String repartitionChambres) {
        this.repartitionChambres = repartitionChambres;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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
                ", adultes=" + adultes +
                ", enfants=" + enfants +
                ", nombreChambres=" + nombreChambres +
                ", modeReservation='" + modeReservation + '\'' +
                ", repartitionChambres='" + repartitionChambres + '\'' +
                ", dateLimitePaiement=" + dateLimitePaiement +
                ", trackingId='" + trackingId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}