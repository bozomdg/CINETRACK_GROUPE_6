package service;

import dao.SalleDaoImpl;
import dao.SalleDaoInterface;
import exception.ValidationException;
import java.util.List;
import modele.Salle;

/**
 * Couche Service pour la gestion des salles de projection.
 * Encapsule les règles métier et l'accès aux données via injection de dépendance.
 */
public class SalleService {

    private final SalleDaoInterface salleDAO;

    /**
     * Injection de dépendance via le constructeur.
     */
    public SalleService(SalleDaoInterface salleDAO) {
        this.salleDAO = salleDAO;
    }

    /**
     * Constructeur par défaut injectant l'implémentation JDBC PostgreSQL.
     */
    public SalleService() {
        this(new SalleDaoImpl());
    }

    public List<Salle> getAllSalles() {
        return salleDAO.findAll();
    }

    public List<Salle> recupererToutesLesSalles() {
        return getAllSalles();
    }

    public Salle getSalleById(int id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("Identifiant de salle invalide.");
        }
        return salleDAO.findById(id);
    }

    public boolean ajouterSalle(String nom, String capaciteStr) throws ValidationException {
        int capacite = validerDonneesSalle(nom, capaciteStr);

        Salle s = new Salle(nom.trim(), capacite, "Standard");
        if (!salleDAO.insert(s)) {
            throw new ValidationException("Impossible d'ajouter la salle (nom peut-être déjà utilisé).");
        }
        return true;
    }

    public boolean modifierSalle(int id, String nom, String capaciteStr) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("Identifiant de salle invalide.");
        }
        int capacite = validerDonneesSalle(nom, capaciteStr);

        Salle s = new Salle(id, nom.trim(), capacite, "Standard");
        if (!salleDAO.update(s)) {
            throw new ValidationException("Impossible de modifier la salle.");
        }
        return true;
    }

    public boolean supprimerSalle(int id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("Identifiant de salle invalide.");
        }
        if (!salleDAO.delete(id)) {
            throw new ValidationException("Impossible de supprimer la salle.");
        }
        return true;
    }

    /** Vérifie le nom et convertit la capacité en entier positif */
    private int validerDonneesSalle(String nom, String capaciteStr) throws ValidationException {
        if (nom == null || nom.trim().isEmpty() || capaciteStr == null || capaciteStr.trim().isEmpty()) {
            throw new ValidationException("Le nom et la capacité de la salle sont obligatoires.");
        }
        int capacite;
        try {
            capacite = Integer.parseInt(capaciteStr.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("La capacité doit être un nombre entier valide.");
        }
        if (capacite <= 0) {
            throw new ValidationException("La capacité de la salle doit être supérieure à 0.");
        }
        return capacite;
    }
}
