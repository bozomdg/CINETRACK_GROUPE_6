package modele;

/**
 * Représente un compte d'accès au système (Admin ou Gestionnaire).
 * Le terme "Utilisateur" a été supprimé au profit de "Compte".
 */
public class Compte {
    private int id;
    private String username;
    private String password;
    private String role; // "admin" ou "gestionnaire"

    public Compte() {}

    public Compte(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "Compte{" + "id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}
