# PyxisSapiens — Lastenheft & Product Backlog (Teil a)

**Dokumentversion:** 0.1
**Stand:** Oktober 2026
**Basis:** `docs/01_Markt-und-Funktionsanalyse.md`
**Charakter:** Anforderungsspezifikation (Lastenheft) + priorisiertes Product Backlog

---

## 1. Zweck, Geltungsbereich, Referenzen

**Zweck:** Verbindliche, priorisierte Anforderungen an die Android-App *PyxisSapiens* —
ein Geologenkompass mit Datenverwaltung, -auswertung, -darstellung und -verknüpfung.

**Geltungsbereich:** Android-App (Smartphone **und** Tablet), lokal/offline-first,
zweisprachig DE/EN, Open Source. Diese Version legt Anforderungen und Release-Schnitt fest.

**Referenzen:**
- `01_Markt-und-Funktionsanalyse.md` (Wettbewerb, Feedback, Genauigkeitsgrundlagen)
- Novakova & Pavlis (2017); Allmendinger et al. (2017); NXP AN4246 (Kalibrierung)
- WMM (World Magnetic Model) der NOAA/BGS

---

## 2. Festgelegte Rahmenbedingungen (aus Entscheidungen)

| # | Entscheidung | Auswirkung |
|---|---|---|
| R-1 | **Zielgruppe: Profis, Gutachter, Behörden zuerst** | Höchste Genauigkeit, Nachvollziehbarkeit, Export-/Dokumentationsqualität, keine „Spielzeug"-Elemente |
| R-2 | **Komplett kostenlos / Open Source** | Kein Feature-Gating, keine Werbung, kein Tracking; Lizenz, Governance, CI, Reproduzierbarkeit |
| R-3 | **MVP = Messkern + Datenverwaltung** | Auswertung (R2), Karte/Linework/Tracking (R3), Verknüpfung (R4) folgen |
| R-4 | **Rein lokal, offline-first** | Keine Serverabhängigkeit; Exporte tragen Interop; optionaler Sync = Out of Scope |
| R-5 | **Zuerst nur Android** | Natives Fundament; keine Cross-Platform-Kompromisse |
| R-6 | **Deutsch + Englisch** | i18n von Anfang an (Ressourcen, Zahlen-/Datumsformate) |

**Empfohlene Festlegungen (noch zu bestätigen, siehe §11):** Lizenz (Vorschlag **GPLv3**),
`minSdk`, Offline-Kartenformat, Signatur-/Verschlüsselungsverfahren.

---

## 3. Rollen / Stakeholder

| Rolle | Kürzel | Beschreibung |
|---|---|---|
| Feldgeologe | FG | Führt Messungen und Kartierung im Gelände durch |
| Gutachter / Behörde | GB | Bewertet Datenqualität, Nachvollziehbarkeit, Berichte |
| Datenmanager | DM | Importiert/exportiert, pflegt Stratigraphie und Austausch |
| Studierender | ST | Lernt Messung und Auswertung |
| Ausbilder | AB | Betreut Lehre, benötigt Konsistenz und Erklärbarkeit |
| Contributor | C | Entwickler/Kartenersteller/Übersetzer (Open Source) |

---

## 4. Produktvision & Erfolgskriterien

**Vision:** Der genaueste, vertrauenswürdigste und funktional umfangreichste digitale
Geologenkompass für Android — verbindet Feldmessung, Datenverwaltung, tiefe strukturelle
Auswertung, Kartendarstellung und Interoperabilität in einer offline-fähigen, quelloffenen App.

**Erfolgskriterien (messbar):**
- **E1 Genauigkeit:** Dip-Fehler ≤ ±2°, Azimut-Fehler ≤ ±3° (95 %) bei kalibriertem, ruhigem Gerät.
- **E2 Stabilität:** ≥ 99,9 % crash-freie Sitzungen; **kein Datenverlust** durch Absturz (Autosave).
- **E3 Vertrauen:** Genauigkeits-/Interferenzanzeige bei jeder Messung sichtbar; Deklination nachvollziehbar.
- **E4 Datenhoheit:** 100 % lokale Nutzbarkeit; vollständiges Löschen möglich; Export ohne Backend.
- **E5 Interop:** vollständiger Export (CSV, GeoJSON/KML/KMZ) öffnet fehlerfrei in QGIS/Google Earth.
- **E6 Bedienbarkeit:** Kernmesstätigkeiten mit Handschuh/Sonnenlicht in ≤ 3 Interaktionen.

