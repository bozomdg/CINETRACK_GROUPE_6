package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connexion.DatabaseConnection;
import exception.SalleErreur;
import modele.Seance;

public class SeanceDaoImpl implements SeanceDaoInterface {

    // transforme une ligne de la base en objet Seance
    private Seance buildSeance(ResultSet rs) throws SQLException {
        int idSeance = rs.getInt("id_seance");
        int idFilm = rs.getInt("id_film");
        int idSalle = rs.getInt("id_salle");
        String date = rs.getString("date_seance");
        String heure = rs.getString("heure_debut");
        int places = rs.getInt("place_dispo");

        Seance s = new Seance(idSeance, idFilm, idSalle, date, heure, places);
        return s;
    }

    // afficher toutes les seances
    @Override
    public List<Seance> findAll() {
        List<Seance> liste = new ArrayList<>();
        String sql = "SELECT * FROM seance ORDER BY date_seance, heure_debut";

        try {
            Connection con = DatabaseConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Seance s = buildSeance(rs);
                liste.add(s);
            }

            rs.close();
            st.close();
            con.close();

        } catch (SQLException e) {
            System.out.println("Erreur findAll seances : " + e.getMessage());
        }

        return liste;
    }

    // chercher les seances d'un film
    @Override
    public List<Seance> findByFilm(int idFilm) {
        List<Seance> liste = new ArrayList<>();
        String sql = "SELECT * FROM seance WHERE id_film = ? ORDER BY date_seance, heure_debut";

        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, idFilm);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Seance s = buildSeance(rs);
                liste.add(s);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) {
            System.out.println("Erreur findByFilm : " + e.getMessage());
        }

        return liste;
    }

    // chercher les seances d'une date (format yyyy-MM-dd)
    @Override
    public List<Seance> findByDate(String date) {
        List<Seance> liste = new ArrayList<>();
        String sql = "SELECT * FROM seance WHERE date_seance = ? ORDER BY heure_debut";

        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Seance s = buildSeance(rs);
                liste.add(s);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) {
            System.out.println("Erreur findByDate : " + e.getMessage());
        }

        return liste;
    }

    // ajouter une seance
    @Override
    public boolean insert(Seance s) {
        String sql = "INSERT INTO seance (id_film, id_salle, date_seance, heure_debut, place_dispo) "
                + "VALUES (?, ?, ?, ?, ?)";
        boolean ok = false;

        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, s.getIdFilm());
            ps.setInt(2, s.getIdSalle());
            ps.setDate(3, java.sql.Date.valueOf(s.getDateSeance()));

          
            String heure = s.getHeure().trim();
            if (heure.length() == 5) {
                heure = heure + ":00";
            }
            ps.setTime(4, java.sql.Time.valueOf(heure));

            ps.setInt(5, s.getPlacesDispo());

            ps.executeUpdate();
            ok = true;

            ps.close();
            con.close();

        } catch (SQLException e) {
            // 23505 = doublon : la salle est deja prise a cette date et heure
            if ("23505".equals(e.getSQLState())) {
                throw new SalleErreur(s.getIdSalle(), s.getDateSeance() + " " + s.getHeure());
            }
            System.out.println("Erreur insert seance : " + e.getMessage());
        }

        return ok;
    }

    // supprimer une seance avec son id
    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM seance WHERE id_seance = ?";
        boolean ok = false;

        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                ok = true;
            }

            ps.close();
            con.close();

        } catch (SQLException e) {
            System.out.println("Erreur delete seance : " + e.getMessage());
        }

        return ok;
    }

    // modifier une seance
    @Override
    public boolean update(Seance s) {
        String sql = "UPDATE seance SET id_film = ?, id_salle = ?, date_seance = ?, "
                + "heure_debut = ?, place_dispo = ? WHERE id_seance = ?";
        boolean ok = false;

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

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

            int lignes = pstmt.executeUpdate();
            if (lignes > 0) {
                ok = true;
            }

            pstmt.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ok;
    }
}