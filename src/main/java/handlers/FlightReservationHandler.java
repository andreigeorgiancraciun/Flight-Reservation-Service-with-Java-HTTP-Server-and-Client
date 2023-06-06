package handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.TicketReservationRequest;
import services.AirlinesService;
import services.AuthenticationService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FlightReservationHandler implements HttpHandler {
    private final AirlinesService airlinesService;
    private final ObjectMapper jacksonObjectMapper;
    private final AuthenticationService authenticationService;

    public FlightReservationHandler(AirlinesService airlinesService, ObjectMapper objectMapper) {
        this.airlinesService = airlinesService;
        this.jacksonObjectMapper = objectMapper;
        this.authenticationService = null;
    }

    public FlightReservationHandler(AirlinesService airlinesService,
                                    ObjectMapper jacksonObjectMapper,
                                    AuthenticationService authenticationService) {
        this.airlinesService = airlinesService;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.authenticationService = authenticationService;
    }

    /**
     * Sends back an HTTP response to the server
     *
     * @param exchange     - Object indicating the exchange of HTTP request/response between
     *                     client/server
     * @param statusCode   - The HTTP response code to be included in the HTTP response
     * @param responseBody - The body payload of the HTTP response
     *                     <p>DO NOT MODIFY THIS METHOD
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
     * Handles HTTP POST requests to reserve a particular flight on particular date
     * Multiple tickets on the same flight may be purchased in one transaction
     * <p>An example of a valid request:
     * <pre>
     * {
     *     "id": 677885206,
     *     "airlineName": "Singapore Airlines",
     *     "numberOfTickets": 29
     * }
     * <pre>
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Flight Reservation Server - Received request to reserve a flight");
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405,
                    "Use POST method for flight reservations");
            return;
        }

        if (!authenticationService.check(exchange.getRequestHeaders().get("Cookie"))) {
            sendResponse(exchange, 401, "Unauthorized User");
            return;
        }

        if (!exchange.getRequestHeaders().containsKey("Content-Type")
                || !exchange.getRequestHeaders().get("Content-Type").contains("application" +
                "/json")) {
            sendResponse(exchange, 415,
                    "Request body is not in the JSON format");
            return;
        }

        long confirmationNumber = reserveTickets(exchange.getRequestBody());
        if (confirmationNumber > 0) {
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            sendResponse(exchange, 200, String.valueOf(confirmationNumber));
        } else {
            sendResponse(exchange, 500, "Flight Reservation failed");
        }
    }

    /**
     * Reads and parses the request body stream into a JSON object.
     * Calls the {@link AirlinesService#reserveFlight(TicketReservationRequest)} method to book
     * the flight.
     * DO NOT MODIFY THIS METHOD
     *
     * @return - a positive confirmation number upon success. A negative number upon failure.
     */
    private long reserveTickets(InputStream requestBodyStream) throws IOException {
        byte[] requestBody = requestBodyStream.readAllBytes();
        TicketReservationRequest ticketReservationRequest
                = jacksonObjectMapper.readValue(
                new String(requestBody), TicketReservationRequest.class);

        return airlinesService.reserveFlight(ticketReservationRequest);
    }
}
