//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tn.esprit.entities;

import java.sql.Timestamp;

public class Events {
    private int id_event;
    private String titre;
    private String description;
    private String categorie;
    private String location;
    private Timestamp dateDebut;
    private Timestamp dateFin;
    private float prix;
    private int capaciteMax;
    private int placesRestantes;
    private String image_url;
    private String statut;
    private int id_createur;

    public Events() {
    }

    public Events(int id_event, String titre, String description, String categorie, String location, Timestamp dateDebut, Timestamp dateFin, float prix, int capaciteMax, int placesRestantes, String image_url, String statut, int id_createur) {
        this.id_event = id_event;
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.location = location;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prix = prix;
        this.capaciteMax = capaciteMax;
        this.placesRestantes = placesRestantes;
        this.image_url = image_url;
        this.statut = statut;
        this.id_createur = id_createur;
    }

    public Events(String titre, String description, String categorie, String location, Timestamp dateDebut, Timestamp dateFin, float prix, int capaciteMax, int placesRestantes, String image_url, String statut, int id_createur) {
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.location = location;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prix = prix;
        this.capaciteMax = capaciteMax;
        this.placesRestantes = placesRestantes;
        this.image_url = image_url;
        this.statut = statut;
        this.id_createur = id_createur;
    }

    public int getId_event() {
        return this.id_event;
    }

    public String getTitre() {
        return this.titre;
    }

    public String getDescription() {
        return this.description;
    }

    public String getCategorie() {
        return this.categorie;
    }

    public String getLocation() {
        return this.location;
    }

    public Timestamp getDateDebut() {
        return this.dateDebut;
    }

    public Timestamp getDateFin() {
        return this.dateFin;
    }

    public float getPrix() {
        return this.prix;
    }

    public int getCapaciteMax() {
        return this.capaciteMax;
    }

    public int getPlacesRestantes() {
        return this.placesRestantes;
    }

    public String getImage_url() {
        return this.image_url;
    }

    public String getStatut() {
        return this.statut;
    }

    public int getId_createur() {
        return this.id_createur;
    }

    public void setId_event(int id_event) {
        this.id_event = id_event;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDateDebut(Timestamp dateDebut) {
        this.dateDebut = dateDebut;
    }

    public void setDateFin(Timestamp dateFin) {
        this.dateFin = dateFin;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public void setCapaciteMax(int capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public void setPlacesRestantes(int placesRestantes) {
        this.placesRestantes = placesRestantes;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setId_createur(int id_createur) {
        this.id_createur = id_createur;
    }

    public String toString() {
        int var10000 = this.id_event;
        return "Events{id_event=" + var10000 + ", titre='" + this.titre + "', description='" + this.description + "', categorie='" + this.categorie + "', location='" + this.location + "', dateDebut=" + String.valueOf(this.dateDebut) + ", dateFin=" + String.valueOf(this.dateFin) + ", prix=" + this.prix + ", capaciteMax=" + this.capaciteMax + ", placesRestantes=" + this.placesRestantes + ", image_url='" + this.image_url + "', statut='" + this.statut + "', id_createur=" + this.id_createur + "}";
    }
}
