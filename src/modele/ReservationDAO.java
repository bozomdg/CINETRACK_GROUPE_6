package modele;

import connexion.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table reservations.
 * Le trigger PostgreSQL gère automatiquement la décrémentation/incrémentation
 * des places disponibles dans la table seances.
 */
public class ReservationDAO {

    /**
     * Construit un objet Reservation à partir d'un ResultSet.
     */
    private Reservation buildReservation(ResultSet rs) throws SQLException {
        return new Reservation(
            rs.getInt("id_reservation"),
            rs.getInt("id_seance"),
            rs.getString("nom_client"),
            rs.getString("prenom_client"),
            rs.getInt("nb_places"),
            rs.getTimestamp("date_reservation"),
            rs.getInt("id_compte"),
            rs.getString("role_compte")
        );
    }

    /**
     * Insère une nouvelle réservation.
     * Le trigger côté PostgreSQL décrémente automatiquement les places de la séance.
     */
    public boolean insert(Reservation r) throws SQLException {
        String sql = "INSERT INTO reservations (id_seance, nom_client, prenom_client, nb_places, id_compte, role_compte) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, r.getIdSeance());
            ps.setString(2, r.getNomClient());
            ps.setString(3, r.getPrenomClient());
            ps.setInt(4, r.getNbPlaces());
            ps.setInt(5, r.getIdCompte());
            ps.setString(6, r.getRoleCompte());

            ps.executeUpdate();
            return true;
        }
        // Les exceptions SQL sont propagées pour que le contrôleur les gère
    }

    /**
     * Récupère toutes les réservations pour une séance donnée.
     */
    public List<Reservation> findBySeance(int idSeance) {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE id_seance = ? ORDER BY date_reservation DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSeance);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(buildReservation(rs));

        } catch (SQLException e) {
            System.err.println("Erreur findBySeance réservations : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Récupère toutes les réservations (toutes séances confondues).
     */
    public List<Reservation> findAll() {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY date_reservation DESC";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) liste.add(buildReservation(rs));

        } catch (SQLException e) {
            System.err.println("Erreur findAll réservations : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Supprime une réservation (annulation).
     * Le trigger côté PostgreSQL ré-incrémente automatiquement les places.
     */
    public boolean delete(int idReservation) {
        String sql = "DELETE FROM reservations WHERE id_reservation = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idReservation);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur delete réservation : " + e.getMessage());
            return false;
        }
    }
}
