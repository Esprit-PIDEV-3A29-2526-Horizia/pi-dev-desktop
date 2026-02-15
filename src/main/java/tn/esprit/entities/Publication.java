package tn.esprit.entities;

import java.time.LocalDateTime;

public class Publication {
    private int id;
    private String titre;
    private String description;
    private LocalDateTime datePublication;
    private String image;

    // Constructeur par défaut
    public Publication() {
        // La date sera définie automatiquement lors de la création
        this.datePublication = LocalDateTime.now();
    }

    // Constructeur avec paramètres (sans date - elle se met auto)
    public Publication(int id, String titre, String description, String image) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.image = image;
        this.datePublication = LocalDateTime.now();
    }

    // Constructeur complet (si tu veux définir la date manuellement)
    public Publication(int id, String titre, String description, LocalDateTime datePublication, String image) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.datePublication = datePublication;
        this.image = image;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Publication{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", datePublication=" + datePublication +
                ", image='" + image + '\'' +
                '}';
    }
}