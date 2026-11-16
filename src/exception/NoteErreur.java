// Exception pour erreur note
package exception;

public class NoteErreur extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private double valeurInvalide;

    public NoteErreur(double valeur) {
        super("Note invalide : " + valeur + " (doit être entre 0 et 10)");
        this.valeurInvalide = valeur;
    }

    public double getValeurInvalide() { return valeurInvalide; }
}
