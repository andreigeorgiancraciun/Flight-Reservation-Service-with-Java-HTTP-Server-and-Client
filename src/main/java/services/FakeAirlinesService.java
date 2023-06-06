package services;

import model.Flight;
import model.TicketReservationRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;


public class FakeAirlinesService implements AirlinesService {

    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public List<Flight> findAllFlight(String origin, String destination, LocalDate date,
                                      Optional<String> referer) {
        return List.of(new Flight(1, "Lufthansa", 300, 2, 4, date),
                new Flight(2, "Hawaiian Airlines", 340, 2, 7, date));
    }

    @Override
    public long reserveFlight(TicketReservationRequest request) {
        return random.nextLong(1, Long.MAX_VALUE);
    }
}
