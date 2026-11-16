package dao;

import connexion.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import modele.Salle;

/**
 * Implémentation JDBC PostgreSQL de SalleDaoInterface.
 */
public class SalleDaoImpl implements SalleDaoInterface {

    @Override
    public List<Salle> findAll() {
        List<Salle> liste = new ArrayList<>();
        String sql = "SELECT id_salle, nom_salle, capacite_salle FROM salle ORDER BY id_salle";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(new Salle(
                        rs.getInt("id_salle"),
                        rs.getString("nom_salle"),
                        rs.getInt("capacite_salle"),
                        "Standard"
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findAll salles : " + e.getMessage());
        }
        return liste;
    }

    @Override
    public Salle findById(int id) {
        String sql = "SELECT id_salle, nom_salle, capacite_salle FROM salle WHERE id_salle = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Salle(
                        rs.getInt("id_salle"),
                        rs.getString("nom_salle"),
                        rs.getInt("capacite_salle"),
                        "Standard"
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur findById salle : " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean insert(Salle s) {
        String sql = "INSERT INTO salle (nom_salle, capacite_salle) VALUES (?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, s.getNom());
            ps.setInt(2, s.getCapacite());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur insert salle : " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Salle s) {
        String sql = "UPDATE salle SET nom_salle = ?, capacite_salle = ? WHERE id_salle = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, s.getNom());
            ps.setInt(2, s.getCapacite());
            ps.setInt(3, s.getIdSalle());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur update salle : " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM salle WHERE id_salle = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur delete salle : " + e.getMessage());
            return false;
        }
    }
}
