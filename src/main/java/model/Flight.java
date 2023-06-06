package model;

import java.time.LocalDate;

public class Flight implements Comparable<Flight> {
    private int id;
    private String airlineName;
    private double priceUSD;
    private int numberOfFreeBags;
    private int numberOfAvailableSeats;
    private LocalDate date;

    public Flight() {
    }

    public Flight(int id, String airlineName, double priceUSD, int numberOfFreeBags,
                  int numberOfAvailableSeats, LocalDate date) {
        this.id = id;
        this.airlineName = airlineName;
        this.priceUSD = Math.round(priceUSD * 100.0) / 100.0;
        this.numberOfFreeBags = numberOfFreeBags;
        this.numberOfAvailableSeats = numberOfAvailableSeats;
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public double getPriceUSD() {
        return priceUSD;
    }

    public void setPriceUSD(double priceUSD) {
        this.priceUSD = Math.round(priceUSD * 100.0) / 100.0;
    }

    public int getNumberOfFreeBags() {
        return numberOfFreeBags;
    }

    public void setNumberOfFreeBags(int numberOfFreeBags) {
        this.numberOfFreeBags = numberOfFreeBags;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumberOfAvailableSeats() {
        return numberOfAvailableSeats;
    }

    public void setNumberOfAvailableSeats(int numberOfAvailableSeats) {
        this.numberOfAvailableSeats = numberOfAvailableSeats;
    }

    @Override
    public int compareTo(Flight other) {
        if (this.priceUSD < other.priceUSD) {
            return 1;
        } else if (this.priceUSD > other.priceUSD) {
            return -1;
        }

        return 0;
    }
}
