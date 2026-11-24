
package service;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import dao.ReservationDaoImpl;
import dao.ReservationDaoInterface;
import dao.SeanceDaoImpl;
import dao.SalleDaoImpl;
import dao.SalleDaoInterface;
import dao.SeanceDaoInterface;
import exception.SalleErreur;
import exception.ValidationException;
import modele.Compte;
import modele.Reservation;
import modele.Salle;
import modele.Seance;

/**
 * Service pour les séances et les réservations.
 * Vérifie les données avant de les envoyer aux DAO.
 */
public class SeanceService {

    private SeanceDaoInterface seanceDAO;
    private ReservationDaoInterface reservationDAO;
    private SalleDaoInterface salleDAO;

    public SeanceService() {
        this.seanceDAO = new SeanceDaoImpl();
        this.reservationDAO = new ReservationDaoImpl();
        this.salleDAO = new SalleDaoImpl();
    }

    /** Retourne toutes les séances */
    public List<Seance> getAllSeances() {
        return seanceDAO.findAll();
    }

    /** Retourne les séances d'une date  */
    public List<Seance> getSeancesParDate(String date) throws ValidationException {
        if (estVide(date)) {
            throw new ValidationException("La date de recherche ne peut pas être vide.");
        }
        return seanceDAO.findByDate(date.trim());
    }

    /** Ajoute une séance */
    public boolean ajouterSeance(String idFilmStr, String idSalleStr, String date, String heure, String placesStr)
            throws ValidationException, SalleErreur {

        Seance s = construireSeance(0, idFilmStr, idSalleStr, date, heure, placesStr);

        if (!seanceDAO.insert(s)) {
            throw new ValidationException("Impossible d'ajouter la séance.");
        }
        return true;
    }

    /** Modifie une séance */
    public boolean modifierSeance(int idSeance, String idFilmStr, String idSalleStr, String date, String heure, String placesStr)
            throws ValidationException, SalleErreur {

        if (idSeance <= 0) {
            throw new ValidationException("Identifiant de séance invalide.");
        }

        Seance s = construireSeance(idSeance, idFilmStr, idSalleStr, date, heure, placesStr);

        if (!seanceDAO.update(s)) {
            throw new ValidationException("Échec de la modification de la séance.");
        }
        return true;
    }

    /** Supprime une séance */
    public boolean supprimerSeance(int idSeance) throws ValidationException {
        if (idSeance <= 0) {
            throw new ValidationException("Identifiant de séance invalide.");
        }
        if (!seanceDAO.delete(idSeance)) {
            throw new ValidationException("Impossible de supprimer la séance.");
        }
        return true;
    }

    /** Réserve des places pour un client */
    public boolean reserverPlaces(int idSeance, String nom, String prenom, String nbPlacesStr, Compte compteConnecte)
            throws ValidationException, SQLException {

        if (idSeance <= 0) {
            throw new ValidationException("Séance invalide.");
        }
        if (estVide(nom) || estVide(prenom)) {
            throw new ValidationException("Nom et prénom du client obligatoires.");
        }
        if (estVide(nbPlacesStr)) {
            throw new ValidationException("Le nombre de places est obligatoire.");
        }
        if (compteConnecte == null) {
            throw new ValidationException("Utilisateur non identifié pour cette réservation.");
        }

        int nbPlaces;
        try {
            nbPlaces = Integer.parseInt(nbPlacesStr.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Le nombre de places doit être un nombre entier.");
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
                compteConnecte.getRole());

        return reservationDAO.insert(r);
    }

    /** Retourne la capacité d'une salle (nombre de places du plan) */
    public int getCapaciteSalle(int idSalle) throws ValidationException {
        Salle salle = salleDAO.findById(idSalle);
        if (salle == null) {
            throw new ValidationException("Salle introuvable.");
        }
        return salle.getCapacite();
    }

    /** Retourne les numéros de places déjà réservées pour une séance */
    public Set<String> getPlacesOccupees(int idSeance) {
        return reservationDAO.findPlacesOccupees(idSeance);
    }

    /** Réserve des places précises choisies sur le plan de salle */
    public boolean reserverPlacesChoisies(int idSeance, String nom, String prenom,
                                          List<String> places, Compte compteConnecte) throws ValidationException, SQLException {

        if (idSeance <= 0) {
            throw new ValidationException("Séance invalide.");
        }
        if (estVide(nom) || estVide(prenom)) {
            throw new ValidationException("Nom et prénom du client obligatoires.");
        }
        if (places == null || places.isEmpty()) {
            throw new ValidationException("Choisissez au moins une place sur le plan.");
        }
        if (compteConnecte == null) {
            throw new ValidationException("Utilisateur non identifié pour cette réservation.");
        }

        Reservation r = new Reservation(
                idSeance,
                nom.trim(),
                prenom.trim(),
                places.size(),
                compteConnecte.getId(),
                compteConnecte.getRole());

        return reservationDAO.insertAvecPlaces(r, places);
    }

    /** Retourne les réservations d'une séance */
    public List<Reservation> getReservationsParSeance(int idSeance) {
        return reservationDAO.findBySeance(idSeance);
    }

    /** Annule une réservation */
    public boolean annulerReservation(int idReservation) throws ValidationException {
        if (idReservation <= 0) {
            throw new ValidationException("Identifiant de réservation invalide.");
        }
        if (!reservationDAO.delete(idReservation)) {
            throw new ValidationException("Erreur lors de l'annulation de la réservation.");
        }
        return true;
    }

    /** Retourne true si le texte est null ou vide */
    private boolean estVide(String texte) {
        return texte == null || texte.trim().isEmpty();
    }

    /** Vérifie les champs d'une séance et crée l'objet Seance */
    private Seance construireSeance(int idSeance, String idFilmStr, String idSalleStr,
                                    String date, String heure, String placesStr) throws ValidationException {

        // Tous les champs doivent être remplis
        if (estVide(idFilmStr) || estVide(idSalleStr) || estVide(date) || estVide(heure) || estVide(placesStr)) {
            throw new ValidationException("Tous les champs sont obligatoires.");
        }

        // Transformer les textes en nombres
        int idFilm;
        int idSalle;
        int places;
        try {
            idFilm = Integer.parseInt(idFilmStr.trim());
            idSalle = Integer.parseInt(idSalleStr.trim());
            places = Integer.parseInt(placesStr.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("ID Film, ID Salle et Places doivent être des nombres entiers.");
        }

        if (places <= 0) {
            throw new ValidationException("Le nombre de places doit être supérieur à 0.");
        }


        String heureTrim = heure.trim();
        if (heureTrim.length() == 5) {
            heureTrim = heureTrim + ":00";
        }

        // idSeance > 0 : modification (la séance a déjà un id), sinon : ajout
        if (idSeance > 0) {
            return new Seance(idSeance, idFilm, idSalle, date.trim(), heureTrim, places);
        } else {
            return new Seance(idFilm, idSalle, date.trim(), heureTrim, places);
        }
    }
}