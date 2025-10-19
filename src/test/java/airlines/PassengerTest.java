package airlines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;

public class PassengerTest {

    @Test
    public void testConstructor() {
        // Test normal constructor
        Passenger p1 = new Passenger("John", "Doe", "1990-01-01");
        assertEquals("John", p1.getFirstName());
        assertEquals("Doe", p1.getLastName());
        assertEquals("1990-01-01", p1.getDateOfBirth());
        
        // Test with null values (should convert to empty strings)
        Passenger p2 = new Passenger(null, null, null);
        assertEquals("", p2.getFirstName());
        assertEquals("", p2.getLastName());
        assertEquals("", p2.getDateOfBirth());
        
        // Test with spaces (should trim)
        Passenger p3 = new Passenger(" Jane ", " Smith ", " 1985-05-15 ");
        assertEquals("Jane", p3.getFirstName());
        assertEquals("Smith", p3.getLastName());
        assertEquals("1985-05-15", p3.getDateOfBirth());
    }
    
    @Test
    public void testSetters() {
        Passenger p = new Passenger("John", "Doe", "1990-01-01");
        
        // Test setting normal values
        p.setFirstName("Jane");
        p.setLastName("Smith");
        p.setDateOfBirth("1985-05-15");
        
        assertEquals("Jane", p.getFirstName());
        assertEquals("Smith", p.getLastName());
        assertEquals("1985-05-15", p.getDateOfBirth());
        
        // Test setting null values (should convert to empty strings)
        p.setFirstName(null);
        p.setLastName(null);
        p.setDateOfBirth(null);
        
        assertEquals("", p.getFirstName());
        assertEquals("", p.getLastName());
        assertEquals("", p.getDateOfBirth());
        
        // Test with spaces (should trim)
        p.setFirstName(" Robert ");
        p.setLastName(" Johnson ");
        p.setDateOfBirth(" 1970-12-31 ");
        
        assertEquals("Robert", p.getFirstName());
        assertEquals("Johnson", p.getLastName());
        assertEquals("1970-12-31", p.getDateOfBirth());
    }
    
    @Test
    public void testGetFullName() {
        // Test normal case
        Passenger p1 = new Passenger("John", "Doe", "1990-01-01");
        assertEquals("John Doe", p1.getFullName());
        
        // Test first name only
        Passenger p2 = new Passenger("John", "", "1990-01-01");
        assertEquals("John", p2.getFullName());
        
        // Test last name only
        Passenger p3 = new Passenger("", "Doe", "1990-01-01");
        assertEquals("Doe", p3.getFullName());
        
        // Test both empty
        Passenger p4 = new Passenger("", "", "1990-01-01");
        assertEquals("", p4.getFullName());
        
        // Test both null
        Passenger p5 = new Passenger(null, null, "1990-01-01");
        assertEquals("", p5.getFullName());
    }
    
    @Test
    public void testToString() {
        Passenger p = new Passenger("John", "Doe", "1990-01-01");
        String expected = "Passenger{John Doe, dob=1990-01-01}";
        assertEquals(expected, p.toString());
    }
    
    @Test
    public void testEquals() {
        Passenger p1 = new Passenger("John", "Doe", "1990-01-01");
        Passenger p2 = new Passenger("John", "Doe", "1990-01-01");
        Passenger p3 = new Passenger("Jane", "Doe", "1990-01-01");
        Passenger p4 = new Passenger("John", "Smith", "1990-01-01");
        Passenger p5 = new Passenger("John", "Doe", "1985-05-15");
        
        // Test reflexivity
        assertEquals(p1, p1);
        
        // Test symmetry
        assertEquals(p1, p2);
        assertEquals(p2, p1);
        
        // Test inequality
        assertNotEquals(p1, p3);
        assertNotEquals(p1, p4);
        assertNotEquals(p1, p5);
        
        // Test with null and different type
        assertNotEquals(p1, null);
        assertNotEquals(p1, "John Doe");
    }
    
    @Test
    public void testHashCode() {
        Passenger p1 = new Passenger("John", "Doe", "1990-01-01");
        Passenger p2 = new Passenger("John", "Doe", "1990-01-01");
        
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}