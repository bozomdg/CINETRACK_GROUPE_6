package modele;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import connexion.DatabaseConnection;

/**
 * DAO pour la gestion des comptes (Admins et Gestionnaires).
 * Gère deux tables distinctes : 'admins' et 'gestionnaires'.
 */
public class CompteDAO {

    /**
     * Tente de connecter un utilisateur en cherchant dans les deux tables.
     */
    public Compte findByUsernameAndPassword(String username, String password) {
        // 1. Chercher dans les admins
        Compte c = findInTable("admins", username, password, "admin");
        if (c != null) return c;

        // 2. Chercher dans les gestionnaires
        return findInTable("gestionnaires", username, password, "gestionnaire");
    }

    private Compte findInTable(String tableName, String username, String password, String role) {
        String idCol = "id_" + (tableName.equals("admins") ? "admin" : "gestionnaire");
        String sql = "SELECT * FROM " + tableName + " WHERE username = ? AND password = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Compte(rs.getInt(idCol), rs.getString("username"), rs.getString("password"), role);
            }
        } catch (SQLException e) {
            System.err.println("Erreur login " + tableName + " : " + e.getMessage());
        }
        return null;
    }

    /**
     * Récupère uniquement la liste des gestionnaires.
     */
    public List<Compte> findAllGestionnaires() {
        List<Compte> liste = new ArrayList<>();
        String sql = "SELECT * FROM gestionnaires ORDER BY username";

        try (Connection con = DatabaseConnection.getConnection();
             java.sql.Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(new Compte(rs.getInt("id_gestionnaire"), rs.getString("username"), rs.getString("password"), "gestionnaire"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findAll gestionnaires : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Ajoute un nouveau gestionnaire.
     */
    public boolean insertGestionnaire(Compte c) {
        String sql = "INSERT INTO gestionnaires (username, password) VALUES (?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getUsername());
            ps.setString(2, c.getPassword());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur insert gestionnaire : " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un gestionnaire.
     */
    public boolean deleteGestionnaire(int id) {
        String sql = "DELETE FROM gestionnaires WHERE id_gestionnaire = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur delete gestionnaire : " + e.getMessage());
            return false;
        }
    }
}
