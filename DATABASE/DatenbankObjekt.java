package DATABASE;

import java.time.LocalDate;

public class DatenbankObjekt {
    private int id;
    private String name;
    private int punkte;
    private String datum;

    public DatenbankObjekt(String name, int punkte) {   // für neue Einträge (id = 0)
        this.name = name;
        this.punkte = punkte;
        this.datum = LocalDate.now().toString();       // Datum automatisch setzen
    }

    public DatenbankObjekt(int id, String datum, String name, int punkte) {  // aus der DB gelesen
        this.id = id;
        this.datum = datum;
        this.name = name;
        this.punkte = punkte;
    }

    public int getId()        { return id; }
    public String getName()   { return name; }
    public int getPunkte()  { return punkte; }
    public String getDatum() { return datum; }

    public void setId(int id) { this.id = id; }          // wird vom DAO nach dem Insert gesetzt
    public void setName(String name)   { this.name = name; }
    public void setPunkte(int punkte)  { this.punkte = punkte; }
    public void setDatum(String datum) { this.datum = datum; }

    @Override
    public String toString() {
        return "Bestenliste [" + id + ", " + datum + ", " + name + ", " + punkte + "]";
    }
}
