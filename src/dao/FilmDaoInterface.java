package dao;

import java.util.List;
import modele.Film;

/**
 * Interface DAO définissant le contrat d'accès aux données pour les Films.
 * Permet l'inversion de dépendance et l'injection dans la couche Service.
 */
public interface FilmDaoInterface {

    List<Film> findAll();

    Film findById(int id);

    List<Film> findByGenre(String genre);

    boolean insert(Film f);

    boolean update(Film f);

    boolean delete(int id);
}
