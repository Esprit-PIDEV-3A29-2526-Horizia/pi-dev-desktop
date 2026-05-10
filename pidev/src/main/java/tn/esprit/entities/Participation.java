package tn.esprit.entities;

import java.sql.Timestamp;

public class Participation {
    private int id_participation;
    private int id_event;
    private int user_id;
    private int nombre_places;
    private double montant_total;
    private String statut;
    private Timestamp date_participation;
    private String email_snapshot;
    private String nom_snapshot;
    private String prenom_snapshot;
    private String telephone_snapshot;

    public Participation() {
    }

    public Participation(int id_participation, int id_event, int user_id, int nombre_places,
                         double montant_total, String statut, Timestamp date_participation,
                         String email_snapshot, String nom_snapshot, String prenom_snapshot,
                         String telephone_snapshot) {
        this.id_participation = id_participation;
        this.id_event = id_event;
        this.user_id = user_id;
        this.nombre_places = nombre_places;
        this.montant_total = montant_total;
        this.statut = statut;
        this.date_participation = date_participation;
        this.email_snapshot = email_snapshot;
        this.nom_snapshot = nom_snapshot;
        this.prenom_snapshot = prenom_snapshot;
        this.telephone_snapshot = telephone_snapshot;
    }

    public Participation(int id_event, int user_id, int nombre_places, double montant_total,
                         String statut, Timestamp date_participation) {
        this.id_event = id_event;
        this.user_id = user_id;
        this.nombre_places = nombre_places;
        this.montant_total = montant_total;
        this.statut = statut;
        this.date_participation = date_participation;
    }

    // Getters et Setters
    public int getId_participation() { return id_participation; }
    public void setId_participation(int id_participation) { this.id_participation = id_participation; }

    public int getId_event() { return id_event; }
    public void setId_event(int id_event) { this.id_event = id_event; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public int getNombre_places() { return nombre_places; }
    public void setNombre_places(int nombre_places) { this.nombre_places = nombre_places; }

    public double getMontant_total() { return montant_total; }
    public void setMontant_total(double montant_total) { this.montant_total = montant_total; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Timestamp getDate_participation() { return date_participation; }
    public void setDate_participation(Timestamp date_participation) { this.date_participation = date_participation; }

    public String getEmail_snapshot() { return email_snapshot; }
    public void setEmail_snapshot(String email_snapshot) { this.email_snapshot = email_snapshot; }

    public String getNom_snapshot() { return nom_snapshot; }
    public void setNom_snapshot(String nom_snapshot) { this.nom_snapshot = nom_snapshot; }

    public String getPrenom_snapshot() { return prenom_snapshot; }
    public void setPrenom_snapshot(String prenom_snapshot) { this.prenom_snapshot = prenom_snapshot; }

    public String getTelephone_snapshot() { return telephone_snapshot; }
    public void setTelephone_snapshot(String telephone_snapshot) { this.telephone_snapshot = telephone_snapshot; }

    @Override
    public String toString() {
        return "Participation{" +
                "id_participation=" + id_participation +
                ", id_event=" + id_event +
                ", user_id=" + user_id +
                ", nombre_places=" + nombre_places +
                ", montant_total=" + montant_total +
                ", statut='" + statut + '\'' +
                ", date_participation=" + date_participation +
                '}';
    }
}