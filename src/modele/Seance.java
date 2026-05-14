package modele;

// Représente une séance (un film dans une salle à une date/heure).
 
public class Seance {

    private int    idSeance;
    private int    idFilm;      
    private int    idSalle;     
    private String dateSeance;  
    private String heure    ;
    private int    placesDispo;

    public Seance() {}

    public Seance(int idSeance, int idFilm, int idSalle,
                  String dateSeance, String heure, int placesDispo) {
        this.idSeance   = idSeance;
        this.idFilm     = idFilm;
        this.idSalle    = idSalle;
        this.dateSeance = dateSeance;
        this.heure      = heure;
        this.placesDispo = placesDispo;
    }

    public Seance(int idFilm, int idSalle, String dateSeance,
                  String heure, int placesDispo) {
        this.idFilm      = idFilm;
        this.idSalle     = idSalle;
        this.dateSeance  = dateSeance;
        this.heure       = heure;
        this.placesDispo = placesDispo;
    }

    // Getters
    public int    getIdSeance()    {
    	return idSeance; }
    public int    getIdFilm()      { 
    	return idFilm; }
    public int    getIdSalle()     { 
    	return idSalle; }
    public String getDateSeance()  { 
    	return dateSeance; }
    public String getHeure()       { 
    	return heure; }
    public int    getPlacesDispo() { 
    	return placesDispo; }

    // Setters
    public void setIdSeance(int id)         { this.idSeance = id; }
    public void setIdFilm(int idFilm)       { this.idFilm = idFilm; }
    public void setIdSalle(int idSalle)     { this.idSalle = idSalle; }
    public void setDateSeance(String d)     { this.dateSeance = d; }
    public void setHeure(String heure)      { this.heure = heure; }
    public void setPlacesDispo(int p)       { this.placesDispo = p; }

    // Méthode métier : la séance est-elle complète ?
     
    public boolean estComplete() {
        return placesDispo == 0;
    }
 

    @Override
    public String toString() {
        return "[Séance #" + idSeance + "] Film#" + idFilm +
               " | Salle#" + idSalle + " | " + dateSeance +
               " à " + heure + " | Places: " + placesDispo +
               (estComplete() ? " COMPLET" : "");
    }
    
}