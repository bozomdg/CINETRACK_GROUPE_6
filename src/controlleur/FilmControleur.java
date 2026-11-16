package controlleur;

import exception.FilmIntrouvable;
import exception.ValidationException;
import java.util.List;
import modele.Film;
import service.FilmService;
import vue.FenetreFilms;

/**
 * Contrôleur Films (Couche Présentation).
 * Fait le pont entre la vue et la couche Service.
 */
@SuppressWarnings("this-escape")
public class FilmControleur {

    private final FilmService filmService;
    private final FenetreFilms vue;

    public FilmControleur(FenetreFilms vue) {
        this.vue = vue;
        this.filmService = new FilmService();
        this.vue.initialiser(this);
        chargerFilms();
    }

    
    public FilmControleur(FenetreFilms vue, FilmService filmService) {
        this.vue = vue;
        this.filmService = filmService;
        this.vue.initialiser(this);
        chargerFilms();
    }

    /** Charge tous les films via le service et rafraîchit la vue */
    public void chargerFilms() {
        List<Film> liste = filmService.getAllFilms();
        vue.afficherFilms(liste);
        vue.mettreAJourTitre(liste.size());
    }

    /** Transmet la demande d'ajout au service */
    public void ajouterFilm(String titre, String genre, String dureeStr, String synopsis) {
        try {
            filmService.ajouterFilm(titre, genre, dureeStr, synopsis);
            vue.afficherSucces("Film \"" + titre.trim() + "\" ajouté avec succès !");
            chargerFilms();
        } catch (ValidationException e) {
            vue.afficherErreur(e.getMessage());
        }
    }

    /** Transmet la demande de modification au service */
    public void modifierFilm(int id, String titre, String genre, String dureeStr, String synopsis) {
        try {
            filmService.modifierFilm(id, titre, genre, dureeStr, synopsis);
            vue.afficherSucces("Film modifié avec succès !");
            chargerFilms();
        } catch (ValidationException | FilmIntrouvable e) {
            vue.afficherErreur(e.getMessage());
        }
    }

    /** Transmet la suppression au service */
    public void supprimerFilm(int id, String titre) {
        try {
            filmService.supprimerFilm(id);
            vue.afficherSucces("Film \"" + titre + "\" supprimé.");
            chargerFilms();
        } catch (FilmIntrouvable | ValidationException e) {
            vue.afficherErreur(e.getMessage());
        }
    }

    /** Filtre les films par genre */
    public void rechercherParGenre(String genre) {
        List<Film> liste = filmService.rechercherParGenre(genre);
        vue.afficherFilms(liste);
    }
}
