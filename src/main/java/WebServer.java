import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpServer;
import handlers.FlightReservationHandler;
import handlers.FlightSearchHandler;
import handlers.StatusHandler;
import services.AirlinesService;
import services.AuthenticationService;
import services.RealAirlinesService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer {
    private static final String STATUS_ROUTE = "/status";
    private static final String SEARCH_FLIGHTS_ROUTE = "/search";
    private static final String RESERVE_TICKETS_ROUTE = "/reserve";

    public static void main(String[] args) throws IOException {
        ObjectMapper jacksonObjectMapper = new ObjectMapper();
        jacksonObjectMapper.registerModule(new JavaTimeModule());

        HttpServer server = HttpServer.create(
                new InetSocketAddress("localhost", 8080),
                0);

        AirlinesService airlinesService = new RealAirlinesService(jacksonObjectMapper);

        server.createContext(STATUS_ROUTE, new StatusHandler());
        server.createContext(SEARCH_FLIGHTS_ROUTE, new FlightSearchHandler(airlinesService,
                jacksonObjectMapper));
        server.createContext(RESERVE_TICKETS_ROUTE, new FlightReservationHandler(airlinesService,
                jacksonObjectMapper, new AuthenticationService()));
        System.out.println("Starting Flight Reservation Server");
        server.start();
    }
}
