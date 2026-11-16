package dao;

import java.util.List;
import modele.Seance;

/**
 * Interface DAO définissant le contrat d'accès aux données pour les Séances.
 */
public interface SeanceDaoInterface {

    List<Seance> findAll();

    List<Seance> findByFilm(int idFilm);

    List<Seance> findByDate(String date);

    boolean insert(Seance s);

    boolean update(Seance s);

    boolean delete(int id);
}
