package tn.esprit.entities;
import tn.esprit.entities.Profil;
public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String telephone;
    private String addresse;
    private Profil profil;

    public User(int id, String nom, String prenom, String email, String password, String telephone, String addresse) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.addresse = addresse;


    }

    public User(String nom, String prenom, String email, String password, String telephone, String addresse) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.addresse = addresse;
    }


    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return addresse;
    }

    public void setAdresse(String adresse) {
        this.addresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Profil getProfil() {
        return profil;
    }
    public void setProfil(Profil profil) {
        this.profil = profil;
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", telephone='" + telephone + '\'' +
                ", 2adresse='" + addresse + '\'' +
                '}';
    }
}
