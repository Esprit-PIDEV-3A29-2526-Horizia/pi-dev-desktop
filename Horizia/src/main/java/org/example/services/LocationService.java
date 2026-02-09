package org.example.services;

import org.example.entities.Location;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationService {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // CREATE - Ajouter une location
    public boolean ajouterLocation(Location l) {
        String sql = "INSERT INTO location (id_vehicule, client_nom_complet, client_telephone, client_cin, " +
                "date_debut, date_fin_prevue, kilometrage_debut, prix_par_jour, montant_total, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, l.getIdVehicule());
            ps.setString(2, l.getClientNomComplet());
            ps.setString(3, l.getClientTelephone());
            ps.setString(4, l.getClientCin());
            ps.setTimestamp(5, l.getDateDebut());
            ps.setTimestamp(6, l.getDateFinPrev());
            ps.setInt(7, l.getKilometrageDebut());
            ps.setDouble(8, l.getPrixParJour());
            ps.setDouble(9, l.getMontantTotal());
            ps.setString(10, l.getStatut());

            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) l.setIdLocation(rs.getInt(1));
                System.out.println("Location ajoutée : ID " + l.getIdLocation());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout location : " + e.getMessage());
        }
        return false;
    }

    // READ - Toutes les locations
    public List<Location> getAllLocations() {
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT * FROM location ORDER BY date_debut DESC";

        try (Connection conn = getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Location l = new Location();
                l.setIdLocation(rs.getInt("id_location"));
                l.setIdVehicule(rs.getInt("id_vehicule"));
                l.setClientNomComplet(rs.getString("client_nom_complet"));
                l.setClientTelephone(rs.getString("client_telephone"));
                l.setClientCin(rs.getString("client_cin"));
                l.setDateDebut(rs.getTimestamp("date_debut"));
                l.setDateFinPrev(rs.getTimestamp("date_fin_prevue"));
                l.setDateFinReelle(rs.getTimestamp("date_fin_reelle"));
                l.setKilometrageDebut(rs.getInt("kilometrage_debut"));
                l.setKilometrageRetour(rs.getInt("kilometrage_retour"));
                l.setPrixParJour(rs.getDouble("prix_par_jour"));
                l.setMontantTotal(rs.getDouble("montant_total"));
                l.setAvance(rs.getDouble("avance"));
                l.setStatut(rs.getString("statut"));
                l.setNotes(rs.getString("notes"));
                locations.add(l);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return locations;
    }

    // READ - Par ID
    public Location getLocationById(int id) {
        String sql = "SELECT * FROM location WHERE id_location = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Location l = new Location();
                l.setIdLocation(rs.getInt("id_location"));
                // remplir tous les champs...
                return l;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE - Modifier une location (ex: retour, statut, notes...)
    public boolean modifierLocation(Location l) {
        String sql = "UPDATE location SET id_vehicule=?, client_nom_complet=?, client_telephone=?, client_cin=?, " +
                "date_debut=?, date_fin_prevue=?, date_fin_reelle=?, kilometrage_debut=?, kilometrage_retour=?, " +
                "prix_par_jour=?, montant_total=?, avance=?, statut=?, notes=? " +
                "WHERE id_location=?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, l.getIdVehicule());
            ps.setString(2, l.getClientNomComplet());
            ps.setString(3, l.getClientTelephone());
            ps.setString(4, l.getClientCin());
            ps.setTimestamp(5, l.getDateDebut());
            ps.setTimestamp(6, l.getDateFinPrev());
            ps.setTimestamp(7, l.getDateFinReelle());
            ps.setInt(8, l.getKilometrageDebut());
            ps.setInt(9, l.getKilometrageRetour());
            ps.setDouble(10, l.getPrixParJour());
            ps.setDouble(11, l.getMontantTotal());
            ps.setDouble(12, l.getAvance());
            ps.setString(13, l.getStatut());
            ps.setString(14, l.getNotes());
            ps.setInt(15, l.getIdLocation());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // DELETE - Supprimer une location
    public boolean supprimerLocation(int id) {
        String sql = "DELETE FROM location WHERE id_location = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Recherche par client (nom ou téléphone)
    public List<Location> rechercherParClient(String recherche) {
        List<Location> resultat = new ArrayList<>();
        String sql = "SELECT * FROM location WHERE client_nom_complet LIKE ? OR client_telephone LIKE ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + recherche + "%");
            ps.setString(2, "%" + recherche + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Location l = new Location();
                l.setIdLocation(rs.getInt("id_location"));
                // remplir...
                resultat.add(l);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultat;
    }

    private boolean vehiculeExiste(int idVehicule) {
        String sql = "SELECT COUNT(*) FROM vehicule WHERE id_vehicule = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idVehicule);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérif véhicule : " + e.getMessage());
        }
        return false;
    }
}