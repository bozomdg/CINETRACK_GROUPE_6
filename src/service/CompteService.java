package service;

import exception.ValidationException;
import modele.Compte;
import modele.CompteDAO;
import modele.LogGestionnaire;
import modele.LogGestionnaireDAO;

import java.util.List;

/**
 * Couche Service pour la gestion des comptes et de l'authentification.
 * Contient la logique métier, validations et délègue aux DAO.
 */
public class CompteService {

    private final CompteDAO compteDAO;
    private final LogGestionnaireDAO logDAO;

    public CompteService() {
        this.compteDAO = new CompteDAO();
        this.logDAO = new LogGestionnaireDAO();
    }

    public CompteService(CompteDAO compteDAO, LogGestionnaireDAO logDAO) {
        this.compteDAO = compteDAO;
        this.logDAO = logDAO;
    }

    /**
     * Authentifie un utilisateur (Admin ou Gestionnaire).
     */
    public Compte authentifier(String username, String password) throws ValidationException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("Champs obligatoires.");
        }

        Compte compte = compteDAO.findByUsernameAndPassword(username.trim(), password);
        if (compte == null) {
            throw new ValidationException("Identifiants incorrects.");
        }
        return compte;
    }

    /**
     * Récupère la liste de tous les gestionnaires.
     */
    public List<Compte> getTousLesGestionnaires() {
        return compteDAO.findAllGestionnaires();
    }

    /**
     * Ajoute un nouveau compte gestionnaire avec validation métier.
     */
    public boolean ajouterGestionnaire(String username, String password) throws ValidationException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new ValidationException("Veuillez remplir tous les champs.");
        }

        if (username.trim().length() < 3) {
            throw new ValidationException("Le nom d'utilisateur doit contenir au moins 3 caractères.");
        }

        Compte nouveau = new Compte(0, username.trim(), password.trim(), "gestionnaire");
        boolean succes = compteDAO.insertGestionnaire(nouveau);
        if (!succes) {
            throw new ValidationException("Erreur lors de l'ajout du gestionnaire (nom potentiellement déjà pris).");
        }
        return true;
    }

    /**
     * Supprime un gestionnaire par son identifiant.
     */
    public boolean supprimerGestionnaire(int idGestionnaire) throws ValidationException {
        if (idGestionnaire <= 0) {
            throw new ValidationException("Identifiant de gestionnaire invalide.");
        }

        boolean succes = compteDAO.deleteGestionnaire(idGestionnaire);
        if (!succes) {
            throw new ValidationException("Erreur lors de la suppression du gestionnaire.");
        }
        return true;
    }

    /**
     * Récupère l'historique des logs d'activité sur les gestionnaires.
     */
    public List<LogGestionnaire> getLogsActivite() {
        return logDAO.findAll();
    }
}

