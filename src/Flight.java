import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Flight domain model
 * - id: stable identifier for the flight (e.g., "F001")
 * - flightNumber: display number (e.g., "NU100")
 * - seats: list of Seat objects (Issue #3 will flesh out Seat)
 */
public class Flight {
    private final String id;
    private String flightNumber;
    private final List<Seat> seats;

    /**
     * Create a Flight with no seats yet (can add later with addSeat()).
     */
    public Flight(String id, String flightNumber) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id cannot be null/blank");
        }
        if (flightNumber == null || flightNumber.isBlank()) {
            throw new IllegalArgumentException("flightNumber cannot be null/blank");
        }
        this.id = id;
        this.flightNumber = flightNumber;
        this.seats = new ArrayList<>();
    }

    /**
     * Optional convenience: construct with an existing list of seats.

     */
    public Flight(String id, String flightNumber, List<Seat> initialSeats) {
        this(id, flightNumber);
        if (initialSeats != null) {
            this.seats.addAll(initialSeats);
        }
    }

    // --------- Getters / basic behavior ---------

    public String getId() {
        return id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        if (flightNumber == null || flightNumber.isBlank()) {
            throw new IllegalArgumentException("flightNumber cannot be null/blank");
        }
        this.flightNumber = flightNumber;
    }

    /**
     * Returns an unmodifiable view of the seat list.
     * Use addSeat/removeSeat to modify.
     */
    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    public void addSeat(Seat seat) {
        if (seat == null) return;
        seats.add(seat);
    }

    public boolean removeSeat(Seat seat) {
        return seats.remove(seat);
    }

    /**
     * Simple helper to find a seat by its seatNumber (e.g., "12A").
     * Returns null if not found.
     */
    public Seat getSeat(String seatNumber) {
        if (seatNumber == null) return null;
        for (Seat s : seats) {
            if (seatNumber.equalsIgnoreCase(s.getSeatNumber())) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "id='" + id + '\'' +
                ", flightNumber='" + flightNumber + '\'' +
                ", seats=" + seats.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Flight)) return false;
        Flight flight = (Flight) o;
        return id.equals(flight.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
