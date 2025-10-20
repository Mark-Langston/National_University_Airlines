import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ValidationUtils {
    private ValidationUtils() {}

    /** Non-empty, trims, allows letters, spaces, hyphens, apostrophes. */
    public static boolean isValidName(String s) {
        if (s == null) return false;
        String t = s.trim();
        if (t.isEmpty()) return false;
        return t.matches("[A-Za-z\\p{L}][A-Za-z\\p{L}'\\- ]*");
    }

    /** yyyy-MM-dd, real calendar date, not in the future. */
    public static boolean isValidDobIso(String s) {
        if (s == null || !s.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        try {
            LocalDate d = LocalDate.parse(s);
            return !d.isAfter(LocalDate.now());
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    // --------------------------------------------------------------------
    // Flight creation validation
    // --------------------------------------------------------------------

    /** Flight ID: required, alphanumeric + dashes allowed (e.g., F003, INTL-01). */
    public static boolean isValidFlightId(String id) {
        if (id == null) return false;
        String t = id.trim();
        if (t.isEmpty()) return false;
        return t.matches("[A-Za-z0-9\\-]+");
    }

    /** Flight number: recommended simple rule "NU" + digits (e.g., NU310). */
    public static boolean isValidFlightNumber(String num) {
        if (num == null) return false;
        String t = num.trim();
        if (t.isEmpty()) return false;
        return t.matches("NU\\d+");
    }

    /** Row range: start >= 1, end >= start (<= 200 rows). */
    public static boolean isValidRowRange(int startRow, int endRow) {
        if (startRow < 1) return false;
        if (endRow < startRow) return false;
        return (endRow - startRow) <= 200;
    }

    /**
     * Seat letters: 1–10 uppercase letters, unique (e.g., ABCDEF).
     * Returns a unique ordered set of uppercase letters, or empty set if invalid.
     */
    public static Set<Character> parseSeatLettersUnique(String letters) {
        Set<Character> out = new LinkedHashSet<>();
        if (letters == null) return out;
        String t = letters.trim().toUpperCase();
        if (t.isEmpty() || t.length() > 10) return out;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c < 'A' || c > 'Z') return new LinkedHashSet<>();
            out.add(c);
        }
        // ensure no duplicates were collapsed
        if (out.size() != t.length()) return new LinkedHashSet<>();
        return out;
    }
}
