package connexion;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Gère la connexion unique à la base de données PostgreSQL.
 
public class DatabaseConnection {

    
    private static final String URL      = "jdbc:postgresql://localhost:5432/cinetrack";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "nanah04";

    // Retourne une nouvelle connexion à chaque appel.
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL non trouvé : " + e.getMessage());
        }
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
