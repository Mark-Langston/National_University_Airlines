import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Home screen showing all flights.
 * Adds: Add Flight, Delete Flight (with confirm), Refresh, Open.
 */
public class HomeFrame extends JFrame {
    private final DatabaseService db;
    private final DefaultListModel<Flight> listModel = new DefaultListModel<>();
    private final JList<Flight> flightList = new JList<>(listModel);

    private final JButton openBtn   = new JButton("Open Flight");
    private final JButton addBtn    = new JButton("Add Flight");
    private final JButton deleteBtn = new JButton("Delete Flight");
    private final JButton refreshBtn = new JButton("Refresh");

    public HomeFrame(DatabaseService db) {
        super("National University Airlines");
        this.db = db;
        setJMenuBar(buildMenuBar());
        initComponents();
        loadFlights();
        setSize(720, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        file.setMnemonic('F');

        JMenuItem add = new JMenuItem("Add Flight");
        add.addActionListener(e -> onAddFlight());

        JMenuItem del = new JMenuItem("Delete Flight");
        del.addActionListener(e -> onDeleteFlight());

        JMenuItem exit = new JMenuItem("Exit");
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        exit.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, mask));
        exit.addActionListener(e -> confirmAndExit());

        file.add(add);
        file.add(del);
        file.addSeparator();
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
        // Header
        JLabel header = new JLabel("National University Airlines", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 24f));
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        // Flight list config
        flightList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightList.setVisibleRowCount(12);
        flightList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Flight f) {
                    l.setText(f.getId() + "  —  " + f.getFlightNumber());
                }
                return l;
            }
        });

        // Double-click to open
        flightList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelectedFlight();
            }
        });

        // Buttons row
        openBtn.addActionListener(e -> openSelectedFlight());
        refreshBtn.addActionListener(e -> reloadFromDisk());
        addBtn.addActionListener(e -> onAddFlight());
        deleteBtn.addActionListener(e -> onDeleteFlight());
        deleteBtn.setEnabled(false);

        // Enable/disable Delete based on selection
        flightList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                deleteBtn.setEnabled(flightList.getSelectedIndex() >= 0);
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshBtn);
        buttons.add(addBtn);
        buttons.add(deleteBtn);
        buttons.add(openBtn);

        // Layout
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        center.add(new JScrollPane(flightList), BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void loadFlights() {
        listModel.clear();
        for (Flight f : db.getFlights()) listModel.addElement(f);

        if (!listModel.isEmpty()) {
            flightList.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(
                this,
                "No flights were loaded.\nA default database will be created when you first save.",
                "No Flights Loaded",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void reloadFromDisk() {
        db.load();
        loadFlights();
    }

    private void openSelectedFlight() {
        Flight selected = flightList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a flight.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SeatsFrame seats = new SeatsFrame(db, selected.getId(), selected.getFlightNumber());
        seats.setVisible(true);
        this.dispose();
    }

    // ---------- Add/Delete handlers ----------

    private void onAddFlight() {
        AddFlightDialog dlg = new AddFlightDialog(this, db);
        dlg.openModal();
        // After dialog closes, refresh and try to select the newly added flight by ID
        String previousId = getSelectedFlightId();
        reloadFromDisk();
        if (previousId != null) selectFlightById(previousId); // fallback to previous
    }

    private void onDeleteFlight() {
        Flight selected = flightList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a flight to delete.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete flight " + selected.getFlightNumber() + " (" + selected.getId() + ") and ALL its seats?\nThis cannot be undone.",
                "Confirm Delete Flight",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = db.deleteFlight(selected.getId());
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Delete failed. The flight may not exist or could not be removed.", "Delete Flight", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Refresh list
        reloadFromDisk();
        if (!listModel.isEmpty()) flightList.setSelectedIndex(Math.min(flightList.getModel().getSize() - 1, 0));
        JOptionPane.showMessageDialog(this, "Flight deleted.", "Delete Flight", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getSelectedFlightId() {
        Flight f = flightList.getSelectedValue();
        return f == null ? null : f.getId();
    }

    private void selectFlightById(String flightId) {
        if (flightId == null) return;
        for (int i = 0; i < listModel.getSize(); i++) {
            Flight f = listModel.get(i);
            if (flightId.equalsIgnoreCase(f.getId())) {
                flightList.setSelectedIndex(i);
                flightList.ensureIndexIsVisible(i);
                return;
            }
        }
    }
}
