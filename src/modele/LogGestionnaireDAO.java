package modele;

import connexion.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour accéder à la table logs_gestionnaires.
 * La table est alimentée automatiquement par les triggers PostgreSQL.
 */
public class LogGestionnaireDAO {

    public List<LogGestionnaire> findAll() {
        List<LogGestionnaire> liste = new ArrayList<>();
        String sql = "SELECT * FROM logs_gestionnaires ORDER BY date_action DESC";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(new LogGestionnaire(
                        rs.getInt("id_log"),
                        rs.getString("action"),
                        rs.getString("username_gestionnaire"),
                        rs.getInt("id_gestionnaire"),
                        rs.getTimestamp("date_action")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération logs : " + e.getMessage());
        }

        return liste;
    }
}
