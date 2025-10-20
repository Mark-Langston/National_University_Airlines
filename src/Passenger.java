import java.util.Objects;

/**
 * dateOfBirth uses ISO string "yyyy-MM-dd" to avoid parsing challenges.
 */
public class Passenger {
    private String firstName;
    private String lastName;
    private String dateOfBirth; // ISO yyyy-MM-dd

    public Passenger(String firstName, String lastName, String dateOfBirth) {
        this.firstName = firstName == null ? "" : firstName.trim();
        this.lastName = lastName == null ? "" : lastName.trim();
        this.dateOfBirth = dateOfBirth == null ? "" : dateOfBirth.trim();
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName == null ? "" : firstName.trim(); }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName == null ? "" : lastName.trim(); }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth == null ? "" : dateOfBirth.trim(); }

    public String getFullName() {
        String fn = firstName == null ? "" : firstName.trim();
        String ln = lastName == null ? "" : lastName.trim();
        if (fn.isEmpty() && ln.isEmpty()) return "";
        if (fn.isEmpty()) return ln;
        if (ln.isEmpty()) return fn;
        return fn + " " + ln;
    }

    @Override
    public String toString() {
        return "Passenger{" + getFullName() + ", dob=" + dateOfBirth + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Passenger)) return false;
        Passenger that = (Passenger) o;
        return Objects.equals(firstName, that.firstName)
                && Objects.equals(lastName, that.lastName)
                && Objects.equals(dateOfBirth, that.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, dateOfBirth);
    }
}
