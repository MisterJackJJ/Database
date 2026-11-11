package DATABASE;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Dritte DAO-Implementierung: echte MySQL-/MariaDB-Datenbank über JDBC.
 *
 * Zugangsdaten stehen NICHT im Code, sondern in der Konfigurationsdatei
 * db.properties (im Projekt-Root, gleiches Arbeitsverzeichnis wie bestenliste.db):
 *
 *   db.url=jdbc:mysql://host:3306/NQ_202527_TN_33
 *   db.user=...
 *   db.password=...
 */
public class Mysql implements DatenbankInterface {

    private final String url;
    private final String user;
    private final String password;

    public Mysql() {
        this("db.properties");
    }

    public Mysql(String konfigurationsdatei) {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Path.of(konfigurationsdatei))) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException(
                "Konfigurationsdatei '" + konfigurationsdatei + "' nicht lesbar. " +
                "Bitte db.properties im Projekt-Root anlegen.", e);
        }
        this.url      = props.getProperty("db.url");
        this.user     = props.getProperty("db.user");
        this.password = props.getProperty("db.password", "");
        if (url == null || user == null) {
            throw new IllegalStateException("db.properties: db.url und db.user muessen gesetzt sein.");
        }
        try {
            // Treiber fuer MySQL und MariaDB
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "mysql-connector-j.jar fehlt! Zum Classpath hinzufuegen.", e);
        }
    }

    @Override
    public void initialisieren() {
        String sql = """
            CREATE TABLE IF NOT EXISTS Bestenliste (
                id     INT          NOT NULL AUTO_INCREMENT,
                name   VARCHAR(256) NULL DEFAULT NULL,
                punkte INT          NULL DEFAULT NULL,
                datum  DATE         NOT NULL DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci""";
        try (Connection con = DriverManager.getConnection(url, user, password);
             Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Tabelle konnte nicht angelegt werden", e);
        }
    }

    @Override
    public void speichern(DatenbankObjekt zeile) {
        if (zeile.getId() == 0) {
            insert(zeile);
        } else {
            update(zeile);
        }
    }

    private void insert(DatenbankObjekt zeile) {
        String sql = "INSERT INTO Bestenliste (name, punkte, datum) VALUES (?, ?, ?)";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, zeile.getName());
            ps.setInt(2, zeile.getPunkte());
            if (zeile.getDatum() != null) {
                // DATE-Spalte erwartet java.sql.Date
                ps.setDate(3, java.sql.Date.valueOf(LocalDate.parse(zeile.getDatum())));
            } else {
                // NULL → DB-Default CURRENT_TIMESTAMP greift
                ps.setNull(3, Types.DATE);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    zeile.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Einfuegen fehlgeschlagen", e);
        }
    }

    private void update(DatenbankObjekt zeile) {
        String sql = "UPDATE Bestenliste SET name = ?, punkte = ?, datum = ? WHERE id = ?";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, zeile.getName());
            ps.setInt(2, zeile.getPunkte());
            if (zeile.getDatum() != null) {
                ps.setDate(3, java.sql.Date.valueOf(LocalDate.parse(zeile.getDatum())));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setInt(4, zeile.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Aktualisieren fehlgeschlagen", e);
        }
    }

    @Override
    public Optional<DatenbankObjekt> findById(int id) {
        String sql = "SELECT id, datum, name, punkte FROM Bestenliste WHERE id = ?";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Suche fehlgeschlagen", e);
        }
    }

    @Override
    public List<DatenbankObjekt> findAll() {
        String sql = "SELECT id, datum, name, punkte FROM Bestenliste ORDER BY punkte DESC";
        List<DatenbankObjekt> result = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Abfrage fehlgeschlagen", e);
        }
    }

    @Override
    public void loeschen(int id) {
        String sql = "DELETE FROM Bestenliste WHERE id = ?";
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Loeschen fehlgeschlagen", e);
        }
    }

    /** Uebersetzt eine Ergebniszeile in ein DatenbankObjekt. */
    private DatenbankObjekt mapRow(ResultSet rs) throws SQLException {
        java.sql.Date datum = rs.getDate("datum");
        return new DatenbankObjekt(
                rs.getInt("id"),
                datum != null ? datum.toLocalDate().toString() : null,   // DATE → String (ISO)
                rs.getString("name"),
                rs.getInt("punkte"));
    }
}
