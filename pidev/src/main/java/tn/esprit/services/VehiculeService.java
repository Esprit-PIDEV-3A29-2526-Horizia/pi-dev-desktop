package tn.esprit.services;

import tn.esprit.entities.Vehicule;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeService {

    private Connection getConn() throws SQLException {
        return MyDataBase.getInstance().getMyConnection();
    }

    // CREATE - Ajouter un véhicule
    public boolean ajouterVehicule(Vehicule v) {
        String sql = "INSERT INTO vehicule (immatriculation, id_modele, annee, carburant, couleur, " +
                "kilometrage, etat, prix_par_jour, photo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, v.getImmatriculation());
            ps.setInt(2, v.getIdModele());
            ps.setInt(3, v.getAnnee());
            ps.setString(4, v.getCarburant());
            ps.setString(5, v.getCouleur());
            ps.setInt(6, v.getKilometrage());
            ps.setString(7, v.getEtat());
            ps.setDouble(8, v.getPrixParJour());
            ps.setString(9, v.getPhoto());

            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) v.setIdVehicule(rs.getInt(1));
                System.out.println("Véhicule ajouté : " + v.getImmatriculation());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout véhicule : " + e.getMessage());
        }
        return false;
    }

    // READ - Tous les véhicules
    public List<Vehicule> getAllVehicules() {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT v.* FROM vehicule v ORDER BY immatriculation ASC";

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vehicule v = mapRowToVehicule(rs);
                vehicules.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicules;
    }

    // READ - Par ID
    public Vehicule getVehiculeById(int id) {
        String sql = "SELECT * FROM vehicule WHERE id_vehicule = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRowToVehicule(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE - Modifier un véhicule
    public boolean modifierVehicule(Vehicule v) {
        String sql = "UPDATE vehicule SET immatriculation=?, id_modele=?, annee=?, carburant=?, " +
                "couleur=?, kilometrage=?, etat=?, prix_par_jour=?, photo=? " +
                "WHERE id_vehicule=?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, v.getImmatriculation());
            ps.setInt(2, v.getIdModele());
            ps.setInt(3, v.getAnnee());
            ps.setString(4, v.getCarburant());
            ps.setString(5, v.getCouleur());
            ps.setInt(6, v.getKilometrage());
            ps.setString(7, v.getEtat());
            ps.setDouble(8, v.getPrixParJour());
            ps.setString(9, v.getPhoto());
            ps.setInt(10, v.getIdVehicule());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // DELETE - Supprimer un véhicule
    public boolean supprimerVehicule(int id) {
        String sql = "DELETE FROM vehicule WHERE id_vehicule = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression : " + e.getMessage());
            if (e.getSQLState().startsWith("23000")) {
                System.out.println("Impossible : ce véhicule a des locations associées.");
            }
        }
        return false;
    }

    // Recherche par immatriculation (partielle)
    public List<Vehicule> rechercherParImmatriculation(String immat) {
        List<Vehicule> resultat = new ArrayList<>();
        String sql = "SELECT * FROM vehicule WHERE immatriculation LIKE ? ORDER BY immatriculation ASC";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + immat + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                resultat.add(mapRowToVehicule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultat;
    }

    // Tri par prix (ASC ou DESC)
    public List<Vehicule> getVehiculesTriesParPrix(String ordre) {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule ORDER BY prix_par_jour " + ("DESC".equalsIgnoreCase(ordre) ? "DESC" : "ASC");

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                vehicules.add(mapRowToVehicule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicules;
    }

    // Helper : mapper ResultSet → Vehicule
    private Vehicule mapRowToVehicule(ResultSet rs) throws SQLException {
        Vehicule v = new Vehicule();
        v.setIdVehicule(rs.getInt("id_vehicule"));
        v.setImmatriculation(rs.getString("immatriculation"));
        v.setIdModele(rs.getInt("id_modele"));
        v.setAnnee(rs.getInt("annee"));
        v.setCarburant(rs.getString("carburant"));
        v.setCouleur(rs.getString("couleur"));
        v.setKilometrage(rs.getInt("kilometrage"));
        v.setEtat(rs.getString("etat"));
        v.setPrixParJour(rs.getDouble("prix_par_jour"));
        v.setPhoto(rs.getString("photo"));
        return v;
    }
}