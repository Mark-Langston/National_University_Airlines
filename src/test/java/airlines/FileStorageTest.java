package airlines;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    public void testReadNonExistentFile() {
        // Test reading from a file that doesn't exist
        String nonExistentFilePath = tempDir.resolve("nonexistent.txt").toString();
        List<Flight> flights = FileStorage.read(nonExistentFilePath);
        
        // Should create default flights
        assertNotNull(flights);
        assertEquals(2, flights.size());
        
        // Verify the file was created
        assertTrue(Files.exists(Paths.get(nonExistentFilePath)));
    }
    
    @Test
    public void testReadExistingValidFile() throws IOException {
        // Create a valid data file
        Path testFilePath = tempDir.resolve("testdata.txt");
        List<String> testData = Arrays.asList(
            "# flightId,flightNumber,seatNumber,status,firstName,lastName,dateOfBirth",
            "F100,NU777,1A,AVAILABLE,,,",
            "F100,NU777,1B,BOOKED,John,Doe,1990-01-01",
            "F200,NU888,10C,AVAILABLE,,,"
        );
        Files.write(testFilePath, testData, StandardCharsets.UTF_8);
        
        // Read the file
        List<Flight> flights = FileStorage.read(testFilePath.toString());
        
        // Verify contents
        assertNotNull(flights);
        assertEquals(2, flights.size());
        
        // Check F100 flight
        Flight flight1 = findFlightById(flights, "F100");
        assertNotNull(flight1);
        assertEquals("NU777", flight1.getFlightNumber());
        assertEquals(2, flight1.getSeats().size());
        
        // Check seats
        Seat seat1A = flight1.getSeat("1A");
        assertNotNull(seat1A);
        assertEquals(SeatStatus.AVAILABLE, seat1A.getStatus());
        assertNull(seat1A.getPassenger());
        
        Seat seat1B = flight1.getSeat("1B");
        assertNotNull(seat1B);
        assertEquals(SeatStatus.BOOKED, seat1B.getStatus());
        assertNotNull(seat1B.getPassenger());
        assertEquals("John", seat1B.getPassenger().getFirstName());
        assertEquals("Doe", seat1B.getPassenger().getLastName());
        assertEquals("1990-01-01", seat1B.getPassenger().getDateOfBirth());
        
        // Check F200 flight
        Flight flight2 = findFlightById(flights, "F200");
        assertNotNull(flight2);
        assertEquals("NU888", flight2.getFlightNumber());
        assertEquals(1, flight2.getSeats().size());
    }
    
    @Test
    public void testReadMalformedFile() throws IOException {
        // Create a malformed data file
        Path testFilePath = tempDir.resolve("malformed.txt");
        List<String> testData = Arrays.asList(
            "# flightId,flightNumber,seatNumber,status,firstName,lastName,dateOfBirth",
            "F100,NU777", // Missing columns
            "invalidline",
            "F200,NU888,10C,INVALID_STATUS,,,", // Invalid status
            "F300,NU999,1A,AVAILABLE,,," // Valid line
        );
        Files.write(testFilePath, testData, StandardCharsets.UTF_8);
        
        // Read the file
        List<Flight> flights = FileStorage.read(testFilePath.toString());
        
        // Should have skipped malformed lines and loaded valid line
        assertNotNull(flights);
        assertEquals(1, flights.size());
        
        Flight flight = flights.get(0);
        assertEquals("F300", flight.getId());
        assertEquals("NU999", flight.getFlightNumber());
    }
    
    @Test
    public void testReadEmptyFile() throws IOException {
        // Create an empty file
        Path testFilePath = tempDir.resolve("empty.txt");
        Files.createFile(testFilePath);
        
        // Read the file
        List<Flight> flights = FileStorage.read(testFilePath.toString());
        
        // Should return default flights
        assertNotNull(flights);
        assertEquals(2, flights.size());
    }
    
    @Test
    public void testWrite() throws IOException {
        // Create flights to write
        Flight flight1 = new Flight("F100", "NU777");
        flight1.addSeat(new Seat("1A"));
        
        Seat bookedSeat = new Seat("1B");
        Passenger passenger = new Passenger("John", "Doe", "1990-01-01");
        bookedSeat.setPassenger(passenger);
        flight1.addSeat(bookedSeat);
        
        Flight flight2 = new Flight("F200", "NU888");
        flight2.addSeat(new Seat("10C"));
        
        List<Flight> flights = Arrays.asList(flight1, flight2);
        
        // Write to file
        String outFilePath = tempDir.resolve("output.txt").toString();
        FileStorage.write(outFilePath, flights);
        
        // Verify file exists
        assertTrue(Files.exists(Paths.get(outFilePath)));
        
        // Read the file back
        List<String> lines = Files.readAllLines(Paths.get(outFilePath), StandardCharsets.UTF_8);
        
        // Check contents
        assertTrue(lines.size() >= 4); // Header + at least 3 data lines
        assertTrue(lines.get(0).startsWith("#")); // Header line
        
        // Check that all seats are included
        boolean foundF100_1A = false;
        boolean foundF100_1B = false;
        boolean foundF200_10C = false;
        
        for (String line : lines) {
            if (line.startsWith("F100,NU777,1A,AVAILABLE")) foundF100_1A = true;
            if (line.startsWith("F100,NU777,1B,BOOKED,John,Doe,1990-01-01")) foundF100_1B = true;
            if (line.startsWith("F200,NU888,10C,AVAILABLE")) foundF200_10C = true;
        }
        
        assertTrue(foundF100_1A);
        assertTrue(foundF100_1B);
        assertTrue(foundF200_10C);
    }
    
    @Test
    public void testDefaultFlights() {
        // Read from non-existent file to trigger default flight creation
        String nonExistentFilePath = tempDir.resolve("defaults.txt").toString();
        List<Flight> flights = FileStorage.read(nonExistentFilePath);
        
        // Check default flights
        assertNotNull(flights);
        assertEquals(2, flights.size());
        
        Flight flight1 = findFlightById(flights, "F001");
        assertNotNull(flight1);
        assertEquals("NU100", flight1.getFlightNumber());
        assertEquals(30, flight1.getSeats().size()); // 5 rows x 6 seats
        
        Flight flight2 = findFlightById(flights, "F002");
        assertNotNull(flight2);
        assertEquals("NU245", flight2.getFlightNumber());
        assertEquals(16, flight2.getSeats().size()); // 4 rows x 4 seats
    }
    
    private Flight findFlightById(List<Flight> flights, String id) {
        for (Flight flight : flights) {
            if (flight.getId().equals(id)) {
                return flight;
            }
        }
        return null;
    }
}