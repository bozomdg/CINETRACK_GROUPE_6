package modele;

import connexion.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table "reservation" (schéma cinetrack).
 */
public class ReservationDAO {

    public List<Reservation> findBySeance(int idSeance) {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT r.id_reservation, r.id_seance, c.nom_client, c.prenom AS prenom_client, "
                   + "       r.nb_place AS nb_places, r.id_client "
                   + "FROM reservation r "
                   + "JOIN client c ON r.id_client = c.id_client "
                   + "WHERE r.id_seance = ? "
                   + "ORDER BY r.id_reservation DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSeance);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new Reservation(
                    rs.getInt("id_reservation"),
                    rs.getInt("id_seance"),
                    rs.getString("nom_client"),
                    rs.getString("prenom_client"),
                    rs.getInt("nb_places"),
                    new Timestamp(System.currentTimeMillis()),
                    rs.getInt("id_client"),
                    "client"
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findBySeance réservations : " + e.getMessage());
        }
        return liste;
    }

    public List<Reservation> findAll() {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT r.id_reservation, r.id_seance, c.nom_client, c.prenom AS prenom_client, "
                   + "       r.nb_place AS nb_places, r.id_client "
                   + "FROM reservation r "
                   + "JOIN client c ON r.id_client = c.id_client "
                   + "ORDER BY r.id_reservation DESC";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(new Reservation(
                    rs.getInt("id_reservation"),
                    rs.getInt("id_seance"),
                    rs.getString("nom_client"),
                    rs.getString("prenom_client"),
                    rs.getInt("nb_places"),
                    new Timestamp(System.currentTimeMillis()),
                    rs.getInt("id_client"),
                    "client"
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findAll réservations : " + e.getMessage());
        }
        return liste;
    }

    public boolean insert(Reservation r) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Trouver ou insérer le client
                int idClient = -1;
                String sqlFindClient = "SELECT id_client FROM client WHERE nom_client = ? AND prenom = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlFindClient)) {
                    ps.setString(1, r.getNomClient());
                    ps.setString(2, r.getPrenomClient());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        idClient = rs.getInt("id_client");
                    }
                }

                if (idClient == -1) {
                    String sqlInsertClient = "INSERT INTO client (nom_client, prenom) VALUES (?, ?) RETURNING id_client";
                    try (PreparedStatement ps = con.prepareStatement(sqlInsertClient)) {
                        ps.setString(1, r.getNomClient());
                        ps.setString(2, r.getPrenomClient());
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            idClient = rs.getInt("id_client");
                        }
                    }
                }

                // 2. Trouver un tarif par défaut (NORMAL ou le premier disponible)
                int idTarif = 1;
                String sqlTarif = "SELECT id_tarif FROM tarif LIMIT 1";
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery(sqlTarif)) {
                    if (rs.next()) {
                        idTarif = rs.getInt("id_tarif");
                    }
                }

                // 3. Insérer la réservation
                String sqlRes = "INSERT INTO reservation (nb_place, statut, id_client, id_seance, id_tarif) "
                              + "VALUES (?, 'CONFIRMEE', ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sqlRes)) {
                    ps.setInt(1, r.getNbPlaces());
                    ps.setInt(2, idClient);
                    ps.setInt(3, r.getIdSeance());
                    ps.setInt(4, idTarif);
                    ps.executeUpdate();
                }

                // 4. Décrémenter les places_dispo de la séance
                String sqlUpdateSeance = "UPDATE seance SET place_dispo = place_dispo - ? WHERE id_seance = ? AND place_dispo >= ?";
                try (PreparedStatement ps = con.prepareStatement(sqlUpdateSeance)) {
                    ps.setInt(1, r.getNbPlaces());
                    ps.setInt(2, r.getIdSeance());
                    ps.setInt(3, r.getNbPlaces());
                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        throw new SQLException("Places insuffisantes pour cette séance.");
                    }
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public boolean delete(int idReservation) {
        String sqlSelect = "SELECT id_seance, nb_place FROM reservation WHERE id_reservation = ?";
        String sqlDelete = "DELETE FROM reservation WHERE id_reservation = ?";
        String sqlUpdate = "UPDATE seance SET place_dispo = place_dispo + ? WHERE id_seance = ?";

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int idSeance = -1;
                int nbPlaces = 0;
                try (PreparedStatement ps = con.prepareStatement(sqlSelect)) {
                    ps.setInt(1, idReservation);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        idSeance = rs.getInt("id_seance");
                        nbPlaces = rs.getInt("nb_place");
                    }
                }

                if (idSeance != -1) {
                    try (PreparedStatement ps = con.prepareStatement(sqlDelete)) {
                        ps.setInt(1, idReservation);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                        ps.setInt(1, nbPlaces);
                        ps.setInt(2, idSeance);
                        ps.executeUpdate();
                    }
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                System.err.println("Erreur delete reservation : " + e.getMessage());
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Erreur connexion delete reservation : " + e.getMessage());
            return false;
        }
    }
}
