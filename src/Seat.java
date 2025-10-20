import java.util.Objects;

/**
 * Seat model
 * - seatNumber: e.g., "12A"
 * - status: AVAILABLE or BOOKED
 * - passenger: optional; if present -> BOOKED, if absent -> AVAILABLE
 */
public class Seat {
    private final String seatNumber;
    private SeatStatus status;
    private Passenger passenger; // nullable

    /** Create an AVAILABLE seat with no passenger. */
    public Seat(String seatNumber) {
        if (seatNumber == null || seatNumber.isBlank()) {
            throw new IllegalArgumentException("seatNumber cannot be null/blank");
        }
        this.seatNumber = seatNumber.trim();
        this.status = SeatStatus.AVAILABLE;
        this.passenger = null;
    }

    /** Create a seat that is BOOKED if passenger is provided, otherwise AVAILABLE. */
    public Seat(String seatNumber, Passenger passenger) {
        this(seatNumber);
        setPassenger(passenger); // will set status accordingly
    }

    public String getSeatNumber() { return seatNumber; }

    public SeatStatus getStatus() { return status; }

    public Passenger getPassenger() { return passenger; }

    /** True when status is BOOKED (or passenger present). */
    public boolean isBooked() {
        return status == SeatStatus.BOOKED;
    }

    /** Assign/replace passenger; null clears booking and marks AVAILABLE. */
    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
        this.status = (passenger == null) ? SeatStatus.AVAILABLE : SeatStatus.BOOKED;
    }

    /** Convenience for clearing a booking. */
    public void clearPassenger() {
        setPassenger(null);
    }

    /** Force status; keeps passenger reference but reconciles consistency. */
    public void setStatus(SeatStatus status) {
        if (status == null) return;
        this.status = status;
        if (status == SeatStatus.AVAILABLE) {
            this.passenger = null; // available implies no passenger
        }
        // if BOOKED with null passenger, UI/service should set passenger next
    }

    @Override
    public String toString() {
        return "Seat{" + seatNumber + ", " + status + (passenger != null ? ", " + passenger.getFullName() : "") + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat)) return false;
        Seat seat = (Seat) o;
        return seatNumber.equalsIgnoreCase(seat.seatNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatNumber.toUpperCase());
    }
}
