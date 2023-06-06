package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Flight;
import model.TicketReservationRequest;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

public class RealAirlinesService implements AirlinesService {
    private static final String RESERVE_ROUTE = "/book_flight";
    private static final String SEARCH_ROUTE = "/show_flights";

    // A map from an airline name to its server address
    private static final Map<String, String> AIRLINE_TO_ADDRESS =
            Map.of("Delta Airlines", "http://127.0.0.1:9000",
                    "Alaska Airlines", "http://127.0.0.1:9001",
                    "Qatar Airways", "http://127.0.0.1:9002",
                    "Singapore Airlines", "http://127.0.0.1:9003",
                    "Japan Airlines", "http://127.0.0.1:9004",
                    "JetBlue", "http://127.0.0.1:9005",
                    "Hawaiian Airlines", "http://127.0.0.1:9006",
                    "British Airways", "http://127.0.0.1:9007",
                    "Korean air", "http://127.0.0.1:9008",
                    "Lufthansa", "http://127.0.0.1:9009");

    private final ObjectMapper jacksonObjectMapper;
    private final HttpClient httpClient;

    public RealAirlinesService(ObjectMapper jacksonObjectMapper) {
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.httpClient = HttpClient.newBuilder().build();
    }

    /**
     * Sends an HTTP GET request to each of the available airlines, requesting an available
     * flight for the given route and date
     * <p>An example of a valid HTTP request:
     * GET http://127.0.0.1:9000/show_flights?origin=lax&destination=sfo&date=2027-12-03
     *
     * @param origin      - The airport code from which the client is departing (Example : lax)
     * @param destination - The airport code where the client wants to arrive (Example: sfo)
     * @param date        - The date on which the client wants to fly from the origin to the
     *                    destination
     * @param referer    - The website where the initial request came from
     */
    @Override
    public List<Flight> findAllFlight(String origin, String destination, LocalDate date,
                                      Optional<String> referer) {
        List<Flight> flights = new ArrayList<>();
        for (String airline : AIRLINE_TO_ADDRESS.keySet()) {
            try {
                URI requestUri = buildSearchRequestURI(airline, origin, destination, date);

                HttpRequest.Builder requestBuilder =
                        HttpRequest.newBuilder()
                                .GET()
                                .uri(requestUri)
                                .setHeader("Accept", "application/json");

                if (referer.isPresent() && !referer.get().isBlank()) {
                    requestBuilder.setHeader("Referer", referer.get());
                }

                HttpResponse<String> response = httpClient.send(requestBuilder.build(),
                        HttpResponse.BodyHandlers.ofString());
                Optional<Flight> flight = convertResponseBodyToFlight(response.body());

                flight.ifPresent(flights::add);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        flights.sort(Collections.reverseOrder());
        return flights;
    }

    /**
     * Sends a request to an airline to book 1 or more tickets on a particular flight
     *
     * @return - a positive confirmation number upon success or a negative number upon failure
     */
    @Override
    public long reserveFlight(TicketReservationRequest ticketReservationRequest) {
        if (!AIRLINE_TO_ADDRESS.containsKey(ticketReservationRequest.getAirlineName())) {
            return -1;
        }
        try {
            URI uri = buildReserveRequestURI(ticketReservationRequest.getAirlineName());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/plain; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(buildReservationRequestBody(ticketReservationRequest)))
                    .build();
            HttpResponse<String> response
                    = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Long.parseLong(response.body());
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Builds the HTTP request URI to send an airline, requesting available an available flight
     * on a particular route and date
     * DO NOT MODIFY THIS METHOD
     */
    private URI buildSearchRequestURI(String airline, String origin, String destination,
                                      LocalDate date) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(AIRLINE_TO_ADDRESS.get(airline));
        uriBuilder.setPath(SEARCH_ROUTE);
        uriBuilder.addParameter("origin", origin);
        uriBuilder.addParameter("destination", destination);
        uriBuilder.addParameter("date", URLEncoder.encode(date.toString(), StandardCharsets.UTF_8));
        return uriBuilder.build();
    }

    /**
     * Builds the HTTP request URI to send an airline, requesting to book a flight
     * DO NOT MODIFY THIS METHOD
     */
    private URI buildReserveRequestURI(String airline) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(AIRLINE_TO_ADDRESS.get(airline));
        uriBuilder.setPath(RESERVE_ROUTE);
        return uriBuilder.build();
    }

    /**
     * Converts a JSON representation of a flight into a {@link Flight} object
     * Returns a {@link Flight} object wrapped in an {@link Optional}
     * or an {@link Optional#empty()} if the body is empty or could not be parsed
     * DO NOT MODIFY THIS METHOD
     */
    private Optional<Flight> convertResponseBodyToFlight(String responseBody) {
        if (responseBody.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(jacksonObjectMapper.readValue(responseBody, Flight.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Builds a request body to book tickets on a particular flight
     *
     * @return - String format of a JSON object representing the ticket reservation request
     * DO NOT MODIFY THIS METHOD
     */
    private String buildReservationRequestBody(TicketReservationRequest request) throws JsonProcessingException {
        return jacksonObjectMapper.writeValueAsString(request);
    }
}
