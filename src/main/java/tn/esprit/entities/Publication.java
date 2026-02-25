package tn.esprit.entities;

import java.time.LocalDateTime;

public class Publication {
    private int id;
    private String titre;
    private String description;
    private LocalDateTime datePublication;
    private String image;
    private int likes;  // Ajouté pour résoudre l'erreur getLikes()
    private int utilisateurId;  // Optionnel : pour savoir qui a créé la publication

    // ============================================
    // CONSTRUCTEURS
    // ============================================

    // Constructeur par défaut
    public Publication() {
        this.datePublication = LocalDateTime.now();
        this.likes = 0;
        this.utilisateurId = 0;
    }

    // Constructeur avec paramètres essentiels (sans date - elle se met auto)
    public Publication(int id, String titre, String description, String image) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.image = image;
        this.datePublication = LocalDateTime.now();
        this.likes = 0;
        this.utilisateurId = 0;
    }

    // Constructeur complet (si tu veux définir la date manuellement)
    public Publication(int id, String titre, String description, LocalDateTime datePublication, String image) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.datePublication = datePublication;
        this.image = image;
        this.likes = 0;
        this.utilisateurId = 0;
    }

    // Constructeur complet avec tous les champs
    public Publication(int id, String titre, String description, LocalDateTime datePublication,
                       String image, int likes, int utilisateurId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.datePublication = datePublication;
        this.image = image;
        this.likes = likes;
        this.utilisateurId = utilisateurId;
    }

    // ============================================
    // GETTERS ET SETTERS
    // ============================================

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

    // NOUVEAU : Gestion des likes
    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void incrementLikes() {
        this.likes++;
    }

    public void decrementLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }

    // NOUVEAU : ID de l'utilisateur créateur
    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    // ============================================
    // MÉTHODES UTILITAIRES
    // ============================================

    @Override
    public String toString() {
        return "Publication{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + (description != null ? description.substring(0, Math.min(30, description.length())) + "..." : "null") + '\'' +
                ", datePublication=" + datePublication +
                ", image='" + image + '\'' +
                ", likes=" + likes +
                ", utilisateurId=" + utilisateurId +
                '}';
    }

    // Pour affichage dans les listes (titre tronqué si trop long)
    public String getTitreAbrege(int maxLength) {
        if (titre == null) return "";
        return titre.length() > maxLength ? titre.substring(0, maxLength) + "..." : titre;
    }

    // Vérifier si la publication a une image
    public boolean hasImage() {
        return image != null && !image.trim().isEmpty();
    }
}