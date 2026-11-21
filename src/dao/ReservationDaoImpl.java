package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
                   + "r.nb_place AS nb_places, t.categorie::text AS cat_tarif, (r.nb_place * t.prix) AS montant, r.id_client "
                   + "FROM reservation r "
                   + "JOIN client c ON r.id_client = c.id_client "
                   + "JOIN tarif t ON r.id_tarif = t.id_tarif "
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
                r.setCategorieTarif(rs.getString("cat_tarif"));
                r.setMontantTotal(rs.getDouble("montant"));
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

            // 4. Chercher le tarif choisi (NORMAL, ETUDIANT, ENFANT, SENIOR)
            int idTarif = 1;
            String cat = (r.getCategorieTarif() != null && !r.getCategorieTarif().trim().isEmpty())
                         ? r.getCategorieTarif().trim().toUpperCase() : "NORMAL";
            try {
                PreparedStatement ps4 = con.prepareStatement(
                    "SELECT id_tarif FROM tarif WHERE categorie = ?::categorie_tarif"
                );
                ps4.setString(1, cat);
                ResultSet rs4 = ps4.executeQuery();
                if (rs4.next()) {
                    idTarif = rs4.getInt("id_tarif");
                }
                ps4.close();
            } catch (SQLException ex) {
                // Secours si la catégorie ne correspond pas
                PreparedStatement psFallback = con.prepareStatement("SELECT id_tarif FROM tarif LIMIT 1");
                ResultSet rsFb = psFallback.executeQuery();
                if (rsFb.next()) idTarif = rsFb.getInt("id_tarif");
                psFallback.close();
            }

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