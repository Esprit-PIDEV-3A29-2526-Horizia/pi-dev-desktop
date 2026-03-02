package tn.esprit.services;

import tn.esprit.entities.Location;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LocationService {

    private Connection getConn() throws SQLException {
        return MyDataBase.getInstance().getMyConnection();
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
                locations.add(mapResultSetToLocation(rs));
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
                return mapResultSetToLocation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE - Modifier une location
    public boolean modifierLocation(Location l) {
        String sql = "UPDATE location SET id_vehicule=?, client_nom_complet=?, client_telephone=?, client_cin=?, " +
                "client_adresse=?, client_ville=?, client_code_postal=?, client_latitude=?, client_longitude=?, " +
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
            ps.setString(5, l.getClientAdresse());
            ps.setString(6, l.getClientVille());
            ps.setString(7, l.getClientCodePostal());

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
            ps.setTimestamp(12, l.getDateFinReelle());
            ps.setInt(13, l.getKilometrageDebut());
            ps.setInt(14, l.getKilometrageRetour());
            ps.setDouble(15, l.getPrixParJour());
            ps.setDouble(16, l.getMontantTotal());
            ps.setDouble(17, l.getAvance());
            ps.setString(18, l.getStatut());
            ps.setString(19, l.getNotes());
            ps.setInt(20, l.getIdLocation());

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

    // ✅ NOUVELLE MÉTHODE : Mise à jour automatique des statuts
    public int mettreAJourStatutsAutomatique() {
        Connection conn = null;
        PreparedStatement ps = null;
        int count = 0;

        try {
            conn = getConn();
            conn.setAutoCommit(false);

            LocalDate aujourdhui = LocalDate.now();
            Timestamp maintenant = Timestamp.valueOf(aujourdhui.atStartOfDay());

            // 1️⃣ Locations "réservée" qui devraient passer en "en_cours"
            String sqlEnCours = """
                UPDATE location 
                SET statut = 'en_cours' 
                WHERE statut = 'réservée' 
                AND date_debut <= ?
                """;

            ps = conn.prepareStatement(sqlEnCours);
            ps.setTimestamp(1, maintenant);
            int updatedEnCours = ps.executeUpdate();
            count += updatedEnCours;

            if (updatedEnCours > 0) {
                System.out.println("✓ " + updatedEnCours + " location(s) passée(s) de 'réservée' à 'en_cours'");
            }
            ps.close();

            // 2️⃣ Locations "en_cours" qui devraient passer en "terminée"
            String sqlTerminee = """
                UPDATE location 
                SET statut = 'terminée' 
                WHERE statut = 'en_cours' 
                AND date_fin_prevue < ?
                """;

            ps = conn.prepareStatement(sqlTerminee);
            ps.setTimestamp(1, maintenant);
            int updatedTerminee = ps.executeUpdate();
            count += updatedTerminee;

            if (updatedTerminee > 0) {
                System.out.println("✓ " + updatedTerminee + " location(s) passée(s) de 'en_cours' à 'terminée'");

                // 3️⃣ Mettre à jour les véhicules correspondants à "disponible"
                String updateVehicules = """
                    UPDATE vehicule v
                    JOIN location l ON v.id_vehicule = l.id_vehicule
                    SET v.etat = 'disponible'
                    WHERE l.statut = 'terminée' 
                    AND l.date_fin_prevue < ?
                    """;

                PreparedStatement psVehicules = conn.prepareStatement(updateVehicules);
                psVehicules.setTimestamp(1, maintenant);
                int updatedVehicules = psVehicules.executeUpdate();
                psVehicules.close();

                if (updatedVehicules > 0) {
                    System.out.println("✓ " + updatedVehicules + " véhicule(s) remis à 'disponible'");
                }
            }
            ps.close();

            // 4️⃣ Locations avec date_fin_reelle renseignée mais statut pas à jour
            String sqlRetourEffectue = """
                UPDATE location 
                SET statut = 'terminée' 
                WHERE statut != 'terminée' 
                AND date_fin_reelle IS NOT NULL
                """;

            ps = conn.prepareStatement(sqlRetourEffectue);
            int updatedRetour = ps.executeUpdate();
            count += updatedRetour;

            if (updatedRetour > 0) {
                System.out.println("✓ " + updatedRetour + " location(s) terminée(s) avec retour effectué");

                // Mettre à jour les véhicules correspondants
                String updateVehiculesRetour = """
                    UPDATE vehicule v
                    JOIN location l ON v.id_vehicule = l.id_vehicule
                    SET v.etat = 'disponible'
                    WHERE l.statut = 'terminée' 
                    AND l.date_fin_reelle IS NOT NULL
                    """;

                PreparedStatement psVehiculesRetour = conn.prepareStatement(updateVehiculesRetour);
                int updatedVehiculesRetour = psVehiculesRetour.executeUpdate();
                psVehiculesRetour.close();

                if (updatedVehiculesRetour > 0) {
                    System.out.println("✓ " + updatedVehiculesRetour + " véhicule(s) remis à 'disponible' (retour)");
                }
            }

            conn.commit();
            System.out.println("✅ Mise à jour automatique des statuts terminée : " + count + " modification(s)");

        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour automatique des statuts : " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {}
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {}
        }

        return count;
    }

    // ✅ NOUVELLE MÉTHODE : Récupérer les locations en retard
    public List<Location> getLocationsEnRetard() {
        List<Location> resultat = new ArrayList<>();
        String sql = """
            SELECT * FROM location 
            WHERE statut IN ('réservée', 'en_cours')
            AND date_fin_prevue < ?
            ORDER BY date_fin_prevue ASC
            """;

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDate.now().atStartOfDay()));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                resultat.add(mapResultSetToLocation(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération locations en retard : " + e.getMessage());
        }

        return resultat;
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
                resultat.add(mapResultSetToLocation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultat;
    }

    // Helper : mapper ResultSet → Location
    private Location mapResultSetToLocation(ResultSet rs) throws SQLException {
        Location l = new Location();
        l.setIdLocation(rs.getInt("id_location"));
        l.setIdVehicule(rs.getInt("id_vehicule"));
        l.setClientNomComplet(rs.getString("client_nom_complet"));
        l.setClientTelephone(rs.getString("client_telephone"));
        l.setClientCin(rs.getString("client_cin"));
        l.setClientAdresse(rs.getString("client_adresse"));
        l.setClientVille(rs.getString("client_ville"));
        l.setClientCodePostal(rs.getString("client_code_postal"));

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

        int kilometrageRetour = rs.getInt("kilometrage_retour");
        if (!rs.wasNull()) {
            l.setKilometrageRetour(kilometrageRetour);
        }

        l.setPrixParJour(rs.getDouble("prix_par_jour"));
        l.setMontantTotal(rs.getDouble("montant_total"));
        l.setAvance(rs.getDouble("avance"));
        l.setStatut(rs.getString("statut"));
        l.setNotes(rs.getString("notes"));
        return l;
    }
}