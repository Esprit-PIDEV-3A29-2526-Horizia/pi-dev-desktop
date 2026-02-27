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
                "client_adresse, client_ville, client_code_postal, client_latitude, client_longitude, " +
                "date_debut, date_fin_prevue, kilometrage_debut, prix_par_jour, montant_total, statut, avance, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConn();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, l.getIdVehicule());
            ps.setString(2, l.getClientNomComplet());
            ps.setString(3, l.getClientTelephone());
            ps.setString(4, l.getClientCin());
            ps.setString(5, l.getClientAdresse());
            ps.setString(6, l.getClientVille());
            ps.setString(7, l.getClientCodePostal());

            // Gérer les valeurs null pour latitude/longitude
            if (l.getClientLatitude() != null) {
                ps.setDouble(8, l.getClientLatitude());
            } else {
                ps.setNull(8, java.sql.Types.DOUBLE);
            }

            if (l.getClientLongitude() != null) {
                ps.setDouble(9, l.getClientLongitude());
            } else {
                ps.setNull(9, java.sql.Types.DOUBLE);
            }

            ps.setTimestamp(10, l.getDateDebut());
            ps.setTimestamp(11, l.getDateFinPrev());
            ps.setInt(12, l.getKilometrageDebut());
            ps.setDouble(13, l.getPrixParJour());
            ps.setDouble(14, l.getMontantTotal());
            ps.setString(15, l.getStatut());
            ps.setDouble(16, l.getAvance());
            ps.setString(17, l.getNotes());

            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) l.setIdLocation(rs.getInt(1));

                // Mettre le véhicule à "louée"
                String updateVehicule = "UPDATE vehicule SET etat = 'louee' WHERE id_vehicule = ?";
                PreparedStatement psUpdate = conn.prepareStatement(updateVehicule);
                psUpdate.setInt(1, l.getIdVehicule());
                psUpdate.executeUpdate();
                psUpdate.close();

                conn.commit();
                System.out.println("✓ Location ajoutée : ID " + l.getIdLocation());
                System.out.println("✓ Véhicule " + l.getIdVehicule() + " → louée");
                return true;
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            System.err.println("Erreur ajout location : " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {}
        }
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
                l.setClientAdresse(rs.getString("client_adresse"));
                l.setClientVille(rs.getString("client_ville"));
                l.setClientCodePostal(rs.getString("client_code_postal"));

                // Gérer les valeurs null pour les coordonnées
                Double latitude = rs.getDouble("client_latitude");
                if (!rs.wasNull()) {
                    l.setClientLatitude(latitude);
                }
                Double longitude = rs.getDouble("client_longitude");
                if (!rs.wasNull()) {
                    l.setClientLongitude(longitude);
                }

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

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConn();
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(sql);

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

            if (ps.executeUpdate() > 0) {
                // Gérer le statut du véhicule selon le statut de la location
                String statut = l.getStatut().toLowerCase();
                String nouvelEtat = null;

                if ("terminée".equals(statut) || "annulée".equals(statut) || "no_show".equals(statut)) {
                    nouvelEtat = "disponible";
                } else if ("réservée".equals(statut) || "en_cours".equals(statut)) {
                    nouvelEtat = "louee";
                }

                if (nouvelEtat != null) {
                    String updateVehicule = "UPDATE vehicule SET etat = ? WHERE id_vehicule = ?";
                    PreparedStatement psUpdate = conn.prepareStatement(updateVehicule);
                    psUpdate.setString(1, nouvelEtat);
                    psUpdate.setInt(2, l.getIdVehicule());
                    psUpdate.executeUpdate();
                    psUpdate.close();
                    System.out.println("✓ Véhicule " + l.getIdVehicule() + " → " + nouvelEtat);
                }

                conn.commit();
                return true;
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {}
        }
    }

    // DELETE - Supprimer une location
    public boolean supprimerLocation(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConn();
            conn.setAutoCommit(false);

            // Récupérer l'ID du véhicule avant suppression
            String getVehicule = "SELECT id_vehicule FROM location WHERE id_location = ?";
            PreparedStatement psGet = conn.prepareStatement(getVehicule);
            psGet.setInt(1, id);
            ResultSet rs = psGet.executeQuery();

            int idVehicule = 0;
            if (rs.next()) {
                idVehicule = rs.getInt("id_vehicule");
            }
            psGet.close();

            // Supprimer la location
            String sql = "DELETE FROM location WHERE id_location = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            if (ps.executeUpdate() > 0 && idVehicule > 0) {
                // Remettre le véhicule à "disponible"
                String updateVehicule = "UPDATE vehicule SET etat = 'disponible' WHERE id_vehicule = ?";
                PreparedStatement psUpdate = conn.prepareStatement(updateVehicule);
                psUpdate.setInt(1, idVehicule);
                psUpdate.executeUpdate();
                psUpdate.close();
                System.out.println("✓ Location supprimée, véhicule " + idVehicule + " → disponible");

                conn.commit();
                return true;
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {}
        }
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