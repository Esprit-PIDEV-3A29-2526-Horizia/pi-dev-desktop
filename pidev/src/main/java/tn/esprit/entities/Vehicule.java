package tn.esprit.entities;

public class Vehicule {
    private int idVehicule;
    private String immatriculation;
    private int idModele;
    private int annee;
    private String carburant;
    private String couleur;
    private int kilometrage;
    private String etat;
    private double prixParJour;
    private String photo;

    public Vehicule() {}

    public Vehicule(String immatriculation, int idModele, int annee, String carburant,
                    String couleur, int kilometrage, String etat, double prixParJour, String photo) {
        this.immatriculation = immatriculation;
        this.idModele = idModele;
        this.annee = annee;
        this.carburant = carburant;
        this.couleur = couleur;
        this.kilometrage = kilometrage;
        this.etat = etat;
        this.prixParJour = prixParJour;
        this.photo = photo;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public int getIdModele() {
        return idModele;
    }

    public void setIdModele(int idModele) {
        this.idModele = idModele;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public String getCarburant() {
        return carburant;
    }

    public void setCarburant(String carburant) {
        this.carburant = carburant;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public int getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(int kilometrage) {
        this.kilometrage = kilometrage;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public double getPrixParJour() {
        return prixParJour;
    }

    public void setPrixParJour(double prixParJour) {
        this.prixParJour = prixParJour;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public String toString() {
        return immatriculation + " - " + etat + " - " + prixParJour + " TND/jour";
    }
}
