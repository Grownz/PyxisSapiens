# PyxisSapiens — Technische Architektur (Teil b)

**Dokumentversion:** 0.1
**Stand:** Oktober 2026
**Basis:** `docs/02_Lastenheft-und-Backlog.md`

---

## 1. Architekturprinzipien

1. **Offline-First & lokal:** Keine Serverabhängigkeit; Netz nur optional (Karten-Tiles, WMM-Aktualisierung).
2. **Genauigkeit zuerst:** Die Messpipeline (Sensorfusion, Kalibrierung, Mittelung, Qualitätsbewertung) ist das Herzstück und wird vollständig gekapselt und getestet.
3. **Domänenschicht portabel (KMP-fähig):** Messmathematik, Stereonet-/Statistik-Algorithmen, Datenmodelle und Use-Cases als reines Kotlin (`commonMain`) — ohne Android-Abhängigkeiten.
4. **Testbarkeit:** Sensoren, Uhr, Position, Dateisystem und WMM als Interfaces/Injection → deterministisch testbar.
5. **Unidirectional Data Flow:** UI (Compose) → ViewModel (State) → UseCase (Domäne) → Repository (Daten).
6. **Datenschutz by design:** Keine Telemetrie, keine Drittanbieter-SDKs, Scoped Storage, optionale Verschlüsselung.

### Festgelegte Technologieentscheidungen

| Bereich | Entscheidung | Begründung |
|---|---|---|
| Sprache | **Kotlin** | Android-Standard, KMP-fähig |
| UI | **Jetpack Compose** (+ Material 3) | modern, testbar, konsistent Phone/Tablet |
| Build | **Gradle Kotlin DSL**, Version Catalogs | Wartbarkeit |
| `minSdk` / `targetSdk` | **26 (Android 8)** / aktuell | breite Feldgerätebasis, moderne APIs |
| Lokale DB | **Room (SQLite) + SQLCipher** | ausgereift, verschlüsselbar |
| Karte (R3) | **MapLibre Native** (OSM, offline PMTiles/MBTiles) | offen, offline, kein Tracking |
| DI | **Hilt** | Android-Standard (Koin-Alternative falls Shared-DI später nötig) |
| Async | **Coroutines + Flow** | reaktiv, Sensor→Flow |
| Serialisierung | **kotlinx.serialization** | KMP-fähig |
| Lizenz | **GPLv3** | Copyleft, schützt Offenheit |

---

## 2. Gesamtbild (Schichten & Flüsse)

```
┌──────────────────────────────────────────────────────────────┐
│ Presentation (Android)                                        │
│  Compose Screens · ViewModels · Navigation · Material3       │
│  Module: :app, :feature:compass, :feature:projects,          │
│          :feature:measurement, :feature:export,              │
│          :feature:stereonet(R2), :feature:map(R3)            │
├──────────────────────────────────────────────────────────────┤
│ Domain (KMP-fähig, reines Kotlin / commonMain)                │
│  Modelle · UseCases · Messpipeline · Geologie-Mathematik      │
│  Module: :core:domain, :core:geology, :core:wmm, :core:ports  │
├──────────────────────────────────────────────────────────────┤
│ Data / Platform (Android)                                     │
│  Repositories (Impl) · Room+SQLCipher · Sensoren · GPS        │
│  Dateien/Medien · Export-Encoder · Karten-Tiles               │
│  Module: :core:data, :core:database, :core:sensors,           │
│          :core:location, :core:media, :core:export, :core:map │
├──────────────────────────────────────────────────────────────┤
│ Android Framework / OS                                        │
└──────────────────────────────────────────────────────────────┘
```

**Abhängigkeitsrichtung:** Presentation → Domain ← Data. Die Domäne kennt **keine** Android-Typen.
Zugriff auf Plattform nur über **Ports** (Interfaces in `:core:ports`), deren Implementierungen in `:core:data`/`:core:sensors` liegen.

---

## 3. Gradle-Modulstruktur

