package tn.esprit.entities;

import java.time.LocalDateTime;

public class utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private String adresse;
    private Role role;
    private LocalDateTime dateInscription;
    private boolean actif;
    private String imageProfil;

    public enum Role {
        ADMIN, USER
    }

    public utilisateur() {
        this.dateInscription = LocalDateTime.now();
        this.role = Role.USER;
        this.actif = true;
    }

    // Getters et Setters
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

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public String getImageProfil() { return imageProfil; }
    public void setImageProfil(String imageProfil) { this.imageProfil = imageProfil; }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
}