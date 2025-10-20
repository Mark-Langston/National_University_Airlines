import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * AddFlightDialog – corrected field alignment + validation + database creation
 */
public class AddFlightDialog extends JDialog {
    private final DatabaseService db;
    private final JTextField idField      = new JTextField(18);
    private final JTextField numberField  = new JTextField(18);
    private final JTextField startRowField= new JTextField(6);
    private final JTextField endRowField  = new JTextField(6);
    private final JTextField lettersField = new JTextField(18);

    private final JButton saveBtn   = new JButton("Save");
    private final JButton cancelBtn = new JButton("Cancel");

    public AddFlightDialog(Frame owner, DatabaseService db) {
        super(owner, "Add Flight", true);
        this.db = db;
        initComponents();
        pack();
        setMinimumSize(new Dimension(520, getHeight()));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JLabel header = new JLabel("Add New Flight", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        idField.setToolTipText("Flight ID (e.g., F003 or INTL-01)");
        numberField.setToolTipText("Flight Number (e.g., NU310)");
        startRowField.setToolTipText("First row number (>= 1)");
        endRowField.setToolTipText("Last row number (>= start row)");
        lettersField.setToolTipText("Seat letters (e.g., ABCDEF) – uppercase, unique, up to 10");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 6, 8, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        // Row 1 – Flight ID
        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.LINE_END; gc.weightx = 0;
        form.add(new JLabel("Flight ID:"), gc);
        gc.gridx = 1; gc.weightx = 1.0; gc.anchor = GridBagConstraints.LINE_START;
        form.add(idField, gc);

        // Row 2 – Flight Number
        gc.gridx = 0; gc.gridy++; gc.weightx = 0; gc.anchor = GridBagConstraints.LINE_END;
        form.add(new JLabel("Flight Number:"), gc);
        gc.gridx = 1; gc.weightx = 1.0; gc.anchor = GridBagConstraints.LINE_START;
        form.add(numberField, gc);

        // Row 3 – Row Range (Start–End)
        gc.gridx = 0; gc.gridy++; gc.weightx = 0; gc.anchor = GridBagConstraints.LINE_END;
        form.add(new JLabel("Row Range:"), gc);

        gc.gridx = 1; gc.weightx = 1.0; gc.anchor = GridBagConstraints.LINE_START;
        JPanel rowRange = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        rowRange.add(startRowField);
        rowRange.add(new JLabel("to"));
        rowRange.add(endRowField);
        form.add(rowRange, gc);

        // Row 4 – Seat Letters
        gc.gridx = 0; gc.gridy++; gc.weightx = 0; gc.anchor = GridBagConstraints.LINE_END;
        form.add(new JLabel("Seat Letters:"), gc);
        gc.gridx = 1; gc.weightx = 1.0; gc.anchor = GridBagConstraints.LINE_START;
        form.add(lettersField, gc);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> onSave());

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void onSave() {
        String id        = idField.getText().trim();
        String number    = numberField.getText().trim();
        String startStr  = startRowField.getText().trim();
        String endStr    = endRowField.getText().trim();
        String lettersRaw= lettersField.getText().trim();

        if (!ValidationUtils.isValidFlightId(id)) {
            warn("Flight ID is required and may only contain letters, numbers, and dashes (e.g., F003, INTL-01).");
            idField.requestFocusInWindow();
            return;
        }
        if (!ValidationUtils.isValidFlightNumber(number)) {
            warn("Flight Number must follow pattern NU### (e.g., NU310).");
            numberField.requestFocusInWindow();
            return;
        }

        int startRow, endRow;
        try {
            startRow = Integer.parseInt(startStr);
            endRow   = Integer.parseInt(endStr);
        } catch (NumberFormatException ex) {
            warn("Row numbers must be integers.");
            startRowField.requestFocusInWindow();
            return;
        }
        if (!ValidationUtils.isValidRowRange(startRow, endRow)) {
            warn("Invalid row range. Start ≥ 1, end ≥ start, and the total ≤ 200 rows.");
            startRowField.requestFocusInWindow();
            return;
        }

        Set<Character> uniqueLetters = ValidationUtils.parseSeatLettersUnique(lettersRaw);
        if (uniqueLetters.isEmpty()) {
            warn("Seat letters must be uppercase A–Z, unique, and up to 10 characters (e.g., ABCDEF).");
            lettersField.requestFocusInWindow();
            return;
        }

        char[] letters = new char[uniqueLetters.size()];
        int i = 0; for (Character c : uniqueLetters) letters[i++] = c;

        boolean ok = db.addFlight(id, number, startRow, endRow, letters);
        if (!ok) {
            warn("Could not add flight. It may already exist or inputs are invalid.");
            return;
        }

        JOptionPane.showMessageDialog(this,
            "Flight created:\n" +
            "ID: " + id + "\n" +
            "Number: " + number + "\n" +
            "Rows: " + startRow + "–" + endRow + "\n" +
            "Seats: " + new String(letters),
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
        dispose();
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    public void openModal() {
        setVisible(true);
    }
}
