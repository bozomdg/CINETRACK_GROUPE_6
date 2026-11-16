package controlleur;

import java.util.List;

import exception.FilmIntrouvable;
import exception.ValidationException;
import modele.Film;
import service.FilmService;
import vue.FenetreFilms;

/**
 * Contrôleur pour la gestion des films (interface Swing).
 * Il fait le lien entre la fenêtre d'affichage (FenetreFilms)
 * et les règles de gestion (FilmService).
 */
public class FilmControleur {

    // Attributs : le service pour la logique et la vue pour l'affichage
    private FilmService filmService;
    private FenetreFilms vue;

    /**
     * Constructeur : initialise la vue et charge les films
     */
    public FilmControleur(FenetreFilms vue) {
        this.vue = vue;
        this.filmService = new FilmService();
        this.vue.initialiser(this);
        chargerFilms();
    }

    /**
     * Récupère tous les films de la base et les envoie à la vue
     */
    public void chargerFilms() {
        List<Film> liste = filmService.recupererTousLesFilms();
        vue.afficherFilms(liste);
        vue.mettreAJourTitre(liste.size());
    }

    /**
     * Ajoute un nouveau film en passant par le service
     */
    public void ajouterFilm(String titre, String genre, String dureeStr, String synopsis) {
        try {
            filmService.ajouterFilm(titre, genre, dureeStr, synopsis);
            vue.afficherSucces("Film ajouté avec succès !");
            chargerFilms();
        } catch (ValidationException e) {
            vue.afficherErreur(e.getMessage());
        }
    }

    /**
     * Modifie un film existant
     */
    public void modifierFilm(int id, String titre, String genre, String dureeStr, String synopsis) {
        try {
            filmService.modifierFilm(id, titre, genre, dureeStr, synopsis);
            vue.afficherSucces("Film modifié avec succès !");
            chargerFilms();
        } catch (ValidationException | FilmIntrouvable e) {
            vue.afficherErreur(e.getMessage());
        }
    }

    /**
     * Supprime un film par son identifiant
     */
    public void supprimerFilm(int id, String titre) {
        try {
            filmService.supprimerFilm(id);
            vue.afficherSucces("Film \"" + titre + "\" supprimé.");
            chargerFilms();
        } catch (ValidationException | FilmIntrouvable e) {
            vue.afficherErreur(e.getMessage());
        }
    }

    /**
     * Filtre la liste des films selon le genre choisi
     */
    public void rechercherParGenre(String genre) {
        List<Film> liste = filmService.rechercherParGenre(genre);
        vue.afficherFilms(liste);
    }
}
