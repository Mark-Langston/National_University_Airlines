package airlines;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileStorageCompatibilityTest {

    @TempDir
    Path tempDir;

    @Test
    void readsLegacyHeaderAndWritesV1() throws IOException {
        Path db = tempDir.resolve("db.txt");
        List<String> legacy = List.of(
            "# flightId,flightNumber,seatNumber,status,firstName,lastName,dateOfBirth",
            "F300,NU999,1A,AVAILABLE,,,"
        );
        Files.write(db, legacy, StandardCharsets.UTF_8);

        var flights = FileStorage.read(db.toString());
        assertEquals(1, flights.size());
        assertEquals("F300", flights.get(0).getId());

        // Trigger write & check header is now v1
        FileStorage.write(db.toString(), flights);
        String first = Files.readAllLines(db, StandardCharsets.UTF_8).get(0);
        assertTrue(first.startsWith("# NUA-DB v1 | "), "should be v1 header");
    }

    @Test
    void readsNoHeaderFile() throws IOException {
        Path db = tempDir.resolve("noheader.txt");
        List<String> rows = List.of(
            "F100,NU777,1A,AVAILABLE,,,",
            "F100,NU777,1B,BOOKED,John,Doe,1990-01-01"
        );
        Files.write(db, rows, StandardCharsets.UTF_8);

        var flights = FileStorage.read(db.toString());
        assertEquals(1, flights.size());
        assertEquals(2, flights.get(0).getSeats().size());
    }

    @Test
    void acceptsCRLFandBOM() throws IOException {
        Path db = tempDir.resolve("crlf_bom.txt");

        // Use text block for readability (Java 15+)
        String content = """
            # flightId,flightNumber,seatNumber,status,firstName,lastName,dateOfBirth\r
            F200,NU888,10C,AVAILABLE,,,\r
            """;

        // Write with BOM + CRLF
        byte[] bom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        Files.write(db, concat(bom, content.getBytes(StandardCharsets.UTF_8)));

        var flights = FileStorage.read(db.toString());
        assertEquals(1, flights.size());
        assertEquals("F200", flights.get(0).getId());
    }

    @Test
    void skipsMalformedRowsAndLoadsValid() throws IOException {
        Path db = tempDir.resolve("mixed.txt");
        List<String> rows = List.of(
            "# any header",
            "F100,NU777",                 // wrong columns
            "F100,NU777,1A,INVALID,,,",  // invalid status
            "",                           // blank
            "F100,NU777,1B,AVAILABLE,,," // valid
        );
        Files.write(db, rows, StandardCharsets.UTF_8);

        var flights = FileStorage.read(db.toString());
        assertEquals(1, flights.size());
        assertEquals(1, flights.get(0).getSeats().size());
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
