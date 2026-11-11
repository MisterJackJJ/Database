package DATABASE;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatenbankVerbindung {
        static {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "SQLite-Treiber fehlt! sqlite-jdbc.jar zum Classpath hinzufügen.", e);
            }
        }
        // Privater Konstruktor: von dieser Klasse soll kein Objekt entstehen.
        // Sie ist reine "Werkzeugkiste" mit statischen Methoden.

        private DatenbankVerbindung() { }

        public static Connection getConnection() throws SQLException {
            // jdbc:sqlite: erzeugt die Datei beim ersten Zugriff automatisch
            return DriverManager.getConnection("jdbc:sqlite:bestenliste.db");
        }
}
