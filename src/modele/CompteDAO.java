package modele;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import connexion.DatabaseConnection;

/**
 * DAO pour la gestion des comptes sur la table 'utilisateur' (schéma cinetrack).
 * Gère le chiffrement bcrypt pgcrypto et les rôles ADMINISTRATEUR / CAISSIER.
 */
public class CompteDAO {

    /**
     * Authentifie un utilisateur en acceptant son email complet (admin@cinetrack.com)
     * ou son préfixe (admin), avec vérification du mot de passe chiffré.
     */
    public Compte findByUsernameAndPassword(String usernameOrEmail, String password) {
        String sql = "SELECT id_user, email, role::text "
                   + "FROM utilisateur "
                   + "WHERE (LOWER(email) = LOWER(?) OR LOWER(split_part(email, '@', 1)) = LOWER(?)) "
                   + "  AND mot_de_passe = crypt(?, mot_de_passe)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, usernameOrEmail.trim());
            ps.setString(2, usernameOrEmail.trim());
            ps.setString(3, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                // Normalise le rôle en minuscule pour l'IHM (admin ou gestionnaire/caissier)
                String normalizedRole = "ADMINISTRATEUR".equalsIgnoreCase(role) ? "admin" : "gestionnaire";
                return new Compte(rs.getInt("id_user"), rs.getString("email"), "", normalizedRole);
            }
        } catch (SQLException e) {
            System.err.println("Erreur login utilisateur : " + e.getMessage());
        }
        return null;
    }

    /**
     * Récupère la liste des utilisateurs ayant le rôle CAISSIER (gestionnaires).
     */
    public List<Compte> findAllGestionnaires() {
        List<Compte> liste = new ArrayList<>();
        String sql = "SELECT id_user, email, role::text FROM utilisateur WHERE role = 'CAISSIER' ORDER BY email";

        try (Connection con = DatabaseConnection.getConnection();
             java.sql.Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(new Compte(rs.getInt("id_user"), rs.getString("email"), "", "gestionnaire"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findAll gestionnaires : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Ajoute un nouveau caissier/gestionnaire avec mot de passe chiffré pgcrypto.
     */
    public boolean insertGestionnaire(Compte c) {
        String email = c.getUsername().trim();
        if (!email.contains("@")) {
            email = email + "@cinetrack.com";
        }

        String sql = "INSERT INTO utilisateur (email, mot_de_passe, role) VALUES (?, crypt(?, gen_salt('bf')), 'CAISSIER')";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, c.getPassword());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur insert utilisateur : " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un utilisateur par son id_user.
     */
    public boolean deleteGestionnaire(int id) {
        String sql = "DELETE FROM utilisateur WHERE id_user = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur delete utilisateur : " + e.getMessage());
            return false;
        }
    }
}
