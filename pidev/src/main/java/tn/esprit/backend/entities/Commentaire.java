package tn.esprit.backend.entities;

import java.time.LocalDateTime;

public class Commentaire {
    private int id;
    private int publicationId;
    private int utilisateurId;
    private String auteur;        // Nom affichÃ©
    private String contenu;
    private LocalDateTime dateCreation;
    private boolean modifie;

    // Constructeur par dÃ©faut
    public Commentaire() {
        this.dateCreation = LocalDateTime.now();
        this.modifie = false;
        this.utilisateurId = 0;
    }

    // Constructeur rapide (avec nom d'auteur)
    public Commentaire(int publicationId, String auteur, String contenu) {
        this();
        this.publicationId = publicationId;
        this.auteur = auteur;
        this.contenu = contenu;
    }

    // Constructeur complet (utilisateur connectÃ©)
    public Commentaire(int publicationId, int utilisateurId, String auteur, String contenu) {
        this(publicationId, auteur, contenu);
        this.utilisateurId = utilisateurId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPublicationId() { return publicationId; }
    public void setPublicationId(int publicationId) { this.publicationId = publicationId; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    // Retourne le nom d'utilisateur (auteur par dÃ©faut)
    public String getNomUtilisateur() {
        return auteur != null ? auteur : "Anonyme";
    }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) {
        this.contenu = contenu;
        this.modifie = true;
    }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public boolean isModifie() { return modifie; }
    public void setModifie(boolean modifie) { this.modifie = modifie; }

    @Override
    public String toString() {
        return "Commentaire{id=" + id + ", auteur='" + auteur + "', contenu='" +
                (contenu != null ? contenu.substring(0, Math.min(20, contenu.length())) + "..." : "null") + "'}";
    }
}

