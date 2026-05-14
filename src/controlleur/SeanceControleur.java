package controlleur;

import modele.Seance;
import modele.SeanceDAO;
import exception.SalleErreur; 
import vue.FenetreSeances;


public class SeanceControleur {

    private SeanceDAO dao;
    private FenetreSeances vue;

    public SeanceControleur(FenetreSeances vue, vue.FenetrePrincipale fp) {
        this.vue = vue;
        this.dao = new SeanceDAO();
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

    // Cette méthode accepte maintenant 2 paramètres pour correspondre à la Vue
    public void supprimerSeance(int id, String detail) {
        try {
            dao.delete(id);
            vue.afficherSucces("Séance " + detail + " supprimée.");
            chargerSeances();
        } catch (Exception e) {
            vue.afficherErreur("Erreur suppression: " + e.getMessage());
        }
    }
}