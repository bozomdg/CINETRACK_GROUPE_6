
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import connexion.DatabaseConnection;
import modele.Reservation;

/**
 * Accès à la base de données pour les réservations.
 */
public class ReservationDaoImpl implements ReservationDaoInterface {

    /** Retourne les réservations d'une séance */
    @Override
    public List<Reservation> findBySeance(int idSeance) {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT r.id_reservation, r.id_seance, c.nom_client, c.prenom AS prenom_client, "
                + "r.nb_place AS nb_places, r.id_client "
                + "FROM reservation r "
                + "JOIN client c ON r.id_client = c.id_client "
                + "WHERE r.id_seance = ? "
                + "ORDER BY r.id_reservation DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSeance);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Reservation r = new Reservation(
                        rs.getInt("id_reservation"),
                        rs.getInt("id_seance"),
                        rs.getString("nom_client"),
                        rs.getString("prenom_client"),
                        rs.getInt("nb_places"),
                        new Timestamp(System.currentTimeMillis()),
                        rs.getInt("id_client"),
                        "client");
                liste.add(r);
            }

        } catch (SQLException e) {
            System.err.println("Erreur findBySeance : " + e.getMessage());
        }
        return liste;
    }

    /** Retourne toutes les réservations */
    @Override
    public List<Reservation> findAll() {
        List<Reservation> liste = new ArrayList<>();
        String sql = "SELECT r.id_reservation, r.id_seance, c.nom_client, c.prenom AS prenom_client, "
                + "r.nb_place AS nb_places, r.id_client "
                + "FROM reservation r "
                + "JOIN client c ON r.id_client = c.id_client "
                + "ORDER BY r.id_reservation DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Reservation r = new Reservation(
                        rs.getInt("id_reservation"),
                        rs.getInt("id_seance"),
                        rs.getString("nom_client"),
                        rs.getString("prenom_client"),
                        rs.getInt("nb_places"),
                        new Timestamp(System.currentTimeMillis()),
                        rs.getInt("id_client"),
                        "client");
                liste.add(r);
            }

        } catch (SQLException e) {
            System.err.println("Erreur findAll : " + e.getMessage());
        }
        return liste;
    }

    /** Ajoute une réservation */
    @Override
    public boolean insert(Reservation r) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {

            // 1. Retirer les places de la séance (marche seulement s'il y en a assez)
            String sqlPlaces = "UPDATE seance SET place_dispo = place_dispo - ? "
                    + "WHERE id_seance = ? AND place_dispo >= ?";
            PreparedStatement ps1 = con.prepareStatement(sqlPlaces);
            ps1.setInt(1, r.getNbPlaces());
            ps1.setInt(2, r.getIdSeance());
            ps1.setInt(3, r.getNbPlaces());
            int lignes = ps1.executeUpdate();
            ps1.close();

            if (lignes == 0) {
                throw new SQLException("Places insuffisantes pour cette séance.");
            }

            // 2. Chercher le client
            int idClient = -1;
            String sqlChercher = "SELECT id_client FROM client WHERE nom_client = ? AND prenom = ?";
            PreparedStatement ps2 = con.prepareStatement(sqlChercher);
            ps2.setString(1, r.getNomClient());
            ps2.setString(2, r.getPrenomClient());
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                idClient = rs2.getInt("id_client");
            }
            ps2.close();

            // 3. Si le client n'existe pas, on le crée
            if (idClient == -1) {
                String sqlCreer = "INSERT INTO client (nom_client, prenom) VALUES (?, ?) RETURNING id_client";
                PreparedStatement ps3 = con.prepareStatement(sqlCreer);
                ps3.setString(1, r.getNomClient());
                ps3.setString(2, r.getPrenomClient());
                ResultSet rs3 = ps3.executeQuery();
                if (rs3.next()) {
                    idClient = rs3.getInt("id_client");
                }
                ps3.close();
            }

            // 4. Chercher un tarif (1 par défaut)
            int idTarif = 1;
            PreparedStatement ps4 = con.prepareStatement("SELECT id_tarif FROM tarif LIMIT 1");
            ResultSet rs4 = ps4.executeQuery();
            if (rs4.next()) {
                idTarif = rs4.getInt("id_tarif");
            }
            ps4.close();

            // 5. Enregistrer la réservation
            String sqlReservation = "INSERT INTO reservation (nb_place, statut, id_client, id_seance, id_tarif) "
                    + "VALUES (?, 'CONFIRMEE', ?, ?, ?)";
            PreparedStatement ps5 = con.prepareStatement(sqlReservation);
            ps5.setInt(1, r.getNbPlaces());
            ps5.setInt(2, idClient);
            ps5.setInt(3, r.getIdSeance());
            ps5.setInt(4, idTarif);
            ps5.executeUpdate();
            ps5.close();

            return true;
        }
    }

    /** Retourne les numéros de places déjà prises pour une séance */
    @Override
    public Set<String> findPlacesOccupees(int idSeance) {
        Set<String> places = new HashSet<>();
        String sql = "SELECT numero_place FROM place_reservee WHERE id_seance = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idSeance);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                places.add(rs.getString("numero_place"));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findPlacesOccupees : " + e.getMessage());
        }
        return places;
    }

    /** Ajoute une réservation avec des places précises (tout ou rien) */
    @Override
    public boolean insertAvecPlaces(Reservation r, List<String> numerosPlaces) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Retirer les places de la séance
                PreparedStatement ps1 = con.prepareStatement(
                        "UPDATE seance SET place_dispo = place_dispo - ? "
                                + "WHERE id_seance = ? AND place_dispo >= ?");
                ps1.setInt(1, numerosPlaces.size());
                ps1.setInt(2, r.getIdSeance());
                ps1.setInt(3, numerosPlaces.size());
                int lignes = ps1.executeUpdate();
                ps1.close();
                if (lignes == 0) {
                    throw new SQLException("Places insuffisantes pour cette séance.");
                }

                // 2. Chercher ou créer le client
                int idClient = -1;
                PreparedStatement ps2 = con.prepareStatement(
                        "SELECT id_client FROM client WHERE nom_client = ? AND prenom = ?");
                ps2.setString(1, r.getNomClient());
                ps2.setString(2, r.getPrenomClient());
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) {
                    idClient = rs2.getInt("id_client");
                }
                ps2.close();

                if (idClient == -1) {
                    PreparedStatement ps3 = con.prepareStatement(
                            "INSERT INTO client (nom_client, prenom) VALUES (?, ?) RETURNING id_client");
                    ps3.setString(1, r.getNomClient());
                    ps3.setString(2, r.getPrenomClient());
                    ResultSet rs3 = ps3.executeQuery();
                    if (rs3.next()) {
                        idClient = rs3.getInt("id_client");
                    }
                    ps3.close();
                }

                // 3. Tarif par défaut
                int idTarif = 1;
                PreparedStatement ps4 = con.prepareStatement("SELECT id_tarif FROM tarif LIMIT 1");
                ResultSet rs4 = ps4.executeQuery();
                if (rs4.next()) {
                    idTarif = rs4.getInt("id_tarif");
                }
                ps4.close();

                // 4. Enregistrer la réservation et récupérer son id
                int idReservation = -1;
                PreparedStatement ps5 = con.prepareStatement(
                        "INSERT INTO reservation (nb_place, statut, id_client, id_seance, id_tarif) "
                                + "VALUES (?, 'CONFIRMEE', ?, ?, ?) RETURNING id_reservation");
                ps5.setInt(1, numerosPlaces.size());
                ps5.setInt(2, idClient);
                ps5.setInt(3, r.getIdSeance());
                ps5.setInt(4, idTarif);
                ResultSet rs5 = ps5.executeQuery();
                if (rs5.next()) {
                    idReservation = rs5.getInt("id_reservation");
                }
                ps5.close();

                // 5. Enregistrer chaque place choisie
                PreparedStatement ps6 = con.prepareStatement(
                        "INSERT INTO place_reservee (numero_place, id_seance, id_reservation) VALUES (?, ?, ?)");
                for (String numero : numerosPlaces) {
                    ps6.setString(1, numero);
                    ps6.setInt(2, r.getIdSeance());
                    ps6.setInt(3, idReservation);
                    ps6.addBatch();
                }
                ps6.executeBatch();
                ps6.close();

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                // 23505 = violation d'unicité : une des places a été prise entre-temps
                if ("23505".equals(e.getSQLState())) {
                    throw new SQLException("Une des places choisies vient d'être réservée par quelqu'un d'autre.");
                }
                throw e;
            }
        }
    }

    /** Annule une réservation et rend les places à la séance */
    @Override
    public boolean delete(int idReservation) {
        try (Connection con = DatabaseConnection.getConnection()) {

            // 1. Chercher la réservation
            String sqlChercher = "SELECT id_seance, nb_place FROM reservation WHERE id_reservation = ?";
            PreparedStatement ps1 = con.prepareStatement(sqlChercher);
            ps1.setInt(1, idReservation);
            ResultSet rs = ps1.executeQuery();

            if (!rs.next()) {
                ps1.close();
                return false; // réservation introuvable
            }
            int idSeance = rs.getInt("id_seance");
            int nbPlaces = rs.getInt("nb_place");
            ps1.close();

            // 2. Supprimer la réservation
            PreparedStatement ps2 = con.prepareStatement("DELETE FROM reservation WHERE id_reservation = ?");
            ps2.setInt(1, idReservation);
            ps2.executeUpdate();
            ps2.close();

            // 3. Rendre les places à la séance
            String sqlPlaces = "UPDATE seance SET place_dispo = place_dispo + ? WHERE id_seance = ?";
            PreparedStatement ps3 = con.prepareStatement(sqlPlaces);
            ps3.setInt(1, nbPlaces);
            ps3.setInt(2, idSeance);
            ps3.executeUpdate();
            ps3.close();

            return true;

        } catch (SQLException e) {
            System.err.println("Erreur delete réservation : " + e.getMessage());
            return false;
        }
    }
}