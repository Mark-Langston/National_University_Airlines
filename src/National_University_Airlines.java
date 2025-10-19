import javax.swing.*;

public class National_University_Airlines {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignored) {}

            DatabaseService db = new DatabaseService("database.txt"); // autoloads or creates
            new HomeFrame(db).setVisible(true);
        });
    }
}
