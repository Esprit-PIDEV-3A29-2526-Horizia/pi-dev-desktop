package org.example.entities;

public class Modele {

    private int idModele;
    private int idMarque;
    private String nomModele;

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

    @Override
    public String toString() {
        return nomModele;
    }
}
