package tn.esprit.entities;

import tn.esprit.utils.MyDataBase;
import java.sql.*;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String telephone;
    private String addresse;
    private Profil profil;
    private String faceDescriptor;

    public User() {}

    public User(int id, String nom, String prenom, String email, String password,
                String telephone, String addresse) {
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

    public String getFaceDescriptor() { return faceDescriptor; }
    public void setFaceDescriptor(String faceDescriptor) { this.faceDescriptor = faceDescriptor; }

    public int getProfil_id() {
        return profil != null ? profil.getId() : 0;
    }

    public void setProfil_id(int profilId) {
        if (this.profil == null) {
            this.profil = new Profil();
        }
        this.profil.setId(profilId);
    }

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
                ", type=" + getType() +
                '}';
    }

    // Méthodes statiques pour la base de données
    public static User getUserByEmail(String email) {
        String query = "SELECT u.id, u.nom, u.prenom, u.email, u.telephone, u.addresse, u.face_descriptor, " +
                "p.id as profil_id, p.type, p.statut " +
                "FROM user u " +
                "LEFT JOIN profil p ON u.profil_id = p.id " +
                "WHERE u.email = ?";

        try (Connection conn = MyDataBase.getInstance().getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setTelephone(rs.getString("telephone"));
                user.setAddresse(rs.getString("addresse"));
                user.setFaceDescriptor(rs.getString("face_descriptor"));

                int profilId = rs.getInt("profil_id");
                if (!rs.wasNull()) {
                    Profil profil = new Profil();
                    profil.setId(profilId);
                    profil.setType(rs.getString("type"));
                    profil.setStatut(rs.getString("statut"));
                    user.setProfil(profil);
                }
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean saveFaceDescriptor(int userId, String faceDescriptor) {
        String query = "UPDATE user SET face_descriptor = ? WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, faceDescriptor);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getFaceDescriptor(int userId) {
        String query = "SELECT face_descriptor FROM user WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getMyConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("face_descriptor");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}