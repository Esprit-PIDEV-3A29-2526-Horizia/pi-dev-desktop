package org.example.entities;

public class Modele {

    private int idModele;
    private int idMarque;
    private String nomModele;
    private String image;  // ← NOUVEAU CHAMP

    public Modele() {}

    public Modele(int idMarque, String nomModele) {
        this.idMarque = idMarque;
        this.nomModele = nomModele;
    }

    public Modele(int idModele, int idMarque, String nomModele) {
        this.idModele = idModele;
        this.idMarque = idMarque;
        this.nomModele = nomModele;
    }

    public Modele(int idModele, int idMarque, String nomModele, String image) {
        this.idModele = idModele;
        this.idMarque = idMarque;
        this.nomModele = nomModele;
        this.image = image;
    }

    public int getIdModele() {
        return idModele;
    }

    public void setIdModele(int idModele) {
        this.idModele = idModele;
    }

    public int getIdMarque() {
        return idMarque;
    }

    public void setIdMarque(int idMarque) {
        this.idMarque = idMarque;
    }

    public String getNomModele() {
        return nomModele;
    }

    public void setNomModele(String nomModele) {
        this.nomModele = nomModele;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return nomModele;
    }
}