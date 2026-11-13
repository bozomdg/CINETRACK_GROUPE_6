package controlleur;

import exception.SalleErreur;
import exception.ValidationException;
import modele.Compte;
import modele.Reservation;
import service.SeanceService;
import vue.FenetrePrincipale;
import vue.FenetreSeances;

import java.sql.SQLException;
import java.util.List;

/**
 * Contrôleur pour la gestion des séances et réservations.
 * Délègue la logique métier à SeanceService.
 */
public class SeanceControleur {

    private final SeanceService seanceService;
    private final FenetreSeances vue;
    private final Compte compteConnecte;

    public SeanceControleur(FenetreSeances vue, FenetrePrincipale fp) {
        this.vue = vue;
        this.seanceService = new SeanceService();
        this.compteConnecte = fp.getCompteConnecte();
        this.vue.initialiser(fp, this);
        chargerSeances();
    }

    public SeanceControleur(FenetreSeances vue, FenetrePrincipale fp, SeanceService seanceService) {
        this.vue = vue;
        this.seanceService = seanceService;
        this.compteConnecte = fp.getCompteConnecte();
        this.vue.initialiser(fp, this);
        chargerSeances();
    }

    public void chargerSeances() {
        try {
            vue.afficherSeances(seanceService.getAllSeances());
        } catch (Exception e) {
            vue.afficherErreur("Erreur chargement: " + e.getMessage());
        }
    }

    public void chargerSeancesParDate(String date) {
        try {
            vue.afficherSeances(seanceService.getSeancesParDate(date));
        } catch (ValidationException e) {
            vue.afficherErreur(e.getMessage());
        } catch (Exception e) {
            vue.afficherErreur("Erreur filtre: " + e.getMessage());
        }
    }

    public void ajouterSeance(String idFilmStr, String idSalleStr, String date, String heure, String placesStr) {
        try {
            seanceService.ajouterSeance(idFilmStr, idSalleStr, date, heure, placesStr);
            vue.afficherSucces("Séance ajoutée !");
            chargerSeances();
        } catch (ValidationException | SalleErreur e) {
            vue.afficherErreur(e.getMessage());
        } catch (Exception e) {
            vue.afficherErreur("Erreur: " + e.getMessage());
        }
    }

    public void modifierSeance(int id, String idFilmStr, String idSalleStr, String date, String heure, String placesStr) {
        try {
            seanceService.modifierSeance(id, idFilmStr, idSalleStr, date, heure, placesStr);
            vue.afficherSucces("Séance modifiée !");
            chargerSeances();
        } catch (ValidationException | SalleErreur e) {
            vue.afficherErreur(e.getMessage());
        } catch (Exception e) {
            vue.afficherErreur("Erreur modification: " + e.getMessage());
        }
    }

    public void supprimerSeance(int id, String detail) {
        try {
            seanceService.supprimerSeance(id);
            vue.afficherSucces("Séance " + detail + " supprimée.");
            chargerSeances();
        } catch (ValidationException e) {
            vue.afficherErreur(e.getMessage());
        } catch (Exception e) {
            vue.afficherErreur("Erreur suppression: " + e.getMessage());
        }
    }

    /**
     * Réserve des places pour un client avec traçabilité du compte connecté.
     */
    public void reserverPlaces(int idSeance, String nom, String prenom, String nbPlacesStr) {
        try {
            seanceService.reserverPlaces(idSeance, nom, prenom, nbPlacesStr, compteConnecte);
            vue.afficherSucces("Réservation effectuée avec succès !");
            chargerSeances(); // Rafraîchit les places restantes
        } catch (ValidationException e) {
            vue.afficherErreur(e.getMessage());
        } catch (SQLException e) {
            vue.afficherErreur("Réservation impossible : " + e.getMessage());
        } catch (Exception e) {
            vue.afficherErreur("Erreur réservation: " + e.getMessage());
        }
    }

    /**
     * Charge les réservations pour une séance donnée.
     */
    public List<Reservation> getReservationsParSeance(int idSeance) {
        return seanceService.getReservationsParSeance(idSeance);
    }

    /**
     * Annule une réservation.
     */
    public void annulerReservation(int idReservation) {
        try {
            seanceService.annulerReservation(idReservation);
            vue.afficherSucces("Réservation annulée.");
            chargerSeances();
        } catch (ValidationException e) {
            vue.afficherErreur(e.getMessage());
        } catch (Exception e) {
            vue.afficherErreur("Erreur annulation: " + e.getMessage());
        }
    }
}
