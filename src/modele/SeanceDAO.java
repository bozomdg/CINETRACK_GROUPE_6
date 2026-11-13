package modele;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connexion.DatabaseConnection;
import exception.SalleErreur;

/**
 * Opérations SQL sur la table "seance" (schéma cinetrack).
 */
public class SeanceDAO {

    private Seance buildSeance(ResultSet rs) throws SQLException {
        return new Seance(
            rs.getInt("id_seance"),
            rs.getInt("id_film"),
            rs.getInt("id_salle"),
            rs.getString("date_seance"),
            rs.getString("heure_debut"),
            rs.getInt("place_dispo")
        );
    }

    public List<Seance> findAll() {
        List<Seance> liste = new ArrayList<>();
        String sql = "SELECT * FROM seance ORDER BY date_seance, heure_debut";

        try (Connection con = DatabaseConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) liste.add(buildSeance(rs));

        } catch (SQLException e) {
            System.err.println("Erreur findAll séances : " + e.getMessage());
        }
        return liste;
    }

    public List<Seance> findByFilm(int idFilm) {
        List<Seance> liste = new ArrayList<>();
        String sql = "SELECT * FROM seance WHERE id_film = ? ORDER BY date_seance, heure_debut";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idFilm);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(buildSeance(rs));

        } catch (SQLException e) {
            System.err.println("Erreur findByFilm : " + e.getMessage());
        }
        return liste;
    }

    public List<Seance> findByDate(String date) {
        List<Seance> liste = new ArrayList<>();
        String sql = "SELECT * FROM seance WHERE date_seance = ? ORDER BY heure_debut";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(date));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(buildSeance(rs));

        } catch (SQLException e) {
            System.err.println("Erreur findByDate : " + e.getMessage());
        }
        return liste;
    }
    
    public boolean insert(Seance s) {
        String sql = "INSERT INTO seance (id_film, id_salle, date_seance, heure_debut, place_dispo) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt (1, s.getIdFilm());
            ps.setInt (2, s.getIdSalle());
            ps.setDate(3, java.sql.Date.valueOf(s.getDateSeance()));
            
            String heure = s.getHeure().trim();
            if (heure.length() == 5) {
                heure = heure + ":00"; 
            }
            ps.setTime(4, java.sql.Time.valueOf(heure));

            ps.setInt (5, s.getPlacesDispo());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new SalleErreur(
                    s.getIdSalle(),
                    s.getDateSeance() + " " + s.getHeure()
                );
            }
            System.err.println("Erreur insert séance : " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM seance WHERE id_seance = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur delete séance : " + e.getMessage());
            return false;
        }
    }

    public boolean update(Seance s) {
        String sql = "UPDATE seance SET id_film = ?, id_salle = ?, date_seance = ?, heure_debut = ?, place_dispo = ? WHERE id_seance = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, s.getIdFilm());
            pstmt.setInt(2, s.getIdSalle());
            pstmt.setDate(3, java.sql.Date.valueOf(s.getDateSeance()));
            
            String heure = s.getHeure().trim();
            if (heure.length() == 5) {
                heure = heure + ":00"; 
            }
            pstmt.setTime(4, java.sql.Time.valueOf(heure));
            pstmt.setInt(5, s.getPlacesDispo());
            pstmt.setInt(6, s.getIdSeance());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
