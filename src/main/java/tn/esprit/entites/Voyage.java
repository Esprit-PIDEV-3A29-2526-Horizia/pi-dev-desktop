package tn.esprit.entites;

import java.sql.Date;

public class Voyage {
    private int id;
    private String destination;
    private String description;
    private double prix;
    private Date dateDepart;
    private Date dateRetour;
    private String imageUrl; // Rappel contrainte : URL, pas de BLOB
    private int idCategorie; // Clé étrangère

    // Constructeurs
    public Voyage() {}

    // Constructeur sans ID (pour l'ajout)
    public Voyage(String destination, String description, double prix, Date dateDepart, Date dateRetour, String imageUrl, int idCategorie) {
        this.destination = destination;
        this.description = description;
        this.prix = prix;
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
        this.imageUrl = imageUrl;
        this.idCategorie = idCategorie;
    }

    // Constructeur complet (pour l'affichage)
    public Voyage(int id, String destination, String description, double prix, Date dateDepart, Date dateRetour, String imageUrl, int idCategorie) {
        this.id = id;
        this.destination = destination;
        this.description = description;
        this.prix = prix;
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
        this.imageUrl = imageUrl;
        this.idCategorie = idCategorie;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public Date getDateDepart() { return dateDepart; }
    public void setDateDepart(Date dateDepart) { this.dateDepart = dateDepart; }
    public Date getDateRetour() { return dateRetour; }
    public void setDateRetour(Date dateRetour) { this.dateRetour = dateRetour; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getIdCategorie() { return idCategorie; }
    public void setIdCategorie(int idCategorie) { this.idCategorie = idCategorie; }

    @Override
    public String toString() {
        return "Voyage{" +
                "id=" + id +
                ", destination='" + destination + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", dateDepart=" + dateDepart +
                ", dateRetour=" + dateRetour +
                ", imageUrl='" + imageUrl + '\'' +
                ", idCategorie=" + idCategorie +
                '}';
    }
}