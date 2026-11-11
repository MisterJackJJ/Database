package DATABASE;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Zweite DAO-Implementierung neben Sqlite: speichert die Bestenliste
 * in einer einfachen Textdatei (eine Zeile pro Eintrag, Format: id;datum;name;punkte).
 *
 * Benötigt keinen JDBC-Treiber und keine Datenbank-Datei –
 * Austauschbarkeit wird durch das DatenbankInterface gewährleistet.
 */
public class Textdatei implements DatenbankInterface {

    private final Path datei;

    public Textdatei() {
        this("bestenliste.txt");
    }

    public Textdatei(String dateiname) {
        this.datei = Path.of(dateiname);
    }

    @Override
    public void initialisieren() {
        try {
            if (Files.notExists(datei)) {
                // Kopfzeile anlegen, damit das Format dokumentiert ist
                Files.writeString(datei,
                        "# id;datum;name;punkte" + System.lineSeparator(),
                        StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Datei konnte nicht angelegt werden: " + datei, e);
        }
    }

    @Override
    public void speichern(DatenbankObjekt zeile) {
        if (zeile.getDatum() == null) {
            zeile.setDatum(LocalDate.now().toString());
        }
        if (zeile.getId() == 0) {
            insert(zeile);
        } else {
            update(zeile);
        }
    }

    private void insert(DatenbankObjekt zeile) {
        int neueId = naechsteFreieId();
        List<String> zeilen = leseZeilen();
        zeilen.add(alsZeile(neueId, zeile));
        schreibeZeilen(zeilen);
        zeile.setId(neueId);              // ID zurück ins Objekt schreiben (wie beim JDBC-Insert)
    }

    private void update(DatenbankObjekt zeile) {
        List<String> zeilen = leseZeilen();
        boolean gefunden = false;
        for (int i = 0; i < zeilen.size(); i++) {
            DatenbankObjekt vorhanden = parse(zeilen.get(i));
            if (vorhanden != null && vorhanden.getId() == zeile.getId()) {
                zeilen.set(i, alsZeile(zeile.getId(), zeile));
                gefunden = true;
            }
        }
        if (!gefunden) {
            throw new RuntimeException("Kein Eintrag mit id=" + zeile.getId() + " vorhanden");
        }
        schreibeZeilen(zeilen);
    }

    @Override
    public Optional<DatenbankObjekt> findById(int id) {
        for (String z : leseZeilen()) {
            DatenbankObjekt d = parse(z);
            if (d != null && d.getId() == id) {
                return Optional.of(d);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<DatenbankObjekt> findAll() {
        List<DatenbankObjekt> ergebnis = new ArrayList<>();
        for (String z : leseZeilen()) {
            DatenbankObjekt d = parse(z);
            if (d != null) {
                ergebnis.add(d);
            }
        }
        return ergebnis;
    }

    @Override
    public void loeschen(int id) {
        List<String> zeilen = leseZeilen();
        boolean entfernt = zeilen.removeIf(z -> {
            DatenbankObjekt d = parse(z);
            return d != null && d.getId() == id;
        });
        if (!entfernt) {
            throw new RuntimeException("Kein Eintrag mit id=" + id + " vorhanden");
        }
        schreibeZeilen(zeilen);
    }

    // ---------- Hilfsmethoden ----------

    private List<String> leseZeilen() {
        try {
            if (Files.notExists(datei)) {
                return new ArrayList<>();
            }
            // Kommentar-/Kopfzeilen (# ...) herausfiltern, damit sie sich
            // beim erneuten Schreiben nicht vervielfachen
            return new ArrayList<>(Files.readAllLines(datei, StandardCharsets.UTF_8)
                    .stream()
                    .filter(z -> !z.isBlank() && !z.startsWith("#"))
                    .toList());
        } catch (IOException e) {
            throw new RuntimeException("Datei konnte nicht gelesen werden: " + datei, e);
        }
    }

    private void schreibeZeilen(List<String> zeilen) {
        List<String> alles = new ArrayList<>();
        alles.add("# id;datum;name;punkte");
        alles.addAll(zeilen);
        try {
            Files.write(datei, alles, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Datei konnte nicht geschrieben werden: " + datei, e);
        }
    }

    private int naechsteFreieId() {
        int max = 0;
        for (String z : leseZeilen()) {
            DatenbankObjekt d = parse(z);
            if (d != null && d.getId() > max) {
                max = d.getId();
            }
        }
        return max + 1;
    }

    /** Baut die Speicherzeile: id;datum;name;punkte */
    private String alsZeile(int id, DatenbankObjekt d) {
        return id + ";" + d.getDatum() + ";" + d.getName() + ";" + d.getPunkte();
    }

    /** Liest eine Speicherzeile; Kommentar-/Kopfzeilen liefern null. */
    private DatenbankObjekt parse(String zeile) {
        if (zeile == null || zeile.isBlank() || zeile.startsWith("#")) {
            return null;
        }
        String[] teile = zeile.split(";", -1);
        if (teile.length != 4) {
            return null;
        }
        try {
            return new DatenbankObjekt(
                    Integer.parseInt(teile[0].trim()),
                    teile[1].trim(),
                    teile[2].trim(),
                    Integer.parseInt(teile[3].trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
