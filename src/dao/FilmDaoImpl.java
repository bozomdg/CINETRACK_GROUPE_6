package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connexion.DatabaseConnection;
import exception.FilmIntrouvable;
import modele.Film;

/**
 * Implémentation JDBC PostgreSQL de FilmDaoInterface.
 */
public class FilmDaoImpl implements FilmDaoInterface {

    @Override
    public List<Film> findAll() {
        List<Film> liste = new ArrayList<>();
        String sql = "SELECT id_film, titre, genre, duree, synopsis FROM film WHERE is_deleted = FALSE ORDER BY id_film";

        try (Connection con = DatabaseConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) {
                Film f = new Film(
                    rs.getInt("id_film"),
                    rs.getString("titre"),
                    rs.getString("genre"),
                    rs.getInt("duree"),
                    0.0,
                    rs.getString("synopsis")
                );
                liste.add(f);
            }

        } catch (SQLException e) {
            System.err.println("Erreur findAll film : " + e.getMessage());
        }

        return liste;
    }

    @Override
    public Film findById(int id) {
        String sql = "SELECT id_film, titre, genre, duree, synopsis FROM film WHERE id_film = ? AND is_deleted = FALSE";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);  
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Film(
                    rs.getInt("id_film"),
                    rs.getString("titre"),
                    rs.getString("genre"),
                    rs.getInt("duree"),
                    0.0,
                    rs.getString("synopsis")
                );
            } else {
                throw new FilmIntrouvable(id);
            }

        } catch (SQLException e) {
            System.err.println("Erreur findById film : " + e.getMessage());
            throw new FilmIntrouvable(id);
        }
    }

    @Override
    public List<Film> findByGenre(String genre) {
        List<Film> liste = new ArrayList<>();
        String sql = "SELECT id_film, titre, genre, duree, synopsis FROM film WHERE genre = ? AND is_deleted = FALSE ORDER BY id_film";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, genre);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                liste.add(new Film(
                    rs.getInt("id_film"),
                    rs.getString("titre"),
                    rs.getString("genre"),
                    rs.getInt("duree"),
                    0.0,
                    rs.getString("synopsis")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findByGenre film : " + e.getMessage());
        }

        return liste;
    }

    @Override
    public boolean insert(Film f) {
        String sql = "INSERT INTO film (titre, genre, duree, synopsis) VALUES (?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, f.getTitre());
            ps.setString(2, f.getGenre());
            ps.setInt   (3, f.getDuree());
            ps.setString(4, f.getSynopsis());

            int lignesAffectees = ps.executeUpdate();
            return lignesAffectees > 0;  

        } catch (SQLException e) {
            System.err.println("Erreur insert film : " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Film f) {
        String sql = "UPDATE film SET titre=?, genre=?, duree=?, synopsis=? WHERE id_film=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, f.getTitre());
            ps.setString(2, f.getGenre());
            ps.setInt   (3, f.getDuree());
            ps.setString(4, f.getSynopsis());
            ps.setInt   (5, f.getIdFilm());

            int lignes = ps.executeUpdate();
            if (lignes == 0) throw new FilmIntrouvable(f.getIdFilm());
            return true;

        } catch (SQLException e) {
            System.err.println("Erreur update film : " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE film SET is_deleted = TRUE WHERE id_film = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur delete film : " + e.getMessage());
            return false;
        }
    }
}
