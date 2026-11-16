// exception pour les erreurs
package exception;

public class FilmIntrouvable  extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int idFilm;

    public FilmIntrouvable(int id) {
        super("Aucun film trouvé avec l'id : " + id);
        this.idFilm = id;
    }

    public int getIdFilm() { return idFilm; }
}
