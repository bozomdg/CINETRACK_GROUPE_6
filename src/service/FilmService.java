package service;

import exception.FilmIntrouvable;
import exception.NoteErreur;
import exception.ValidationException;
import modele.Film;
import modele.FilmDAO;

import java.util.List;

/**
 * Couche Service pour la gestion des films.
 * Encapsule les règles métier (validation durée, note, champs requis) et l'accès aux données.
 */
public class FilmService {

    private final FilmDAO filmDAO;

    public FilmService() {
        this.filmDAO = new FilmDAO();
    }

    public FilmService(FilmDAO filmDAO) {
        this.filmDAO = filmDAO;
    }

    /**
     * Retourne tous les films.
     */
    public List<Film> getAllFilms() {
        return filmDAO.findAll();
    }

    /**
     * Recherche un film par son identifiant.
     */
    public Film getFilmById(int id) throws FilmIntrouvable {
        return filmDAO.findById(id);
    }

    /**
     * Recherche les films par genre.
     */
    public List<Film> rechercherParGenre(String genre) {
        if (genre == null || genre.trim().isEmpty() || "Tous".equalsIgnoreCase(genre.trim())) {
            return filmDAO.findAll();
        }
        return filmDAO.findByGenre(genre.trim());
    }

    /**
     * Valide les données et insère un nouveau film.
     */
    public boolean ajouterFilm(String titre, String genre, String dureeStr, String noteStr, String synopsis)
            throws ValidationException, NoteErreur {

        validerChampsTexte(titre, genre);

        int duree = validerEtConvertirDuree(dureeStr);
        double note = validerEtConvertirNote(noteStr);

        Film f = new Film(titre.trim(), genre.trim(), duree, note, synopsis != null ? synopsis.trim() : "");
        f.setNote(note);

        boolean succes = filmDAO.insert(f);
        if (!succes) {
            throw new ValidationException("Impossible d'ajouter le film dans la base de données.");
        }
        return true;
    }

    /**
     * Valide et modifie un film existant.
     */
    public boolean modifierFilm(int id, String titre, String genre, String dureeStr, String noteStr, String synopsis)
            throws ValidationException, NoteErreur, FilmIntrouvable {

        if (id <= 0) {
            throw new ValidationException("Identifiant du film invalide.");
        }

        validerChampsTexte(titre, genre);

        int duree = validerEtConvertirDuree(dureeStr);
        double note = validerEtConvertirNote(noteStr);

        Film f = new Film(id, titre.trim(), genre.trim(), duree, note, synopsis != null ? synopsis.trim() : "");
        f.setNote(note);

        boolean succes = filmDAO.update(f);
        if (!succes) {
            throw new ValidationException("Échec de la mise à jour du film.");
        }
        return true;
    }

    /**
     * Supprime un film par son identifiant.
     */
    public boolean supprimerFilm(int id) throws FilmIntrouvable, ValidationException {
        if (id <= 0) {
            throw new ValidationException("Identifiant de film invalide.");
        }
        boolean succes = filmDAO.delete(id);
        if (!succes) {
            throw new ValidationException("Impossible de supprimer le film.");
        }
        return true;
    }

    // --- Méthodes de validation métier privées ---

    private void validerChampsTexte(String titre, String genre) throws ValidationException {
        if (titre == null || titre.trim().isEmpty() || genre == null || genre.trim().isEmpty()) {
            throw new ValidationException("Le titre et le genre sont obligatoires.");
        }
    }

    private double validerEtConvertirNote(String noteStr) {
        if (noteStr == null || noteStr.trim().isEmpty()) {
            return 0.0;
        }
        try {
            double note = Double.parseDouble(noteStr.trim().replace(',', '.'));
            return (note < 0 || note > 10) ? 0.0 : note;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public int validerEtConvertirDuree(String dureeStr) throws ValidationException {
        if (dureeStr == null || dureeStr.trim().isEmpty()) {
            throw new ValidationException("La durée est obligatoire.");
        }

        String s = dureeStr.trim().toLowerCase();
        int h = 0, m = 0;

        try {
            if (s.contains(":")) {
                String[] parts = s.split(":");
                h = Integer.parseInt(parts[0].trim());
                m = Integer.parseInt(parts[1].trim());
            } else if (s.contains("h")) {
                String[] parts = s.split("h");
                h = Integer.parseInt(parts[0].trim());
                String minutesPart = parts.length > 1 ? parts[1].replaceAll("[^0-9]", "").trim() : "";
                m = minutesPart.isEmpty() ? 0 : Integer.parseInt(minutesPart);
            } else {
                return Integer.parseInt(s);
            }

            if (h < 0 || m < 0 || m >= 60) {
                throw new ValidationException("Minutes invalides (doivent être comprises entre 0 et 59).");
            }
            int totalMinutes = h * 60 + m;
            if (totalMinutes <= 0) {
                throw new ValidationException("La durée doit être supérieure à 0 minute.");
            }
            return totalMinutes;
        } catch (NumberFormatException e) {
            throw new ValidationException("Format durée invalide. Utilisez H:mm (ex: 2h15 ou 2:15) ou un nombre de minutes.");
        }
    }
}

