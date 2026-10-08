# PyxisSapiens

Geologenkompass für Android mit Datenverwaltung, -auswertung, -darstellung und -verknüpfung.
Offline-first, Open Source (GPLv3), zweisprachig DE/EN, ausgelegt für Profis, Gutachter und Behörden.

[![CI](https://github.com/OWNER/PyxisSapiens/actions/workflows/ci.yml/badge.svg)](https://github.com/OWNER/PyxisSapiens/actions/workflows/ci.yml)
[![License: GPLv3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20(API%2026)-3DDC84.svg)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF.svg)](#)

> Hinweis: `OWNER` in den Badges/Links durch den GitHub-Nutzer/-Org ersetzen (siehe [`docs/17_GitHub-Setup.md`](docs/17_GitHub-Setup.md)).

## Dokumentation

| Dokument | Inhalt |
|---|---|
| [`docs/01_Markt-und-Funktionsanalyse.md`](docs/01_Markt-und-Funktionsanalyse.md) | Wettbewerbsanalyse (FieldMove Clino, GeoCompass, Field Geology, u. a.), Nutzerfeedback, Genauigkeitsgrundlagen |
| [`docs/02_Lastenheft-und-Backlog.md`](docs/02_Lastenheft-und-Backlog.md) | Lastenheft: Epics, User Stories, NFA, Roadmap R1–R4, MVP-Backlog |
| [`docs/03_Technische-Architektur.md`](docs/03_Technische-Architektur.md) | Module, Schichten, Messpipeline, Persistenz/Verschlüsselung, Auswertungs-Engine, Karte, CI |
| [`docs/04_UI-UX-Konzept.md`](docs/04_UI-UX-Konzept.md) | Design-System, Navigation, Messablauf, Screens, Feldtauglichkeit, Accessibility |
| [`docs/05_Scaffold-Status.md`](docs/05_Scaffold-Status.md) | Umgesetztes Gradle-Multimodul-Gerüst, Toolchain-Versionen, Build-Verifikation |
| [`docs/06_Design-System.md`](docs/06_Design-System.md) | Tokens, JetBrains-Mono-Typografie, Geologie-Icons, Komponenten-Inventar, Galerie |
| [`docs/07_Messkern.md`](docs/07_Messkern.md) | WMM2025, Messpipeline, Sensoranbindung, Persistenz, Mess-UI, Verifikation |
| [`docs/08_Stereonet-Auswertung.md`](docs/08_Stereonet-Auswertung.md) | Projektionen, Statistik, Dichte (Kamb/Fisher), interaktive Auswertung, SVG/PNG-Export |
| [`docs/09_Export-Import.md`](docs/09_Export-Import.md) | CSV/GeoJSON/KML/KMZ/PDF/Archiv, Verschlüsselung, Import-Mapping, Share-Sheet |
| [`docs/10_Karte-Tracking.md`](docs/10_Karte-Tracking.md) | MapLibre, Linework, Tracking/GPX, Standort, MBTiles-Import, DB-Migration |
| [`docs/11_Datenhoheit.md`](docs/11_Datenhoheit.md) | SQLCipher+Keystore, Auto-Verschlüsselung/Re-Key, Historie/Undo, Validierung, Löschung |
| [`docs/12_Kalibrierung-Datenpflege.md`](docs/12_Kalibrierung-Datenpflege.md) | Kalibrier-Assistent, Datentypen/Einheiten, manuelle Eingabe, Filter |
| [`docs/13_Karte-Tracking-Nachzug.md`](docs/13_Karte-Tracking-Nachzug.md) | Offline-MBTiles-Rendering, Foreground-Tracking, Linework-Bearbeitung, Basiskarten-Verwaltung |
| [`docs/14_Genauigkeit-RohMag-Heading.md`](docs/14_Genauigkeit-RohMag-Heading.md) | Accel+Mag-Fusion mit Hard/Soft-Iron-Korrektur, Fallback Rotationsvektor |
| [`docs/15_Verknuepfung.md`](docs/15_Verknuepfung.md) | Geologischer WMS/XYZ-Layer, Macrostrat-Lookup, GeoJSON/KML-Import, kein Backend |
| [`docs/16_Release-Politur.md`](docs/16_Release-Politur.md) | R8-Release + Signing, App-Icon, Robolectric-Tests, i18n-Grundgerüst |
| [`docs/17_GitHub-Setup.md`](docs/17_GitHub-Setup.md) | Repository anlegen, Remote/Push, Secrets-Hinweise, CI |

## Festgelegte Entscheidungen

- **Zielgruppe:** Profis / Gutachter / Behörden zuerst
- **Modell:** komplett kostenlos, Open Source, **GPLv3**, keine Werbung/Tracking
- **MVP:** Messkern + Datenverwaltung (Auswertung R2, Karte R3, Verknüpfung R4)
- **Datenhaltung:** rein lokal, offline-first
- **Plattform:** Android-first (`minSdk 26`), Domänenschicht **KMP-fähig**
- **Technik:** Kotlin, Jetpack Compose, Room + SQLCipher, MapLibre Native
- **Sprache:** Deutsch + Englisch
- **UX:** Instrument-Charakter, Compass-first, Auto-Stabilisierung + Bestätigen, Dark-Default, Bottom-Navigation, geführte Kalibrierung

## Roadmap

| Release | Inhalt |
|---|---|
| **R1 (MVP)** | Genauer Kompass-Klinometer + Datenverwaltung + Export (CSV/GeoJSON/KML/KMZ/Archiv) |
| **R2** | Auswertung: Stereonet, Statistik, Rose/Histogramm, Hangstabilität |
| **R3** | Karte, Offline-Basiskarten, Linework, GPS-Tracking |
| **R4** | Verknüpfung/Interoperabilität/Erweiterbarkeit |

## Build

Voraussetzung: **JDK 17+** (Android-Studio-JBR genügt), Android SDK mit **Platform API 37** und
**Build-Tools 36.0.0**. Gradle kommt über den Wrapper.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug      # Debug-APK
.\gradlew.bat test               # alle Unit-Tests (inkl. Robolectric)
.\gradlew.bat lint               # Android Lint
.\gradlew.bat assembleRelease    # R8-Release (Signing-Platzhalter)
```

Für echtes Release-Signing `keystore.properties.example` → `keystore.properties` kopieren und
ausfüllen (git-ignoriert).

## Beitragen

Beiträge sind willkommen. Siehe [`CONTRIBUTING.md`](CONTRIBUTING.md) und den
[`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md). Sicherheitslücken bitte gemäß
[`SECURITY.md`](SECURITY.md) privat melden. Änderungen: [`CHANGELOG.md`](CHANGELOG.md).

## Lizenz

GNU General Public License v3.0 — siehe [`LICENSE`](LICENSE).

