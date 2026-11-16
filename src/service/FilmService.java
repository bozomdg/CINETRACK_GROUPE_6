package service;

import java.util.List;

import dao.FilmDaoImpl;
import dao.FilmDaoInterface;
import exception.FilmIntrouvable;
import exception.ValidationException;
import modele.Film;

public class FilmService {

    private FilmDaoInterface filmDao;

    // constructeur avec un DAO donné
    public FilmService(FilmDaoInterface filmDao) {
        this.filmDao = filmDao;
    }

    // constructeur par défaut (base PostgreSQL)
    public FilmService() {
        this.filmDao = new FilmDaoImpl();
    }

    // ---------- Lecture ----------

    public List<Film> recupererTousLesFilms() {
        return filmDao.findAll();
    }

    // Alias pour la compatibilité
    public List<Film> getAllFilms() {
        return recupererTousLesFilms();
    }

    public Film recupererFilmParId(int id) throws FilmIntrouvable {
        return filmDao.findById(id);
    }

    public Film getFilmById(int id) throws FilmIntrouvable {
        return recupererFilmParId(id);
    }

    public List<Film> rechercherParGenre(String genre) {
        if (genre == null || genre.trim().isEmpty() || genre.trim().equalsIgnoreCase("Tous")) {
            return filmDao.findAll();
        }
        return filmDao.findByGenre(genre.trim());
    }

    // ---------- Ajout ----------

    // ajout sans note (la note vaut 0)
    public boolean ajouterFilm(String titre, String genre, String dureeStr, String synopsis)
            throws ValidationException {
        return ajouterFilm(titre, genre, dureeStr, "0", synopsis);
    }

    // ajout avec note
    public boolean ajouterFilm(String titre, String genre, String dureeStr, String noteStr, String synopsis)
            throws ValidationException {

        verifierTitreEtGenre(titre, genre);

        int duree = validerEtConvertirDuree(dureeStr);
        double note = convertirNote(noteStr);

        Film film = new Film(titre.trim(), genre.trim(), duree, note, nettoyerSynopsis(synopsis));

        boolean ok = filmDao.insert(film);
        if (!ok) {
            throw new ValidationException("Impossible d'ajouter le film dans la base de données.");
        }
        return true;
    }

    // ---------- Modification ----------

    // modification sans toucher à la note (on garde l'ancienne)
    public boolean modifierFilm(int id, String titre, String genre, String dureeStr, String synopsis)
            throws ValidationException, FilmIntrouvable {

        if (id <= 0) {
            throw new ValidationException("Identifiant du film invalide.");
        }

        Film ancienFilm = filmDao.findById(id);
        String ancienneNote = String.valueOf(ancienFilm.getNote());

        return modifierFilm(id, titre, genre, dureeStr, ancienneNote, synopsis);
    }

    // modification avec une nouvelle note
    public boolean modifierFilm(int id, String titre, String genre, String dureeStr, String noteStr, String synopsis)
            throws ValidationException, FilmIntrouvable {

        if (id <= 0) {
            throw new ValidationException("Identifiant du film invalide.");
        }

        verifierTitreEtGenre(titre, genre);

        int duree = validerEtConvertirDuree(dureeStr);
        double note = convertirNote(noteStr);

        Film film = new Film(id, titre.trim(), genre.trim(), duree, note, nettoyerSynopsis(synopsis));

        boolean ok = filmDao.update(film);
        if (!ok) {
            throw new ValidationException("Échec de la mise à jour du film.");
        }
        return true;
    }

    // ---------- Suppression ----------

    public boolean supprimerFilm(int id) throws FilmIntrouvable, ValidationException {
        if (id <= 0) {
            throw new ValidationException("Identifiant de film invalide.");
        }

        boolean ok = filmDao.delete(id);
        if (!ok) {
            throw new ValidationException("Impossible de supprimer le film.");
        }
        return true;
    }

    // ---------- Méthodes de vérification ----------

    // le titre et le genre ne doivent pas être vides
    private void verifierTitreEtGenre(String titre, String genre) throws ValidationException {
        if (titre == null || titre.trim().isEmpty() || genre == null || genre.trim().isEmpty()) {
            throw new ValidationException("Le titre et le genre sont obligatoires.");
        }
    }

    // si le synopsis est null on met une chaine vide
    private String nettoyerSynopsis(String synopsis) {
        if (synopsis == null) {
            return "";
        }
        return synopsis.trim();
    }

    // transforme la note en nombre (0 si vide, invalide ou hors de 0 à 10)
    private double convertirNote(String noteStr) {
        if (noteStr == null || noteStr.trim().isEmpty()) {
            return 0.0;
        }

        try {
            double note = Double.parseDouble(noteStr.trim().replace(',', '.'));
            if (note < 0 || note > 10) {
                return 0.0;
            }
            return note;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // accepte "135", "2h15", "2h", "2:15" et renvoie la durée en minutes
    public int validerEtConvertirDuree(String dureeStr) throws ValidationException {
        if (dureeStr == null || dureeStr.trim().isEmpty()) {
            throw new ValidationException("La durée est obligatoire.");
        }

        String texte = dureeStr.trim().toLowerCase();
        int heures = 0;
        int minutes = 0;

        try {
            if (texte.contains(":")) {
                // format 2:15
                String[] parties = texte.split(":");
                heures = Integer.parseInt(parties[0].trim());
                minutes = Integer.parseInt(parties[1].trim());

            } else if (texte.contains("h")) {
                // format 2h15
                String[] parties = texte.split("h");
                heures = Integer.parseInt(parties[0].trim());

                if (parties.length > 1) {
                    String texteMinutes = parties[1].replaceAll("[^0-9]", "");
                    if (!texteMinutes.isEmpty()) {
                        minutes = Integer.parseInt(texteMinutes);
                    }
                }

            } else {
                // simple nombre de minutes
                int total = Integer.parseInt(texte);
                if (total <= 0) {
                    throw new ValidationException("La durée doit être supérieure à 0 minute.");
                }
                return total;
            }

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new ValidationException("Format durée invalide. Utilisez H:mm (ex: 2h15 ou 2:15) ou un nombre de minutes.");
        }

        if (heures < 0 || minutes < 0 || minutes >= 60) {
            throw new ValidationException("Minutes invalides doivent être comprises entre 0 et 59");
        }

        int totalMinutes = heures * 60 + minutes;
        if (totalMinutes <= 0) {
            throw new ValidationException("La durée doit être supérieure à 0 minute.");
        }
        return totalMinutes;
    }
}