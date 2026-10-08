# PyxisSapiens — Karte / Linework / Tracking (Schritt 6)

**Stand:** Oktober 2026
**Ergebnis:** MapLibre-Karte mit Messpunkten, draw-barem Linework, GPS-Tracking + GPX, echte
Standortanbindung, MBTiles-Import und Datenmodell/Migration für Linework und Tracks.

---

## 1. Karte (`feature:map`)

- **MapLibre Native** über `AndroidView` (MapView) mit Lifecycle-Weiterleitung.
- **Konfigurierbare Style-URL**, Default `https://demotiles.maplibre.org/style.json` (kein Key).
- **Overlays (GeoJsonSource + Layer):**
  - `measurements` – Punkte je Messart (Farbe nach Flächen/Lineare/Peilung),
  - `linework` – Linien/Polygone (ein-/ausblendbar),
  - `tracks` – aufgezeichnete Pfade,
  - `draft` – aktueller Zeichenzug.
- Steuerung: **Standort zentrieren**, **Tracking Start/Stop**, **GPX**, Zeichenmodus
  **Linie/Polygon**, **Fertig/Abbruch**, Ein-/Ausblenden von Linework/Tracks; Live-Anzeige
  (Distanz/Punkte).

## 2. Linework

- Zeichnen durch Tippen auf die Karte (MapLibre `OnMapClickListener` → LatLng → Punkt).
- Speichern als **GeoJSON** (`LineString`/`Polygon`, geschlossen) in der DB; Filter nach Einheit/Typ
  im Datenmodell vorgesehen.

## 3. Tracking (`MapViewModel`)

- Start sammelt **Fixes** über `LocationPort`, Berechnung der Distanz (**Haversine**), Live-Pfad.
- Stop speichert einen **Track** (Punkte als GeoJSON, Distanz, Dauer).
- **GPX-Export** (`core:export.GpxExporter`) nach `getExternalFilesDir/exports/`.

## 4. Standort (`core:location`)

- `AndroidLocationPort` nutzt den **LocationManager** (kein Play Services), prüft die
  Laufzeitberechtigung, liefert `fixes()` (callbackFlow) und `lastKnown()`.
- Manifest-Berechtigungen ergänzt (FINE/COARSE, FOREGROUND_SERVICE[_LOCATION], POST_NOTIFICATIONS).

## 5. Persistenz

- **Room-Schema v2**: Tabellen `linework` und `tracks` (mit Indizes); **Migration 1→2**
  (erhaltend, kein Datenverlust). Repositories mappen GeoJSON ↔ Domänenmodelle.

## 6. MBTiles-Import

- **Import** in `files/maps/`; `MbtilesSource` liest `tiles`/`metadata` (SQLite, read-only),
  meldet Kachelanzahl.
- **Hinweis:** Das Rendern importierter MBTiles in MapLibre erfordert einen eigenen
  `TileProvider`/Custom-Scheme und ist als Folgeschritt dokumentiert (siehe §8).

## 7. Verifikation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug test lint
```

Ergebnis: **BUILD SUCCESSFUL** — Build ✅, alle Unit-Tests ✅, Lint ✅ (Standort mit
`@SuppressLint("MissingPermission")` nach manueller Prüfung).

## 8. Offen / bewusst zurückgestellt

- **MBTiles-Rendering** in MapLibre (custom TileProvider) – Import/Reader vorhanden.
- **Hintergrund-Tracking** über einen Foreground-Service (Vordergrund-Tracking im VM umgesetzt).
- **Linework-Bearbeitung** (Punkte verschieben/löschen) – Zeichnen/Neuzeichnen umgesetzt.
- **Filter nach Einheit/Typ** in der Karten-UI.
- **Marker-Clustering** für große Datensätze.
