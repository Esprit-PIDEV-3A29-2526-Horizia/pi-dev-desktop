package tn.esprit.backend.entities;

import java.time.LocalDateTime;

public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private String adresse;
    private Role role;
    private String imageProfil;
    private LocalDateTime dateInscription;
    private boolean actif;

    public Utilisateur() {
        this.role = Role.USER;
        this.dateInscription = LocalDateTime.now();
        this.actif = true;
    }

    // Getters & Setters complets
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getImageProfil() { return imageProfil; }
    public void setImageProfil(String imageProfil) { this.imageProfil = imageProfil; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}

