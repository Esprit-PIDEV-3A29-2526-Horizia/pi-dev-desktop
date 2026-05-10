package tn.esprit.services;

import tn.esprit.entites.Reservation;
import tn.esprit.entites.Voyage;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements IService<Reservation> {

    private final Connection cnx;

    public ReservationService() {
        cnx = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public void ajouter(Reservation r) {
        String qry = "INSERT INTO reservation (date_reservation, statut, id_voyage, id_user, nbr_personnes, nb_adultes, nb_enfants, prix_total, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setTimestamp(1, r.getDate_reservation() != null ? r.getDate_reservation() : new Timestamp(System.currentTimeMillis()));
            pstm.setString(2, r.getStatut());
            pstm.setInt(3, r.getId_voyage());
            pstm.setInt(4, r.getId_user());
            pstm.setInt(5, r.getNbr_personnes());
            pstm.setInt(6, r.getNb_adultes());
            pstm.setInt(7, r.getNb_enfants());
            pstm.setDouble(8, r.getPrix_total());
            pstm.setString(9, r.getPayment_status());
            pstm.executeUpdate();

            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                r.setId(rs.getInt(1));
            }
            System.out.println("Réservation effectuée !");
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la réservation : " + ex.getMessage());
        }
    }

    @Override
    public void modifier(Reservation r) {
        String qry = "UPDATE reservation SET nbr_personnes = ?, nb_adultes = ?, nb_enfants = ?, statut = ?, id_voyage = ?, id_user = ?, prix_total = ?, payment_status = ? WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, r.getNbr_personnes());
            pstm.setInt(2, r.getNb_adultes());
            pstm.setInt(3, r.getNb_enfants());
            pstm.setString(4, r.getStatut());
            pstm.setInt(5, r.getId_voyage());
            pstm.setInt(6, r.getId_user());
            pstm.setDouble(7, r.getPrix_total());
            pstm.setString(8, r.getPayment_status());
            pstm.setInt(9, r.getId());
            pstm.executeUpdate();
            System.out.println("Réservation mise à jour !");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String qry = "DELETE FROM reservation WHERE id = ?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            pstm.executeUpdate();
            System.out.println("Réservation supprimée !");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public List<Reservation> afficher() {
        List<Reservation> reservations = new ArrayList<>();
        String qry = "SELECT * FROM reservation";

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(qry)) {
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setDate_reservation(rs.getTimestamp("date_reservation"));
                r.setNbr_personnes(rs.getInt("nbr_personnes"));
                r.setNb_adultes(rs.getInt("nb_adultes"));
                r.setNb_enfants(rs.getInt("nb_enfants"));
                r.setStatut(rs.getString("statut"));
                r.setId_voyage(rs.getInt("id_voyage"));
                r.setId_user(rs.getInt("id_user"));
                r.setPrix_total(rs.getDouble("prix_total"));
                r.setPayment_status(rs.getString("payment_status"));
                reservations.add(r);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return reservations;
    }

    public Reservation getById(int id) {
        String sql = "SELECT * FROM reservation WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setDate_reservation(rs.getTimestamp("date_reservation"));
                r.setNbr_personnes(rs.getInt("nbr_personnes"));
                r.setNb_adultes(rs.getInt("nb_adultes"));
                r.setNb_enfants(rs.getInt("nb_enfants"));
                r.setStatut(rs.getString("statut"));
                r.setId_voyage(rs.getInt("id_voyage"));
                r.setId_user(rs.getInt("id_user"));
                r.setPrix_total(rs.getDouble("prix_total"));
                r.setPayment_status(rs.getString("payment_status"));
                return r;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Méthode complète avec tous les paramètres
    public boolean effectuerReservation(int idVoyage, int idUser, int nbrPersonnes, int nbAdultes, int nbEnfants, double prixTotal) {
        String sqlRes = "INSERT INTO reservation (date_reservation, statut, id_voyage, id_user, nbr_personnes, nb_adultes, nb_enfants, prix_total, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlVoyage = "UPDATE voyage SET places_restantes = places_restantes - ? WHERE id = ?";

        try {
            cnx.setAutoCommit(false);

            try (PreparedStatement psRes = cnx.prepareStatement(sqlRes, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psVoy = cnx.prepareStatement(sqlVoyage)) {

                psRes.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                psRes.setString(2, "CONFIRMEE");
                psRes.setInt(3, idVoyage);
                psRes.setInt(4, idUser);
                psRes.setInt(5, nbrPersonnes);
                psRes.setInt(6, nbAdultes);
                psRes.setInt(7, nbEnfants);
                psRes.setDouble(8, prixTotal);
                psRes.setString(9, "NON_PAYEE");
                psRes.executeUpdate();

                psVoy.setInt(1, nbrPersonnes);
                psVoy.setInt(2, idVoyage);
                psVoy.executeUpdate();

                cnx.commit();
                System.out.println("Réservation réussie et places mises à jour !");
                return true;

            } catch (SQLException e) {
                cnx.rollback();
                throw e;
            } finally {
                cnx.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Surcharge pour 3 paramètres (pour le contrôleur existant)
    public boolean effectuerReservation(int idVoyage, int idUser, int nbrPersonnes) {
        int nbAdultes = nbrPersonnes;
        int nbEnfants = 0;

        VoyageService vs = new VoyageService();
        Voyage voyage = vs.getById(idVoyage);

        if (voyage == null) {
            System.err.println("Voyage non trouvé !");
            return false;
        }

        double prixTotal = nbrPersonnes * voyage.getPrix();

        return effectuerReservation(idVoyage, idUser, nbrPersonnes, nbAdultes, nbEnfants, prixTotal);
    }

    public List<Reservation> getReservationsParUtilisateur(int idUser) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = """
            SELECT r.id,
                   r.date_reservation,
                   r.nbr_personnes,
                   r.nb_adultes,
                   r.nb_enfants,
                   r.statut,
                   r.id_voyage,
                   r.prix_total,
                   r.payment_status,
                   v.destination,
                   v.image_url
            FROM reservation r
            JOIN voyage v ON r.id_voyage = v.id
            WHERE r.id_user = ?
            ORDER BY r.date_reservation DESC
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setDate_reservation(rs.getTimestamp("date_reservation"));
                r.setNbr_personnes(rs.getInt("nbr_personnes"));
                r.setNb_adultes(rs.getInt("nb_adultes"));
                r.setNb_enfants(rs.getInt("nb_enfants"));
                r.setStatut(rs.getString("statut"));
                r.setId_voyage(rs.getInt("id_voyage"));
                r.setPrix_total(rs.getDouble("prix_total"));
                r.setPayment_status(rs.getString("payment_status"));
                r.setDestination(rs.getString("destination"));
                r.setImageUrl(rs.getString("image_url"));
                reservations.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL Mes Réservations : " + e.getMessage());
        }
        return reservations;
    }

    public boolean annulerReservation(int reservationId) {
        String sqlGet = "SELECT id_voyage, nbr_personnes FROM reservation WHERE id = ?";
        String sqlUpdate = "UPDATE reservation SET statut = 'ANNULEE' WHERE id = ?";
        String sqlUpdateVoyage = "UPDATE voyage SET places_restantes = places_restantes + ? WHERE id = ?";

        try {
            cnx.setAutoCommit(false);

            int idVoyage = -1;
            int nbr = 0;

            try (PreparedStatement psGet = cnx.prepareStatement(sqlGet)) {
                psGet.setInt(1, reservationId);
                ResultSet rs = psGet.executeQuery();
                if (rs.next()) {
                    idVoyage = rs.getInt("id_voyage");
                    nbr = rs.getInt("nbr_personnes");
                } else {
                    cnx.rollback();
                    System.err.println("Réservation introuvable id=" + reservationId);
                    return false;
                }
            }

            try (PreparedStatement psUp = cnx.prepareStatement(sqlUpdate);
                 PreparedStatement psVoy = cnx.prepareStatement(sqlUpdateVoyage)) {

                psUp.setInt(1, reservationId);
                psUp.executeUpdate();

                psVoy.setInt(1, nbr);
                psVoy.setInt(2, idVoyage);
                psVoy.executeUpdate();

                cnx.commit();
                System.out.println("Réservation annulée + places restaurées !");
                return true;

            } catch (SQLException e) {
                cnx.rollback();
                throw e;
            } finally {
                cnx.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePaymentStatus(int reservationId, String status) {
        String sql = "UPDATE reservation SET payment_status = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatut(int reservationId, String statut) {
        String sql = "UPDATE reservation SET statut = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean confirmerReservation(int reservationId) {
        String sqlUpdate = "UPDATE reservation SET statut = 'CONFIRMEE' WHERE id = ?";
        try (PreparedStatement psUpdate = cnx.prepareStatement(sqlUpdate)) {
            psUpdate.setInt(1, reservationId);
            psUpdate.executeUpdate();
            System.out.println("Réservation confirmée !");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT r.*, v.destination, v.image_url FROM reservation r JOIN voyage v ON r.id_voyage = v.id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setDate_reservation(rs.getTimestamp("date_reservation"));
                r.setNbr_personnes(rs.getInt("nbr_personnes"));
                r.setNb_adultes(rs.getInt("nb_adultes"));
                r.setNb_enfants(rs.getInt("nb_enfants"));
                r.setStatut(rs.getString("statut"));
                r.setId_voyage(rs.getInt("id_voyage"));
                r.setId_user(rs.getInt("id_user"));
                r.setPrix_total(rs.getDouble("prix_total"));
                r.setPayment_status(rs.getString("payment_status"));
                r.setDestination(rs.getString("destination"));
                r.setImageUrl(rs.getString("image_url"));
                liste.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }
}