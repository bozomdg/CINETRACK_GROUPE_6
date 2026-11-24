package dao;

import java.util.List;
import modele.Compte;

/**
 * Interface DAO définissant le contrat d'accès aux données pour l'authentification et les comptes.
 */
public interface CompteDaoInterface {

    Compte findByUsernameAndPassword(String usernameOrEmail, String password);

    List<Compte> findAllGestionnaires();

    boolean insertGestionnaire(Compte c);

    boolean insertGestionnaire(String usernameOrEmail, String password);

    boolean insertGestionnaire(String usernameOrEmail, String password, String role);

    boolean deleteGestionnaire(int id);
}
