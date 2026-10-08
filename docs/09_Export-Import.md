# PyxisSapiens — Export / Import (Schritt 5)

**Stand:** Oktober 2026
**Ergebnis:** Sechs Exportformate (CSV, GeoJSON, KML, KMZ, PDF, Projektarchiv), verschlüsselbares
Archiv, CSV-/KML-Import mit Mapping/Validierung, UI-Wizard und Android-Share-Sheet.

---

## 1. Formate (`core:export`)

| Format | Umsetzung |
|---|---|
| **CSV** | konfigurierbare Spalten, UTF-8, Round-Trip-fähig (Import) |
| **GeoJSON** | FeatureCollection mit Punktgeometrie + Eigenschaften (kotlinx.serialization) |
| **KML** | Placemarks mit Styles je Messart + Metadaten-Beschreibung |
| **KMZ** | ZIP mit `doc.kml` |
| **PDF** | `android.graphics.pdf.PdfDocument`: Deckblatt/Metadaten, **Mini-Stereonet-Skizze**, Statistikzeilen, paginierte Tabelle |
| **Projektarchiv (.pyxis)** | ZIP (manifest + CSV + GeoJSON), **optional AES-GCM-verschlüsselt** |

## 2. Verschlüsseltes Archiv

- `ProjectArchive`: Layout `MAGIC(6) | salt(16) | iv(12) | ciphertext`.
- Schlüsselableitung **PBKDF2-HMAC-SHA256** (100 000 Iterationen, 256 Bit), **AES-GCM** (128-Bit-Tag).
- `isEncrypted(...)` erkennt Archive ohne Passwort; `read(...)` verlangt das richtige Passwort
  (sonst schlägt die GCM-Authentifizierung fehl).

## 3. Validierung & Import

- `ExportValidator`: Wertebereiche (Dip 0–90°, Richtung/Trend 0–360°, Lat/Lon), Duplikat-IDs →
  Warnungen; export wird mit Warnhinweis gekennzeichnet.
- **CSV-Import** (`CsvImporter`): Header-Auto-Mapping für `dip`, `dipDirection`/`dd`,
  `strike` (**RHR → Dip-Direction +90°**), `trend`/`plunge`, `bearing`, `latitude`/`longitude`,
  `altitude`, `accuracy`, `note`, `id`; erkennt Fläche/Linear/Peilung je Zeile; Warnungen.
- **KML-Import** (`KmlImporter`): liest Placemarks (Name + Koordinaten) als Messpunkte.

## 4. UI (`feature:export`, Hilt-VM)

- Erreichbar über **Mehr → Export / Import**.
- Formatwahl (Chips), Passwortfeld fürs Archiv, „Exportieren" und „Alle Formate"; Statusmeldung
  mit Dateiname und **„Teilen"** (Android-Share-Sheet via `FileProvider`
  `de.pyxissapiens.fileprovider`, `file_paths.xml` unter `external-files-path/exports`).
- **Import** über den Systemdateiauswahl (`OpenDocument`) für CSV/KML; Rückmeldung mit Anzahl und
  Warnungen.

## 5. Verifikation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug test lint
```

Ergebnis: **BUILD SUCCESSFUL** — CSV-Round-Trip, RHR-Strike-Konvertierung, verschlüsseltes/offenes
Archiv-Round-Trip, Validierung und KMZ-Inhalt: **6 Tests grün**; Build ✅, Lint ✅.

## 6. Offen / bewusst zurückgestellt

- **KMZ mit eingebetteten Medien** (Fotos/Notizen) noch nicht; derzeit nur `doc.kml`.
- **KML-Import** erzeugt positionsbasierte Datensätze ohne Orientierung (sofern die KML keine
  strukturierten Werte enthält).
- **Import-Mapping-UI** (manuelle Spaltenzuordnung/Vorschau) folgt; derzeit Automapping.
- **PDF-Diagramm** ist eine einfache, direkt gezeichnete Stereonet-Skizze (kein gerendertes PNG).
- **Teilen** nutzt eine feste FileProvider-Authority; anpassbar.
