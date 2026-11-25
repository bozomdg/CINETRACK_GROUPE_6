
package service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dao.FilmDaoImpl;
import dao.FilmDaoInterface;
import dao.ReservationDaoImpl;
import dao.ReservationDaoInterface;
import dao.SeanceDaoImpl;
import dao.SalleDaoImpl;
import dao.SalleDaoInterface;
import dao.SeanceDaoInterface;
import exception.FilmIntrouvable;
import exception.SalleErreur;
import exception.ValidationException;
import modele.Compte;
import modele.Film;
import modele.Reservation;
import modele.Salle;
import modele.Seance;

/**
 * Service pour les séances, les tarifs et les réservations.
 * Regroupe toutes les règles métier avant d'appeler les DAO.
 */
public class SeanceService {

    private static final int PAUSE_NETTOYAGE_MINUTES = 15;

    private SeanceDaoInterface seanceDAO;
    private ReservationDaoInterface reservationDAO;
    private SalleDaoInterface salleDAO;
    private FilmDaoInterface filmDAO;

    public SeanceService() {
        this.seanceDAO = new SeanceDaoImpl();
        this.reservationDAO = new ReservationDaoImpl();
        this.salleDAO = new SalleDaoImpl();
        this.filmDAO = new FilmDaoImpl();
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

    /** Ajoute une séance (les places sont calculées automatiquement selon la capacité de la salle) */
    public boolean ajouterSeance(String idFilmStr, String idSalleStr, String date, String heure)
            throws ValidationException, SalleErreur {

        if (estVide(idSalleStr)) {
            throw new ValidationException("L'ID de la salle est obligatoire.");
        }

        int idSalle;
        try {
            idSalle = Integer.parseInt(idSalleStr.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("L'ID de la salle doit être un nombre entier.");
        }

        int places = getCapaciteSalle(idSalle);
        if (places <= 0) {
            throw new ValidationException("Salle introuvable ou capacité nulle.");
        }

        return ajouterSeance(idFilmStr, idSalleStr, date, heure, String.valueOf(places));
    }

    /** Ajoute une séance avec un nombre de places explicite */
    public boolean ajouterSeance(String idFilmStr, String idSalleStr, String date, String heure, String placesStr)
            throws ValidationException, SalleErreur {

        Seance s = construireSeance(0, idFilmStr, idSalleStr, date, heure, placesStr);
        verifierConflitHoraire(0, s.getIdFilm(), s.getIdSalle(), s.getDateSeance(), s.getHeure());

        if (!seanceDAO.insert(s)) {
            throw new ValidationException("Impossible d'ajouter la séance.");
        }
        return true;
    }

    /** Modifie une séance en conservant les places disponibles existantes */
    public boolean modifierSeance(int idSeance, String idFilmStr, String idSalleStr, String date, String heure, int placesActuelles)
            throws ValidationException, SalleErreur {

        int places = placesActuelles;
        if (places <= 0 && !estVide(idSalleStr)) {
            try {
                int idSalle = Integer.parseInt(idSalleStr.trim());
                places = getCapaciteSalle(idSalle);
            } catch (Exception ignored) {}
        }
        if (places <= 0) {
            places = 1;
        }

        return modifierSeance(idSeance, idFilmStr, idSalleStr, date, heure, String.valueOf(places));
    }

    /** Modifie une séance */
    public boolean modifierSeance(int idSeance, String idFilmStr, String idSalleStr, String date, String heure, String placesStr)
            throws ValidationException, SalleErreur {

        if (idSeance <= 0) {
            throw new ValidationException("Identifiant de séance invalide.");
        }

        Seance s = construireSeance(idSeance, idFilmStr, idSalleStr, date, heure, placesStr);
        verifierConflitHoraire(idSeance, s.getIdFilm(), s.getIdSalle(), s.getDateSeance(), s.getHeure());

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

    // ───── Règles métiers des Tarifs ──────────────────────────────────────

    /** Retourne les tarifs (depuis la base, avec valeurs par défaut si besoin) */
    public Map<String, Double> getTarifsMap() {
        Map<String, Double> tarifs = reservationDAO.findAllTarifs();
        if (tarifs == null || tarifs.isEmpty()) {
            tarifs = new LinkedHashMap<>();
            tarifs.put("NORMAL", 350.0);
            tarifs.put("ETUDIANT", 250.0);
            tarifs.put("ENFANT", 200.0);
            tarifs.put("SENIOR", 250.0);
        }
        return tarifs;
    }

    /** Retourne la liste lisible des tarifs pour la ComboBox de l'interface */
    public List<String> getTarifsAffichage() {
        List<String> liste = new ArrayList<>();
        Map<String, Double> tarifs = getTarifsMap();
        for (Map.Entry<String, Double> entry : tarifs.entrySet()) {
            liste.add(entry.getKey() + " - " + entry.getValue().intValue() + " Rs");
        }
        return liste;
    }

    /** Extrait le nom de la catégorie ("ETUDIANT") depuis "ETUDIANT - 250 Rs" */
    public String extraireCategorieTarif(String texteChoisi) {
        if (estVide(texteChoisi)) {
            return "NORMAL";
        }
        String propre = texteChoisi.trim().toUpperCase();
        if (propre.contains(" - ")) {
            propre = propre.split(" - ")[0].trim();
        }
        if (getTarifsMap().containsKey(propre)) {
            return propre;
        }
        return "NORMAL";
    }

    /** Calcule le montant total selon le tarif et le nombre de places */
    public double calculerMontant(String categorieTarif, int nbPlaces) {
        String cat = extraireCategorieTarif(categorieTarif);
        double prix = getTarifsMap().getOrDefault(cat, 350.0);
        return prix * nbPlaces;
    }

    // ───── Règles métiers des Réservations ────────────────────────────────

    /** Valide que le nom et le prénom du client sont renseignés */
    public void validerClient(String nom, String prenom) throws ValidationException {
        if (estVide(nom) || estVide(prenom)) {
            throw new ValidationException("Nom et prénom du client obligatoires.");
        }
    }

    /** Réserve des places pour un client (tarif par défaut NORMAL) */
    public boolean reserverPlaces(int idSeance, String nom, String prenom, String nbPlacesStr, Compte compteConnecte)
            throws ValidationException, SQLException {
        return reserverPlaces(idSeance, nom, prenom, nbPlacesStr, "NORMAL", compteConnecte);
    }

    /** Réserve des places pour un client avec catégorie tarifaire */
    public boolean reserverPlaces(int idSeance, String nom, String prenom, String nbPlacesStr, String categorieTarif, Compte compteConnecte)
            throws ValidationException, SQLException {

        if (idSeance <= 0) {
            throw new ValidationException("Séance invalide.");
        }
        validerClient(nom, prenom);

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

        String categorie = extraireCategorieTarif(categorieTarif);
        double montant = calculerMontant(categorie, nbPlaces);

        Reservation r = new Reservation(
                idSeance,
                nom.trim(),
                prenom.trim(),
                nbPlaces,
                compteConnecte.getId(),
                compteConnecte.getRole());
        r.setCategorieTarif(categorie);
        r.setMontantTotal(montant);

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

    /** Réserve des places précises choisies sur le plan de salle (tarif par défaut NORMAL) */
    public boolean reserverPlacesChoisies(int idSeance, String nom, String prenom,
                                          List<String> places, Compte compteConnecte) throws ValidationException, SQLException {
        return reserverPlacesChoisies(idSeance, nom, prenom, places, "NORMAL", compteConnecte);
    }

    /** Réserve des places précises choisies sur le plan de salle avec tarif */
    public boolean reserverPlacesChoisies(int idSeance, String nom, String prenom,
                                          List<String> places, String categorieTarif, Compte compteConnecte) throws ValidationException, SQLException {

        if (idSeance <= 0) {
            throw new ValidationException("Séance invalide.");
        }
        validerClient(nom, prenom);

        if (places == null || places.isEmpty()) {
            throw new ValidationException("Choisissez au moins une place sur le plan.");
        }
        if (compteConnecte == null) {
            throw new ValidationException("Utilisateur non identifié pour cette réservation.");
        }

        String categorie = extraireCategorieTarif(categorieTarif);
        double montant = calculerMontant(categorie, places.size());

        Reservation r = new Reservation(
                idSeance,
                nom.trim(),
                prenom.trim(),
                places.size(),
                compteConnecte.getId(),
                compteConnecte.getRole());
        r.setCategorieTarif(categorie);
        r.setMontantTotal(montant);

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

        // Vérifier que le film et la salle existent bien
        try {
            filmDAO.findById(idFilm);
        } catch (FilmIntrouvable e) {
            throw new ValidationException("Film #" + idFilm + " introuvable.");
        }
        if (salleDAO.findById(idSalle) == null) {
            throw new ValidationException("Salle #" + idSalle + " introuvable.");
        }

        // Vérifier format de la date (AAAA-MM-JJ)
        String dateTrim = date.trim();
        try {
            LocalDate.parse(dateTrim);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Format de date invalide. Utilisez AAAA-MM-JJ (ex: 2026-11-25).");
        }

        // Vérifier format de l'heure (HH:MM ou HH:MM:SS)
        String heureTrim = heure.trim();
        if (heureTrim.length() == 5) {
            heureTrim = heureTrim + ":00";
        }
        try {
            LocalTime.parse(heureTrim);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Format d'heure invalide. Utilisez HH:MM (ex: 14:30).");
        }

        // idSeance > 0 : modification (la séance a déjà un id), sinon : ajout
        if (idSeance > 0) {
            return new Seance(idSeance, idFilm, idSalle, dateTrim, heureTrim, places);
        } else {
            return new Seance(idFilm, idSalle, dateTrim, heureTrim, places);
        }
    }

    /**
     * Règle métier : détecte si une séance entre en conflit d'horaire
     * avec une autre séance déjà programmée dans la même salle le même jour.
     */
    private void verifierConflitHoraire(int idSeanceActuelle, int idFilm, int idSalle,
                                        String date, String heureDebut) throws SalleErreur {
        int debutNouveau = heureEnMinutes(heureDebut);
        int dureeNouveau = getDureeFilm(idFilm) + PAUSE_NETTOYAGE_MINUTES;
        int finNouveau = debutNouveau + dureeNouveau;

        List<Seance> existantes = seanceDAO.findAll();
        for (Seance s : existantes) {
            // Ignorer la séance qu'on est en train de modifier
            if (s.getIdSeance() == idSeanceActuelle) {
                continue;
            }
            if (s.getIdSalle() == idSalle && date.equals(s.getDateSeance())) {
                int debutExistant = heureEnMinutes(s.getHeure());
                int dureeExistante = getDureeFilm(s.getIdFilm()) + PAUSE_NETTOYAGE_MINUTES;
                int finExistante = debutExistant + dureeExistante;

                // Chevauchement des deux créneaux horaires
                if (debutNouveau < finExistante && finNouveau > debutExistant) {
                    throw new SalleErreur(idSalle, date + " à " + s.getHeure());
                }
            }
        }
    }

    private int getDureeFilm(int idFilm) {
        try {
            Film f = filmDAO.findById(idFilm);
            return f.getDuree();
        } catch (Exception e) {
            return 120; // durée par défaut si non trouvée
        }
    }

    private int heureEnMinutes(String heure) {
        String h = heure.trim();
        if (h.length() == 5) {
            h = h + ":00";
        }
        LocalTime t = LocalTime.parse(h);
        return t.getHour() * 60 + t.getMinute();
    }
}