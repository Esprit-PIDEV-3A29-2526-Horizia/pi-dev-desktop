package tn.esprit.entities;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String telephone;
    private String addresse;
    private Profil profil;
    private int profil_id;

    public User() {}

    public User(int id, String nom, String prenom, String email, String password, String telephone, String addresse) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.addresse = addresse;
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

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAddresse() { return addresse; }
    public void setAddresse(String addresse) { this.addresse = addresse; }

    public Profil getProfil() { return profil; }
    public void setProfil(Profil profil) { this.profil = profil; }

    public String getType() {
        return profil != null ? profil.getType() : null;
    }

    public String getStatut() {
        return profil != null ? profil.getStatut() : null;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", addresse='" + addresse + '\'' +
                ", type=" + (profil != null ? profil.getType() : "null") +
                ", statut=" + (profil != null ? profil.getStatut() : "null") +
                '}';
    }

    public int getProfil_id() {
        int profil_id = 0;
        return profil_id;
    }

    public void setProfil_id(int profil_id) {
        this.profil_id = profil_id;
    }
}