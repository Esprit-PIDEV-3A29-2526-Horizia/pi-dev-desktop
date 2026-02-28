package tn.esprit.entites;

public class Categorie {
    private int id;
    private String nom;
    private String description;

    //Constructeur vide
    public Categorie() {
    }

    //Constructeur sans ID (utilisé pour l'ajout/insertion)
    public Categorie(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    //Constructeur complet (utilisé pour l'affichage/récupération)
    public Categorie(int id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Redéfinition de toString pour l'affichage
    @Override
    public String toString() {
        return "Categorie{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}