import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * SeatEditorDialog – Validation Added 10-12-2025
 * - Names must be non-empty (when booking) and pass a simple name rule.
 * - DOB must be yyyy-MM-dd, real date, and not in the future.
 * - Clear visual feedback: invalid fields are highlighted and focused.
 * - Release checkbox is never pre-selected; user opts in.
 */
public class SeatEditorDialog extends JDialog {
    private final DatabaseService db;
    private final String flightId;
    private final String flightNumber;
    private final String seatNumber;

    // UI fields
    private final JTextField firstField = new JTextField(18);
    private final JTextField lastField  = new JTextField(18);
    private final JTextField dobField   = new JTextField(10); // yyyy-MM-dd

    private final JCheckBox releaseCheck = new JCheckBox("Mark seat AVAILABLE (release booking)");

    private final JButton saveBtn   = new JButton("Save");
    private final JButton cancelBtn = new JButton("Cancel");

    // colors for validation feedback
    private final Color normalBg;
    private final Color invalidBg = new Color(255, 230, 230);

    public SeatEditorDialog(Frame owner, DatabaseService db, String flightId, String flightNumber, String seatNumber) {
        super(owner, "Edit Seat – " + seatNumber + " / " + flightNumber, true);
        this.db = db;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.seatNumber = seatNumber;

        // stash default bg color before we ever mutate
        normalBg = firstField.getBackground();

        initComponents();
        loadExistingValues();
        setSize(520, 300);
        setLocationRelativeTo(owner);
        wireKeyShortcuts();
    }

    private void initComponents() {
        // Header
        JLabel header = new JLabel("Flight " + flightNumber + " (" + flightId + ") – Seat " + seatNumber, SwingConstants.CENTER);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));

        // Tooltips
        firstField.setToolTipText("First name (letters, spaces, - and ' allowed)");
        lastField.setToolTipText("Last name (letters, spaces, - and ' allowed)");
        dobField.setToolTipText("Date of Birth in yyyy-MM-dd (e.g., 1990-05-04, not in the future)");

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.insets = new Insets(6, 4, 6, 8); gc.anchor = GridBagConstraints.LINE_END;
        form.add(new JLabel("First Name:"), gc);
        gc.gridy++;
        form.add(new JLabel("Last Name:"), gc);
        gc.gridy++;
        form.add(new JLabel("Date of Birth:"), gc);

        gc.gridx = 1; gc.gridy = 0; gc.anchor = GridBagConstraints.LINE_START;
        form.add(firstField, gc);
        gc.gridy++;
        form.add(lastField, gc);
        gc.gridy++;
        JPanel dobRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dobRow.add(dobField);
        dobRow.add(new JLabel("   (yyyy-MM-dd)"));
        form.add(dobRow, gc);

        // Release checkbox
        JPanel releasePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        releasePanel.add(releaseCheck);
        releaseCheck.addActionListener(e -> toggleReleaseMode());

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> onSave());

        // Layout
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout());
        center.add(form, BorderLayout.CENTER);
        center.add(releasePanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void loadExistingValues() {
        List<Seat> seats = db.getSeats(flightId);
        for (Seat s : seats) {
            if (seatNumber.equalsIgnoreCase(s.getSeatNumber())) {
                if (s.getPassenger() != null) {
                    firstField.setText(s.getPassenger().getFirstName());
                    lastField.setText(s.getPassenger().getLastName());
                    dobField.setText(s.getPassenger().getDateOfBirth());
                } else {
                    firstField.setText("");
                    lastField.setText("");
                    dobField.setText("");
                }

                // Always start unchecked; user must intentionally release
                releaseCheck.setSelected(false);
                toggleReleaseMode();

                // Show status in title
                setTitle("Edit Seat – " + seatNumber + " / " + flightNumber + " [" + (s.isBooked() ? "BOOKED" : "AVAILABLE") + "]");
                break;
            }
        }
    }

    private void toggleReleaseMode() {
        boolean releasing = releaseCheck.isSelected();
        setFieldEnabled(firstField, !releasing);
        setFieldEnabled(lastField, !releasing);
        setFieldEnabled(dobField, !releasing);
        if (!releasing) clearInvalidHighlights(); // reset if user re-enables fields
    }

    private void setFieldEnabled(JTextField field, boolean enabled) {
        field.setEnabled(enabled);
        field.setBackground(enabled ? normalBg : UIManager.getColor("Panel.background"));
    }

    private void clearInvalidHighlights() {
        firstField.setBackground(normalBg);
        lastField.setBackground(normalBg);
        dobField.setBackground(normalBg);
    }

    private void markInvalid(JTextField field, String message) {
        field.setBackground(invalidBg);
        field.requestFocusInWindow();
        JOptionPane.showMessageDialog(this, message, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    private void onSave() {
        // Path 1: explicit release
        if (releaseCheck.isSelected()) {
            boolean ok = db.releaseSeat(flightId, seatNumber);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Failed to release seat. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
            return;
        }

        // Path 2: implicit release if all fields empty
        String first = firstField.getText().trim();
        String last  = lastField.getText().trim();
        String dob   = dobField.getText().trim();

        if (first.isEmpty() && last.isEmpty() && dob.isEmpty()) {
            boolean ok = db.releaseSeat(flightId, seatNumber);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Failed to mark seat AVAILABLE. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
            return;
        }

        // Path 3: booking
        clearInvalidHighlights();

        if (!ValidationUtils.isValidName(first)) {
            markInvalid(firstField, "Please enter a valid first name (letters, spaces, apostrophes, and hyphens allowed).");
            return;
        }
        if (!ValidationUtils.isValidName(last)) {
            markInvalid(lastField, "Please enter a valid last name (letters, spaces, apostrophes, and hyphens allowed).");
            return;
        }
        if (!ValidationUtils.isValidDobIso(dob)) {
            markInvalid(dobField, "Date of Birth must be yyyy-MM-dd, a real date, and not in the future (e.g., 1990-05-04).");
            return;
        }

        Passenger p = new Passenger(first, last, dob);
        boolean ok = db.bookSeat(flightId, seatNumber, p); // autosaves
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to book seat. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
    }

    private void wireKeyShortcuts() {
        // Enter = Save, Esc = Cancel
        getRootPane().setDefaultButton(saveBtn);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void openModal() {
        setVisible(true);
    }
}