```
PyxisSapiens/
├─ app/                       # Einstiegspunkt, DI-Wiring, Navigation, Theme
├─ core/
│  ├─ common/                 # Utils, Result, Dispatcher, Extensions
│  ├─ ports/                  # Interfaces zu Plattform (Sensor, Location, Clock, Files, WMM)
│  ├─ domain/                 # (KMP) Datenmodelle + UseCases
│  ├─ geology/                # (KMP) Vektor-/Stereonet-/Statistik-Mathematik
│  ├─ wmm/                    # (KMP) World-Magnetic-Model-Berechnung + Koeffizienten
│  ├─ database/               # Room-Entities, DAOs, Migrations, SQLCipher
│  ├─ data/                   # Repository-Implementierungen, Mapper
│  ├─ sensors/                # SensorManager-Adapter → Ports
│  ├─ location/               # FusedLocation/LocationManager → Ports
│  ├─ media/                  # Kamera/Dateien, Scoped Storage, Verschlüsselung
│  ├─ export/                 # CSV/GeoJSON/KML/KMZ/PDF/Archiv-Encoder
│  └─ ui/                     # Design-System, wiederverwendbare Compose-Komponenten
├─ feature/
│  ├─ compass/                # Mess-UI (Epic 1)
│  ├─ projects/               # Projekte/Aufschlüsse (Epic 2)
│  ├─ measurements/           # Liste, Detail, Suche/Filter (Epic 2)
│  ├─ media/                  # Galerie, Skizzen, Annotation (Epic 2)
│  ├─ export/                 # Export/Import-Wizard (Epic 3)
│  ├─ stereonet/              # Auswertung (R2)
│  ├─ map/                    # Karte/Linework/Tracking (R3)
│  └─ settings/               # Kalibrierung, Sprache, Sicherheit
└─ build-logic/               # Convention Plugins
```

**KMP-Strategie:** `core/domain`, `core/geology`, `core/wmm`, `core/ports` liegen in `commonMain`.
Später: iOS-App ergänzt `iosMain`-Implementierungen der Ports + SwiftUI — Logik unverändert.
Compose, Sensoren, Room und MapLibre bleiben Android-spezifisch.

---

## 4. Kernstück: Messpipeline (Genauigkeit)

### 4.1 Komponenten

```
SensorPort (Flow<SensorSample>)
        │  accel, gyro, magnetometer, pressure, fused-rotation
        ▼
┌───────────────────────────────────────────────┐
│ OrientationFusion                              │
│  - Tilt/Roll/Pitch aus Accel+Gyro (driftkorrigiert)│
│  - Azimuth aus Magnetometer+Accel (kalibriert) │
│  - Quaternion-basierte Gerätelage              │
├───────────────────────────────────────────────┤
│ CalibrationManager                             │
│  - Hard-/Soft-Iron (Ellipsoid-Fit → Offset+3x3)│
│  - Bump/Contact-Plane-Offset (Kamerabump)      │
├───────────────────────────────────────────────┤
│ DeclinationResolver (WMM, offline)             │
│  - Position/Zeit → Deklination/Inklination     │
├───────────────────────────────────────────────┤
│ InterferenceDetector                           │
│  - |B| vs. WMM-Erwartung, Android-Genauigkeitsstufe│
├───────────────────────────────────────────────┤
│ MeasurementAverager                            │
│  - n Samples, Ausreißer (>Schwelle) verwerfen  │
│  - Mittelung als Vektor/Plane (nicht Winkel!)  │
├───────────────────────────────────────────────┤
│ AttitudeCalculator                             │
│  - Gerätelage → Fläche (Dip/DD bzw. RHR S/D)   │
│  - Gerätelage → Linear (Trend/Plunge)          │
│  - Notation & Konventionen                     │
├───────────────────────────────────────────────┤
│ QualityScorer                                  │
│  - Restunsicherheit, Stabilität, Kalibierzustand│
└───────────────────────────────────────────────┘
        ▼
   MeasurementResult { values, notation, quality, position, declination, northRef, history }
```

### 4.2 Rechenkern (Details)

- **Tilt-Kompensation & Vertikalfestigkeit:** Statt einzelner Euler-Winkel wird die **volle
  Rotationsmatrix/Quaternion** verwendet. Flächennormale = Geräte-Z-Achse (bzw. Auflagefläche)
  im Weltsystem; Dip = Winkel der Normalen zur Lotrechten, Dip-Direction = Azimut der
  Projektion der Falllinie. Dadurch wird der **„Vertical-Orientation-Bug"** (Clino) vermieden.
- **Azimut-Problem adressieren:** Azimut ist der fehleranfälligste Wert (Hard-/Soft-Iron, Bewegung).
  Daher: (a) Kalibrierpflicht-Workflow, (b) Live-Interferenzanzeige, (c) Mittelung über n Samples,
  (d) Qualitätswarnung, (e) Schnellvergleich/Kreuzmessung.
- **Mittelung richtig:** Flächen/Linien werden als **Einheitsvektoren** gemittelt (Normale → Mittel­normale
  → Rückrechnung Dip/DD). Kein arithmetisches Mitteln von Winkeln (Vermeidung von Verwerfungen an 0°/360°).
