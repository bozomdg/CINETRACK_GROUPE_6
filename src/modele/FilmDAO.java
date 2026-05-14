package modele;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connexion.DatabaseConnection;
import exception.FilmIntrouvable;
import exception.NoteErreur;

// Toutes les opérations SQL sur la table "films".
 
public class FilmDAO {

    
    // LIRE TOUS LES FILMS
    
    public List<Film> findAll() {
        List<Film> liste = new ArrayList<>();
        String sql = "SELECT * FROM films ORDER BY titre";

        
        try (Connection con = DatabaseConnection.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            while (rs.next()) {
                // On reconstruit un objet Film depuis chaque ligne SQL
                Film f = new Film(
                    rs.getInt("id_film"),
                    rs.getString("titre"),
                    rs.getString("genre"),
                    rs.getInt("duree"),
                    rs.getDouble("note"),
                    rs.getString("synopsis")
                );
                liste.add(f);
            }

        } catch (SQLException e) {
            System.err.println("Erreur findAll films : " + e.getMessage());
        }

        return liste;
    }

    
    // LIRE UN FILM PAR ID
    
    public Film findById(int id) {
        String sql = "SELECT * FROM films WHERE id_film = ?";

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
                    rs.getDouble("note"),
                    rs.getString("synopsis")
                );
            } else {
                // Film non trouvé, on lance notre exception métier
                throw new FilmIntrouvable(id);
            }

        } catch (SQLException e) {
            System.err.println("Erreur findById : " + e.getMessage());
            throw new FilmIntrouvable(id);
        }
    }

    
    // RECHERCHER PAR GENRE
    
    public List<Film> findByGenre(String genre) {
        List<Film> liste = new ArrayList<>();
        String sql = "SELECT * FROM films WHERE genre = ? ORDER BY titre";

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
                    rs.getDouble("note"),
                    rs.getString("synopsis")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findByGenre : " + e.getMessage());
        }

        return liste;
    }

    // INSÉRER UN FILM
    
    public boolean insert(Film f) {

        if (f.getNote() < 0 || f.getNote() > 10) {
            throw new NoteErreur(f.getNote());
        }

        String sql = "INSERT INTO films (titre, genre, duree, note, synopsis) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, f.getTitre());
            ps.setString(2, f.getGenre());
            ps.setInt   (3, f.getDuree());
            ps.setDouble(4, f.getNote());
            ps.setString(5, f.getSynopsis());

            int lignesAffectees = ps.executeUpdate();
            return lignesAffectees > 0;  

        } catch (SQLException e) {
            System.err.println("Erreur insert film : " + e.getMessage());
            return false;
        }
    }

    
    // MODIFIER UN FILM
    
    public boolean update(Film f) {
        String sql = "UPDATE films SET titre=?, genre=?, duree=?, note=?, synopsis=? " +
                     "WHERE id_film=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, f.getTitre());
            ps.setString(2, f.getGenre());
            ps.setInt   (3, f.getDuree());
            ps.setDouble(4, f.getNote());
            ps.setString(5, f.getSynopsis());
            ps.setInt   (6, f.getIdFilm());

            int lignes = ps.executeUpdate();
            if (lignes == 0) throw new FilmIntrouvable(f.getIdFilm());
            return true;

        } catch (SQLException e) {
            System.err.println("Erreur update film : " + e.getMessage());
            return false;
        }
    }

    
    // SUPPRIMER UN FILM
    
    public boolean delete(int id) {

        findById(id);  

        String sql = "DELETE FROM films WHERE id_film = ?";

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