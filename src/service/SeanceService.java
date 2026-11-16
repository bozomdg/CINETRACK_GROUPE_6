package service;

import dao.ReservationDAO;
import dao.SeanceDAO;
import exception.SalleErreur;
import exception.ValidationException;
import modele.Compte;
import modele.Reservation;
import modele.Seance;

import java.sql.SQLException;
import java.util.List;

/**
 * Couche Service pour les séances et les réservations.
 * Gère les vérifications métier avant insertion et délègue aux DAO.
 */
public class SeanceService {

    private final SeanceDAO seanceDAO;
    private final ReservationDAO reservationDAO;

    public SeanceService() {
        this.seanceDAO = new SeanceDAO();
        this.reservationDAO = new ReservationDAO();
    }

    public SeanceService(SeanceDAO seanceDAO, ReservationDAO reservationDAO) {
        this.seanceDAO = seanceDAO;
        this.reservationDAO = reservationDAO;
    }

    /**
     * Récupère la liste de toutes les séances ordonnées par date et heure.
     */
    public List<Seance> getAllSeances() {
        return seanceDAO.findAll();
    }

    /**
     * Filtre les séances pour une date donnée (YYYY-MM-DD).
     */
    public List<Seance> getSeancesParDate(String date) throws ValidationException {
        if (date == null || date.trim().isEmpty()) {
            throw new ValidationException("La date de recherche ne peut pas être vide.");
        }
        return seanceDAO.findByDate(date.trim());
    }

    /**
     * Valide et insère une nouvelle séance.
     */
    public boolean ajouterSeance(String idFilmStr, String idSalleStr, String date, String heure, String placesStr)
            throws ValidationException, SalleErreur {

        Seance s = validerEtConstruireSeance(0, idFilmStr, idSalleStr, date, heure, placesStr);
        boolean succes = seanceDAO.insert(s);
        if (!succes) {
            throw new ValidationException("Impossible d'ajouter la séance.");
        }
        return true;
    }

    /**
     * Valide et modifie une séance existante.
     */
    public boolean modifierSeance(int idSeance, String idFilmStr, String idSalleStr, String date, String heure, String placesStr)
            throws ValidationException, SalleErreur {

        if (idSeance <= 0) {
            throw new ValidationException("Identifiant de séance invalide.");
        }

        Seance s = validerEtConstruireSeance(idSeance, idFilmStr, idSalleStr, date, heure, placesStr);
        boolean succes = seanceDAO.update(s);
        if (!succes) {
            throw new ValidationException("Échec de la modification de la séance.");
        }
        return true;
    }

    /**
     * Supprime une séance.
     */
    public boolean supprimerSeance(int idSeance) throws ValidationException {
        if (idSeance <= 0) {
            throw new ValidationException("Identifiant de séance invalide.");
        }
        boolean succes = seanceDAO.delete(idSeance);
        if (!succes) {
            throw new ValidationException("Impossible de supprimer la séance.");
        }
        return true;
    }

    /**
     * Effectue une réservation avec validation et traçabilité du compte utilisateur.
     */
    public boolean reserverPlaces(int idSeance, String nom, String prenom, String nbPlacesStr, Compte compteConnecte)
            throws ValidationException, SQLException {

        if (idSeance <= 0) {
            throw new ValidationException("Séance invalide.");
        }
        if (nom == null || nom.trim().isEmpty() || prenom == null || prenom.trim().isEmpty()) {
            throw new ValidationException("Nom et prénom du client obligatoires.");
        }
        if (compteConnecte == null) {
            throw new ValidationException("Utilisateur non identifié pour cette réservation.");
        }

        int nbPlaces;
        try {
            nbPlaces = Integer.parseInt(nbPlacesStr.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Le format du nombre de places est invalide.");
        }

        if (nbPlaces <= 0) {
            throw new ValidationException("Le nombre de places doit être supérieur à 0.");
        }

        Reservation r = new Reservation(
                idSeance,
                nom.trim(),
                prenom.trim(),
                nbPlaces,
                compteConnecte.getId(),
                compteConnecte.getRole()
        );

        return reservationDAO.insert(r);
    }

    /**
     * Récupère les réservations d'une séance.
     */
    public List<Reservation> getReservationsParSeance(int idSeance) {
        return reservationDAO.findBySeance(idSeance);
    }

    /**
     * Annule une réservation existante.
     */
    public boolean annulerReservation(int idReservation) throws ValidationException {
        if (idReservation <= 0) {
            throw new ValidationException("Identifiant de réservation invalide.");
        }
        boolean succes = reservationDAO.delete(idReservation);
        if (!succes) {
            throw new ValidationException("Erreur lors de l'annulation de la réservation.");
        }
        return true;
    }

    // --- Validation métier privée ---

    private Seance validerEtConstruireSeance(int idSeance, String idFilmStr, String idSalleStr, String date, String heure, String placesStr)
            throws ValidationException {

        if (idFilmStr == null || idSalleStr == null || date == null || heure == null || placesStr == null
                || idFilmStr.trim().isEmpty() || idSalleStr.trim().isEmpty() || date.trim().isEmpty()
                || heure.trim().isEmpty() || placesStr.trim().isEmpty()) {
            throw new ValidationException("Tous les champs sont obligatoires.");
        }

        int idFilm, idSalle, places;
        try {
            idFilm = Integer.parseInt(idFilmStr.trim());
            idSalle = Integer.parseInt(idSalleStr.trim());
            places = Integer.parseInt(placesStr.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Les champs ID Film, ID Salle et Places doivent être des entiers valides.");
        }

        if (places < 0) {
            throw new ValidationException("Le nombre de places ne peut pas être négatif.");
        }

        String heureTrim = heure.trim();
        if (heureTrim.length() == 5) {
            heureTrim += ":00";
        }

        if (idSeance > 0) {
            return new Seance(idSeance, idFilm, idSalle, date.trim(), heureTrim, places);
        } else {
            return new Seance(idFilm, idSalle, date.trim(), heureTrim, places);
        }
    }
}
