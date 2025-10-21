package airlines;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * FileStorage - Handles reading and initializing the local CSV database.
 *
 * V1 header:
 *   # NUA-DB v1 | flightId,flightNumber,seatNumber,status,firstName,lastName,dateOfBirth
 *
 * Row columns (7):
 *   flightId,flightNumber,seatNumber,status,firstName,lastName,dateOfBirth
 */
public final class FileStorage {

    private static final String V1_HEADER =
            "# NUA-DB v1 | flightId,flightNumber,seatNumber,status,firstName,lastName,dateOfBirth";

    private FileStorage() {}

    /**
     * Reads the local database file. If it doesn't exist, auto-creates it with default
     * sample flights and returns those. Accepts legacy files (no header or old header),
     * CRLF/LF line endings, and UTF-8 BOM. Skips malformed rows safely.
     */
    public static List<Flight> read(String path) {
        Path p = Path.of(path);

        // If file does not exist, create with defaults (v1 header) and return them.
        if (!Files.exists(p)) {
            System.out.println("[FileStorage] " + path + " not found. Creating default database...");
            List<Flight> defaults = defaultFlights();
            try {
                write(p.toString(), defaults);
            } catch (IOException e) {
                System.out.println("[FileStorage] Error creating default file: " + e.getMessage());
            }
            return defaults;
        }

        Map<String, Flight> flightsById = new LinkedHashMap<>();

        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                // Strip UTF-8 BOM if present on the very first line
                if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
                    line = line.substring(1);
                }

                String raw = line;
                if (raw.isBlank() || raw.startsWith("#")) {
                    // Accept any comment/header line (legacy or v1). We don't parse version for logic.
                    continue;
                }

                String[] cols = raw.split(",", -1); // keep empty fields
                if (cols.length != 7) {
                    System.out.println("[FileStorage] Skipping malformed row (wrong column count): " + raw);
                    continue;
                }

                String flightId     = cols[0].trim();
                String flightNumber = cols[1].trim();
                String seatNumber   = cols[2].trim();
                String statusStr    = cols[3].trim().toUpperCase(Locale.ROOT);
                String firstName    = cols[4].trim();
                String lastName     = cols[5].trim();
                String dob          = cols[6].trim();

                // Required fields
                if (flightId.isEmpty() || flightNumber.isEmpty() || seatNumber.isEmpty()) {
                    System.out.println("[FileStorage] Skipping malformed row (missing required fields): " + raw);
                    continue;
                }
                // Status must be one of the two
                if (!"AVAILABLE".equals(statusStr) && !"BOOKED".equals(statusStr)) {
                    System.out.println("[FileStorage] Skipping malformed row (invalid status): " + raw);
                    continue;
                }

                Flight flight = flightsById.computeIfAbsent(flightId, id -> new Flight(id, flightNumber));
                Seat seat = "BOOKED".equals(statusStr)
                        ? new Seat(seatNumber, new Passenger(firstName, lastName, dob))
                        : new Seat(seatNumber);
                flight.addSeat(seat);
            }
        } catch (IOException e) {
            System.out.println("[FileStorage] Error reading file, using defaults: " + e.getMessage());
            return defaultFlights();
        }

        if (flightsById.isEmpty()) {
            System.out.println("[FileStorage] File empty or invalid. Rebuilding with defaults.");
            List<Flight> defaults = defaultFlights();
            try {
                write(p.toString(), defaults); // also normalizes header to v1
            } catch (IOException e) {
                System.out.println("[FileStorage] Could not rebuild file: " + e.getMessage());
            }
            return defaults;
        }

        System.out.println("[FileStorage] Loaded " + flightsById.size() + " flights from " + path);
        return new ArrayList<>(flightsById.values());
    }

    /**
     * Writes flights to the given CSV path in canonical v1 format (UTF-8, LF).
     * Any legacy file read earlier will be upgraded to v1 on the next write.
     */
    public static void write(String path, List<Flight> flights) throws IOException {
        List<String> lines = new ArrayList<>(1 + Math.max(0, flights.size() * 8));
        lines.add(V1_HEADER);

        for (Flight f : flights) {
            for (Seat s : f.getSeats()) {
                Passenger p = s.getPassenger();
                String first = p != null ? p.getFirstName() : "";
                String last  = p != null ? p.getLastName()  : "";
                String dob   = p != null ? p.getDateOfBirth(): "";
                lines.add(String.join(",",
                        f.getId(),
                        f.getFlightNumber(),
                        s.getSeatNumber(),
                        s.getStatus().name(),
                        first, last, dob));
            }
        }

        // Always write UTF-8; Files.write preserves LF when writing strings.
        Files.write(Path.of(path), lines, StandardCharsets.UTF_8);
        System.out.println("[FileStorage] Saved " + flights.size() + " flights to " + path + " (v1)");
    }

    /** Default dataset used when file is missing or invalid. */
    private static List<Flight> defaultFlights() {
        List<Flight> flights = new ArrayList<>();

        Flight f1 = new Flight("F001", "NU100");
        addSeats(f1, 1, 5, new char[]{'A','B','C','D','E','F'});

        Flight f2 = new Flight("F002", "NU245");
        addSeats(f2, 1, 4, new char[]{'A','B','C','D'});

        flights.add(f1);
        flights.add(f2);
        return flights;
    }

    private static void addSeats(Flight flight, int startRow, int endRow, char[] letters) {
        for (int row = startRow; row <= endRow; row++) {
            for (char c : letters) {
                flight.addSeat(new Seat(row + String.valueOf(c)));
            }
        }
    }
}
