# PyxisSapiens — Messkern (Schritt 3)

**Stand:** Oktober 2026
**Ergebnis:** Vertikaler Durchstich von der Sensororientierung über Kalibrierung/Mittelung/Qualität
und WMM-Deklination bis zur gespeicherten Messung inkl. Liste. Alle Tests grün.

---

## 1. WMM2025-Deklination (`core:wmm`)

- **Vendored, gemeinfreier** NOAA/Los-Alamos-Referenzcode `TSAGeoMag` (Public-Domain-Notice
  erhalten); `log4j` durch `java.util.logging` ersetzt.
- Gebündelte Koeffizienten: `core/wmm/src/main/resources/de/pyxissapiens/core/wmm/WMM.COF`
  (WMM2025, Epoche 2025.0).
- `WmmGeomagneticProvider` liefert **Deklination, Inklination, Totalfeld** (µT) offline für
  (lat, lon, Höhe, Zeitpunkt).
- **Validierung:** Unit-Test gegen die **offiziellen NOAA-Testwerte** (`WMM2025_TEST_VALUES.txt`):
  D und I innerhalb **0,05°**, F innerhalb **5 nT** – für mehrere Orte/Höhen/Epochen. ✅

## 2. Ports (`core:ports`)

- `DeviceFrame` — Geräteachsen **in Welt-ENU** (X=Ost, Y=Nord, Z=Up): exakt die Spalten der
  Android-Rotationsmatrix, damit die Konvention eindeutig und testbar ist.
- `OrientationPort` (Frames + Magnetfeld-µT), `GeomagneticPort`.

## 3. Messpipeline (`core:domain`, Android-frei)

`MeasurementEngine` mit klaren Konventionen (docs/03 §4):

- **Welt-ENU**; `world = R · device` (Android-Rotationsmatrix).
- **Flächennormale = Aufwärtsnormale** → `poleToPlane` klappt aufwärts, Dip ∈ [0°,90°]
  (behebt den „Vertical-Orientation"-Fehler); horizontale Ebene → Dip-Dir 0°.
- **Lineation down-plunge** (`z = −sin(plunge)`), automatisch auf Abwärtsrichtung normalisiert.
- **Mittelung als Vektoren** (Normale/Lineation), nicht als Winkel; Ausreißerfilter (Default 3°).
- **Fisher-κ** über stabilen Bisektionssolver (overflow-sicher).
- **Qualität** 0..100 aus Stichprobenzahl, Streuung (σ) und Android-Sensorstufe.
- `measurePlane` / `measureLine` / `measureBearing` liefern Werte + `DataQuality`.

**Tests** (`:core:domain`): Flächen-Round-Trip, vertikale Ebene, Kontaktflächen-Umschaltung,
Lineations-Round-Trip, Down-Plunge-Normalisierung, Peilung, verrauschte Mittelung, κ-Monotonie,
Qualitäts-Penalty. ✅ (nachdem Aufwärtsnormale und Down-Plunge-Konvention korrigiert wurden).

## 4. Android-Anbindung

- `AndroidOrientationPort` (`core:sensors`): nutzt `TYPE_ROTATION_VECTOR` →
  `getRotationMatrixFromVector`; Spalten = Geräteachsen in Welt. Magnetfeld-Flow für Störungsanzeige.
  Kalibrierung/Mittelung/Deklination bleiben in der Domäne.
- Hilt: `SensorsModule` bindet `SensorPort`, `OrientationPort` und (via `@Provides`)
  `GeomagneticPort → WmmGeomagneticProvider`.

## 5. Persistenz & UI

- `core:data`: `ensureDefaultProject`/`ensureDefaultSite`, `add(...)`, `observeAll()`; vollständiges
  Entity↔Domain-Mapping; neue DAO-Query `observeAll`.
- **Mess-Screen** (`feature:compass`, Hilt-VM): Live-Werte (Fläche/Linear/Peilung), Kompassrose +
  Klinometer, Stabilitätsring, Qualität, Interferenz-/Sensor-/Deklinationsanzeige, Modus-,
  Nordreferenz- und Kontaktflächen-Umschaltung; „MESSEN" friert ein (gehaltene Messung mit
  Qualität) → **Speichern/Verwerfen**. Deklination wird bei Vorliegen eines GPS-Fixes angewandt
  (magnetisch ↔ geografisch).
- **Messungsliste** (`feature:measurements`, Hilt-VM): beobachtet `observeAll()` und zeigt Symbol,
  Werte und Qualität; im Reiter „Daten".

## 6. Verifikation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug test lint
```

Ergebnis: **BUILD SUCCESSFUL** — Build ✅, alle Unit-Tests (geology/domain/wmm) ✅, Lint ✅.

## 7. Offen / bewusst zurückgestellt

- **SQLCipher** weiterhin nicht verdrahtet (DB unverschlüsselt; Abhängigkeit vorhanden).
- **LocationPort** ist ein Stub → ohne GPS-Fix keine Deklinationskorrektur/Position (Messung wird
  dann magnetisch gespeichert).
- **Manuelle Eingabe/Korrektur** und **Undo/History** in der UI noch nicht implementiert
  (Engine/Datenmodell dafür vorbereitet).
- `hiltViewModel`-Import ist als deprecated markiert (neuer Paketpfad) – nur Warnung.
