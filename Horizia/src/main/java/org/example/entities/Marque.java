package org.example.entities;

public class Marque {

    private int idMarque;
    private String nomMarque;
    private String logo;  // ← NOUVEAU CHAMP

    public Marque() {}

    public Marque(String nomMarque) {
        this.nomMarque = nomMarque;
    }

    public Marque(int idMarque, String nomMarque) {
        this.idMarque = idMarque;
        this.nomMarque = nomMarque;
    }

    public Marque(int idMarque, String nomMarque, String logo) {
        this.idMarque = idMarque;
        this.nomMarque = nomMarque;
        this.logo = logo;
    }

    public int getIdMarque() {
        return idMarque;
    }

    public void setIdMarque(int idMarque) {
        this.idMarque = idMarque;
    }

    public String getNomMarque() {
        return nomMarque;
    }

    public void setNomMarque(String nomMarque) {
        this.nomMarque = nomMarque;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public String toString() {
        return nomMarque;
    }
}