package airlines;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ValidationUtilsTest {

    @Test
    public void testIsValidName() {
        // Valid names
        assertTrue(ValidationUtils.isValidName("John"));
        assertTrue(ValidationUtils.isValidName("Mary"));
        assertTrue(ValidationUtils.isValidName("John Doe"));
        assertTrue(ValidationUtils.isValidName("Mary-Jane"));
        assertTrue(ValidationUtils.isValidName("O'Connor"));
        assertTrue(ValidationUtils.isValidName("José María"));
        
        // Invalid names
        assertFalse(ValidationUtils.isValidName(null));
        assertFalse(ValidationUtils.isValidName(""));
        assertFalse(ValidationUtils.isValidName("  "));
        assertFalse(ValidationUtils.isValidName("123"));
        assertFalse(ValidationUtils.isValidName("John123"));
        assertFalse(ValidationUtils.isValidName("John@Doe"));
        
        // Edge cases
        assertFalse(ValidationUtils.isValidName(" 123"));
        assertFalse(ValidationUtils.isValidName("123 "));
        assertTrue(ValidationUtils.isValidName(" John ")); // Should trim
    }
    
    @Test
    public void testIsValidDobIso() {
        // Valid dates
        assertTrue(ValidationUtils.isValidDobIso("1990-01-01"));
        assertTrue(ValidationUtils.isValidDobIso("2000-12-31"));
        assertTrue(ValidationUtils.isValidDobIso("1900-01-01"));
        
        // Invalid dates
        assertFalse(ValidationUtils.isValidDobIso(null));
        assertFalse(ValidationUtils.isValidDobIso(""));
        assertFalse(ValidationUtils.isValidDobIso("01/01/1990")); // Wrong format
        assertFalse(ValidationUtils.isValidDobIso("1990/01/01")); // Wrong format
        assertFalse(ValidationUtils.isValidDobIso("1990-13-01")); // Invalid month
        assertFalse(ValidationUtils.isValidDobIso("1990-01-32")); // Invalid day
        assertFalse(ValidationUtils.isValidDobIso("abcd-ef-gh")); // Not a date
        
        // Future dates should be invalid
        String futureYear = (java.time.LocalDate.now().getYear() + 10) + "-01-01";
        assertFalse(ValidationUtils.isValidDobIso(futureYear));
    }
    
    @Test
    public void testIsValidFlightId() {
        // Valid flight IDs
        assertTrue(ValidationUtils.isValidFlightId("F001"));
        assertTrue(ValidationUtils.isValidFlightId("ABC123"));
        assertTrue(ValidationUtils.isValidFlightId("INTL-01"));
        assertTrue(ValidationUtils.isValidFlightId("F-123-456"));
        
        // Invalid flight IDs
        assertFalse(ValidationUtils.isValidFlightId(null));
        assertFalse(ValidationUtils.isValidFlightId(""));
        assertFalse(ValidationUtils.isValidFlightId("  "));
        assertFalse(ValidationUtils.isValidFlightId("F@001"));
        assertFalse(ValidationUtils.isValidFlightId("F.001"));
        assertFalse(ValidationUtils.isValidFlightId("F 001"));
        
        // Edge cases
        assertTrue(ValidationUtils.isValidFlightId("F001 ")); // Should trim
        assertTrue(ValidationUtils.isValidFlightId(" F001")); // Should trim
    }
    
    @Test
    public void testIsValidFlightNumber() {
        // Valid flight numbers
        assertTrue(ValidationUtils.isValidFlightNumber("NU123"));
        assertTrue(ValidationUtils.isValidFlightNumber("NU1"));
        assertTrue(ValidationUtils.isValidFlightNumber("NU9999"));
        
        // Invalid flight numbers
        assertFalse(ValidationUtils.isValidFlightNumber(null));
        assertFalse(ValidationUtils.isValidFlightNumber(""));
        assertFalse(ValidationUtils.isValidFlightNumber("  "));
        assertFalse(ValidationUtils.isValidFlightNumber("123"));
        assertFalse(ValidationUtils.isValidFlightNumber("AA123"));
        assertFalse(ValidationUtils.isValidFlightNumber("NUA123"));
        assertFalse(ValidationUtils.isValidFlightNumber("NU-123"));
        assertFalse(ValidationUtils.isValidFlightNumber("NU 123"));
        
        // Edge cases
        assertTrue(ValidationUtils.isValidFlightNumber("NU123 ")); // Should trim
        assertTrue(ValidationUtils.isValidFlightNumber(" NU123")); // Should trim
    }
    
    @Test
    public void testIsValidRowRange() {
        // Valid row ranges
        assertTrue(ValidationUtils.isValidRowRange(1, 1));
        assertTrue(ValidationUtils.isValidRowRange(1, 10));
        assertTrue(ValidationUtils.isValidRowRange(5, 10));
        assertTrue(ValidationUtils.isValidRowRange(1, 200));
        
        // Invalid row ranges
        assertFalse(ValidationUtils.isValidRowRange(0, 10)); // startRow < 1
        assertFalse(ValidationUtils.isValidRowRange(-1, 10)); // startRow < 1
        assertFalse(ValidationUtils.isValidRowRange(10, 5)); // endRow < startRow
        assertFalse(ValidationUtils.isValidRowRange(1, 202)); // > 200 rows
    }
    
    @Test
    public void testParseSeatLettersUnique() {
        // Valid seat letters
        Set<Character> seats1 = ValidationUtils.parseSeatLettersUnique("ABCDEF");
        assertEquals(6, seats1.size());
        assertTrue(seats1.contains('A'));
        assertTrue(seats1.contains('F'));
        
        Set<Character> seats2 = ValidationUtils.parseSeatLettersUnique("A");
        assertEquals(1, seats2.size());
        assertTrue(seats2.contains('A'));
        
        // Should convert to uppercase
        Set<Character> seats3 = ValidationUtils.parseSeatLettersUnique("abcdef");
        assertEquals(6, seats3.size());
        assertTrue(seats3.contains('A'));
        assertTrue(seats3.contains('F'));
        
        // Invalid seat letters
        assertTrue(ValidationUtils.parseSeatLettersUnique(null).isEmpty());
        assertTrue(ValidationUtils.parseSeatLettersUnique("").isEmpty());
        assertTrue(ValidationUtils.parseSeatLettersUnique("  ").isEmpty());
        assertTrue(ValidationUtils.parseSeatLettersUnique("A1B2").isEmpty()); // Contains numbers
        assertTrue(ValidationUtils.parseSeatLettersUnique("A B").isEmpty()); // Contains spaces
        assertTrue(ValidationUtils.parseSeatLettersUnique("ABCDEFGHIJK").isEmpty()); // > 10 letters
        
        // Duplicate letters
        assertTrue(ValidationUtils.parseSeatLettersUnique("AABCDE").isEmpty());
        
        // Edge cases
        Set<Character> seats4 = ValidationUtils.parseSeatLettersUnique(" ABC ");
        assertEquals(3, seats4.size());
        assertTrue(seats4.contains('A'));
        assertTrue(seats4.contains('B'));
        assertTrue(seats4.contains('C'));
    }
}