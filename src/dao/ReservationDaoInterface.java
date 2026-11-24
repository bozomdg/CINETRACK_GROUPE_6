
package dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import modele.Reservation;

/**
 * Interface DAO définissant le contrat d'accès aux données pour les Réservations.
 */
public interface ReservationDaoInterface {

    List<Reservation> findBySeance(int idSeance);

    List<Reservation> findAll();

    boolean insert(Reservation r) throws SQLException;

    /** Réserve des places précises (ex: "A1", "A2") pour une séance. */
    boolean insertAvecPlaces(Reservation r, List<String> numerosPlaces) throws SQLException;

    /** Retourne les numéros de places déjà réservées pour une séance. */
    Set<String> findPlacesOccupees(int idSeance);

    boolean delete(int idReservation);
}
