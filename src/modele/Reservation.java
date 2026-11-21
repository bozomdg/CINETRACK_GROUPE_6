package modele;

import java.sql.Timestamp;

/**
 * Représente une réservation d'un client pour une séance.
 * Stocke aussi la traçabilité (quel admin/gestionnaire a créé la réservation).
 */
public class Reservation {

    private int idReservation;
    private int idSeance;
    private String nomClient;
    private String prenomClient;
    private int nbPlaces;
    private Timestamp dateReservation;
    // Traçabilité
    private int idCompte;
    private String roleCompte;
    // Tarif
    private String categorieTarif;
    private double montantTotal;

    public Reservation() {}

    public Reservation(int idSeance, String nomClient, String prenomClient,
                       int nbPlaces, int idCompte, String roleCompte) {
        this.idSeance = idSeance;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.nbPlaces = nbPlaces;
        this.idCompte = idCompte;
        this.roleCompte = roleCompte;
        this.categorieTarif = "NORMAL";
    }

    public Reservation(int idReservation, int idSeance, String nomClient, String prenomClient,
                       int nbPlaces, Timestamp dateReservation, int idCompte, String roleCompte) {
        this.idReservation = idReservation;
        this.idSeance = idSeance;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.nbPlaces = nbPlaces;
        this.dateReservation = dateReservation;
        this.idCompte = idCompte;
        this.roleCompte = roleCompte;
        this.categorieTarif = "NORMAL";
    }

    // Getters
    public int getIdReservation() { return idReservation; }
    public int getIdSeance() { return idSeance; }
    public String getNomClient() { return nomClient; }
    public String getPrenomClient() { return prenomClient; }
    public int getNbPlaces() { return nbPlaces; }
    public Timestamp getDateReservation() { return dateReservation; }
    public int getIdCompte() { return idCompte; }
    public String getRoleCompte() { return roleCompte; }
    public String getCategorieTarif() { return categorieTarif; }
    public double getMontantTotal() { return montantTotal; }

    // Setters
    public void setIdReservation(int id) { this.idReservation = id; }
    public void setIdSeance(int idSeance) { this.idSeance = idSeance; }
    public void setNomClient(String nom) { this.nomClient = nom; }
    public void setPrenomClient(String prenom) { this.prenomClient = prenom; }
    public void setNbPlaces(int nb) { this.nbPlaces = nb; }
    public void setDateReservation(Timestamp date) { this.dateReservation = date; }
    public void setIdCompte(int id) { this.idCompte = id; }
    public void setRoleCompte(String role) { this.roleCompte = role; }
    public void setCategorieTarif(String cat) { this.categorieTarif = cat; }
    public void setMontantTotal(double montant) { this.montantTotal = montant; }

    @Override
    public String toString() {
        return "[Réservation #" + idReservation + "] " + prenomClient + " " + nomClient +
               " | Séance#" + idSeance + " | " + nbPlaces + " place(s)" +
               " | Par: " + roleCompte + "#" + idCompte;
    }
}
