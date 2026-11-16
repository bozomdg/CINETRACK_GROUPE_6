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
        if (nom == null || nom.trim().isEmpty()) {
            throw new ValidationException("Le nom de la salle est obligatoire.");
        }
        int capacite;
        try {
            capacite = Integer.parseInt(capaciteStr.trim());
            if (capacite <= 0) {
                throw new ValidationException("La capacité de la salle doit être supérieure à 0.");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("La capacité doit être un nombre entier valide.");
        }

        Salle s = new Salle(nom.trim(), capacite, "Standard");
        return salleDAO.insert(s);
    }

    public boolean modifierSalle(int id, String nom, String capaciteStr) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("Identifiant de salle invalide.");
        }
        if (nom == null || nom.trim().isEmpty()) {
            throw new ValidationException("Le nom de la salle est obligatoire.");
        }
        int capacite;
        try {
            capacite = Integer.parseInt(capaciteStr.trim());
            if (capacite <= 0) {
                throw new ValidationException("La capacité doit être supérieure à 0.");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("La capacité doit être un nombre valide.");
        }

        Salle s = new Salle(id, nom.trim(), capacite, "Standard");
        return salleDAO.update(s);
    }

    public boolean supprimerSalle(int id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("Identifiant de salle invalide.");
        }
        return salleDAO.delete(id);
    }
}
