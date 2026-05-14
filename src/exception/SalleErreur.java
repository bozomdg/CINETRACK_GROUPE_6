// --- SalleIndisponibleException.java ---
package exception;

public class SalleErreur extends RuntimeException {
    private int    idSalle;
    private String dateHeure;

    public SalleErreur(int idSalle, String dateHeure) {
        super("Salle #" + idSalle + " déjà réservée le " + dateHeure);
        this.idSalle   = idSalle;
        this.dateHeure = dateHeure;
    }

    public int    getIdSalle()   { return idSalle; }
    public String getDateHeure() { return dateHeure; }
}