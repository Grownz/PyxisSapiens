# PyxisSapiens

Geologenkompass für Android mit Datenverwaltung, -auswertung, -darstellung und -verknüpfung.
Offline-first, Open Source (GPLv3), zweisprachig DE/EN, ausgelegt für Profis, Gutachter und Behörden.

[![CI](https://github.com/Grownz/PyxisSapiens/actions/workflows/ci.yml/badge.svg)](https://github.com/Grownz/PyxisSapiens/actions/workflows/ci.yml)
[![License: GPLv3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20(API%2026)-3DDC84.svg)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF.svg)](#)

<p align="center">
  <img src="docs/assets/icon-512.png" width="512" height="512" alt="PyxisSapiens App-Icon">
</p>

## Funktionen im Überblick

PyxisSapiens vereint Messung, Datenverwaltung, Auswertung, Kartierung und Interoperabilität in
einer **offline-fähigen** App. Alle Daten bleiben **lokal auf dem Gerät**, verschlüsselt und ohne
Konto.

- 🧭 **Präzise Messung** – Kompass-Klinometer für Flächen und Lineare, WMM2025-Deklination, geführte Kalibrierung, Qualitätsbewertung
- 🗂️ **Datenverwaltung** – Projekte, Aufschlüsse, Messungen, Medien, Feld-Historie mit Undo/Redo
- 📊 **Auswertung** – Stereonet, Statistik, Dichte-Konturen, Rose-/Dip-Diagramme
- 🗺️ **Karte & Kartierung** – MapLibre, Offline-Basiskarten, Linework, GPS-Tracking
- 🔄 **Export & Import** – CSV, GeoJSON, KML/KMZ, PDF-Bericht, verschlüsseltes Projektarchiv
- 🔒 **Datenschutz** – SQLCipher-Verschlüsselung, keine Telemetrie, vollständige Löschbarkeit

## Kernfunktionen

### 🧭 Messung: Kompass-Klinometer
Digitaler Kompass-Klinometer für **planare und lineare** Strukturen. Die Orientierung wird aus
einer **Accelerometer- + Magnetometer-Fusion** berechnet, wobei die **Hard-/Soft-Iron-Korrektur
direkt in den Azimut** einfließt; ohne Magnetometer greift der fusionierte Rotationsvektor. Die
**Deklination** stammt aus dem **WMM2025** (offline, gegen die NOAA-Testwerte validiert).
Messungen werden **vektoriell gemittelt** (Ausreißerfilter), mit **Fisher-κ** und einem
**Qualitätsscore** versehen und mit GPS-Position, Fotos und Notizen verknüpft.

### 🗂️ Datenverwaltung
Struktur **Projekte → Aufschlüsse → Messungen** mit frei definierbaren **Datentypen** und einem
**Stratigraphie-/Einheiten-Katalog**. Vollständige Bearbeitung, **Feld-Historie mit Undo/Redo**,
Validierung und **echte Löschbarkeit** (inklusive zugehöriger Medien).

### 📊 Auswertung: Stereonet & Statistik
Interaktives **Stereonet** (Schmidt/Wulff) mit Großkreisen und Polen, **Kamb-** und
**Fisher-Kernel-Dichte**, **Fisher-Statistik** (κ, R̄, α95), **Eigenvektoren/Woodcock**,
**Girdle-/Fold-Achse**, **mittlerer Fläche**, **β-Achse**, **Rose-** und **Dip-Histogramm**;
Export als **SVG/PNG**.

### 🗺️ Karte, Linework & Tracking
**MapLibre**-Karte mit Messpunkten (Filter nach Datentyp), **Offline-Basiskarten** aus
importierten **MBTiles**, **Linework** zeichnen/bearbeiten (Kontakte, Störungen, Flächen) und
**GPS-Tracking** im Hintergrund mit **GPX-Export**.

### 🔄 Export, Import & Verknüpfung
Export als **CSV, GeoJSON, KML, KMZ, PDF-Bericht** und **verschlüsseltes Projektarchiv (.pyxis)**;
Import von **CSV/KML** sowie **GeoJSON/KML-Linework**; Teilen über das **Android-Share-Sheet**.
Optional: **Macrostrat-Lookup** und ein konfigurierbarer **geologischer WMS/XYZ-Layer**.

### 🔒 Datenschutz & Offline-First
Rein lokale Datenhaltung, **verschlüsselte Datenbank** (SQLCipher mit Android-Keystore-Schlüssel),
optional zusätzliche **Passphrase**, **keine Konten/Telemetrie** und vollständige **Löschbarkeit**.

### 🎛️ Kalibrierung & Feldtauglichkeit
Geführter **Assistent** (Figur-8 für Hard-/Soft-Iron, Auflageflächen-/Bump-Korrektur,
Sensor-Test), **Instrument-Design** mit Dark-Default und Hochkontrast-Modus, große Touchziele,
handschuh- und sonnenlichttauglich sowie zweisprachig **DE/EN**.

## Build

Voraussetzung: **JDK 17+** (z. B. das in Android Studio enthaltene JBR), Android SDK mit
**Platform API 37** und **Build-Tools 36.0.0**. Gradle kommt über den Wrapper.

`JAVA_HOME` auf eine JDK-17+-Installation setzen (beispielhaft; an die eigene Umgebung anpassen):

```bash
# Linux / macOS
export JAVA_HOME=/path/to/jdk-17
# Windows (PowerShell)
# $env:JAVA_HOME = 'C:\path\to\jdk-17'

./gradlew assembleDebug      # Debug-APK        (Windows: .\gradlew.bat assembleDebug)
./gradlew test               # alle Unit-Tests (inkl. Robolectric)
./gradlew lint               # Android Lint
./gradlew assembleRelease    # R8-Release (Signing-Platzhalter)
```

Für echtes Release-Signing `keystore.properties.example` → `keystore.properties` kopieren und
ausfüllen (git-ignoriert).

## Beitragen

Beiträge sind willkommen. Siehe [`CONTRIBUTING.md`](CONTRIBUTING.md) und den
[`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md). Sicherheitslücken bitte gemäß
[`SECURITY.md`](SECURITY.md) privat melden. Änderungen: [`CHANGELOG.md`](CHANGELOG.md).

## Lizenz

GNU General Public License v3.0 — siehe [`LICENSE`](LICENSE).
