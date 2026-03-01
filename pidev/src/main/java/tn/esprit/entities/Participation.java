//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tn.esprit.entities;

import java.sql.Timestamp;

public class Participation {
    private int id_participation;
    private int id_event;
    private int nombrePlaces;
    private float montantTotal;
    private String statut;
    private Timestamp dateParticipation;

    public Participation() {
    }

    public Participation(int id_participation, int id_event, int nombrePlaces, float montantTotal, String statut, Timestamp dateParticipation) {
        this.id_participation = id_participation;
        this.id_event = id_event;
        this.nombrePlaces = nombrePlaces;
        this.montantTotal = montantTotal;
        this.statut = statut;
        this.dateParticipation = dateParticipation;
    }

    public Participation(int id_event, int nombrePlaces, float montantTotal, String statut, Timestamp dateParticipation) {
        this.id_event = id_event;
        this.nombrePlaces = nombrePlaces;
        this.montantTotal = montantTotal;
        this.statut = statut;
        this.dateParticipation = dateParticipation;
    }

    public int getId_participation() {
        return this.id_participation;
    }

    public void setId_participation(int id_participation) {
        this.id_participation = id_participation;
    }

    public int getNombrePlaces() {
        return this.nombrePlaces;
    }

    public void setNombrePlaces(int nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
    }

    public float getMontantTotal() {
        return this.montantTotal;
    }

    public void setMontantTotal(float montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getStatut() {
        return this.statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Timestamp getDateParticipation() {
        return this.dateParticipation;
    }

    public void setDateParticipation(Timestamp dateParticipation) {
        this.dateParticipation = dateParticipation;
    }

    public int getId_event() {
        return id_event;
    }

    public void setId_event(int id_event) {
        this.id_event = id_event;
    }

    @Override
    public String toString() {
        return "Participation{" +
                "id_participation=" + id_participation +
                ", id_event=" + id_event +
                ", nombrePlaces=" + nombrePlaces +
                ", montantTotal=" + montantTotal +
                ", statut='" + statut + '\'' +
                ", dateParticipation=" + dateParticipation +
                '}';
    }
}
