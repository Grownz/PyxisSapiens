# PyxisSapiens

Geologenkompass für Android mit Datenverwaltung, -auswertung, -darstellung und -verknüpfung.
Offline-first, Open Source (GPLv3), zweisprachig DE/EN, ausgelegt für Profis, Gutachter und Behörden.

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
