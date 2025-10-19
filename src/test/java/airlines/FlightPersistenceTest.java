package airlines;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FlightPersistenceTest {

    @TempDir
    Path tempDir;

    private DatabaseService db;
    private String dbFile;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        dbFile = tempDir.resolve("database.txt").toString(); // DatabaseService expects a String path
        db = new DatabaseService(dbFile);
        db.load(); // prepopulates 2 default flights
    }

    @Test
    void addFlight_uniqueId_increasesCount() {
        int before = sizeSafe(db.getFlights());

        boolean ok = db.addFlight("T100", "NU123", 2, 2, new char[]{'A','B'});
        assertTrue(ok, "addFlight should return true for a new flightId");

        int after = sizeSafe(db.getFlights());
        assertEquals(before + 1, after, "Flight count should increase by 1");
    }

    @Test
    void addFlight_duplicateId_isRejectedAndSizeUnchanged() {
        int before = sizeSafe(db.getFlights());

        boolean first = db.addFlight("T200", "NU200", 3, 3, new char[]{'A','B','C'});
        assertTrue(first, "First insert with id T200 should succeed");

        int afterOnce = sizeSafe(db.getFlights());

        // same ID, different number—should be rejected (service checks duplicate flightId)
        boolean second = db.addFlight("T200", "NU201", 4, 4, new char[]{'A','B','C','D'});
        assertFalse(second, "Duplicate flightId should be rejected (addFlight returns false)");

        int afterTwice = sizeSafe(db.getFlights());
        assertEquals(afterOnce, afterTwice, "Size should remain unchanged after duplicate insert");
        assertEquals(before + 1, afterTwice, "Exactly one new flight should exist");
    }

    private static int sizeSafe(List<?> list) {
        return (list == null) ? 0 : list.size();
    }
}
