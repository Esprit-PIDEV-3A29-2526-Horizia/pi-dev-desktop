package tn.esprit.entities;

import java.time.LocalDateTime;

public class Publication {
    private int id;
    private String titre;
    private String lieu;
    private String image;
    private String contenu;
    private float tarif;
    private boolean actif;
    private String type;
    private LocalDateTime datePublication;
    private int likes;
    private boolean estPublie;

    // Constructeur par défaut
    public Publication() {
    }

    // Constructeur avec paramètres
    public Publication(int id, String titre, String lieu, String image, String contenu,
                       float tarif, boolean actif, String type, LocalDateTime datePublication,
                       int likes, boolean estPublie) {
        this.id = id;
        this.titre = titre;
        this.lieu = lieu;
        this.image = image;
        this.contenu = contenu;
        this.tarif = tarif;
        this.actif = actif;
        this.type = type;
        this.datePublication = datePublication;
        this.likes = likes;
        this.estPublie = estPublie;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public float getTarif() {
        return tarif;
    }

    public void setTarif(float tarif) {
        this.tarif = tarif;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public boolean isEstPublie() {
        return estPublie;
    }

    public void setEstPublie(boolean estPublie) {
        this.estPublie = estPublie;
    }

    @Override
    public String toString() {
        return "Publication{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", lieu='" + lieu + '\'' +
                ", type='" + type + '\'' +
                ", tarif=" + tarif +
                '}';
    }
}