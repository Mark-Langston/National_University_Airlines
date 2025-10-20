package airlines;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class National_University_Airlines {
    public static void main(String[] args) {
        // Set LAF first
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (UnsupportedLookAndFeelException | ReflectiveOperationException ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            // Make the path explicit
            Path dbPath = Paths.get("database.txt");
            DatabaseService db = new DatabaseService(dbPath.toString()); // autoloads or creates
            new HomeFrame(db).setVisible(true);
        });
    }
}
