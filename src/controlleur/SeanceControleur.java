package controlleur;

import modele.Reservation;
import modele.ReservationDAO;
import modele.Seance;
import modele.SeanceDAO;
import modele.Compte;
import exception.SalleErreur;
import vue.FenetreSeances;

public class SeanceControleur {

    private SeanceDAO dao;
    private ReservationDAO reservationDAO;
    private FenetreSeances vue;
    private Compte compteConnecte;

    public SeanceControleur(FenetreSeances vue, vue.FenetrePrincipale fp) {
        this.vue = vue;
        this.dao = new SeanceDAO();
        this.reservationDAO = new ReservationDAO();
        // Récupère le compte connecté pour la traçabilité des réservations
        this.compteConnecte = fp.getCompteConnecte();
        this.vue.initialiser(fp, this);
        chargerSeances();
    }

    public void chargerSeances() {
        try {
            vue.afficherSeances(dao.findAll());
        } catch (Exception e) {
            vue.afficherErreur("Erreur chargement: " + e.getMessage());
        }
    }

    public void chargerSeancesParDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            vue.afficherErreur("Date vide.");
            return;
        }
        try {
            vue.afficherSeances(dao.findByDate(date.trim()));
        } catch (Exception e) {
            vue.afficherErreur("Erreur filtre: " + e.getMessage());
        }
    }

    public void ajouterSeance(String idFilmStr, String idSalleStr,
                              String date, String heure, String placesStr) {
        try {
            int idFilm = Integer.parseInt(idFilmStr.trim());
            int idSalle = Integer.parseInt(idSalleStr.trim());
            int places = Integer.parseInt(placesStr.trim());

            Seance s = new Seance(idFilm, idSalle, date.trim(), heure.trim(), places);
            dao.insert(s);

            vue.afficherSucces("Séance ajoutée !");
            chargerSeances();
        } catch (NumberFormatException e) {
            vue.afficherErreur("Format nombre invalide.");
        } catch (SalleErreur e) {
            vue.afficherErreur(e.getMessage());
        } catch (Exception e) {
            vue.afficherErreur("Erreur: " + e.getMessage());
        }
    }

    public void modifierSeance(int id, String idFilmStr, String idSalleStr,
                               String date, String heure, String placesStr) {
        try {
            int idFilm = Integer.parseInt(idFilmStr.trim());
            int idSalle = Integer.parseInt(idSalleStr.trim());
            int places = Integer.parseInt(placesStr.trim());

            Seance s = new Seance(id, idFilm, idSalle, date.trim(), heure.trim(), places);
            dao.update(s);

            vue.afficherSucces("Séance modifiée !");
            chargerSeances();
        } catch (NumberFormatException e) {
            vue.afficherErreur("Format nombre invalide.");
        } catch (SalleErreur e) {
            vue.afficherErreur(e.getMessage());
        } catch (Exception e) {
            vue.afficherErreur("Erreur modification: " + e.getMessage());
        }
    }

    public void supprimerSeance(int id, String detail) {
        try {
            dao.delete(id);
            vue.afficherSucces("Séance " + detail + " supprimée.");
            chargerSeances();
        } catch (Exception e) {
            vue.afficherErreur("Erreur suppression: " + e.getMessage());
        }
    }

    /**
     * Réserve des places pour un client.
     * Le trigger PostgreSQL décrémente automatiquement les places_dispo.
     * La traçabilité (quel compte a fait la réservation) est enregistrée.
     */
    public void reserverPlaces(int idSeance, String nom, String prenom, String nbPlacesStr) {
        try {
            if (nom.isEmpty() || prenom.isEmpty()) {
                vue.afficherErreur("Nom et prénom du client obligatoires.");
                return;
            }

            int nbPlaces = Integer.parseInt(nbPlacesStr.trim());
            if (nbPlaces <= 0) {
                vue.afficherErreur("Le nombre de places doit être supérieur à 0.");
                return;
            }

            // Création de la réservation avec traçabilité du compte connecté
            Reservation r = new Reservation(
                idSeance, nom.trim(), prenom.trim(), nbPlaces,
                compteConnecte.getId(), compteConnecte.getRole()
            );

            reservationDAO.insert(r);
            vue.afficherSucces("Réservation de " + nbPlaces + " place(s) pour "
                    + prenom.trim() + " " + nom.trim() + " effectuée !");
            chargerSeances(); // Rafraîchit le tableau (les places ont été mises à jour par le trigger)

        } catch (NumberFormatException e) {
            vue.afficherErreur("Format nombre de places invalide.");
        } catch (java.sql.SQLException e) {
            // Le trigger lève une exception si pas assez de places
            vue.afficherErreur("Réservation impossible : " + e.getMessage());
        } catch (Exception e) {
            vue.afficherErreur("Erreur réservation: " + e.getMessage());
        }
    }

    /**
     * Charge les réservations pour une séance donnée.
     */
    public java.util.List<Reservation> getReservationsParSeance(int idSeance) {
        return reservationDAO.findBySeance(idSeance);
    }

    /**
     * Annule une réservation. Le trigger ré-incrémente les places.
     */
    public void annulerReservation(int idReservation) {
        try {
            if (reservationDAO.delete(idReservation)) {
                vue.afficherSucces("Réservation annulée.");
                chargerSeances();
            } else {
                vue.afficherErreur("Erreur lors de l'annulation.");
            }
        } catch (Exception e) {
            vue.afficherErreur("Erreur annulation: " + e.getMessage());
        }
    }
}