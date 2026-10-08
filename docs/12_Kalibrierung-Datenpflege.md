# PyxisSapiens — Kalibrierung & Datenpflege (Schritt 8)

**Stand:** Oktober 2026
**Ergebnis:** Kalibrier-Assistent (Hard/Soft-Iron + Auflagefläche), Datentypen-/Einheiten-
Verwaltung, manuelle Eingabe/Bearbeitung und Filter in Liste und Karte.

---

## 1. Kalibrier-Mathematik (`core:domain`, getestet)

- `Calibration.fit(...)`: **Min/Max/Ellipsoid**-Kalibrierung aus einer Figur-8-Rotation –
  Hard-Iron-Offset (Bounding-Box-Zentrum) + achsenweise Soft-Iron-Skalierung.
- **Qualität = Rotationsabdeckung** (belegte Oktanten), nicht Spherizität (Soft-Iron ist erwartet).
- `correctedMagnitude(...)` für die Interferenzprüfung; `rotationFromTo(...)` (Rodrigues) und
  `tiltFrom(...)` für die **Auflageflächen-/Bump-Korrektur** (Referenznormale → Up).
- Tests: Offset-Rückgewinnung + Normierung auf ~50 µT; Tilt-Korrektur bildet Normale auf Up ab.

## 2. Kalibrier-Persistenz & Anwendung

- `CalibrationRepository` (core:data): speichert Magnetometer- und Tilt-Kalibrierung **pro Gerät**
  (Hardware-Fingerprint) als JSON.
- **Anwendung im Messkern** (`CompassViewModel`): die **Tilt-Korrektur** wird auf jede
  Orientierungs-Frame angewandt; die **Magnetometer-Kalibrierung** korrigiert die
  Feldstärke für die **Interferenzanzeige**.
- Hinweis: Der Heading kommt weiterhin aus dem Android-Rotationsvektor; eine vollständige
  Heading-Korrektur würde eine Roh-Mag-Fusion erfordern (dokumentiert als Erweiterung).

## 3. Kalibrier-Assistent (`feature:settings`)

- **Sensor-Test** (Magnetometer/Gyroskop vorhanden), **Figur-8** mit Live-Probenzahl und
  Qualitätsbewertung, **Auflageflächen-Referenz** setzen, **Kalibrierung löschen**.

## 4. Datentypen & Einheiten

- **Room v3**: Tabellen `data_types` und `units` + **Migration 2→3** (erhaltend); in Migration/
  Re-Key-Sicherung von `DatabaseEncryption` berücksichtigt.
- Domänenmodelle `DataType` (Name/Farbe/Symbol) und `RockUnit` (Name/Kürzel); Repositories.
- **Verwaltungs-UI** (Mehr → Datentypen & Einheiten): anlegen/löschen.

## 5. Manuelle Eingabe/Bearbeitung & Filter

- **Messungsliste**: „+" öffnet einen Dialog **(Fläche/Linear/Peilung, Werte, Notiz, Datentyp,
  Einheit)**; je Datensatz **Bearbeiten** und **Löschen**; Antippen zeigt die Historie.
- Speichern über den `MeasurementEditManager` (Historie + Undo/Redo + `MeasurementValidator`).
- **Filter** nach Datentyp in **Liste** und **Karte** (Messpunkt-Layer).

## 6. Verifikation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug test lint
```

Ergebnis: **BUILD SUCCESSFUL** — Build ✅, alle Unit-Tests (inkl. Kalibrierung) ✅, Lint ✅.

## 7. Offen / Hinweise

- **Heading-Korrektur** aus Roh-Magnetometer (statt Rotationsvektor) ist der nächste
  Genauigkeitsschritt.
- **Datentyp-Symbolauswahl** ist vorerst textbasiert; eine Symbol-/Farbauswahl-UI folgt.
- **Ellipsoid-Fit** ist ein robuster Min/Max-Fit; ein vollständiger algebraischer Ellipsoid-Fit
  ist eine mögliche Verfeinerung.
