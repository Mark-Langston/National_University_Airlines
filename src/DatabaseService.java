import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DatabaseService 10-12-2025
 * - Owns in-memory flights list
 * - Loads from local file on startup (auto-creates if missing)
 * - Provides read APIs for UI
 * - Persists changes (autosave) after updates
 * - NEW: addFlight(...) and deleteFlight(...) for flight management
 */
public class DatabaseService {

    private final String dbPath;
    private final List<Flight> flights = new ArrayList<>();

    /** Loads data from dbPath; auto-creates file if missing (via FileStorage.read). */
    public DatabaseService(String dbPath) {
        this.dbPath = dbPath == null ? "database.txt" : dbPath;
        load(); // autoload on construction
    }

    /** Re-load from disk, replacing in-memory flights. */
    public final void load() {
        flights.clear();
        flights.addAll(FileStorage.read(dbPath));
    }

    /** Expose read-only list for UI binding. */
    public List<Flight> getFlights() {
        return Collections.unmodifiableList(flights);
    }

    /** Seats for a specific flight (read-only list); empty list if not found. */
    public List<Seat> getSeats(String flightId) {
        Flight f = findFlight(flightId);
        return f == null ? List.of() : f.getSeats(); // Flight#getSeats
    }

    /**
     * Update (book or change) a seat's passenger.
     * - passenger == null -> releases the seat (AVAILABLE)
     * - otherwise -> BOOKED with provided passenger
     * Autosaves after successful update.
     * @return true if flight+seat found and updated
     */
    public boolean updateSeat(String flightId, String seatNumber, Passenger passenger) {
        Flight f = findFlight(flightId);
        if (f == null) return false;
        Seat seat = f.getSeat(seatNumber);
        if (seat == null) return false;

        if (passenger == null) {
            seat.clearPassenger();           // AVAILABLE
        } else {
            seat.setPassenger(passenger);    // BOOKED
        }
        save(); // AUTOSAVE
        return true;
    }

    /** Convenience wrappers */
    public boolean bookSeat(String flightId, String seatNumber, Passenger passenger) {
        return updateSeat(flightId, seatNumber, passenger);
    }
    public boolean releaseSeat(String flightId, String seatNumber) {
        return updateSeat(flightId, seatNumber, null);
    }

    /** Persist current flights to disk. */
    public boolean save() {
        try {
            FileStorage.write(dbPath, flights);
            return true;
        } catch (IOException e) {
            System.out.println("[DatabaseService] Save failed: " + e.getMessage());
            return false;
        }
    }

    // =====================================================================
    //                          NEW IN ISSUE #16
    // =====================================================================

    /**
     * Add a new flight and generate seats.
     *
     * @param flightId unique id (e.g., "F003")
     * @param flightNumber human-readable number (e.g., "NU310")
     * @param startRow first row index (>=1)
     * @param endRow last row index (>= startRow)
     * @param seatLetters array of seat letters (e.g., {'A','B','C','D','E','F'})
     * @return true if added and saved; false if validation fails or duplicate id
     */
    public synchronized boolean addFlight(String flightId, String flightNumber,
                                          int startRow, int endRow, char[] seatLetters) {
        // Validation
        if (isBlank(flightId) || isBlank(flightNumber)) {
            System.out.println("[DatabaseService] addFlight: missing id/number");
            return false;
        }
        if (findFlight(flightId) != null) {
            System.out.println("[DatabaseService] addFlight: duplicate flightId " + flightId);
            return false;
        }
        if (startRow < 1 || endRow < startRow) {
            System.out.println("[DatabaseService] addFlight: invalid row range " + startRow + ".." + endRow);
            return false;
        }
        if (seatLetters == null || seatLetters.length == 0) {
            System.out.println("[DatabaseService] addFlight: no seat letters provided");
            return false;
        }
        // Limiter so someone doesn't make 10k seats by accident
        if ((long)(endRow - startRow + 1) * (long)seatLetters.length > 5000) {
            System.out.println("[DatabaseService] addFlight: too many seats requested");
            return false;
        }

        Flight flight = new Flight(flightId, flightNumber);
        generateSeats(flight, startRow, endRow, seatLetters);
        flights.add(flight);
        return save();
    }

    /**
     * Delete a flight by id (and all its seats).
     * @return true if removed and saved; false if not found
     */
    public synchronized boolean deleteFlight(String flightId) {
        Flight f = findFlight(flightId);
        if (f == null) {
            System.out.println("[DatabaseService] deleteFlight: not found " + flightId);
            return false;
        }
        flights.remove(f);
        return save();
    }

    // ---------- helpers ----------
    private Flight findFlight(String flightId) {
        if (flightId == null) return null;
        for (Flight f : flights) {
            if (flightId.equalsIgnoreCase(f.getId())) return f;
        }
        return null;
    }

    private void generateSeats(Flight flight, int startRow, int endRow, char[] seatLetters) {
        for (int row = startRow; row <= endRow; row++) {
            for (char c : seatLetters) {
                // Ensure letter is uppercase and valid
                char letter = Character.toUpperCase(c);
                String seatNum = row + String.valueOf(letter);
                flight.addSeat(new Seat(seatNum));
            }
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
