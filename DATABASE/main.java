package DATABASE;

import java.util.List;
import java.util.Optional;

public class main {

    public static void main(String[] args) {
        // Interface-Typ referenzieren, Implementierung austauschbar
        DatenbankInterface db = new Textdatei();

        // 1. Tabelle anlegen
        // db.initialisieren();
        // System.out.println("Datenbank initialisiert.");

        // 2. Neue Einträge speichern (id = 0 → INSERT)
//        DatenbankObjekt eintrag1 = new DatenbankObjekt(0, "2026-11-11", "Dominik", 150);
//        DatenbankObjekt eintrag2 = new DatenbankObjekt(0, "2026-11-11", "Lino", 90);
//        DatenbankObjekt eintrag3 = new DatenbankObjekt(0, "2026-11-11", "Alex", 500);
//        DatenbankObjekt eintrag4 = new DatenbankObjekt(0, "2026-11-11", "Jakob", 10);
//        db.speichern(eintrag1);
//        db.speichern(eintrag2);
//        db.speichern(eintrag3);
//        db.speichern(eintrag4);
//        System.out.println("Gespeichert: " + eintrag1.getName() + ", " + eintrag2.getName());

        // 3. Alle Einträge anzeigen
        List<DatenbankObjekt> alle = db.findAll();
        System.out.println("\nBestenliste:");
        for (DatenbankObjekt e : alle) {
            System.out.printf("  #%d: %-10s %3d Punkte (%s)%n",
                    e.getId(), e.getName(), e.getPunkte(), e.getDatum());
        }

        // 4. Einzelnen Eintrag suchen
        Optional<DatenbankObjekt> treffer = db.findById(1);
        if (treffer.isPresent()) {
            DatenbankObjekt e = treffer.get();
            System.out.println("\nGefunden: " + e.getName() + " mit " + e.getPunkte() + " Punkten");

            // 5. Punkte ändern und erneut speichern (id != 0 → UPDATE)
            e.setPunkte(200);
            db.speichern(e);
            System.out.println("Aktualisiert: " + e.getName() + " hat jetzt " + e.getPunkte() + " Punkte");
        } else {
            System.out.println("\nKein Eintrag mit ID 1 gefunden.");
        }

        // 6. Eintrag löschen
        // db.loeschen(2);
        // System.out.println("\nEintrag mit ID 2 gelöscht.");

        // 7. Finaler Stand
        System.out.println("\nBestenliste nach dem Löschen:");
        db.findAll().forEach(e -> System.out.println("  " + e));
    }
}
