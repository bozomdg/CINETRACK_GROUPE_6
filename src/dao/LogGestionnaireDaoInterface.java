package dao;

import java.util.List;
import modele.LogGestionnaire;

/**
 * Interface DAO définissant le contrat d'accès aux données pour les Logs d'activité.
 */
public interface LogGestionnaireDaoInterface {

    List<LogGestionnaire> findAll();
}
