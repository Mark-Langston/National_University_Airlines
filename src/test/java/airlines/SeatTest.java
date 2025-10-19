package airlines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class SeatTest {

    @Test
    public void testConstructorWithSeatNumberOnly() {
        // Test normal constructor
        Seat seat = new Seat("12A");
        assertEquals("12A", seat.getSeatNumber());
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertNull(seat.getPassenger());
        assertFalse(seat.isBooked());
        
        // Test with trimmed input
        Seat seat2 = new Seat(" 14B ");
        assertEquals("14B", seat2.getSeatNumber());
    }
    
    @Test
    public void testConstructorWithNullOrEmptySeatNumber() {
        // Test with null seat number (should throw exception)
        assertThrows(IllegalArgumentException.class, () -> new Seat(null));
        
        // Test with empty seat number (should throw exception)
        assertThrows(IllegalArgumentException.class, () -> new Seat(""));
        assertThrows(IllegalArgumentException.class, () -> new Seat("   "));
    }
    
    @Test
    public void testConstructorWithPassenger() {
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        
        // Test constructor with passenger
        Seat seat = new Seat("12A", passenger);
        assertEquals("12A", seat.getSeatNumber());
        assertEquals(SeatStatus.BOOKED, seat.getStatus());
        assertEquals(passenger, seat.getPassenger());
        assertTrue(seat.isBooked());
        
        // Test constructor with null passenger (should be available)
        Seat seat2 = new Seat("14B", null);
        assertEquals(SeatStatus.AVAILABLE, seat2.getStatus());
        assertNull(seat2.getPassenger());
        assertFalse(seat2.isBooked());
    }
    
    @Test
    public void testSetPassenger() {
        Seat seat = new Seat("12A");
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        
        // Initially seat should be available
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertFalse(seat.isBooked());
        
        // Set passenger, seat should be booked
        seat.setPassenger(passenger);
        assertEquals(SeatStatus.BOOKED, seat.getStatus());
        assertEquals(passenger, seat.getPassenger());
        assertTrue(seat.isBooked());
        
        // Set null passenger, seat should be available
        seat.setPassenger(null);
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertNull(seat.getPassenger());
        assertFalse(seat.isBooked());
        
        // Test setting a different passenger
        Passenger passenger2 = new Passenger("Jane", "Smith", "1985-05-15");
        seat.setPassenger(passenger);
        seat.setPassenger(passenger2);
        assertEquals(passenger2, seat.getPassenger());
    }
    
    @Test
    public void testClearPassenger() {
        Seat seat = new Seat("12A");
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        
        // Book the seat
        seat.setPassenger(passenger);
        assertTrue(seat.isBooked());
        
        // Clear the passenger
        seat.clearPassenger();
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertNull(seat.getPassenger());
        assertFalse(seat.isBooked());
    }
    
    @Test
    public void testSetStatus() {
        Seat seat = new Seat("12A");
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        
        // Set status to BOOKED (doesn't set passenger automatically)
        seat.setStatus(SeatStatus.BOOKED);
        assertEquals(SeatStatus.BOOKED, seat.getStatus());
        assertNull(seat.getPassenger());
        assertTrue(seat.isBooked());
        
        // Set passenger
        seat.setPassenger(passenger);
        assertEquals(passenger, seat.getPassenger());
        
        // Set status to AVAILABLE (should clear passenger)
        seat.setStatus(SeatStatus.AVAILABLE);
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertNull(seat.getPassenger());
        assertFalse(seat.isBooked());
        
        // Test with null status (should have no effect)
        seat.setStatus(SeatStatus.BOOKED);
        seat.setStatus(null);
        assertEquals(SeatStatus.BOOKED, seat.getStatus());
    }
    
    @Test
    public void testToString() {
        Seat seat1 = new Seat("12A");
        String expected1 = "Seat{12A, AVAILABLE}";
        assertEquals(expected1, seat1.toString());
        
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        Seat seat2 = new Seat("14B", passenger);
        String expected2 = "Seat{14B, BOOKED, John Doe}";
        assertEquals(expected2, seat2.toString());
    }
    
    @Test
    public void testEquals() {
        Seat seat1 = new Seat("12A");
        Seat seat2 = new Seat("12A");
        Seat seat3 = new Seat("12B");
        Seat seat4 = new Seat("12a"); // Testing case insensitivity
        
        // Test reflexivity
        assertEquals(seat1, seat1);
        
        // Test symmetry
        assertEquals(seat1, seat2);
        assertEquals(seat2, seat1);
        
        // Test inequality
        assertNotEquals(seat1, seat3);
        
        // Test case insensitivity
        assertEquals(seat1, seat4);
        
        // Test with null and different type
        assertNotEquals(seat1, null);
        assertNotEquals(seat1, "12A");
        
        // Test with same seat number but different passenger/status
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        Seat seat5 = new Seat("12A", passenger);
        assertEquals(seat1, seat5); // Should be equal since only seat number matters
    }
    
    @Test
    public void testHashCode() {
        Seat seat1 = new Seat("12A");
        Seat seat2 = new Seat("12A");
        Seat seat3 = new Seat("12a"); // Testing case insensitivity
        
        assertEquals(seat1.hashCode(), seat2.hashCode());
        assertEquals(seat1.hashCode(), seat3.hashCode()); // Case insensitive
    }
}