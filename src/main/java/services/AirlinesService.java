package services;

import model.Flight;
import model.TicketReservationRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AirlinesService {

    /**
     * Finds and returns all the available flights for the given route and date
     *
     * @param origin      - The airport code from which the client is departing (Example : lax)
     * @param destination - The airport code where the client wants to arrive (Example: sfo)
     * @param date        - The date on which the client wants to fly from the origin to the
     *                    destination
     * @param referer    - The website where the initial request came from
     */
    List<Flight> findAllFlight(String origin, String destination, LocalDate date, Optional<String> referer);

    default List<Flight> findAllFlight(String origin, String destination, LocalDate date) {
        return findAllFlight(origin, destination, date, Optional.empty());
    }

    /**
     * Attempts to reserve a flight on behalf of the client
     *
     * @return - a positive confirmation number upon success or a negative number upon failure
     */
    long reserveFlight(TicketReservationRequest request);
}
