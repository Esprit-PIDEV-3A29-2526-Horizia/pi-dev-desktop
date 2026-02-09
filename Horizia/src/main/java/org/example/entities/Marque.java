package org.example.entities;

public class Marque {

    private int idMarque;
    private String nomMarque;

    public Marque() {}

    public Marque(String nomMarque) {
        this.nomMarque = nomMarque;
    }

    public Marque(int idMarque, String nomMarque) {
        this.idMarque = idMarque;
        this.nomMarque = nomMarque;
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

    @Override
    public String toString() {
        return nomMarque;
    }

}
