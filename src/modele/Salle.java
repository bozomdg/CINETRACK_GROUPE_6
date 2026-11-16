package modele;

//Représente une salle de cinéma.
 
public class Salle {

    private int    idSalle;
    private String nom;
    private int    capacite;
    private String equipement;  

    // constructeur

    public Salle(int idSalle, String nom, int capacite, String equipement) {
        this.idSalle    = idSalle;
        this.nom        = nom;
        this.capacite   = capacite;
        this.equipement = equipement;
    }

    public Salle(String nom, int capacite, String equipement) {
        this.nom        = nom;
        this.capacite   = capacite;
        this.equipement = equipement;
    }

    // Getters
    public int    getIdSalle()    { 
    	return idSalle; }
    public String getNom()        { 
    	return nom; }
    public String getNomSalle()   {
        return nom; }
    public int    getCapacite()   { 
    	return capacite; }
    public int    getCapaciteSalle() {
        return capacite; }
    public String getEquipement() { 
    	return equipement; }

    // Setters
    public void setIdSalle(int idSalle)         { 
    	this.idSalle = idSalle; }
    public void setNom(String nom)              { 
    	this.nom = nom; }
    public void setCapacite(int capacite)       { 
    	this.capacite = capacite; }
    public void setEquipement(String e)         { 
    	this.equipement = e; }

    @Override
    public String toString() {
        return "[Salle #" + idSalle + "] " + nom +
               " — Capacité: " + capacite + " places — " + equipement;
    }
}