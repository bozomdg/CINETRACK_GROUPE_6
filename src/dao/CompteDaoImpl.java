package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import connexion.DatabaseConnection;
import modele.Compte;

public class CompteDaoImpl implements CompteDaoInterface {

    // connexion d'un utilisateur 
    @Override
    public Compte findByUsernameAndPassword(String usernameOrEmail, String password) {
        String sql = "SELECT id_user, email, role::text FROM utilisateur "
                + "WHERE (LOWER(email) = LOWER(?) OR LOWER(split_part(email, '@', 1)) = LOWER(?)) "
                + "AND mot_de_passe = crypt(?, mot_de_passe)";

        Compte compte = null;

        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            String login = usernameOrEmail.trim();
            ps.setString(1, login);
            ps.setString(2, login);
            ps.setString(3, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id_user");
                String email = rs.getString("email");
                String role = rs.getString("role");

                // on transforme le role de la base en role pour l'application
                String roleFinal;
                if (role.equalsIgnoreCase("ADMINISTRATEUR")) {
                    roleFinal = "admin";
                } else if (role.equalsIgnoreCase("MANAGER")) {
                    roleFinal = "manager";
                } else {
                    roleFinal = "caissier";
                }

                compte = new Compte(id, email, "", roleFinal);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) {
            System.out.println("Erreur login : " + e.getMessage());
        }

        return compte;
    }

    // recuperer tous les comptes du personnel (CAISSIER et MANAGER dans la base)
    @Override
    public List<Compte> findAllGestionnaires() {
        List<Compte> liste = new ArrayList<>();
        String sql = "SELECT id_user, email, role::text FROM utilisateur "
                + "WHERE role IN ('CAISSIER', 'MANAGER') ORDER BY id_user";

        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_user");
                String email = rs.getString("email");
                String role = rs.getString("role");
                String roleAffiche = "MANAGER".equalsIgnoreCase(role) ? "manager" : "caissier";
                Compte c = new Compte(id, email, "", roleAffiche);
                liste.add(c);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) {
            System.out.println("Erreur findAllGestionnaires : " + e.getMessage());
        }

        return liste;
    }

    // ajouter un gestionnaire a partir d'un objet Compte
    @Override
    public boolean insertGestionnaire(Compte c) {
        return insertGestionnaire(c.getUsername(), c.getPassword(), c.getRole());
    }

    // ajouter avec rôle par défaut (CAISSIER)
    @Override
    public boolean insertGestionnaire(String email, String password) {
        return insertGestionnaire(email, password, "CAISSIER");
    }

    // ajouter avec rôle spécifié (CAISSIER ou MANAGER)
    @Override
    public boolean insertGestionnaire(String email, String password, String role) {
        
        String emailFinal;
        if (email.contains("@")) {
            emailFinal = email.trim();
        } else {
            emailFinal = email.trim().toLowerCase() + "@cinetrack.com";
        }

        String roleDb = "MANAGER".equalsIgnoreCase(role) ? "MANAGER" : "CAISSIER";

        String sql = "INSERT INTO utilisateur (email, mot_de_passe, role) "
                + "VALUES (?, crypt(?, gen_salt('bf')), ?::role_user)";

        boolean ok = false;

        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, emailFinal);
            ps.setString(2, password);
            ps.setString(3, roleDb);

            int lignes = ps.executeUpdate();
            if (lignes > 0) {
                ok = true;
            }

            ps.close();
            con.close();

        } catch (SQLException e) {
            System.out.println("Erreur insertGestionnaire : " + e.getMessage());
        }

        return ok;
    }

    // supprimer un personnel avec son id (sauf admin)
    @Override
    public boolean deleteGestionnaire(int id) {
        String sql = "DELETE FROM utilisateur WHERE id_user = ? AND role != 'ADMINISTRATEUR'";
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
            System.out.println("Erreur deleteGestionnaire : " + e.getMessage());
        }

        return ok;
    }
}