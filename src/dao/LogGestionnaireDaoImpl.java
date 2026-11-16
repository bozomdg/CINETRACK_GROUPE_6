package dao;

import connexion.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import modele.LogGestionnaire;

/**
 * Implémentation JDBC PostgreSQL de LogGestionnaireDaoInterface.
 */
public class LogGestionnaireDaoImpl implements LogGestionnaireDaoInterface {

    @Override
    public List<LogGestionnaire> findAll() {
        List<LogGestionnaire> liste = new ArrayList<>();
        String sql = "SELECT l.id_log, l.action, COALESCE(u.email, 'Inconnu') AS username, l.id_user, l.date_modification "
                   + "FROM log l "
                   + "LEFT JOIN utilisateur u ON l.id_user = u.id_user "
                   + "ORDER BY l.date_modification DESC";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(new LogGestionnaire(
                        rs.getInt("id_log"),
                        rs.getString("action"),
                        rs.getString("username"),
                        rs.getInt("id_user"),
                        rs.getTimestamp("date_modification")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération logs : " + e.getMessage());
        }

        return liste;
    }
}
