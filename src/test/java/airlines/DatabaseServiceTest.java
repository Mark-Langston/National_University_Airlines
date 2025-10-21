package airlines;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DatabaseServiceTest {

    @TempDir
    Path tempDir;

    private DatabaseService db;
    private String dbFilePath;

    @BeforeEach
    public void setUp() {
        dbFilePath = tempDir.resolve("test_database.txt").toString();
        db = new DatabaseService(dbFilePath);
}

    @Test
    void constructorWithNullPathUsesDefault() {
        DatabaseService nullPathDb = new DatabaseService(null);
        assertNotNull(nullPathDb.getFlights());
        assertFalse(nullPathDb.getFlights().isEmpty());
    }

    @Test
    void getFlightsReturnsUnmodifiableList() {
        List<Flight> flights = db.getFlights();

        // Use a void-return mutator AND capture the thrown exception
        UnsupportedOperationException ex = assertThrows(
        UnsupportedOperationException.class,
        () -> flights.add(0, new Flight("F999", "NU999"))
    );
    assertNotNull(ex); // (optional) or assertTrue(ex.getMessage() == null || !ex.getMessage().isEmpty());
}


    @Test
    void getSeatsReturnsCorrectSeatsForFlight() {
        db.addFlight("T001", "NU999", 1, 2, new char[]{'A', 'B'});

        List<Seat> seats = db.getSeats("T001");

        assertEquals(4, seats.size());           // 2 rows x 2 seats
        assertNotNull(findSeatByNumber(seats, "1A"));
        assertNotNull(findSeatByNumber(seats, "1B"));
        assertNotNull(findSeatByNumber(seats, "2A"));
        assertNotNull(findSeatByNumber(seats, "2B"));
    }

    @Test
    void getSeatsForNonexistentFlightReturnsEmptyList() {
        List<Seat> seats = db.getSeats("NONEXISTENT");
        assertNotNull(seats);
        assertTrue(seats.isEmpty());
    }

    @Test
    void getSeatsForNullFlightIdReturnsEmptyList() {
        List<Seat> seats = db.getSeats(null);
        assertNotNull(seats);
        assertTrue(seats.isEmpty());
    }

    @Test
    void updateSeatBookingPassenger() {
        db.addFlight("T001", "NU999", 1, 1, new char[]{'A', 'B'});

        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");

        boolean booked = db.updateSeat("T001", "1A", passenger);
        assertTrue(booked);

        List<Seat> seats = db.getSeats("T001");
        Seat seat = findSeatByNumber(seats, "1A");
        assertNotNull(seat);
        assertEquals(SeatStatus.BOOKED, seat.getStatus());
        assertNotNull(seat.getPassenger());
        assertEquals("John", seat.getPassenger().getFirstName());
    }

    @Test
    void updateSeatReleasingPassenger() {
        db.addFlight("T001", "NU999", 1, 1, new char[]{'A', 'B'});

        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        db.updateSeat("T001", "1A", passenger);
        boolean released = db.updateSeat("T001", "1A", null);

        assertTrue(released);

        List<Seat> seats = db.getSeats("T001");
        Seat seat = findSeatByNumber(seats, "1A");
        assertNotNull(seat);
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertNull(seat.getPassenger());
    }

    @Test
    void updateSeatWithNonexistentFlightReturnsFalse() {
        boolean result = db.updateSeat("NONEXISTENT", "1A",
                new Passenger("John", "Doe", "1990-01-01"));
        assertFalse(result);
    }

    @Test
    void updateSeatWithNonexistentSeatReturnsFalse() {
        db.addFlight("T001", "NU999", 1, 1, new char[]{'A', 'B'});

        boolean result = db.updateSeat("T001", "99Z",
                new Passenger("John", "Doe", "1990-01-01"));
        assertFalse(result);
    }

    @Test
    void bookSeatWrapper() {
        db.addFlight("T001", "NU999", 1, 1, new char[]{'A'});

        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        boolean booked = db.bookSeat("T001", "1A", passenger);

        assertTrue(booked);

        List<Seat> seats = db.getSeats("T001");
        Seat seat = findSeatByNumber(seats, "1A");
        assertEquals(SeatStatus.BOOKED, seat.getStatus());
    }

    @Test
    void releaseSeatWrapper() {
        db.addFlight("T001", "NU999", 1, 1, new char[]{'A'});

        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        db.bookSeat("T001", "1A", passenger);
        boolean released = db.releaseSeat("T001", "1A");

        assertTrue(released);

        List<Seat> seats = db.getSeats("T001");
        Seat seat = findSeatByNumber(seats, "1A");
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
    }

    @Test
    void addFlightValid() {
        int initialCount = db.getFlights().size();

        boolean added = db.addFlight("T100", "NU100", 1, 3, new char[]{'A', 'B', 'C'});
        assertTrue(added);

        assertEquals(initialCount + 1, db.getFlights().size());

        Flight flight = findFlightById(db.getFlights(), "T100");
        assertNotNull(flight);
        assertEquals("NU100", flight.getFlightNumber());

        assertEquals(9, flight.getSeats().size()); // 3 rows * 3 cols
        assertNotNull(flight.getSeat("1A"));
        assertNotNull(flight.getSeat("2B"));
        assertNotNull(flight.getSeat("3C"));
    }

    @Test
    void addFlightWithInvalidParameters() {
        int initialCount = db.getFlights().size();

        assertFalse(db.addFlight(null, "NU100", 1, 3, new char[]{'A', 'B'}));
        assertFalse(db.addFlight("", "NU100", 1, 3, new char[]{'A', 'B'}));

        assertFalse(db.addFlight("T100", null, 1, 3, new char[]{'A', 'B'}));
        assertFalse(db.addFlight("T100", "", 1, 3, new char[]{'A', 'B'}));

        assertFalse(db.addFlight("T100", "NU100", 0, 3, new char[]{'A', 'B'}));
        assertFalse(db.addFlight("T100", "NU100", 3, 1, new char[]{'A', 'B'}));

        assertFalse(db.addFlight("T100", "NU100", 1, 3, null));
        assertFalse(db.addFlight("T100", "NU100", 1, 3, new char[]{}));

        assertFalse(db.addFlight("T100", "NU100", 1, 1000,
                new char[]{'A', 'B', 'C', 'D', 'E', 'F'}));

        db.addFlight("T100", "NU100", 1, 2, new char[]{'A', 'B'});
        assertFalse(db.addFlight("T100", "NU999", 1, 2, new char[]{'A', 'B'}));

        assertEquals(initialCount + 1, db.getFlights().size());
    }

    @Test
    void deleteFlightValid() {
        db.addFlight("T100", "NU100", 1, 2, new char[]{'A', 'B'});
        int countAfterAdd = db.getFlights().size();

        boolean deleted = db.deleteFlight("T100");
        assertTrue(deleted);

        assertEquals(countAfterAdd - 1, db.getFlights().size());
        assertNull(findFlightById(db.getFlights(), "T100"));
    }

    @Test
    void deleteNonexistentFlightReturnsFalse() {
        int initialCount = db.getFlights().size();

        boolean deleted = db.deleteFlight("NONEXISTENT");
        assertFalse(deleted);

        assertEquals(initialCount, db.getFlights().size());
    }

    @Test
    void saveAndLoadPreservesData() {
        db.addFlight("T001", "NU999", 1, 1, new char[]{'A', 'B'});
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        db.bookSeat("T001", "1A", passenger);

        assertTrue(db.save());

        DatabaseService newDb = new DatabaseService(dbFilePath);

        assertEquals(db.getFlights().size(), newDb.getFlights().size());

        Flight flight = findFlightById(newDb.getFlights(), "T001");
        assertNotNull(flight);
        assertEquals("NU999", flight.getFlightNumber());

        Seat seat = flight.getSeat("1A");
        assertNotNull(seat);
        assertEquals(SeatStatus.BOOKED, seat.getStatus());
        assertNotNull(seat.getPassenger());
        assertEquals("John", seat.getPassenger().getFirstName());
        assertEquals("Doe", seat.getPassenger().getLastName());
    }

    // Helpers
    private Flight findFlightById(List<Flight> flights, String id) {
        for (Flight flight : flights) {
            if (flight.getId().equals(id)) return flight;
        }
        return null;
    }

    private Seat findSeatByNumber(List<Seat> seats, String seatNumber) {
        for (Seat seat : seats) {
            if (seat.getSeatNumber().equalsIgnoreCase(seatNumber)) return seat;
        }
        return null;
    }
}