---

## 5. Funktionale Anforderungen (Epics als User Stories)

Format: **US-ID · Priorität (MoSCoW, MVP-bezogen) · Story · Akzeptanzkriterien (AK)**
Priorität bezieht sich auf das **MVP**; Release-Zuordnung in §8.

### EPIC 1 — Kompass-Klinometer (Messung)

- **US-1.1 [Must] Flächenmessung**
  *Als FG möchte ich die Orientierung einer Fläche (Schicht-/Flächenlage) messen, damit ich Strike/Dip bzw. Dip-Direction/Dip erfasse.*
  AK: Gerät flach anlegen → Ergebnis stabil; Ausgabe konfigurierbar (Strike/Dip **oder** Dip-Direction/Dip); RHR-Konvention; Einheit Grad; ±0,1°-Auflösung.

- **US-1.2 [Must] Linearmessung**
  *Als FG möchte ich Lineare (Trend/Plunge) messen.*
  AK: Erfassung Trend (0–360°) + Plunge (0–90°); Anzeige und Speicherung wie US-1.1.

- **US-1.3 [Should] Peilung / Handkompass**
  *Als FG möchte ich eine Peilung (bearing) nehmen, damit ich Richtungen anvisiere.*
  AK: Anvisieren über Kante/Gerät; Zielpeilung stabilisierbar (Hold).

- **US-1.4 [Must] Messmodi (Einzel / kontinuierlich / Mittelung)**
  *Als FG möchte ich wählen, ob ich eine Einzelmessung, eine laufende Anzeige oder eine gemittelte Messung erhalte.*
  AK: Modus wechselbar; Mittelung über konfigurierbares n (Default ≥ 10, bis ~40); verwerfe Einzelwerte > Schwellwert (Default 3°); Anzeige von Mittelwert + Streuung.

- **US-1.5 [Must] Genauigkeits-/Stabilitätsindikator**
  *Als GB möchte ich sehen, wie zuverlässig eine Messung ist, damit ich ihr vertrauen kann.*
  AK: Anzeige Sensorgenauigkeitsstufe + Stabilität (ruhig/bewegt) + Restunsicherheit; Speicherung des Qualitätswerts am Datensatz.

- **US-1.6 [Must] Magnetische Interferenzwarnung**
  *Als FG möchte ich vor magnetischen Störungen gewarnt werden.*
  AK: Live-µT-Anzeige, Warnung außerhalb plausibler Feldstärke; Vorschlag Figur-8-Kalibrierung.

- **US-1.7 [Must] Kalibrierung (Hard/Soft-Iron)**
  *Als FG möchte ich das Magnetometer geführt kalibrieren.*
  AK: Geführter Figur-8-/Rotationsablauf; Bewertung des Ergebnisses (µT/Radius); Speicherung pro Gerät; Wiederholungsempfehlung.

- **US-1.8 [Must] Auflageflächen-/Bump-Kalibrierung**
  *Als FG möchte ich den Kamerabump/nicht-planare Rückseite kompensieren.*
  AK: Geführte Kalibrierung; Korrekturfaktor persistent, auf alle Messungen angewendet.

- **US-1.9 [Must] Deklination (WMM, offline)**
  *Als FG möchte ich automatisch geografisch Nord erhalten.*
  AK: WMM-Modell offline; Deklination aus Position berechnet; Umschaltung magnetisch/geografisch; Wert im Datensatz dokumentiert.

- **US-1.10 [Must] Manuelle Eingabe & Korrektur**
  *Als FG möchte ich Werte manuell eingeben oder nachträglich korrigieren.*
  AK: Messung ohne Sensor möglich; Bearbeiten aller Messwerte nachträglich; Änderungshistorie.

- **US-1.11 [Should] Notations-/Formatkonvertierung**
  *Als DM möchte ich Daten in verschiedenen Notationen ein-/ausgeben.*
  AK: Dip-Direction/Dip ↔ RHR-Strike/Dip ↔ Quadrant (N30W,20NE) konvertierbar.

