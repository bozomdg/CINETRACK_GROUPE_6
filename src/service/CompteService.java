package service;

import dao.CompteDaoImpl;
import dao.CompteDaoInterface;
import dao.LogGestionnaireDaoImpl;
import dao.LogGestionnaireDaoInterface;
import exception.ValidationException;
import java.util.List;
import modele.Compte;
import modele.LogGestionnaire;

/**
 * Couche Service pour la gestion des comptes et de l'authentification.
 * Contient la logique métier, validations et délègue aux DAO via injection de dépendance.
 */
public class CompteService {

    private final CompteDaoInterface compteDAO;
    private final LogGestionnaireDaoInterface logDAO;

    /**
     * Injection de dépendance via le constructeur.
     */
    public CompteService(CompteDaoInterface compteDAO, LogGestionnaireDaoInterface logDAO) {
        this.compteDAO = compteDAO;
        this.logDAO = logDAO;
    }

    /**
     * Constructeur par défaut injectant les implémentations JDBC PostgreSQL.
     */
    public CompteService() {
        this(new CompteDaoImpl(), new LogGestionnaireDaoImpl());
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
     * Valide et ajoute un nouveau compte (rôle par défaut : caissier).
     */
    public boolean ajouterGestionnaire(String username, String password) throws ValidationException {
        return ajouterGestionnaire(username, password, "caissier");
    }

    /**
     * Valide et ajoute un nouveau compte avec son rôle (manager ou caissier).
     */
    public boolean ajouterGestionnaire(String username, String password, String role) throws ValidationException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new ValidationException("Identifiant et mot de passe obligatoires.");
        }

        String login = username.trim();
        if (login.length() < 3) {
            throw new ValidationException("Le nom d'utilisateur doit contenir au moins 3 caractères.");
        }

        // Règle métier : si l'utilisateur saisit juste un pseudo, on complète avec @cinetrack.com
        String emailFinal;
        if (login.contains("@")) {
            emailFinal = login;
        } else {
            emailFinal = login.toLowerCase() + "@cinetrack.com";
        }

        String roleFinal = ("manager".equalsIgnoreCase(role)) ? "manager" : "caissier";
        Compte nouveau = new Compte(0, emailFinal, password.trim(), roleFinal);
        boolean succes = compteDAO.insertGestionnaire(nouveau);
        if (!succes) {
            throw new ValidationException("Erreur lors de l'ajout (nom potentiellement déjà pris).");
        }
        return true;
    }

    /**
     * Supprime un gestionnaire par son identifiant.
     * Règle métier : il est interdit de supprimer un compte ADMINISTRATEUR.
     */
    public boolean supprimerGestionnaire(int idGestionnaire) throws ValidationException {
        if (idGestionnaire <= 0) {
            throw new ValidationException("Identifiant de gestionnaire invalide.");
        }

        String role = compteDAO.findRoleById(idGestionnaire);
        if ("ADMINISTRATEUR".equalsIgnoreCase(role)) {
            throw new ValidationException("Impossible de supprimer un compte Administrateur.");
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
