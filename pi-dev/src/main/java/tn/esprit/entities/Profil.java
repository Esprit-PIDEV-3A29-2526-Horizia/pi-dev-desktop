package tn.esprit.entities;

public class Profil {
    private int id;
    private String type;   // CLIENT, AGENT, ADMIN
    private String statut; // ACTIF, BLOQUE

    public Profil() {}

    public Profil(String type, String statut) {
        this.type = type;
        this.statut = statut;
    }

    public Profil(int id, String type, String statut) {
        this.id = id;
        this.type = type;
        this.statut = statut;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Profil{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
