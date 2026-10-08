# PyxisSapiens — Karte/Tracking nachziehen (Schritt 9)

**Stand:** Oktober 2026
**Ergebnis:** Offline-MBTiles-Rendering, Hintergrund-Tracking per Foreground-Service,
Linework-Punktbearbeitung und ein Basiskarten-Verwaltungsbereich.

---

## 1. Offline-MBTiles-Rendering

- `MbtilesTileServer`: kleiner **lokaler HTTP-Server** (`ServerSocket`, ephemerer Port), der Kacheln
  aus der MBTiles-SQLite über `/<z>/<x>/<y>.<ext>` liefert (Format aus `metadata`).
- In der Karte wird die aktive MBTiles als **MapLibre `RasterSource`** (URL `http://127.0.0.1:<port>/{z}/{x}/{y}`)
  mit `RasterLayer` eingebunden; Server wird beim Wechsel/Verlassen gestoppt.

## 2. Hintergrund-Tracking

- `TrackingService` (`@AndroidEntryPoint`): **Foreground-Service** mit Notification und
  `foregroundServiceType=location`.
- Der Service schreibt den **laufenden Track kontinuierlich** in die DB (Karte zeigt den Live-Pfad)
  und finalisiert beim Stopp (`endedAt`, Distanz) sowie **GPX-Export**.
- Distanz über `GeoMath.haversineMeters(...)` (jetzt in `core:geology`).
- Start/Stopp aus der Karte über Service-Intents; `tracking`/Live-Statistik werden aus der DB
  abgeleitet.
- Manifest: `<service ... foregroundServiceType="location"/>`; Berechtigungen bereits vorhanden.

## 3. Linework-Punktbearbeitung

- Vorhandene Linienzüge **auswählen** (Karten-Panel), Punkte durch Tippen **anhängen**,
  **letzten Punkt löschen**, **Fertig** speichert als GeoJSON (Neu oder Update).

## 4. Basiskarten-Verwaltung (`BasemapsRoute`)

- **Style-URL** editierbar/speichern.
- **Liste importierter MBTiles** mit **Aktivieren/Deaktivieren/Löschen** und **Import** per
  Systemdateiauswahl.
- Einstellungen in `MapSettings` (SharedPreferences) persistiert.

## 5. Verifikation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug test lint
```

Ergebnis: **BUILD SUCCESSFUL** — Build ✅, alle Unit-Tests ✅, Lint ✅.

## 6. Offen / Hinweise

- **Laufzeitverifikation** von Tile-Server/Service war hier **nicht möglich** (kein Emulator/Gerät);
  verifiziert sind Kompilierung und Lint.
- **Tile-Server** bindet an `127.0.0.1` (nur lokal); für sehr große Karten ist HTTP-Range/Caching
  eine mögliche Optimierung.
- **Linework-Bearbeitung** erlaubt Anhängen/Löschen des letzten Punkts; freies Verschieben einzelner
  Punkte per Drag ist ein Folgeschritt.