- **US-1.12 [Should] Sensor-Deaktivierung (manueller Modus)**
  *Als FG mit schlechten Sensoren möchte ich rein manuell arbeiten.*
  AK: Sensoren global abschaltbar; manuelle Eingabe wird Standardpfad.

- **US-1.13 [Must] Messung halten/sperren**
  *Als FG möchte ich eine stabile Messung einfrieren, um sie in Ruhe zu prüfen.*
  AK: Hold/Lock; erst nach Bestätigung speichern; Verwerfen möglich.

- **US-1.14 [Should] Ausreißerbehandlung & Redundanz**
  *Als GB möchte ich, dass Messreihen gefiltert werden.*
  AK: Statistik (Mittel, Stdabw., n); Ausreißer markier-/ausschließbar; Rohdaten optional erhalten.

### EPIC 2 — Datenverwaltung

- **US-2.1 [Must] Projekte**
  *Als FG möchte ich Projekte anlegen/wechseln.*
  AK: Anlegen/umbenennen/duplizieren/löschen; Metadaten (Ort, Bearbeiter, Datum, Notiz).

- **US-2.2 [Must] Aufschlüsse/Lokalitäten**
  *Als FG möchte ich Messungen zu Aufschlüssen gruppieren.*
  AK: Aufschluss mit Position, Name, Beschreibung, Fotos; Messungen zuordnen/verschieben.

