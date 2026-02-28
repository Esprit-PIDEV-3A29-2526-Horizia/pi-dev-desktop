package tn.esprit.backend.entities;

import java.time.LocalDateTime;

public class Publication {
    private int id;
    private String titre;
    private String description;
    private String image;
    private Categorie categorie;     // â† Utiliser l'enum
    private int utilisateurId;
    private String auteur;
    private int likes;
    private int commentaires;
    private LocalDateTime dateCreation;

    public Publication() {
        this.dateCreation = LocalDateTime.now();
        this.likes = 0;
        this.commentaires = 0;
        this.categorie = Categorie.TOUS;
    }

    // Getters & Setters complets
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getCommentaires() { return commentaires; }
    public void setCommentaires(int commentaires) { this.commentaires = commentaires; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getTitreAbrege(int max) {
        return titre != null && titre.length() > max ? titre.substring(0, max) + "..." : titre;
    }
}

