package modele;

// Représente un film dans le cinéma.
public class Film {

    //  Attributs 
    private int    idFilm;
    private String titre;
    private String genre;
    private int    duree;    
    private double note;      
    private String synopsis;

    // Constructeur AVEC id  
    public Film(int idFilm, String titre, String genre, int duree, double note, String synopsis) {
        this.idFilm   = idFilm;
        this.titre    = titre;
        this.genre    = genre;
        this.duree    = duree;
        this.note     = note;
        this.synopsis = synopsis;
    }


    public Film(String titre, String genre, int duree, double note, String synopsis) {
        this.titre    = titre;
        this.genre    = genre;
        this.duree    = duree;
        this.note     = note;
        this.synopsis = synopsis;
    }

    // Getters et Setters 
    public int    getIdFilm()   { 
    	return idFilm; 
    	}
    public String getTitre()    { 
    	return titre; }
    public String getGenre()    { 
    	return genre; }
    public int    getDuree()    { 
    	return duree; }
    public double getNote()     { 
    	return note; }
    public String getSynopsis() { 
    	return synopsis; }

    public void setIdFilm(int idFilm)      { 
    	this.idFilm = idFilm; }
    public void setTitre(String titre)     { 
    	this.titre = titre; }
    public void setGenre(String genre)     { 
    	this.genre = genre; }
    public void setDuree(int duree)        { 
    	this.duree = duree; }
    public void setSynopsis(String s)      { 
    	this.synopsis = s; }

    // Setter spécial pour la note : lance une exception si hors de [0, 10]
    public void setNote(double note) {
        if (note < 0 || note > 10) {
            
            throw new exception.NoteErreur(note);
        }
        this.note = note;
    }

    // Méthode métier : convertit la durée en minutes en format lisible.
    public String getDureeFormatee() {
        int heures  = duree / 60;  
        int minutes = duree % 60;   
        return heures + "h " + minutes + "min";
    }

    // Affichage lisible pour les tests console. 
    @Override
    public String toString() {
        return "[Film #" + idFilm + "] " + titre +
               " (" + genre + ") — " + getDureeFormatee() +
               " — Note: " + note + "/10";
    }
}