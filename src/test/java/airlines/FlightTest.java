package airlines;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class FlightTest {

    @Test
    public void testConstructorBasic() {
        // Normal constructor
        Flight flight = new Flight("F001", "NU100");
        assertEquals("F001", flight.getId());
        assertEquals("NU100", flight.getFlightNumber());
        assertTrue(flight.getSeats().isEmpty());

        // Invalid id cases
        IllegalArgumentException ex1 =
                assertThrows(IllegalArgumentException.class, () -> new Flight(null, "NU100"));
        assertTrue(ex1.getMessage().contains("id cannot be null/blank"));

        IllegalArgumentException ex2 =
                assertThrows(IllegalArgumentException.class, () -> new Flight("", "NU100"));
        assertTrue(ex2.getMessage().contains("id cannot be null/blank"));

        IllegalArgumentException ex3 =
                assertThrows(IllegalArgumentException.class, () -> new Flight("  ", "NU100"));
        assertTrue(ex3.getMessage().contains("id cannot be null/blank"));

        // Invalid flightNumber cases
        IllegalArgumentException ex4 =
                assertThrows(IllegalArgumentException.class, () -> new Flight("F001", null));
        assertTrue(ex4.getMessage().contains("flightNumber cannot be null/blank"));

        IllegalArgumentException ex5 =
                assertThrows(IllegalArgumentException.class, () -> new Flight("F001", ""));
        assertTrue(ex5.getMessage().contains("flightNumber cannot be null/blank"));

        IllegalArgumentException ex6 =
                assertThrows(IllegalArgumentException.class, () -> new Flight("F001", "  "));
        assertTrue(ex6.getMessage().contains("flightNumber cannot be null/blank"));
    }

    @Test
    public void testConstructorWithSeats() {
        List<Seat> seats = new ArrayList<>();
        seats.add(new Seat("1A"));
        seats.add(new Seat("1B"));
        seats.add(new Seat("2A"));

        Flight flight = new Flight("F001", "NU100", seats);
        assertEquals("F001", flight.getId());
        assertEquals("NU100", flight.getFlightNumber());
        assertEquals(3, flight.getSeats().size());

        Flight flight2 = new Flight("F002", "NU200", null);
        assertEquals("F002", flight2.getId());
        assertEquals("NU200", flight2.getFlightNumber());
        assertTrue(flight2.getSeats().isEmpty());
    }

    @Test
    public void testSetFlightNumber() {
        Flight flight = new Flight("F001", "NU100");

        flight.setFlightNumber("NU200");
        assertEquals("NU200", flight.getFlightNumber());

        IllegalArgumentException ex1 =
                assertThrows(IllegalArgumentException.class, () -> flight.setFlightNumber(null));
        assertTrue(ex1.getMessage().contains("flightNumber cannot be null/blank"));

        IllegalArgumentException ex2 =
                assertThrows(IllegalArgumentException.class, () -> flight.setFlightNumber(""));
        assertTrue(ex2.getMessage().contains("flightNumber cannot be null/blank"));

        IllegalArgumentException ex3 =
                assertThrows(IllegalArgumentException.class, () -> flight.setFlightNumber("  "));
        assertTrue(ex3.getMessage().contains("flightNumber cannot be null/blank"));
    }

    @Test
    public void testAddSeat() {
        Flight flight = new Flight("F001", "NU100");

        Seat seat1 = new Seat("1A");
        Seat seat2 = new Seat("1B");

        flight.addSeat(seat1);
        assertEquals(1, flight.getSeats().size());
        assertTrue(flight.getSeats().contains(seat1));

        flight.addSeat(seat2);
        assertEquals(2, flight.getSeats().size());
        assertTrue(flight.getSeats().contains(seat2));

        // Adding null seat should be ignored
        flight.addSeat(null);
        assertEquals(2, flight.getSeats().size());

        // Duplicate seat allowed (different object)
        Seat seat1Dup = new Seat("1A");
        flight.addSeat(seat1Dup);
        assertEquals(3, flight.getSeats().size());
    }

    @Test
    public void testRemoveSeat() {
        Flight flight = new Flight("F001", "NU100");

        Seat seat1 = new Seat("1A");
        Seat seat2 = new Seat("1B");

        flight.addSeat(seat1);
        flight.addSeat(seat2);
        assertEquals(2, flight.getSeats().size());

        boolean removed = flight.removeSeat(seat1);
        assertTrue(removed);
        assertEquals(1, flight.getSeats().size());
        assertFalse(flight.getSeats().contains(seat1));
        assertTrue(flight.getSeats().contains(seat2));

        Seat seat3 = new Seat("2A");
        assertFalse(flight.removeSeat(seat3));
        assertFalse(flight.removeSeat(null));
    }

    @Test
    public void testGetSeat() {
        Flight flight = new Flight("F001", "NU100");

        Seat seat1 = new Seat("1A");
        Seat seat2 = new Seat("1B");

        flight.addSeat(seat1);
        flight.addSeat(seat2);

        Seat retrieved1 = flight.getSeat("1A");
        assertNotNull(retrieved1);
        assertEquals("1A", retrieved1.getSeatNumber());
        assertSame(seat1, retrieved1);

        Seat retrieved2 = flight.getSeat("1a");
        assertNotNull(retrieved2);
        assertEquals("1A", retrieved2.getSeatNumber());
        assertSame(seat1, retrieved2);

        assertNull(flight.getSeat("2A"));
        assertNull(flight.getSeat(null));
    }

    @Test
    public void testToString() {
        Flight flight = new Flight("F001", "NU100");
        flight.addSeat(new Seat("1A"));
        flight.addSeat(new Seat("1B"));

        String expected = "Flight{id='F001', flightNumber='NU100', seats=2}";
        assertEquals(expected, flight.toString());
    }

    @Test
    public void testEquals() {
        Flight f1 = new Flight("F001", "NU100");
        Flight f2 = new Flight("F001", "NU200");
        Flight f3 = new Flight("F002", "NU100");

        assertEquals(f1, f1);
        assertEquals(f1, f2);
        assertNotEquals(f1, f3);
        f1.addSeat(new Seat("1A"));
        assertEquals(f1, f2);
        assertNotEquals(f1, null);
        assertNotEquals(f1, "F001");
    }

    @Test
    public void testHashCode() {
        Flight f1 = new Flight("F001", "NU100");
        Flight f2 = new Flight("F001", "NU200");
        assertEquals(f1.hashCode(), f2.hashCode());
    }
}
