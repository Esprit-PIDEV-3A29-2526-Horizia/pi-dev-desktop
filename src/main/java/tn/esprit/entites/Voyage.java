package tn.esprit.entites;

import java.sql.Date;

public class Voyage {
    private int id;
    private String titre;
    private String destination;
    private String description;
    private double prix;
    private Date date_depart;
    private Date date_retour;
    private String image_url;
    private int id_categorie;
    private int places_total;
    private int places_restantes;

    // 1. Constructeur vide
    public Voyage() {}

    // 2. Constructeur pour l'ajout (SANS ID)
    public Voyage(String titre, String destination, String description, double prix, Date date_depart, Date date_retour, String image_url, int id_categorie, int places_total, int places_restantes) {
        this.titre = titre;
        this.destination = destination;
        this.description = description;
        this.prix = prix;
        this.date_depart = date_depart;
        this.date_retour = date_retour;
        this.image_url = image_url;
        this.id_categorie = id_categorie;
        this.places_total = places_total;
        this.places_restantes = places_restantes;
    }

    // 3. Constructeur complet (AVEC ID)
    public Voyage(int id, String titre, String destination, String description, double prix, Date date_depart, Date date_retour, String image_url, int id_categorie, int places_total, int places_restantes) {
        this.id = id;
        this.titre = titre; // Corrigé : ajout du "=" manquant
        this.destination = destination;
        this.description = description;
        this.prix = prix;
        this.date_depart = date_depart;
        this.date_retour = date_retour;
        this.image_url = image_url;
        this.id_categorie = id_categorie;
        this.places_total = places_total;
        this.places_restantes = places_restantes;
    }

    // --- GETTERS ET SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public Date getDate_depart() { return date_depart; }
    public void setDate_depart(Date date_depart) { this.date_depart = date_depart; }

    public Date getDate_retour() { return date_retour; }
    public void setDate_retour(Date date_retour) { this.date_retour = date_retour; }

    public String getImage_url() { return image_url; }
    public void setImage_url(String image_url) { this.image_url = image_url; }

    public int getId_categorie() { return id_categorie; }
    public void setId_categorie(int id_categorie) { this.id_categorie = id_categorie; }

    public int getPlaces_total() { return places_total; }
    public void setPlaces_total(int places_total) { this.places_total = places_total; }

    public int getPlaces_restantes() { return places_restantes; }
    public void setPlaces_restantes(int places_restantes) { this.places_restantes = places_restantes; }

    @Override
    public String toString() {
        return "Voyage{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", destination='" + destination + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", date_depart=" + date_depart +
                ", date_retour=" + date_retour +
                ", image_url='" + image_url + '\'' +
                ", id_categorie=" + id_categorie +
                ", places_total=" + places_total +
                ", places_restantes=" + places_restantes +
                '}';
    }
}