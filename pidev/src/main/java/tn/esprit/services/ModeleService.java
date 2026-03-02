package tn.esprit.services;

import tn.esprit.entities.Modele;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModeleService {

    private Connection getConn() throws SQLException {
        return MyDataBase.getInstance().getMyConnection();
    }

    // ═══════════════════════════════════════════════════════
    // Lister tous les modèles (tri par nom)
    // ═══════════════════════════════════════════════════════
    public List<Modele> getAllModeles() {
        List<Modele> modeles = new ArrayList<>();

        // ← IMPORTANT : Sélectionner aussi la colonne "image"
        String sql = "SELECT m.id_modele, m.id_marque, m.nom_modele, m.image, ma.nom_marque " +
                "FROM modele m " +
                "JOIN marque ma ON m.id_marque = ma.id_marque " +
                "ORDER BY ma.nom_marque ASC, m.nom_modele ASC";

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Modele m = new Modele();
                m.setIdModele(rs.getInt("id_modele"));
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomModele(rs.getString("nom_modele"));
                m.setImage(rs.getString("image"));  // ← RÉCUPÉRER L'IMAGE
                modeles.add(m);
            }

            if (modeles.isEmpty()) {
                System.out.println("Aucun modèle présent dans la base.");
            } else {
                System.out.println("Nombre de modèles trouvés : " + modeles.size());
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération modèles : " + e.getMessage());
        }

        return modeles;
    }

    // ═══════════════════════════════════════════════════════
    // Rechercher un modèle par nom (partiel)
    // ═══════════════════════════════════════════════════════
    public List<Modele> rechercherModeleParNom(String recherche) {
        List<Modele> resultat = new ArrayList<>();

        if (recherche == null || recherche.trim().isEmpty()) {
            return getAllModeles();
        }

        String sql = "SELECT m.id_modele, m.id_marque, m.nom_modele, m.image, ma.nom_marque " +
                "FROM modele m " +
                "JOIN marque ma ON m.id_marque = ma.id_marque " +
                "WHERE LOWER(m.nom_modele) LIKE LOWER(?) " +
                "ORDER BY ma.nom_marque ASC, m.nom_modele ASC";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + recherche.trim() + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Modele m = new Modele();
                m.setIdModele(rs.getInt("id_modele"));
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomModele(rs.getString("nom_modele"));
                m.setImage(rs.getString("image"));  // ← RÉCUPÉRER L'IMAGE
                resultat.add(m);
            }

        } catch (SQLException e) {
            System.err.println("Erreur recherche modèle : " + e.getMessage());
        }

        return resultat;
    }

    // ═══════════════════════════════════════════════════════
    // Récupérer un modèle par ID
    // ═══════════════════════════════════════════════════════
    public Modele getModeleById(int id) {
        if (id <= 0) {
            System.out.println("ID invalide.");
            return null;
        }

        String sql = "SELECT m.id_modele, m.id_marque, m.nom_modele, m.image, ma.nom_marque " +
                "FROM modele m " +
                "JOIN marque ma ON m.id_marque = ma.id_marque " +
                "WHERE m.id_modele = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Modele m = new Modele();
                m.setIdModele(rs.getInt("id_modele"));
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomModele(rs.getString("nom_modele"));
                m.setImage(rs.getString("image"));  // ← RÉCUPÉRER L'IMAGE
                return m;
            }
        } catch (SQLException e) {
            System.err.println("Erreur getModeleById : " + e.getMessage());
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════
    // Ajouter un modèle
    // ═══════════════════════════════════════════════════════
    public boolean ajouterModele(Modele modele) {
        if (modele == null || modele.getNomModele() == null || modele.getNomModele().trim().isEmpty()) {
            System.out.println("Erreur : Nom du modèle invalide.");
            return false;
        }

        String sql = "INSERT INTO modele (id_marque, nom_modele, image) VALUES (?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, modele.getIdMarque());
            ps.setString(2, modele.getNomModele().trim());
            ps.setString(3, modele.getImage());  // ← INSÉRER L'IMAGE

            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    modele.setIdModele(rs.getInt(1));
                }
                System.out.println("Succès : Modèle ajouté (ID: " + modele.getIdModele() + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout modèle : " + e.getMessage());
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════
    // Rechercher modèles par nom de marque
    // ═══════════════════════════════════════════════════════
    public List<Modele> rechercherModelesParNomMarque(String nomMarqueRecherche) {
        List<Modele> resultat = new ArrayList<>();

        if (nomMarqueRecherche == null || nomMarqueRecherche.trim().isEmpty()) {
            return getAllModeles();
        }

        String sql = "SELECT m.id_modele, m.id_marque, m.nom_modele, m.image, ma.nom_marque " +
                "FROM modele m " +
                "JOIN marque ma ON m.id_marque = ma.id_marque " +
                "WHERE LOWER(ma.nom_marque) LIKE LOWER(?) " +
                "ORDER BY ma.nom_marque ASC, m.nom_modele ASC";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nomMarqueRecherche.trim() + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Modele m = new Modele();
                m.setIdModele(rs.getInt("id_modele"));
                m.setIdMarque(rs.getInt("id_marque"));
                m.setNomModele(rs.getString("nom_modele"));
                m.setImage(rs.getString("image"));  // ← RÉCUPÉRER L'IMAGE
                resultat.add(m);
            }

        } catch (SQLException e) {
            System.err.println("Erreur recherche modèles par marque : " + e.getMessage());
        }

        return resultat;
    }
}