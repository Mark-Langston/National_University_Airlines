import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * FileStorage - Handles reading and initializing the local CSV database.
 *
 * CSV columns:
 * flightId,flightNumber,seatNumber,status,firstName,lastName,dateOfBirth
 */
public final class FileStorage {

    private FileStorage() {}

    /**
     * Reads the local database file. If it doesn't exist,
     * auto-creates it with default sample flights and returns those.
     */
    public static List<Flight> read(String path) {
        Path p = Path.of(path);

        // 1️⃣ If file does not exist, create with defaults
        if (!Files.exists(p)) {
            System.out.println("[FileStorage] " + path + " not found. Creating default database...");
            List<Flight> defaults = defaultFlights();
            try {
                write(p.toString(), defaults); // create the file
            } catch (IOException e) {
                System.out.println("[FileStorage] Error creating default file: " + e.getMessage());
            }
            return defaults;
        }

        // 2️⃣ Otherwise, load from file
        Map<String, Flight> flightsById = new LinkedHashMap<>();
        try {
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            for (String raw : lines) {
                if (raw == null || raw.isBlank() || raw.startsWith("#")) continue;

                String[] cols = raw.split(",", -1);
                if (cols.length < 7) {
                    System.out.println("[FileStorage] Skipping malformed row: " + raw);
                    continue;
                }

                String flightId = cols[0].trim();
                String flightNumber = cols[1].trim();
                String seatNumber = cols[2].trim();
                String statusStr = cols[3].trim().toUpperCase(Locale.ROOT);
                String firstName = cols[4].trim();
                String lastName = cols[5].trim();
                String dob = cols[6].trim();

                if (flightId.isEmpty() || flightNumber.isEmpty() || seatNumber.isEmpty()) continue;

                Flight flight = flightsById.computeIfAbsent(flightId, id -> new Flight(id, flightNumber));

                Seat seat;
                if (statusStr.equals("BOOKED")) {
                    Passenger passenger = new Passenger(firstName, lastName, dob);
                    seat = new Seat(seatNumber, passenger);
                } else {
                    seat = new Seat(seatNumber);
                }
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
                write(p.toString(), defaults);
            } catch (IOException e) {
                System.out.println("[FileStorage] Could not rebuild file: " + e.getMessage());
            }
            return defaults;
        }

        System.out.println("[FileStorage] Loaded " + flightsById.size() + " flights from " + path);
        return new ArrayList<>(flightsById.values());
    }

    /**
     * Writes flights to the given CSV path.
     */
    public static void write(String path, List<Flight> flights) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# flightId,flightNumber,seatNumber,status,firstName,lastName,dateOfBirth");

        for (Flight f : flights) {
            for (Seat s : f.getSeats()) {
                Passenger p = s.getPassenger();
                String first = p != null ? p.getFirstName() : "";
                String last = p != null ? p.getLastName() : "";
                String dob = p != null ? p.getDateOfBirth() : "";
                lines.add(String.join(",", f.getId(), f.getFlightNumber(),
                        s.getSeatNumber(), s.getStatus().name(), first, last, dob));
            }
        }

        Files.write(Path.of(path), lines, StandardCharsets.UTF_8);
        System.out.println("[FileStorage] Saved " + flights.size() + " flights to " + path);
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
