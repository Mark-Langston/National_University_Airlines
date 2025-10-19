import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class SeatsFrame extends JFrame {
    private final DatabaseService db;
    private final String flightId;
    private final String flightNumber;

    private final JTable table;
    private final DefaultTableModel model;
    private final JButton editBtn = new JButton("Edit Seat");
    private final JButton backBtn = new JButton("Back");
    private final JButton refreshBtn = new JButton("Refresh");

    public SeatsFrame(DatabaseService db, String flightId, String flightNumber) {
        super("Seats – " + flightNumber + " (" + flightId + ")");
        this.db = db;
        this.flightId = flightId;
        this.flightNumber = flightNumber;

        setJMenuBar(buildMenuBar());               // <-- Menu bar

        model = new DefaultTableModel(new Object[]{"Seat", "Status", "Passenger", "DOB"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        table.setAutoCreateRowSorter(true);

        initComponents();
        loadSeats();

        setSize(800, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic('F');

        JMenuItem exit = new JMenuItem("Exit");
        exit.setMnemonic('E');
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, mask));
        exit.addActionListener(e -> confirmAndExit());

        file.add(exit);
        bar.add(file);
        return bar;
    }

    private void confirmAndExit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Exit National University Airlines?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void initComponents() {
        JLabel header = new JLabel("Flight " + flightNumber + " (" + flightId + ")", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        editBtn.setEnabled(false);
        editBtn.addActionListener(e -> openEditorForSelected());

        backBtn.addActionListener(e -> {
            new HomeFrame(db).setVisible(true);
            dispose();
        });

        refreshBtn.addActionListener(e -> loadSeats());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                editBtn.setEnabled(table.getSelectedRow() >= 0);
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openEditorForSelected();
                }
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshBtn);
        south.add(backBtn);
        south.add(editBtn);

        setLayout(new BorderLayout(8, 8));
        add(header, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    private void loadSeats() {
        model.setRowCount(0);
        List<Seat> seats = db.getSeats(flightId);
        for (Seat s : seats) {
            String name = s.getPassenger() == null ? "" : s.getPassenger().getFullName();
            String dob  = s.getPassenger() == null ? "" : s.getPassenger().getDateOfBirth();
            model.addRow(new Object[]{ s.getSeatNumber(), s.getStatus().name(), name, dob });
        }
        editBtn.setEnabled(false);
    }

    private void openEditorForSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int row = table.convertRowIndexToModel(viewRow);
        String seatNumber = (String) model.getValueAt(row, 0);

        SeatEditorDialog dlg = new SeatEditorDialog(this, db, flightId, flightNumber, seatNumber);
        dlg.openModal();
        loadSeats();
    }
}
