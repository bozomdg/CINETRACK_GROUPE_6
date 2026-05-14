package controlleur;

import modele.Film;
import modele.FilmDAO;
import exception.FilmIntrouvable;
import exception.NoteErreur;
import vue.FenetreFilms;

import java.util.List;

/** Contrôleur Films  */

public class FilmControleur {

    private FilmDAO      dao;
    private FenetreFilms vue;

    public FilmControleur(FenetreFilms vue) {
        this.vue = vue;
        this.dao = new FilmDAO();
        this.vue.initialiser(this);
        chargerFilms();
    }

    /** Charge tous les films et les envoie à la vue */
    public void chargerFilms() {
        List<Film> liste = dao.findAll();
        vue.afficherFilms(liste);
        vue.mettreAJourTitre(liste.size());
    }

    /** Valide les saisies, crée le Film, appelle dao.insert() */
    public void ajouterFilm(String titre, String genre, String dureeStr, String noteStr, String synopsis) {
        try {
            if (titre.trim().isEmpty() || genre.trim().isEmpty()) {
                vue.afficherErreur("Le titre et le genre sont obligatoires.");
                return;
            }

            int duree = parseHMToMinutes(dureeStr); // <-- Conversion H:mm → minutes
            double note = Double.parseDouble(noteStr.trim());

            Film f = new Film(titre.trim(), genre.trim(), duree, note, synopsis.trim());
            f.setNote(note);

            boolean ok = dao.insert(f);
            if (ok) {
                vue.afficherSucces("Film \"" + titre + "\" ajouté avec succès !");
                chargerFilms();
            }
        } catch (NumberFormatException e) {
            vue.afficherErreur("Format durée invalide. Utilisez H:mm (ex: 2h15 ou 2:15).\nNote doit être un nombre.");
        } catch (NoteErreur e) {
            vue.afficherErreur(e.getMessage());
        }
    }

    /** Modifie un film existant */
    public void modifierFilm(int id, String titre, String genre,
                              String dureeStr, String noteStr, String synopsis) {
        try {
        	int duree = parseHMToMinutes(dureeStr);
            double note  = Double.parseDouble(noteStr.trim());

            Film f = new Film(id, titre.trim(), genre.trim(), duree, note, synopsis.trim());
            f.setNote(note);

            boolean updated = dao.update(f);
            if (updated) {
                vue.afficherSucces("Film modifié avec succès !");
                chargerFilms();
            } else {
                vue.afficherErreur("Échec de la mise à jour du film.");
            }

        } catch (NumberFormatException e) {
            vue.afficherErreur("Durée et note doivent être des nombres.");
        } catch (NoteErreur e) {
            vue.afficherErreur(e.getMessage());
        } catch (FilmIntrouvable e) {
            vue.afficherErreur(e.getMessage());
        }
    }

    /** Supprime un film après confirmation */
    public void supprimerFilm(int id, String titre) {
        try {
            dao.delete(id);
            vue.afficherSucces("Film \"" + titre + "\" supprimé.");
            chargerFilms();
        } catch (FilmIntrouvable e) {
            vue.afficherErreur(e.getMessage());
        }
    }

    /** Filtre par genre */
    public void rechercherParGenre(String genre) {
        List<Film> liste = dao.findByGenre(genre);
        vue.afficherFilms(liste);
    }
    private int parseHMToMinutes(String dureeStr) throws NumberFormatException {
        // Accepts formats: "H:mm", "HhMM", "Hh MMmin" etc.
        String s = dureeStr.trim().toLowerCase();
        int h = 0, m = 0;

        if (s.contains(":")) {
            String[] parts = s.split(":");
            h = Integer.parseInt(parts[0].trim());
            m = Integer.parseInt(parts[1].trim());
        } else if (s.contains("h")) {
            // Split on 'h' and extract numeric minutes, ignoring any non‑digit characters (e.g., "min")
            String[] parts = s.split("h");
            h = Integer.parseInt(parts[0].trim());
            String minutesPart = parts[1].replaceAll("[^0-9]", "").trim();
            m = minutesPart.isEmpty() ? 0 : Integer.parseInt(minutesPart);
        } else {
            // If only minutes are provided
            return Integer.parseInt(s);
        }
        
        if (h < 0 || m < 0 || m >= 60) {
            throw new NumberFormatException("Minutes invalides");
        }
        return h * 60 + m;
    }
}