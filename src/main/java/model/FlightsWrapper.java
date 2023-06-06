package model;

import java.util.Collections;
import java.util.List;

public class FlightsWrapper {
    private List<Flight> flights = Collections.EMPTY_LIST;

    public FlightsWrapper(List<Flight> flights) {
        this.flights = flights;
    }

    public List<Flight> getFlights() {
        return flights;
    }
}
