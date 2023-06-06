package handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Flight;
import model.FlightsWrapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import services.AirlinesService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlightSearchHandler implements HttpHandler {
    private static final String ORIGIN_PARAMETER_NAME = "origin";
    private static final String DESTINATION_PARAMETER_NAME = "destination";
    private static final String DAY_PARAMETER_NAME = "day";
    private static final String MONTH_PARAMETER_NAME = "month";
    private static final String YEAR_PARAMETER_NAME = "year";
    private final ObjectMapper jacksonObjectMapper;
    private final AirlinesService airlinesService;

    public FlightSearchHandler(AirlinesService airlinesService, ObjectMapper jacksonObjectMapper) {
        this.airlinesService = airlinesService;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    /**
     * Sends back an HTTP response to the server
     *
     * @param exchange     - Object indicating the exchange of HTTP request/response between
     *                     client/server
     * @param statusCode   - The HTTP response code to be included in the HTTP response
     * @param responseBody - The body payload of the HTTP response
     *                     DO NOT MODIFY THIS METHOD
     */
    private static void sendResponse(HttpExchange exchange,
                                     int statusCode,
                                     String responseBody) throws IOException {
        if (!responseBody.isBlank() && !responseBody.endsWith("\n")) {
            responseBody += "\n";
        }
        exchange.sendResponseHeaders(statusCode, responseBody.getBytes().length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBody.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Handles HTTP GET requests to search for available flights at a certain date
     * Response with a JSON object representing all the available flights for the given date
     *
     * <p>Example a valid request:
     * http://127.0.0.1:8080/search?origin=lax&destination=sfo&day=03&month=12&year=2023
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Flight Reservation Server - Received request to search for flights");
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405,
                    "Use GET method to search for flights");
            return;
        }

        if (exchange.getRequestHeaders().containsKey("Accept")
                && !exchange.getRequestHeaders().get("Accept").contains("application/json")) {
            sendResponse(exchange, 406,
                    "Client needs to support JSON response format\n");
            return;
        }

        Map<String, String> parameters = parseQueryParameters(exchange.getRequestURI());

        if (checkMissingParameters(parameters)) {
            sendResponse(exchange, 400,
                    "One of the URL parameters is missing");
            return;
        }

        String origin = parameters.get(ORIGIN_PARAMETER_NAME);
        String destination = parameters.get(DESTINATION_PARAMETER_NAME);
        LocalDate localDate = parseDate(parameters);

        System.out.println(String.format("Flight Reservation Server - Origin: %s Destination: %s," +
                        " for date: %s",
                origin,
                destination,
                localDate));

        Optional<String> referer = exchange.getRequestHeaders().containsKey("Referer")
                ? Optional.of(exchange.getRequestHeaders().getFirst("Referer"))
                : Optional.empty();

        List<Flight> flights = airlinesService.findAllFlight(origin,
                destination,
                localDate,
                referer);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        String flightsToJSONString = convertFlightsToJSONString(flights);
        sendResponse(exchange, 200, flightsToJSONString);
    }

    /**
     * Checks if an essential parameter of the HTTP reqeust is missing
     *
     * @return - true if a parameter is missing
     */
    private boolean checkMissingParameters(Map<String, String> parameters) {
        return !parameters.containsKey(ORIGIN_PARAMETER_NAME)
                || !parameters.containsKey(DESTINATION_PARAMETER_NAME)
                || !parameters.containsKey(DAY_PARAMETER_NAME)
                || !parameters.containsKey(MONTH_PARAMETER_NAME)
                || !parameters.containsKey(YEAR_PARAMETER_NAME);
    }

    /**
     * Parses a map of URL parameters to their values into a {@link LocalDate} object
     *
     * @param parameters - Map from a URL parameter to its value
     *                   <p>DO NOT MODIFY THIS METHOD
     */
    private LocalDate parseDate(Map<String, String> parameters) {
        String day = parameters.get(DAY_PARAMETER_NAME);
        String month = parameters.get(MONTH_PARAMETER_NAME);
        String year = parameters.get(YEAR_PARAMETER_NAME);
        return LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
    }

    /**
     * Parses an HTTP request {@link URI}.
     * Assumes that each parameter is present only once.
     *
     * @return - Map from a URL parameter to its value.
     * Example:
     * {"origin" -> "lax", "destination" -> "sfo", "day" -> "1", "month" -> "12", "year" -> "2030"}
     * <p>
     * DO NOT MODIFY THIS METHOD
     */
    private Map<String, String> parseQueryParameters(URI requestUri) {
        List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(requestUri, "UTF-8");
        return nameValuePairs.stream()
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
    }

    /**
     * Converts a list of {@link Flight}s to a JSON object represented as a String
     * DO NOT MODIFY THIS METHOD
     */
    private String convertFlightsToJSONString(List<Flight> flights) throws JsonProcessingException {
        return jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new FlightsWrapper(flights));
    }
}
