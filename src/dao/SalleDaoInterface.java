package dao;

import java.util.List;
import modele.Salle;

/**
 * Interface DAO définissant le contrat d'accès aux données pour les Salles.
 */
public interface SalleDaoInterface {

    List<Salle> findAll();

    Salle findById(int id);

    boolean insert(Salle s);

    boolean update(Salle s);

    boolean delete(int id);
}
