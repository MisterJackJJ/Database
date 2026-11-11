package DATABASE;
import java.util.List;
import java.util.Optional;

public interface DatenbankInterface {
    void initialisieren();                                // Tabelle anlegen
    void speichern(DatenbankObjekt zeile);                // Eintrag speichern
    Optional<DatenbankObjekt> findById(int id);           // Eintrag suchen
    List<DatenbankObjekt> findAll();                      // Alle Einträge
    void loeschen(int id);                                // Eintrag löschen
}
