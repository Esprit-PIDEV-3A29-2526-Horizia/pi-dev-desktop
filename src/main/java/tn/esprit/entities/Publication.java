package tn.esprit.entities;

import java.time.LocalDate;

public class Publication {

    private int id;                 // ID de la publication
    private String titre;           // Titre de la publication
    private String contenu;         // Contenu ou description
    private String image;           // URL ou chemin image
    private String lieu;            // Lieu associé
    private String type;            // Type de publication
    private float tarif;            // Tarif si applicable
    private boolean actif;          // Statut actif / publié
    private LocalDate datePublication; // Date de création/publication

    // Constructeurs
    public Publication() {}

    public Publication(int id, String titre, String contenu, String image, String lieu,
                       String type, float tarif, boolean actif, LocalDate datePublication) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.image = image;
        this.lieu = lieu;
        this.type = type;
        this.tarif = tarif;
        this.actif = actif;
        this.datePublication = datePublication;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public float getTarif() { return tarif; }
    public void setTarif(float tarif) { this.tarif = tarif; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public LocalDate getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDate datePublication) { this.datePublication = datePublication; }

    // Méthode pratique pour afficher en console
    @Override
    public String toString() {
        return "Publication{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", image='" + image + '\'' +
                ", lieu='" + lieu + '\'' +
                ", type='" + type + '\'' +
                ", tarif=" + tarif +
                ", actif=" + actif +
                ", datePublication=" + datePublication +
                '}';
    }
}
