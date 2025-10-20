package airlines;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

public class FlightTest {

    @Test
    public void testConstructorBasic() {
        // Test normal constructor
        Flight flight = new Flight("F001", "NU100");
        assertEquals("F001", flight.getId());
        assertEquals("NU100", flight.getFlightNumber());
        assertTrue(flight.getSeats().isEmpty());
        
        // Test with invalid inputs (null or blank)
        try {
            new Flight(null, "NU100");
            fail("Should throw IllegalArgumentException for null id");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("id cannot be null/blank"));
        }
        
        try {
            new Flight("", "NU100");
            fail("Should throw IllegalArgumentException for empty id");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("id cannot be null/blank"));
        }
        
        try {
            new Flight("  ", "NU100");
            fail("Should throw IllegalArgumentException for blank id");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("id cannot be null/blank"));
        }
        
        try {
            new Flight("F001", null);
            fail("Should throw IllegalArgumentException for null flightNumber");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("flightNumber cannot be null/blank"));
        }
        
        try {
            new Flight("F001", "");
            fail("Should throw IllegalArgumentException for empty flightNumber");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("flightNumber cannot be null/blank"));
        }
        
        try {
            new Flight("F001", "  ");
            fail("Should throw IllegalArgumentException for blank flightNumber");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("flightNumber cannot be null/blank"));
        }
    }
    
    @Test
    public void testConstructorWithSeats() {
        // Create a list of seats
        List<Seat> seats = new ArrayList<>();
        seats.add(new Seat("1A"));
        seats.add(new Seat("1B"));
        seats.add(new Seat("2A"));
        
        // Test constructor with seats
        Flight flight = new Flight("F001", "NU100", seats);
        assertEquals("F001", flight.getId());
        assertEquals("NU100", flight.getFlightNumber());
        assertEquals(3, flight.getSeats().size());
        
        // Test with null seat list (should create empty seat list)
        Flight flight2 = new Flight("F002", "NU200", null);
        assertEquals("F002", flight2.getId());
        assertEquals("NU200", flight2.getFlightNumber());
        assertTrue(flight2.getSeats().isEmpty());
    }
    
    @Test
    public void testSetFlightNumber() {
        Flight flight = new Flight("F001", "NU100");
        
        // Test setting valid flight number
        flight.setFlightNumber("NU200");
        assertEquals("NU200", flight.getFlightNumber());
        
        // Test with invalid inputs (null or blank)
        try {
            flight.setFlightNumber(null);
            fail("Should throw IllegalArgumentException for null flightNumber");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("flightNumber cannot be null/blank"));
        }
        
        try {
            flight.setFlightNumber("");
            fail("Should throw IllegalArgumentException for empty flightNumber");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("flightNumber cannot be null/blank"));
        }
        
        try {
            flight.setFlightNumber("  ");
            fail("Should throw IllegalArgumentException for blank flightNumber");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("flightNumber cannot be null/blank"));
        }
    }
    
    @Test
    public void testAddSeat() {
        Flight flight = new Flight("F001", "NU100");
        
        // Test adding seats
        Seat seat1 = new Seat("1A");
        Seat seat2 = new Seat("1B");
        
        flight.addSeat(seat1);
        assertEquals(1, flight.getSeats().size());
        assertTrue(flight.getSeats().contains(seat1));
        
        flight.addSeat(seat2);
        assertEquals(2, flight.getSeats().size());
        assertTrue(flight.getSeats().contains(seat2));
        
        // Test adding null seat (should be ignored)
        flight.addSeat(null);
        assertEquals(2, flight.getSeats().size());
        
        // Test adding duplicate seat (should add as separate instance)
        Seat seat1Duplicate = new Seat("1A");
        flight.addSeat(seat1Duplicate);
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
        
        // Test removing existing seat
        boolean removed = flight.removeSeat(seat1);
        assertTrue(removed);
        assertEquals(1, flight.getSeats().size());
        assertFalse(flight.getSeats().contains(seat1));
        assertTrue(flight.getSeats().contains(seat2));
        
        // Test removing non-existent seat
        Seat seat3 = new Seat("2A");
        boolean removedNonExistent = flight.removeSeat(seat3);
        assertFalse(removedNonExistent);
        assertEquals(1, flight.getSeats().size());
        
        // Test removing null seat
        boolean removedNull = flight.removeSeat(null);
        assertFalse(removedNull);
        assertEquals(1, flight.getSeats().size());
    }
    
    @Test
    public void testGetSeat() {
        Flight flight = new Flight("F001", "NU100");
        
        Seat seat1 = new Seat("1A");
        Seat seat2 = new Seat("1B");
        
        flight.addSeat(seat1);
        flight.addSeat(seat2);
        
        // Test getting existing seat
        Seat retrieved1 = flight.getSeat("1A");
        assertNotNull(retrieved1);
        assertEquals("1A", retrieved1.getSeatNumber());
        assertSame(seat1, retrieved1);  // Should be same instance
        
        // Test case insensitivity
        Seat retrieved2 = flight.getSeat("1a");
        assertNotNull(retrieved2);
        assertEquals("1A", retrieved2.getSeatNumber());
        assertSame(seat1, retrieved2);  // Should be same instance
        
        // Test getting non-existent seat
        Seat retrieved3 = flight.getSeat("2A");
        assertNull(retrieved3);
        
        // Test with null input
        Seat retrieved4 = flight.getSeat(null);
        assertNull(retrieved4);
    }
    
    @Test
    public void testGetSeatsUnmodifiable() {
        Flight flight = new Flight("F001", "NU100");
        
        flight.addSeat(new Seat("1A"));
        flight.addSeat(new Seat("1B"));
        
        List<Seat> seats = flight.getSeats();
        
        // Verify list is unmodifiable
        try {
            seats.add(new Seat("2A"));
            fail("Should throw UnsupportedOperationException for add operation");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        
        try {
            seats.remove(0);
            fail("Should throw UnsupportedOperationException for remove operation");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        
        try {
            seats.clear();
            fail("Should throw UnsupportedOperationException for clear operation");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
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
        Flight flight1 = new Flight("F001", "NU100");
        Flight flight2 = new Flight("F001", "NU200"); // Same ID, different number
        Flight flight3 = new Flight("F002", "NU100"); // Different ID, same number
        
        // Test reflexivity
        assertEquals(flight1, flight1);
        
        // Test that equals only compares ID
        assertEquals(flight1, flight2); // Should be equal because same ID
        assertNotEquals(flight1, flight3); // Different ID
        
        // Add seats to flight1
        flight1.addSeat(new Seat("1A"));
        flight1.addSeat(new Seat("1B"));
        
        // Should still be equal to flight2 despite having seats
        assertEquals(flight1, flight2);
        
        // Test with null and different type
        assertNotEquals(flight1, null);
        assertNotEquals(flight1, "F001");
    }
    
    @Test
    public void testHashCode() {
        Flight flight1 = new Flight("F001", "NU100");
        Flight flight2 = new Flight("F001", "NU200"); // Same ID, different number
        
        assertEquals(flight1.hashCode(), flight2.hashCode()); // Same ID = same hashCode
    }
}