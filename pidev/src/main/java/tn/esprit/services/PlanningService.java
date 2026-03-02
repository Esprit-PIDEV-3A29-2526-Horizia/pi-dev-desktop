package tn.esprit.services;

import tn.esprit.entities.Location;
import tn.esprit.utils.MyDataBase;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Service de Planning et Calendrier pour Horizia
 * FIX : java.sql.Date utilisé explicitement (supprime l'ambiguïté avec java.util.Date)
 */
public class PlanningService {

    private static Connection getConn() throws SQLException {
        return MyDataBase.getInstance().getMyConnection();
    }

    // ─────────────────────────────────────────────────────────────
    // RÉCUPÉRATION DES LOCATIONS PAR PÉRIODE
    // ─────────────────────────────────────────────────────────────

    public static List<Location> getLocationsDuMois(int annee, int mois) {
        List<Location> locations = new ArrayList<>();
        String sql = """
            SELECT l.*
            FROM location l
            WHERE (
                (YEAR(l.date_debut) = ? AND MONTH(l.date_debut) = ?)
                OR
                (YEAR(l.date_fin_prevue) = ? AND MONTH(l.date_fin_prevue) = ?)
                OR
                (l.date_debut <= LAST_DAY(CONCAT(?, '-', LPAD(?, 2, '0'), '-01'))
                 AND l.date_fin_prevue >= CONCAT(?, '-', LPAD(?, 2, '0'), '-01'))
            )
            AND l.statut NOT IN ('annulée', 'no_show')
            ORDER BY l.date_debut ASC
            """;

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, annee); stmt.setInt(2, mois);
            stmt.setInt(3, annee); stmt.setInt(4, mois);
            stmt.setInt(5, annee); stmt.setInt(6, mois);
            stmt.setInt(7, annee); stmt.setInt(8, mois);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                locations.add(mapResultSetToLocation(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PlanningService] Erreur getLocationsDuMois: " + e.getMessage());
        }
        return locations;
    }

    public static List<Location> getLocationsPeriode(LocalDate debut, LocalDate fin) {
        List<Location> locations = new ArrayList<>();
        String sql = """
            SELECT l.*
            FROM location l
            WHERE l.date_debut <= ? AND l.date_fin_prevue >= ?
            AND l.statut NOT IN ('annulée', 'no_show')
            ORDER BY l.date_debut
            """;

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // FIX : java.sql.Date.valueOf() explicite — supprime l'ambiguïté
            stmt.setDate(1, java.sql.Date.valueOf(fin));
            stmt.setDate(2, java.sql.Date.valueOf(debut));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                locations.add(mapResultSetToLocation(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PlanningService] Erreur getLocationsPeriode: " + e.getMessage());
        }
        return locations;
    }

    public static List<Location> getLocationsParDate(LocalDate date) {
        return getLocationsPeriode(date, date);
    }

    // ─────────────────────────────────────────────────────────────
    // DÉTECTION DE CONFLITS
    // ─────────────────────────────────────────────────────────────

    public static boolean isVehiculeDisponible(int idVehicule, LocalDate debut, LocalDate fin,
                                               int excludeLocationId) {
        String sql = """
            SELECT COUNT(*) FROM location
            WHERE id_vehicule = ?
            AND id_location != ?
            AND statut NOT IN ('annulée', 'no_show', 'terminée')
            AND date_debut < ?
            AND date_fin_prevue > ?
            """;

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVehicule);
            stmt.setInt(2, excludeLocationId);
            // FIX : java.sql.Date.valueOf() explicite
            stmt.setDate(3, java.sql.Date.valueOf(fin));
            stmt.setDate(4, java.sql.Date.valueOf(debut));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            System.err.println("[PlanningService] Erreur isVehiculeDisponible: " + e.getMessage());
        }
        return false;
    }

    public static Map<Integer, List<Location>> detecterConflitsDuMois(int annee, int mois) {
        Map<Integer, List<Location>> conflits = new HashMap<>();
        List<Location> locations = getLocationsDuMois(annee, mois);

        Map<Integer, List<Location>> parVehicule = new HashMap<>();
        for (Location loc : locations) {
            int idVehicule = loc.getIdVehicule();
            if (idVehicule > 0) {
                parVehicule.computeIfAbsent(idVehicule, k -> new ArrayList<>()).add(loc);
            }
        }

        for (Map.Entry<Integer, List<Location>> entry : parVehicule.entrySet()) {
            List<Location> locs = entry.getValue();
            for (int i = 0; i < locs.size(); i++) {
                for (int j = i + 1; j < locs.size(); j++) {
                    if (seChevauchent(locs.get(i), locs.get(j))) {
                        conflits.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                                .addAll(Arrays.asList(locs.get(i), locs.get(j)));
                    }
                }
            }
        }
        return conflits;
    }

    public static boolean seChevauchent(Location l1, Location l2) {
        if (l1.getDateDebut() == null || l1.getDateFinPrev() == null ||
                l2.getDateDebut() == null || l2.getDateFinPrev() == null) return false;

        LocalDate debut1 = l1.getDateDebut().toLocalDateTime().toLocalDate();
        LocalDate fin1   = l1.getDateFinPrev().toLocalDateTime().toLocalDate();
        LocalDate debut2 = l2.getDateDebut().toLocalDateTime().toLocalDate();
        LocalDate fin2   = l2.getDateFinPrev().toLocalDateTime().toLocalDate();

        return debut1.isBefore(fin2) && debut2.isBefore(fin1);
    }

    // ─────────────────────────────────────────────────────────────
    // STATISTIQUES DE PLANNING
    // ─────────────────────────────────────────────────────────────

    public static double calculerTauxOccupation(int annee, int mois) {
        YearMonth ym = YearMonth.of(annee, mois);
        int nbJoursMois = ym.lengthOfMonth();

        int nbVehicules = getNbVehiculesTotal();
        if (nbVehicules == 0) return 0;

        int joursOccupes = calculerJoursOccupes(annee, mois, nbJoursMois);
        return (double) joursOccupes / (nbVehicules * nbJoursMois) * 100;
    }

    public static double calculerCADuMois(int annee, int mois) {
        String sql = """
            SELECT COALESCE(SUM(prix_par_jour * DATEDIFF(date_fin_prevue, date_debut)), 0)
            FROM location
            WHERE YEAR(date_debut) = ? AND MONTH(date_debut) = ?
            AND statut NOT IN ('annulée', 'no_show')
            """;

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, annee);
            stmt.setInt(2, mois);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[PlanningService] Erreur calculerCADuMois: " + e.getMessage());
        }
        return 0;
    }

    public static Map<String, Integer> getStatutsParMois(int annee, int mois) {
        Map<String, Integer> statuts = new LinkedHashMap<>();
        String sql = """
            SELECT statut, COUNT(*) as nb
            FROM location
            WHERE YEAR(date_debut) = ? AND MONTH(date_debut) = ?
            GROUP BY statut
            ORDER BY nb DESC
            """;

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, annee);
            stmt.setInt(2, mois);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                statuts.put(rs.getString("statut"), rs.getInt("nb"));
            }
        } catch (SQLException e) {
            System.err.println("[PlanningService] Erreur getStatutsParMois: " + e.getMessage());
        }
        return statuts;
    }

    public static List<Location> getLocationsQuiTerminentBientot(int nbJours) {
        List<Location> locations = new ArrayList<>();
        LocalDate debut = LocalDate.now();
        LocalDate fin   = LocalDate.now().plusDays(nbJours);

        String sql = """
            SELECT l.*
            FROM location l
            WHERE l.date_fin_prevue BETWEEN ? AND ?
            AND l.statut = 'en_cours'
            ORDER BY l.date_fin_prevue ASC
            """;

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // FIX : java.sql.Date.valueOf() explicite
            stmt.setDate(1, java.sql.Date.valueOf(debut));
            stmt.setDate(2, java.sql.Date.valueOf(fin));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                locations.add(mapResultSetToLocation(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PlanningService] Erreur getLocationsQuiTerminentBientot: " + e.getMessage());
        }
        return locations;
    }

    // ─────────────────────────────────────────────────────────────
    // UTILITAIRES CALENDRIER
    // ─────────────────────────────────────────────────────────────

    public static String getNomMois(int mois, int annee) {
        return Month.of(mois).getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + annee;
    }

    public static String getCouleurStatut(String statut) {
        if (statut == null) return "#95a5a6";
        return switch (statut.toLowerCase()) {
            case "réservée" -> "#3498db";
            case "en_cours" -> "#27ae60";
            case "terminée" -> "#95a5a6";
            case "annulée"  -> "#e74c3c";
            case "no_show"  -> "#e67e22";
            default         -> "#95a5a6";
        };
    }

    public static String getEmojiStatut(String statut) {
        if (statut == null) return "❓";
        return switch (statut.toLowerCase()) {
            case "réservée" -> "📅";
            case "en_cours" -> "🚗";
            case "terminée" -> "✅";
            case "annulée"  -> "❌";
            case "no_show"  -> "⚠️";
            default         -> "❓";
        };
    }

    // ─────────────────────────────────────────────────────────────
    // MÉTHODES PRIVÉES
    // ─────────────────────────────────────────────────────────────

    private static int getNbVehiculesTotal() {
        String sql = "SELECT COUNT(*) FROM vehicule WHERE etat != 'hors_service'";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[PlanningService] Erreur getNbVehiculesTotal: " + e.getMessage());
        }
        return 0;
    }

    private static int calculerJoursOccupes(int annee, int mois, int nbJoursMois) {
        List<Location> locations = getLocationsDuMois(annee, mois);
        Set<String> joursOccupes = new HashSet<>();

        LocalDate debutMois = LocalDate.of(annee, mois, 1);
        LocalDate finMois   = debutMois.withDayOfMonth(nbJoursMois);

        for (Location loc : locations) {
            if (loc.getDateDebut() == null || loc.getDateFinPrev() == null) continue;
            LocalDate locDebut = loc.getDateDebut().toLocalDateTime().toLocalDate();
            LocalDate locFin   = loc.getDateFinPrev().toLocalDateTime().toLocalDate();

            LocalDate d = locDebut.isAfter(debutMois) ? locDebut : debutMois;
            LocalDate f = locFin.isBefore(finMois)    ? locFin   : finMois;
            int idVehicule = loc.getIdVehicule();
            while (!d.isAfter(f)) {
                joursOccupes.add(idVehicule + "-" + d);
                d = d.plusDays(1);
            }
        }
        return joursOccupes.size();
    }

    private static Location mapResultSetToLocation(ResultSet rs) throws SQLException {
        Location loc = new Location();
        loc.setIdLocation(rs.getInt("id_location"));
        loc.setIdVehicule(rs.getInt("id_vehicule"));
        loc.setClientNomComplet(rs.getString("client_nom_complet"));
        loc.setClientTelephone(rs.getString("client_telephone"));
        loc.setClientCin(rs.getString("client_cin"));
        loc.setStatut(rs.getString("statut"));
        loc.setNotes(rs.getString("notes"));
        loc.setPrixParJour(rs.getDouble("prix_par_jour"));
        loc.setMontantTotal(rs.getDouble("montant_total"));
        loc.setAvance(rs.getDouble("avance"));
        loc.setKilometrageDebut(rs.getInt("kilometrage_debut"));

        Timestamp dateDebut = rs.getTimestamp("date_debut");
        Timestamp dateFin   = rs.getTimestamp("date_fin_prevue");
        Timestamp dateFinR  = rs.getTimestamp("date_fin_reelle");
        if (dateDebut != null) loc.setDateDebut(dateDebut);
        if (dateFin   != null) loc.setDateFinPrev(dateFin);
        if (dateFinR  != null) loc.setDateFinReelle(dateFinR);

        return loc;
    }
}