- **Fisher-Statistik** liefert kappa/α95 → Eingang in `QualityScorer`.
- **Kalibrierung:**
  - Hard-/Soft-Iron: Ellipsoid-Fit (Least Squares) über Rotationsproben → Offset **b** + Matrix **W**;
    Anwendung `B_corr = W · (B_raw − b)`.
  - Bump/Contact-Plane: geführte Messung auf bekannter Ebene/Referenz → Offset zwischen Kontaktfläche
    und Sensorzentrum; Korrekturgröße persistent pro Gerät.
- **Deklination:** WMM-Koeffizienten mitgeliefert, `DeclinationResolver` liefert Deklination
  für (lat, lon, höhe, datum) offline; Ausgabe von **magnetisch und geografisch** Nord; Wert am Datensatz gespeichert.
- **Interferenz:** Vergleich `|B_measured|` mit WMM-Totalfeld **F**; Abweichung > Toleranz → Warnung.
- **Sensoren ohne Magnetometer/fehlerhaft:** globaler manueller Modus (US-1.12) — Pipeline liefert
  dann direkt manuelle Werte.

> **Testbarkeit:** Alle Eingänge laufen über `SensorPort`. Reale Sensoraufzeichnungen (JSON) und
> synthetische Datensätze (bekannte Lagen) erlauben reproduzierbare Unit-Tests der gesamten Kette.

### 4.3 Messzustandsautomat

```
Idle → Calibrating(? ) → Measuring → Stabilizing(n/quality) → Held → (Save | Discard) → Idle
                                   └─ Interference/PoorQuality → Warnung → Measuring
```

---

## 5. Datenmodell & Persistenz

### 5.1 Room-Schema (Auszug)

| Tabelle | Schlüsselfelder |
|---|---|
| `Project` | id, name, author, description, crs, createdAt, updatedAt |
| `Site` (Lokalität/Aufschluss) | id, projectId, name, lat, lon, elev, accuracy, description |
| `Measurement` | id, siteId, kind(PLANE/LINE/BEARING), dip, dipDirection, strike, trend, plunge, notation, northRef, declination, quality(score, sensorAcc, stability, n, sigma, kappa), lat, lon, elev, accuracy, posManual, typeId, unitId, note, createdAt, updatedAt |
| `MeasurementHistory` | id, measurementId, field, oldValue, newValue, at, actor |
| `DataType` | id, name, color, symbolRef |
| `Unit` (Stratigraphie) | id, name, parentId, code, rank |
| `Media` | id, ownerType, ownerId, uri, kind(PHOTO/VIDEO/SKETCH), lat, lon, bearing, encrypted, createdAt |
| `Note` | id, ownerType, ownerId, html, createdAt |
| `FilterPreset` | id, projectId, name, json |
| `Calibration` | id, deviceFingerprint, hardIron(b), softIron(3x3), bumpOffset, at, quality |
| `Linework` (R3) | id, projectId, kind(CONTACT/FAULT/POLYGON), geometryGeoJson, unitId |
| `Track` (R3) | id, projectId, startedAt, endedAt, geometryGeoJson |

- **Migrationen:** Room `Migration` je Schema­version, getestet (Migration-Tests).
- **Historie/Undo:** Änderungen werden in `MeasurementHistory` protokolliert → Undo/Redo + Audit
  (wichtig für Gutachter/Behörden, US-2.3/2.4).

### 5.2 Verschlüsselung

- **DB:** SQLCipher; Passphrase aus Android **Keystore** abgeleitet (kein Klartext-Secret).
- **Medien:** optional verschlüsselt im App-internen Speicher (AES-GCM, Schlüssel im Keystore).
- **Export-Archiv:** AES-GCM + passwortbasierte Schlüsselableitung (Argon2id/PBKDF2); Integritätsprüfung (Hash).

### 5.3 Dateispeicher (Scoped Storage)

```
.../files/projects/{projectId}/
   ├─ media/…            # Fotos/Videos/Skizzen
   ├─ exports/…          # erzeugte Exporte
   └─ archive/…          # Projektarchive
```
Kein Zugriff auf fremde Verzeichnisse; Teilen nur über Saf/Share-Sheet.

---

## 6. Export / Import (Epic 3)

**Pipeline:** `Query → Mapper → Encoder → Validator → Writer/Share`

