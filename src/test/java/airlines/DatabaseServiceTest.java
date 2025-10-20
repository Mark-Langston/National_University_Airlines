package airlines;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DatabaseServiceTest {

    @TempDir
    Path tempDir;
    
    private DatabaseService db;
    private String dbFilePath;
    
    @BeforeEach
    void setUp() {
        dbFilePath = tempDir.resolve("test_database.txt").toString();
        db = new DatabaseService(dbFilePath);
        // On construction, it will auto-load (creating default flights)
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
        
        // Verify list is unmodifiable
        try {
            flights.add(new Flight("F999", "NU999"));
            fail("List should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
    
    @Test
    void getSeatsReturnsCorrectSeatsForFlight() {
        // Add a flight with specific seats
        db.addFlight("T001", "NU999", 1, 2, new char[]{'A', 'B'});
        
        List<Seat> seats = db.getSeats("T001");
        
        // Should have 4 seats (2 rows x 2 seats)
        assertEquals(4, seats.size());
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
        // Add a flight with seats
        db.addFlight("T001", "NU999", 1, 1, new char[]{'A', 'B'});
        
        // Create a passenger
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        
        // Book a seat
        boolean booked = db.updateSeat("T001", "1A", passenger);
        assertTrue(booked);
        
        // Verify seat is booked
        List<Seat> seats = db.getSeats("T001");
        Seat seat = findSeatByNumber(seats, "1A");
        assertNotNull(seat);
        assertEquals(SeatStatus.BOOKED, seat.getStatus());
        assertNotNull(seat.getPassenger());
        assertEquals("John", seat.getPassenger().getFirstName());
    }
    
    @Test
    void updateSeatReleasingPassenger() {
        // Add a flight with seats
        db.addFlight("T001", "NU999", 1, 1, new char[]{'A', 'B'});
        
        // Book then release a seat
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        db.updateSeat("T001", "1A", passenger);
        boolean released = db.updateSeat("T001", "1A", null);
        
        assertTrue(released);
        
        // Verify seat is available
        List<Seat> seats = db.getSeats("T001");
        Seat seat = findSeatByNumber(seats, "1A");
        assertNotNull(seat);
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertNull(seat.getPassenger());
    }
    
    @Test
    void updateSeatWithNonexistentFlightReturnsFalse() {
        boolean result = db.updateSeat("NONEXISTENT", "1A", new Passenger("John", "Doe", "1990-01-01"));
        assertFalse(result);
    }
    
    @Test
    void updateSeatWithNonexistentSeatReturnsFalse() {
        // Add a flight with seats
        db.addFlight("T001", "NU999", 1, 1, new char[]{'A', 'B'});
        
        boolean result = db.updateSeat("T001", "99Z", new Passenger("John", "Doe", "1990-01-01"));
        assertFalse(result);
    }
    
    @Test
    void bookSeatWrapper() {
        // Add a flight with seats
        db.addFlight("T001", "NU999", 1, 1, new char[]{'A'});
        
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        boolean booked = db.bookSeat("T001", "1A", passenger);
        
        assertTrue(booked);
        
        // Verify seat is booked
        List<Seat> seats = db.getSeats("T001");
        Seat seat = findSeatByNumber(seats, "1A");
        assertEquals(SeatStatus.BOOKED, seat.getStatus());
    }
    
    @Test
    void releaseSeatWrapper() {
        // Add a flight with seats
        db.addFlight("T001", "NU999", 1, 1, new char[]{'A'});
        
        // Book then release
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        db.bookSeat("T001", "1A", passenger);
        boolean released = db.releaseSeat("T001", "1A");
        
        assertTrue(released);
        
        // Verify seat is available
        List<Seat> seats = db.getSeats("T001");
        Seat seat = findSeatByNumber(seats, "1A");
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
    }
    
    @Test
    void addFlightValid() {
        int initialCount = db.getFlights().size();
        
        boolean added = db.addFlight("T100", "NU100", 1, 3, new char[]{'A', 'B', 'C'});
        assertTrue(added);
        
        // Check flight count increased
        assertEquals(initialCount + 1, db.getFlights().size());
        
        // Check the flight exists
        Flight flight = findFlightById(db.getFlights(), "T100");
        assertNotNull(flight);
        assertEquals("NU100", flight.getFlightNumber());
        
        // Check all seats were created (3 rows * 3 columns = 9 seats)
        assertEquals(9, flight.getSeats().size());
        assertNotNull(flight.getSeat("1A"));
        assertNotNull(flight.getSeat("2B"));
        assertNotNull(flight.getSeat("3C"));
    }
    
    @Test
    void addFlightWithInvalidParameters() {
        int initialCount = db.getFlights().size();
        
        // Invalid flightId
        assertFalse(db.addFlight(null, "NU100", 1, 3, new char[]{'A', 'B'}));
        assertFalse(db.addFlight("", "NU100", 1, 3, new char[]{'A', 'B'}));
        
        // Invalid flightNumber
        assertFalse(db.addFlight("T100", null, 1, 3, new char[]{'A', 'B'}));
        assertFalse(db.addFlight("T100", "", 1, 3, new char[]{'A', 'B'}));
        
        // Invalid row range
        assertFalse(db.addFlight("T100", "NU100", 0, 3, new char[]{'A', 'B'}));
        assertFalse(db.addFlight("T100", "NU100", 3, 1, new char[]{'A', 'B'}));
        
        // Invalid seat letters
        assertFalse(db.addFlight("T100", "NU100", 1, 3, null));
        assertFalse(db.addFlight("T100", "NU100", 1, 3, new char[]{}));
        
        // Too many seats
        assertFalse(db.addFlight("T100", "NU100", 1, 1000, new char[]{'A', 'B', 'C', 'D', 'E', 'F'}));
        
        // Duplicate flightId
        db.addFlight("T100", "NU100", 1, 2, new char[]{'A', 'B'});
        assertFalse(db.addFlight("T100", "NU999", 1, 2, new char[]{'A', 'B'}));
        
        // Make sure only one flight was added
        assertEquals(initialCount + 1, db.getFlights().size());
    }
    
    @Test
    void deleteFlightValid() {
        // Add a flight then delete it
        db.addFlight("T100", "NU100", 1, 2, new char[]{'A', 'B'});
        int countAfterAdd = db.getFlights().size();
        
        boolean deleted = db.deleteFlight("T100");
        assertTrue(deleted);
        
        // Check flight count decreased
        assertEquals(countAfterAdd - 1, db.getFlights().size());
        
        // Check the flight no longer exists
        assertNull(findFlightById(db.getFlights(), "T100"));
    }
    
    @Test
    void deleteNonexistentFlightReturnsFalse() {
        int initialCount = db.getFlights().size();
        
        boolean deleted = db.deleteFlight("NONEXISTENT");
        assertFalse(deleted);
        
        // Count should remain the same
        assertEquals(initialCount, db.getFlights().size());
    }
    
    @Test
    void saveAndLoadPreservesData() {
        // Add a flight with a booked seat
        db.addFlight("T001", "NU999", 1, 1, new char[]{'A', 'B'});
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        db.bookSeat("T001", "1A", passenger);
        
        // Save explicitly
        assertTrue(db.save());
        
        // Create a new database service that loads from the same file
        DatabaseService newDb = new DatabaseService(dbFilePath);
        
        // Check that all data was preserved
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
    
    // Helper methods
    private Flight findFlightById(List<Flight> flights, String id) {
        for (Flight flight : flights) {
            if (flight.getId().equals(id)) {
                return flight;
            }
        }
        return null;
    }
    
    private Seat findSeatByNumber(List<Seat> seats, String seatNumber) {
        for (Seat seat : seats) {
            if (seat.getSeatNumber().equalsIgnoreCase(seatNumber)) {
                return seat;
            }
        }
        return null;
    }
}