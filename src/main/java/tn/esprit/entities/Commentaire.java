package tn.esprit.entities;

import java.time.LocalDateTime;

public class Commentaire {
    private int id;
    private int publicationId;
    private int utilisateurId;
    private String auteur;
    private String nomUtilisateur;
    private String contenu;
    private LocalDateTime dateCreation;
    private boolean modifie;

    // Constructeurs
    public Commentaire() {
        this.modifie = false;
    }

    // Four‑argument constructor (existing)
    public Commentaire(int publicationId, int utilisateurId, String auteur, String contenu) {
        this.publicationId = publicationId;
        this.utilisateurId = utilisateurId;
        this.auteur = auteur;
        this.nomUtilisateur = auteur;  // Par défaut, nom = auteur
        this.contenu = contenu;
        this.dateCreation = LocalDateTime.now();
        this.modifie = false;
    }

    // NEW three‑argument constructor that delegates to the four‑arg one
    public Commentaire(int publicationId, String auteur, String contenu) {
        this(publicationId, 0, auteur, contenu); // utilisateurId mis à 0 par défaut
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

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) {
        this.contenu = contenu;
        this.modifie = true;  // Marquer comme modifié
    }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public boolean isModifie() { return modifie; }
    public void setModifie(boolean modifie) { this.modifie = modifie; }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", auteur='" + auteur + '\'' +
                ", contenu='" + contenu.substring(0, Math.min(30, contenu.length())) + "...'" +
                ", modifie=" + modifie +
                '}';
    }
}