| Format | Encoder | Besonderheit |
|---|---|---|
| CSV | konfigurierbare Spalten, Locale, UTF-8 | Round-Trip-fähig (Import) |
| GeoJSON | Punkte + Properties + Symbolik | GIS (QGIS) |
| KML/KMZ | Placemarks + Styles + Medien-Refs | Google Earth |
| PDF | Deckblatt, Tabelle, Diagramm-Snapshots (ab R2/R3) | Dokumentation (GB) |
| Projektarchiv | `.pyxis` (Manifest + DB-Export + Medien), AES-GCM | vollständig, portabel |

- **Import:** CSV/KML mit Spalten-/Notationsmapping (DD/Dip ↔ RHR ↔ Quadrant) + Vorschau.
- **Validator:** prüft Wertebereiche, Koordinaten, Notation, Dubletten → verhindert korrupte Exporte (Clino-Schmerzpunkt #2).

---

## 7. Auswertungs-Engine (Epic 4, R2)

Reines Kotlin in `:core:geology` (KMP-fähig), vollständig deterministisch und referenzvalidiert.

- **Konvertierungen:** (Dip-Direction, Dip) ↔ (RHR-Strike, Dip) ↔ Normale/Pole ↔ (Trend, Plunge).
- **Projektionen:** Equal-Area (Schmidt/Lambert), Equal-Angle (Wulff), Lower Hemisphere.
- **Plots:** Großkreise, Pole, Lineare.
- **Statistik:** Fisher-Mittel (R̄, κ, α95), Orientierungstensor + Eigenzerlegung (S1/S2/S3),
  Woodcock **K/C**, **Kamb-Dichte-Konturen** (Vollmer-Kernel), Girdle/**Fold-Axis** (π),
  Mean Plane, Zwei-Ebenen-Intersektion (β-Achse).
- **Zusatz:** Rose-Diagramm, Dip-Histogramm, Winkelmaße, Rotationen, kinematische Hangstabilität.
- **Darstellung:** eigenes Compose-`Canvas` (performant, exportierbar als SVG/PNG).
- **Validierung:** Golden-Tests gegen Referenz­implementationen (InnStereo, Stereonet Mobile, GeoStereonet).

```
geology/
 ├─ math/ (Vectors, Quaternion, Matrix3, Tensor)
 ├─ projection/ (Schmidt, Wulff)
 ├─ stats/ (Fisher, Eigen, Kamb, Woodcock)
 ├─ analysis/ (Girdle, FoldAxis, MeanPlane, Intersection, Rotation)
 └─ models/ (Plane, Lineation, Dataset)
```

---

## 8. Karte, Linework, Tracking (Epic 5, R3)

- **Engine:** MapLibre Native (Android) mit Style von OSM-Vektor-/Rasterdaten.
- **Offline:** MBTiles/PMTiles-Import; Kachel-/Cache-Verwaltung; „Region vorab laden".
- **Marker:** Messpunkte mit Symbolik aus `DataType`; Clustering bei Massendaten.
- **Filter:** nach Einheit/Typ/Qualität (deckt Clino-Lücke #6).
- **Linework:** Geometrien als GeoJSON/WKB in `Linework`-Tabelle; Werkzeuge: zeichnen,
  Punkte hinzufügen/verschieben/löschen; **volle Android-Parität**.
- **Tracking:** Foreground-Service, `LocationProvider`, Pfadaufzeichnung (US-5.6),
  Export als GPX/GeoJSON; Akku-optimiert (Distanz-/Zeit-Threshold).
- **Tablet-Layout:** adaptive Compose-Layouts (WindowSizeClass).

---

## 9. Querschnitt: DI, Concurrency, Fehlerbehandlung

- **DI:** Hilt-Module binden Ports → Implementierungen (Test/Prod austauschbar).
- **Concurrency:** Sensor-/GPS-Callbacks → `callbackFlow`; Datenbank-IO auf `Dispatchers.IO`;
  Messpipeline auf dediziertem Single-Thread-Dispatcher (deterministische Reihenfolge).
- **Fehler:** `Result`-Typ für Fachfehler (Kalibrierung fehlt, Interferenz, Pos. unplausibel);
  Nutzerfeedback in Domänensprache; Crash-sicheres Autosave via Transaktionen.

---

## 10. Sicherheit, Datenschutz, Compliance

- Keine Analytics/Crash-Reporting-SDKs; keine Werbe-/Tracking-Bibliotheken.
- Daten verlassen das Gerät nur per explizitem Export/Teilen.
- Vollständige Löschung (Projekt/Daten) real umgesetzt (Clino: „can't be deleted").
- Scoped Storage, kein Overreach; Berechtigungen minimal (Kamera nur bei Nutzung, Standort kontextbezogen).
- GPLv3: Lizenz-/Copyright-Header, NOTICE, Drittlizenz-Inventar (FOSS-Liste).

---

## 11. Internationalisierung (DE/EN)

- Compose `stringResource`; Default-Ressourcen Englisch (`values/`), Deutsch (`values-de/`).
- Zahlen-/Datums-/Koordinatenformate lokalisiert; Geologie-Notationen davon unabhängig.
- Übersetzungs-Workflow für Community (Open Source).

---

## 12. Teststrategie

| Ebene | Inhalt | Werkzeug |
|---|---|---|
| Domäne (Unit) | Vektor-/Stereonet-/Statistik-Mathematik, Konvertierungen | JUnit + property-based Tests |
| Messpipeline | Aufgezeichnete + synthetische Sensorsignale, Kalibrierung, Mittelung | JUnit |
| Golden/Referenz | Stereonet/Statistik vs. InnStereo/Stereonet Mobile | Snapshot-Vergleich |
| Daten | DAOs, Migrationen, Undo/History, Export-Round-Trip | Room-Test, JUnit |
| UI | Compose-Screens, Tablet-Layouts | Compose UI-Test |
| Instrumentierung | reale Sensoren/GPS/Kamera (Smoke) | AndroidX Test |
| Manuell/Feld | Kalibrierszenarien, Interferenzquellen, Outdoor | Testprotokoll |

**DoD-Beispiel (Messung):** Unit + Pipeline-Tests grün, Genauigkeitsziel E1 in Laborprüfung erreicht, Dark-Mode/Tablet geprüft, DE/EN übersetzt, Accessibility-Check.

---

## 13. Build, CI/CD, Release

- **Gradle Kotlin DSL** + Convention Plugins in `build-logic`.
- **CI (GitHub Actions):** `assemble`, `testDebugUnitTest`, Lint, ktlint/detekt, Compose-Tests,
  FOSS-Lizenzprüfung; Artefakte pro Commit.
- **Release:** signierte APK/AAB für Google Play **und** F-Droid-Metadaten; reproduzierbare Builds anstreben.
- **Branching:** Trunk-based; Feature-Branches; Changelog automatisiert.
- **Governance:** CONTRIBUTING, CODE_OF_CONDUCT, Issue-/PR-Templates, Roadmap im Repo.

---

## 14. Performance & Energie

- Sensor-Sampling adaptiv (nur bei aktiver Messung/„kontinuierlich").
- GPS nur bei Bedarf; `Fused`/gebündeltes Polling; Tracking mit Thresholds.
- DB-Indizes auf häufige Filter (Projekt, Typ, Einheit, Zeit).
- Diagramme/Karten mit Downsampling/Caching für große Datensätze (NFA-3).
- Baseline Profiles für schnellen Start.

---

## 15. Technische Risiken & Gegenmaßnahmen

| Risiko | Auswirkung | Gegenmaßnahme |
|---|---|---|
| Sensorqualität variiert stark | Genauigkeitsziel E1 nicht überall | Kalibrierung, Qualitätsanzeige, manueller Modus, dokumentierte Grenzen |
| WMM-Aktualität | Deklinationsfehler über Jahre | Koeffizienten aktuell bündeln + Update-Mechanismus |
| MapLibre-Offline-Komplexität | R3-Verzögerung | MVP entkoppelt; Karten als eigenes Modul |
| SQLCipher-Performance | Latenz bei Massendaten | Indizes, Stapel-Transaktionen, Profiling |
| KMP-Grenzen | Umbauaufwand bei iOS | Domäne strikt Android-frei halten, Ports abstrahieren |
| Rechts-/Nachweis-Anforderungen (Behörden) | Akzeptanz | Audit-Historie, unsigned/export­bare Rohdaten, dokumentierte Algorithmen |

---

## 16. Offene technische Punkte

1. **Kartenstil/Quelle** für MapLibre (OSM-Vektor vs. eigene Tiles) + Lizenz-/Attributionsfragen.
2. **PDF-Engine** (eigenes Rendering vs. Bibliothek) unter GPLv3-Kompatibilität.
3. **WMM-Aktualisierungspfad** (manuell/optionaler Download).
4. **Crash-Telemetrie** bewusst offen lassen (Datenschutz) → lokale Logs auf Nutzerwunsch exportierbar.
5. **Performance-Budgets** konkretisieren (Startzeit, Messlatenz, Akku %/h im Tracking).
6. **KML/KMZ-Medieneinbettung** (Größenlimits, Verweise).