- **US-2.3 [Must] Messungen: volle CRUD + Undo**
  *Als FG möchte ich Datensätze jederzeit bearbeiten, damit mir keine Fehler bleiben.*
  AK: Erstellen/Bearbeiten/Löschen; **Undo/Redo**; Papierkorb; keine unwiederbringlichen Löschungen (vgl. Clino-Schmerzpunkt #3).

- **US-2.4 [Must] Autosave & Crash-Sicherheit**
  *Als FG möchte ich, dass nichts verloren geht.*
  AK: Kontinuierliches Speichern; Transaktionen; Wiederherstellung nach Absturz; kein Datenverlust (E2).

- **US-2.5 [Must] Fotos/Videos georeferenziert**
  *Als FG möchte ich Medien an Messungen/Aufschlüsse hängen.*
  AK: Kamera/Import stabil; Medien mit Position, Zeit, Richtung; Vorschau.

- **US-2.6 [Must] Textnotizen**
  *Als FG möchte ich Notizen erfassen.*
  AK: Rich-Text (fett/kursiv/Listen); an Messung/Aufschluss/Projekt; durchsuchbar.

- **US-2.7 [Should] Skizzen & Foto-Annotation**
  *Als FG möchte ich zeichnen und Bilder beschriften.*
  AK: Freihand/Formen/Text/Pfeile; Ebenen; speichern als verknüpftes Medium.

- **US-2.8 [Must] Datentypen/Tags**
  *Als FG möchte ich Datentypen (bedding, cleavage, fault, slickenside, foliation …) frei definieren.*
  AK: Anlegen/Bearbeiten; Farbe/Symbol; Filter danach.

- **US-2.9 [Should] Stratigraphie-/Einheitenkatalog**
  *Als DM möchte ich Einheiten pflegen und zuordnen.*
  AK: Einheiten-Hierarchie; optional internationaler Bezug; Import/Export.

- **US-2.10 [Should] Symbolik pro Datentyp**
  *Als FG möchte ich Symbole je Typ.*
  AK: Symbolbibliothek; eigene Symbole; konsistente Darstellung in Liste/Karte/Export.

- **US-2.11 [Must] Suche / Filter / Sortierung**
  *Als GB möchte ich Datensätze gezielt finden.*
  AK: Volltext; Filter (Projekt, Typ, Einheit, Qualität, Zeit, Position); Sortierung; gespeicherte Filter.

- **US-2.12 [Must] Position: Override & Validierung**
  *Als FG möchte ich Position prüfen/überschreiben.*
  AK: GPS + manueller Punkt; Plausibilitätswarnung bei Sprüngen/identischen Positionen (vgl. Clino-GPS-Bug #2); Genauigkeitsangabe je Datensatz.

- **US-2.13 [Should] Sammlungs-Dashboard**
  *Als GB möchte ich Überblick über die Datenlage.*
  AK: Anzahl Messungen je Typ/Einheit; Qualitätsverteilung; Abdeckung.

- **US-2.14 [Could] Proben-/Registerverwaltung**
  *Als FG möchte ich Proben mit Messungen verknüpfen.*
  AK: Probe mit ID, Lagerort, Analysebezug.

### EPIC 3 — Export / Import / Interop

- **US-3.1 [Must] CSV-Export (konfigurierbar)**
  *Als DM möchte ich Datensätze als CSV ausgeben.*
  AK: Spaltenauswahl; Dezimaltrennzeichen/Locale; UTF-8; validiertes Ergebnis.

- **US-3.2 [Must] GeoJSON/KML/KMZ-Export**
  *Als DM möchte ich Daten in GIS/Earth öffnen.*
  AK: Punkte + Symbolik; Foto-/Notiz-Referenzen; öffnet fehlerfrei in QGIS/Google Earth (E5).

- **US-3.3 [Must] Projektarchiv Export/Import**
  *Als DM möchte ich ein vollständiges, portables Projekt weitergeben.*
  AK: Archiv (Projekt + Medien) exportieren/importieren; **verschlüsselbar**; Integritätsprüfung.

- **US-3.4 [Should] Fremddaten-Import mit Mapping**
  *Als DM möchte ich bestehende Daten übernehmen.*
  AK: CSV/KML importieren; Spalten-/Notationsmapping; Vorschau.

- **US-3.5 [Should] PDF-Bericht**
  *Als GB möchte ich einen dokumentierten Bericht.*
  AK: Deckblatt/Metadaten; Tabelle; Karten-/Stereonet-Snapshots (ab R2/R3); reproduzierbar.

- **US-3.6 [Should] Teilen**
  *Als FG möchte ich Exporte teilen.*
  AK: Android-Share-Sheet (Drive, Mail, Messenger).

### EPIC 4 — Auswertung (Release 2)

- US-4.1 [R2] Stereonet (Schmidt/Wulff, Lower Hemisphere)
- US-4.2 [R2] Poles & Großkreise
- US-4.3 [R2] Dichte-Konturen (Kamb, Sigmawert)
- US-4.4 [R2] Fisher-Statistik (κ, R̄, α95)
- US-4.5 [R2] Eigenvektoren (S1/S2/S3, Woodcock K/C)
- US-4.6 [R2] Girdle/Fold-Axis (π-Diagramm)
- US-4.7 [R2] Mean Plane / Best-Fit
- US-4.8 [R2] Zwei-Ebenen-Intersektion (β-Achse)
- US-4.9 [R2] Winkelmaße, Rotationen, Poles↔Planes
- US-4.10 [R2] Rose-Diagramm & Dip-Histogramm
- US-4.11 [R2] Set-/Cluster-Analyse
- US-4.12 [R2] Kinematische Hangstabilitätsanalyse
- US-4.13 [R2] Filter/Datentypen im Plot, Export der Diagramme (SVG/PNG)
  AK (Epic 4 gesamt): Ergebnisse mathematisch konsistent mit etablierten Referenzen (z. B. InnStereo/Stereonet); Konventionen dokumentiert; Plots exportierbar.

### EPIC 5 — Karte, Linework, Tracking (Release 3)

- US-5.1 [R3] Kartenansicht (online) mit Messpunkten
- US-5.2 [R3] Offline-Basiskarten (Import MBTiles/GeoTIFF)
- US-5.3 [R3] Punktdarstellung mit Symbolik/Typ
- US-5.4 [R3] Filter nach Einheit/Typ/Qualität
- US-5.5 [R3] Linework (Kontakte/Störungen/Polygone) — **Android-Parität, editierbar**
- US-5.6 [R3] GPS-Tracking/Begehung von Kontakten
- US-5.7 [R3] Maßstab, Nordpfeil, Koordinatensystem, Zoom bis „fat-finger"-tauglich
  AK (Epic 5 gesamt): offline vollständig nutzbar; keine Feature-Lücke Android vs. iOS-Referenz; Tablet-Layout.

### EPIC 6 — Verknüpfung / Erweiterbarkeit (Release 4)

- US-6.1 [R4] Anbindung externer geologischer Kartendienste
- US-6.2 [R4] Stratigraphie-/Fachdatenbank-Verknüpfung (optional)
- US-6.3 [R4] Erweiterbarkeit (Plugin-/Format-Import)
- US-6.4 [R4] Optionales Teilen/Community (datenschutzkonform, opt-in)

---

## 6. Nicht-funktionale Anforderungen (NFA)

| ID | Kategorie | Anforderung |
|---|---|---|
| NFA-1 | Genauigkeit | Messziele gemäß E1; Restunsicherheit stets kommuniziert; Rohwerte nachvollziehbar |
| NFA-2 | Zuverlässigkeit | ≥ 99,9 % crash-frei; Autosave; keine Datenverluste; Transaktionsintegrität |
| NFA-3 | Performance | flüssige Sensor-Samplingrate; App-Start < 2 s; Mess-Reaktion < 100 ms; große Datensätze (>10.000 Punkte) performant |
| NFA-4 | Energie | sparsames Sensor-/GPS-Handling; messbar geringerer Verbrauch als Referenz-Apps im Feldmodus |
| NFA-5 | Offline-First | alle Kernfunktionen ohne Netz; Netz nur optional (Karten/WMM optional vorab geladen) |
| NFA-6 | Datenschutz | keine Telemetrie/Drittanbieter-Tracking; lokale Speicherung; vollständige Löschung; Export verschlüsselbar |
| NFA-7 | Sicherheit | Verschlüsselung sensibler Exporte; sichere Dateipfade (Scoped Storage) |
| NFA-8 | Bedienbarkeit | Handschuh-/Sonnenlicht-tauglich; große Touchziele; hoher Kontrast; Einhandbedienung |
| NFA-9 | Barrierefreiheit | TalkBack, Skalierung, Farbsehschwäche-taugliche Palette |
| NFA-10 | i18n | DE/EN vollständig; Format-/Einheitenlokalisierung; erweiterbar |
| NFA-11 | Kompatibilität | Android `minSdk`/`targetSdk` festzulegen; Smartphone + Tablet; Orbit-/Sensor-Abdeckung |
| NFA-12 | Wartbarkeit | modulare Architektur; automatisierte Tests; Doku |
| NFA-13 | Open Source | OSI-Lizenz; CI (Build/Test/Lint); reproduzierbare Builds; Contribution-Leitfaden; Issue-Templates |
| NFA-14 | Governance | klare Maintainer-/Review-Regeln; Roadmap transparent; CHANGELOG |
| NFA-15 | Lokalisierung Geodaten | korrekte Datumsbezüge (WGS84), Höhenmodell optional; keine falschen Präzisionen |

---

## 7. Grobes Datenmodell (konzeptionell)

```
Projekt
 ├─ Metadaten (Name, Autor, Zeitraum, Beschreibung, Koordinatensystem)
 ├─ Stratigraphie-Katalog (Einheiten)
 ├─ Datentyp-Definitionen (Symbol, Farbe)
 ├─ Lokalität / Aufschluss
 │    ├─ Position (GPS/manuell, Genauigkeit)
 │    ├─ Medien (Foto/Video/Skizze)
 │    ├─ Notizen
 │    └─ Messung*
 │          ├─ Art (Fläche | Linear | Peilung)
 │          ├─ Werte (Strike/Dip | DD/Dip | Trend/Plunge)
 │          ├─ Notation + Konvention (RHR etc.)
 │          ├─ Qualität (Sensorstufe, Stabilität, Restunsicherheit, n)
 │          ├─ Position (GPS/manuell, Genauigkeit, Override-Flag)
 │          ├─ Deklination + Nord-Referenz (magnetisch/geografisch)
 │          ├─ Typ/Tag + Einheit
 │          ├─ Medien + Notiz
 │          └─ Historie (Erstellung/Änderungen)
 └─ Export-/Import-Profile (Felder, Formate)
```

---

## 8. Release-Planung (Roadmap)

| Release | Inhalt | Kernnutzen |
|---|---|---|
| **MVP (R1)** | Epics 1+2 im Must-Umfang + Export (US-3.1/3.2/3.3) | Genauer, vertrauenswürdiger Mess- und Verwaltungskern, offline |
| **R2** | Epic 4 (Auswertung) + US-3.5 | Strukturanalyse & Berichte in einer App |
| **R3** | Epic 5 (Karte/Linework/Tracking) | Vollwertige Kartierung auf Android/Tablet |
| **R4** | Epic 6 (Verknüpfung/Erweiterbarkeit) | Interoperabilität/Ökosystem |

---

## 9. Priorisiertes Backlog (MVP-Schnitt, top-down)

| Rang | US | Titel | Prio | Aufwand* | Wert |
|---|---|---|---|---|---|
| 1 | US-1.1/1.2 | Flächen-/Linearmessung | Must | M | Kern |
| 2 | US-1.4/1.5 | Messmodi + Genauigkeitsindikator | Must | L | Vertrauen |
| 3 | US-1.6/1.7/1.8 | Interferenz + Kalibrierung | Must | L | Genauigkeit |
| 4 | US-1.9 | WMM-Deklination offline | Must | M | Genauigkeit |
| 5 | US-1.10/1.13 | Manuelle Eingabe/Korrektur + Hold | Must | M | Kontrolle |
| 6 | US-2.1/2.2/2.3/2.4 | Projekte, Aufschlüsse, CRUD, Undo, Autosave | Must | L | Datenhoheit |
| 7 | US-2.5/2.6/2.8/2.11/2.12 | Medien, Notizen, Typen, Suche, Position | Must | L | Datenqualität |
| 8 | US-3.1/3.2/3.3 | CSV, GeoJSON/KML/KMZ, Projektarchiv | Must | M | Interop |
| 9 | US-1.11/1.12/1.14 | Notation, Sensor-off, Ausreißer | Should | M | Robustheit |
| 10 | US-2.7/2.9/2.10/2.13 | Skizzen, Stratigraphie, Symbolik, Dashboard | Should | L | Tiefe |
| 11 | US-3.4/3.5/3.6 | Import, PDF, Teilen | Should | M | Dokumentation |

\* Aufwand grob: S/M/L/XL — finale Schätzung im Refinement.

---

## 10. Abgrenzung (Out of Scope für MVP)

- Cloud-Sync, Benutzerkonten, Community/Public-Daten (R4, opt-in)
- KI-Bilderkennung/Assistent (nicht Kern; optional später)
- Cross-Platform (iOS/Web) — später via separater Entscheidung
- Photogrammetrie/Drohnenauswertung, Bohrkern-Modellierung
- Kaufabwicklung/Monetarisierung (Open Source → entfällt)

---

## 11. Offene Punkte / zu bestätigende Entscheidungen

1. **Lizenz:** Vorschlag **GPLv3** (Copyleft passt zu „frei & offen") vs. Apache-2.0 (permissiver).
2. **`minSdk`/`targetSdk`:** Vorschlag `minSdk 26 (Android 8)`, `targetSdk` aktuell.
3. **Offline-Kartenformat:** MBTiles (+ GeoTIFF optional) — Betrachtung in (b).
4. **Verschlüsselung:** Export-Archiv (z. B. AES-GCM) + DB-Verschlüsselung (SQLCipher?) — Betrachtung in (b).
5. **WMM-Bezug:** mitgelieferte Koeffizienten + Aktualisierungspfad.
6. **Diagramm-Referenz:** Validierung gegen InnStereo/Stereonet Mobile.

---

## 12. Traceability (Analyse → Anforderung)

| Schmerzpunkt (Doc 01) | Adressiert durch |
|---|---|
| #1 Abstürze | NFA-2, US-2.4 |
| #2 GPS-/Datenintegrität | US-2.12, US-3.3 |
| #3 Kein Undo/Bearbeiten | US-2.3 |
| #4 Genauigkeit/Kalibrierung/Deklination | US-1.4–1.9, E1 |
| #5 Keine manuelle Korrektur | US-1.10–1.12, 1.14 |
| #6 Karten-/Linework-Lücken | US-5.5, US-5.2, R-5-Parität |
| #7 UX/Organisation | US-2.1/2.2/2.11 |
| #8 Freemium/Werbung | R-2 (kein Gating) |
| #9 Datenschutz/Portabilität | NFA-6/7, US-3.3 |
| #10 Performance/Akku | NFA-3/4 |
| #11 Backend-Abhängigkeit | R-4, NFA-5 |
