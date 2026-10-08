# PyxisSapiens — Verknüpfung / Erweiterbarkeit (Schritt 11)

**Stand:** Oktober 2026
**Ergebnis:** Externer geologischer Kartenlayer (WMS/XYZ) mit Link-out, optionale
Macrostrat-Abfrage mit Offline-Fallback und GeoJSON/KML-Import für Linework. Kein Backend.

---

## 1. Externer geologischer Kartenlayer

- `MapSettings` erweitert um **Geologie-Layer-URL**, **Aktiv-Schalter**.
- In der Karte wird der Layer als MapLibre **`RasterSource`/`RasterLayer`** eingebunden:
  - **XYZ-Template** (enthält `{z}/{x}/{y}`) wird direkt genutzt;
  - sonst wird eine **WMS-GetMap-URL** mit `&bbox={bbox-epsg-3857}` ergänzt.
- **Basiskarten-Verwaltung**: URL-Feld, „Aktiv"-Chip und **Link-out** (öffnet den Dienst im
  Browser, z. B. macrostrat.org/map).

## 2. Stratigraphie-Anbindung (optional)

- `StratigraphyClient` (core:data): Abfrage des **Macrostrat-API**
  (`geologic_units/map?lat=&lng=`) über `HttpURLConnection`, JSON-Parsing.
- **Offline-sicher**: jede Netzwerk-/Parse-Ausnahme liefert eine leere Liste.
- **UI** (Mehr → Datentypen & Einheiten): „Am Standort abfragen" listet gefundene Einheiten;
  „Übernehmen" legt sie als `RockUnit` an. Benötigt die INTERNET-Berechtigung.

## 3. Linework-Import (GeoJSON/KML)

- `LineworkImporter` (core:export): liest **LineString/Polygon** aus **GeoJSON**
  (kotlinx.serialization) und **KML** (Regex) → `Linework` (GeoJSON-Geometrie).
- **UI** (Basiskarten): „GeoJSON/KML importieren" über die Systemdateiauswahl; importiert in die
  Kartierung.

## 4. Kein Backend

- Bewusst **offline-first**: keine Konten, kein Sync, kein öffentliches Teilen.
  Verknüpfung geschieht ausschließlich über **Export/Teilen**, **externe Links** und den
  **optionalen** Online-Lookup.

## 5. Verifikation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug test lint
```

Ergebnis: **BUILD SUCCESSFUL** — Build ✅, alle Unit-Tests ✅, Lint ✅.

## 6. Offen / Hinweise

- **Laufzeitverifikation** von Layer/Netzabfrage war hier nicht möglich (kein Emulator/Gerät).
- **WMS-Parameter** (Layer-Name, CRS) sind derzeit in der URL zu kodieren; ein Formular mit
  Einzelfeldern ist eine mögliche Erweiterung.
- **Shapefile/GPX**-Importe (Linework/Tracks) sind weitere sinnvolle Formate.
