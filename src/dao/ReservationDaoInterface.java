package dao;

import java.sql.SQLException;
import java.util.List;
import modele.Reservation;

/**
 * Interface DAO définissant le contrat d'accès aux données pour les Réservations.
 */
public interface ReservationDaoInterface {

    List<Reservation> findBySeance(int idSeance);

    List<Reservation> findAll();

    boolean insert(Reservation r) throws SQLException;

    boolean delete(int idReservation);
}
