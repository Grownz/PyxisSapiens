# PyxisSapiens — UI/UX-Konzept (Teil c)

**Dokumentversion:** 0.1
**Stand:** Oktober 2026
**Basis:** `docs/02_Lastenheft-und-Backlog.md`, `docs/03_Technische-Architektur.md`

---

## 1. Designprinzipien

1. **Instrument, nicht App:** Die Oberfläche wirkt wie ein präzises Messgerät — hoher Kontrast,
   große Ziffern, klare Skalen, reduzierte Ablenkung. Stärkt den Profi-/Behörden-Anspruch.
2. **Vertrauen sichtbar machen:** Jede Messung zeigt Qualität, Sensorzustand, Deklination und
   Nordreferenz. Der Nutzer weiß jederzeit, ob er dem Wert trauen kann.
3. **Feld zuerst:** Bedienung mit Handschuhen, bei Sonnenlicht, einhändig, oft ohne Netz.
4. **Wenige Schritte, keine Sackgassen:** Kernablauf (messen → speichern) in ≤ 3 Interaktionen;
   Undo statt Bestätigungsdialoge.
5. **Konsistenz Phone/Tablet:** gleiche Bausteine, adaptives Layout.
6. **Zweisprachig & zugänglich:** DE/EN, TalkBack, Skalierung, farbenblind-taugliche Zustände.

---

## 2. Design-System

### 2.1 Farbtokens (Dark-Default, „Instrument")

| Token | Wert (Vorschlag) | Verwendung |
|---|---|---|
| `bg.base` | `#0B0F14` | Grundfläche |
| `bg.panel` | `#131A22` | Karten/Panels |
| `bg.raised` | `#1B2631` | Hervorhebungen |
| `text.primary` | `#E8EEF5` | Primärtext |
| `text.secondary` | `#9AA7B4` | Sekundärtext |
| `accent` | `#FFB000` | Messwert/Akzent (amber) |
| `ok` | `#37D67A` | Qualität gut |
| `warn` | `#FFB020` | Qualität mittel / Kalibrierhinweis |
| `crit` | `#FF5C5C` | Interferenz / schlecht |
| `north` | `#4EA1FF` | Nord-/Referenzelemente |
| `grid` | `#2A3644` | Skalen/Netzlinien |

**Hochkontrast-Light** (Sonne): `bg.base #FFFFFF`, `text.primary #000000`, `accent #B36B00`,
Statusfarben abgedunkelt (WCAG AA/AAA), keine Transparenzen.

### 2.2 Typografie

- **Messwerte:** Monospace/tabulare Ziffern (z. B. JetBrains Mono / IBM Plex Mono), sehr groß (48–96 sp),
  damit sich ändernde Ziffern nicht springen.
- **Labels/UI:** Roboto/System, klar hierarchisiert.
- **Einheiten/Grad** kleiner, hochgestellt (`°`).
- Zahlen/Datums-/Koordinatenformate lokalisiert (DE/EN), Notationen fix.

### 2.3 Komponenten (Compose Design-System in `:core:ui`)

| Komponente | Zweck |
|---|---|
| `NumericReadout` | Großer Messwert mit Einheit, tabular |
| `CompassFace` | Kompassrose mit Peil-/Azimutzeiger |
| `ClinometerGauge` | Neigungswinkel-Anzeige (Bogen) |
| `StabilizationRing` | Fortschritt/Qualität der Auto-Stabilisierung |
| `QualityBadge` | Qualität (gut/mittel/schlecht) + Restunsicherheit |
| `InterferenceMeter` | Live-µT + Warnzustand |
| `NorthRefToggle` | magnetisch ⇄ geografisch |
| `NotationToggle` | DD/Dip ⇄ RHR-Strike/Dip ⇄ Quadrant |
| `HoldButton` | Große Primäraktion (Stabilisieren/Festhalten) |
| `MeasurementListItem` | Listendarstellung mit Symbolik/Typ/Qualität |
| `BigTouchTarget` | ≥ 56–64 dp, handschuh-tauglich |

### 2.4 Ikonografie & Rückmeldung

- Klare, funktionale Icons (Struktur-Symbole, Typ-Symbole).
- **Haptik**: kurzer Impuls bei Stabilisierung, doppelt bei Bestätigung, lang bei Verwerfen.
- **Akustik (optional)**: Piepton bei „eingefroren" — freihändig nutzbar.
- Zustände niemals nur über Farbe (Symbol + Text).

---

## 3. Informationsarchitektur & Navigation

**Bottom-Navigation (5 Ziele):**

```
[ Messen ]  [ Daten ]  [ Karte ]  [ Auswertung ]  [ Mehr ]
   Compass    Projekte/   (R3)      Stereonet/      Einstellungen,
   -first     Messungen             Statistik(R2)   Kalibrierung,
                                                     Export, Hilfe
```

- **Messen** = Startbildschirm (compass-first), sofort messbereit.
- **Daten** = Projekte → Aufschlüsse → Messungen (Liste/Detail/Suche).
- **Karte** (R3) = Messpunkte, Filter, Linework, Tracking.
- **Auswertung** (R2) = Stereonet/Statistik/Rose.
- **Mehr** = Kalibrierung, Sprache, Theme, Sicherheit, Export/Import, Info/Lizenz.

Kontextaktionen (Export, Foto, Notiz) sind **kontextnah** an Messung/Aufschluss verankert, nicht global.

---

## 4. Kernablauf: Messung (Compass-first)

### 4.1 Zustände (parallel zur Messpipeline, Doc 03 §4.3)

```
IDLE ──(Gerät angelegt / Start)──▶ MEASURING (Live)
   ▲                                    │
   │                            Stabilisierung n/Quality
   │                                    ▼
   │                              STABILIZING (Ring füllt sich)
   │                                    │
   │                          Qualität ok ▼        Qualität schlecht ─▶ WARNUNG (Interferenz/
   │                                HELD (eingefroren)                  Kalibrierung/ Bewegung)
   │                              ┌───┴───────┐
   └──── VERWERFEN ◀─────────────┤  SPEICHERN ├──▶ zurück zu IDLE (bereit für nächste Messung)
                                 └───────────┘
                                   (auch: Bearbeiten/Notation wechseln/manuell korrigieren)
```

### 4.2 Messbildschirm (Portrait, Drahtmodell)

```
┌─────────────────────────────────────────────┐
│ Projekt ▾  Bergkamm 2026   GPS ±3 m   🔋78% │  Statuszeile
│ Nord: GEO  Dekl +4,2°  Sensor: GUT  |B|49µT │  Referenz-/Sensorzeile
├─────────────────────────────────────────────┤
│   PLÄCHE │ LINEAR │ PEILUNG        (Umschalt)│
│                                             │
│              .  N                            │
│           W  ┌─────┐  E                     │  CompassFace / ClinometerGauge
│              │  ◉  │                         │
│           S  └─────┘                         │
│                                             │
│        ┌───────────────┬───────────────┐    │
│        │  DIP          │  DIP-DIR      │    │  NumericReadout (groß, tabular)
│        │  34,2°        │  148°         │    │
│        └───────────────┴───────────────┘    │
│                                             │
│        Qualität ●●●●○  Restunsicherheit ±1,3°│  QualityBadge
│        ◜◝◜◝ Stabilisierung 27/40 …          │  StabilizationRing
├─────────────────────────────────────────────┤
│  [  MESSEN  ]   ⌫ Verwerfen   ✎ Manuell     │  Primäraktion (riesig) + Sekundär
│  ⌾ Foto   ✐ Notiz   ⋮ (Notation/Typ/Einheit) │  Anhängen/Kontext
└─────────────────────────────────────────────┘
```

**Interaktion (Auto-Stabilisierung + Bestätigen):**
1. Gerät anlegen → `MEASURING` (Live-Werte, sofort sichtbar).
2. Stabilität erreicht → `STABILIZING` (Ring füllt sich, n Samples, Ausreißer verworfen);
   Haptik-Impuls + optional Piepton.
3. `HELD`: Werte eingefroren, große Anzeige, Qualität + Restunsicherheit.
   Aktionen: **Speichern** (Primär, haptisch doppelt), **Verwerfen**, **Manuell korrigieren**,
   Notation/Typ/Einheit ändern, Foto/Notiz anhängen.
4. Nach Speichern sofort wieder `IDLE` „bereit für nächste Messung" (hoher Durchsatz).
5. Bei schlechter Qualität: `WARNUNG` mit Grund („magnetische Störung", „nicht kalibriert",
   „bewegt") + direktem Link zur Kalibrierung.

**Umschaltlogik:** Ein Screen für alle drei Messarten (Fläche/Linear/Peilung) mit identischem
Muster — reduziert Lernaufwand. Fläche zeigt Dip/Dip-Dir (oder RHR-Strike/Dip je `NotationToggle`),
Linear zeigt Trend/Plunge.

### 4.3 Freihand-/Einhandmodus

- Großer `HoldButton` in Daumenreichweite (unteres Drittel).
- Optionaler **Beep bei HELD** → Gerät kann ohne Blickkontakt bestätigt werden (Haptik).
- Bildschirm bleibt an (konfigurierbar), Kiosk-tauglich.

---

## 5. Weitere Screens

### 5.1 Onboarding & Kalibrierung (geführter Assistent)

```
Willkommen → Sprache → Sensoren testen (Accel/Gyro/Mag vorhanden?)
         → Figur-8-Kalibrierung (Live-Ring, µT, Ergebnis-Score)
         → Bump-/Auflageflächen-Kalibrierung (Gerät auf Referenz legen)
         → Standorttest (GPS-Fix, Genauigkeit)  → Testmessung → Fertig
```
- Jeder Schritt **überspringbar**, aber nachdrücklich empfohlen; Zustand wird gemerkt.
- Rekalibrier-Erinnerung **kontextuell** bei schlechter Qualität (nicht-blockierend).
- Erfolgsanzeige: „Kalibrierung gut (±0,8°) — bereit für Feldmessungen."

### 5.2 Daten (Projekte → Aufschlüsse → Messungen)

- **Projektliste:** Karte/Liste, letzte Aktivität, Anzahl Messungen.
- **Aufschlussdetail:** Position, Beschreibung, Medien, Liste der Messungen.
- **Messungsdetail:** alle Werte, Qualität, Nordreferenz/Deklination, Position (mit Karte),
  Medien, Notiz, Historie/Undo, Bearbeiten, Löschen (Papierkorb).
- **Suche/Filter:** Volltext + Filter (Typ, Einheit, Qualität, Zeit, Position); gespeicherte Filter.
- **Listen-Dichte:** kompakte „Feldliste" mit Symbol + Werten, große Touchziele.

```
┌───────────────────────────────────────────┐
│ 🔎 Suchen…            Filter ▾   + Neu      │
├───────────────────────────────────────────┤
│ ▣ 148/34  bedding      ●  Bergkamm N-1      │
│ ▣ 152/31  bedding      ●  Bergkamm N-1      │
│ ╱ 210↓42  foliation    ●● Bergkamm S-2      │
│ ⟋ 032→18  lineation    ○  (uncalib.)        │
└───────────────────────────────────────────┘
```

### 5.3 Medien, Skizzen, Annotation

- Galerie je Objekt; Kamera/Import; Standort-/Zeit-/Richtungsstempel.
- Skizzeneditor: Freihand, Formen, Text, Pfeile, Ebenen; als verknüpftes Medium.
- Foto-Annotation mit Pfeil/Text/Beschriftung.

### 5.4 Export/Import-Wizard

```
Ziel wählen:  CSV | GeoJSON/KML | KMZ | PDF | Projektarchiv(.pyxis)
Umfang:       Auswahl (Filter) · Felder/Spalten · Optionen (Symbolik, Medien)
Sicherheit:   (Archiv) Passwort/Verschlüsselung
Ergebnis:     Vorschau + Validierung ✓   →  Speichern/Teilen
```
- Validierungsergebnis sichtbar (Zeilen, Dubletten, Warnungen) — kein „korrupter Export".

### 5.5 Auswertung (R2): Stereonet & Statistik

```
┌───────────────────────────────────────────┐
│ Projekt: Bergkamm ▾   Datentyp: bedding ▾  │
│ Projektion: ⦿ Schmidt  ○ Wulff   LH        │
│                                            │
│        ( Stereonet-Plot )                  │
│         ·  ·   ⌒ Großkreise                │
│           ·  ·  Pole                       │
│                                            │
│ Statistik: n=42  κ=18,4  R̄=0,97  α95=4,1° │
│ Konturen ▢  Eigen ◫  Mean-Plane ◯  Rose ▤  │
├───────────────────────────────────────────┤
│  Filter ▾    Export SVG/PNG    + Datensatz │
└───────────────────────────────────────────┘
```
- Interaktiv: Antippen eines Punktes → Messung; Datentypen ein-/ausblenden; Kontur-Sigma einstellen.
- Zusätzliche Tabs: Rose-Diagramm, Dip-Histogramm, Set-Analyse, Hangstabilität.

### 5.6 Karte & Tracking (R3)

- Vollflächige Karte, Messpunkte mit Symbolik, **Filter nach Einheit/Typ/Qualität**.
- Werkzeugleiste: Linework zeichnen/bearbeiten, Messen aus Karte, Tracking Start/Stop, Basiskarten verwalten.
- Tracking-Ansicht: Live-Pfad, Distanz/Dauer, „Kontakt begehen", Export GPX.

### 5.7 Einstellungen / Mehr

- Kalibrierung (Status je Gerät, neu kalibrieren), Sprache (DE/EN), Theme (Dark/Hochkontrast-Light),
  Einheiten/Notation/Auflösung, Sicherheit (DB-/Export-Verschlüsselung, Löschen), Speicherorte,
  Info/Lizenz (GPLv3)/Hilfe, lokale Logs (optional exportierbar).

---

## 6. Feldtauglichkeit (harte Anforderungen)

- **Handschuhe:** Touchziele ≥ 56–64 dp, keine Filigran-Gesten als Pflichtpfad.
- **Sonne:** Hochkontrast-Modus, keine dünnen Linien, ausreichend große Schrift.
- **Einhand:** Primäraktionen in Daumenreichweite; Navigation unten.
- **Kein Netz:** alle Kerncreens offline; klare Offline-Indikatoren.
- **Akku:** Dark-Default; Sensorsampling nur bei Bedarf; Bildschirm-Timeout konfigurierbar.
- **Witterung:** Versehentliches Speichern/Verwerfen durch Haptik + Undo abgefedert.

---

## 7. Barrierefreiheit

- TalkBack: alle Messwerte/Gauges mit Text-Äquivalenten („Dip 34,2 Grad, Qualität gut").
- Skalierbare Schrift; flexible Layouts bei großer Schrift.
- Farbenblind-Sicherheit: Status immer über Symbol + Text zusätzlich zur Farbe.
- Kontrast mind. AA (Hochkontrast-Modus AAA).

---

## 8. Adaptive Layouts (Phone / Tablet)

| Größe | Layout |
|---|---|
| Kompakt (Phone) | Einspaltig; Mess-Screen wie oben |
| Medium/Large (Tablet) | **Zwei-Pane** in Daten: Liste links, Detail rechts; Karte + Werkzeuge neben Panel; Auswertung: Stereonet groß + Steuerpanel seitlich |
| Foldables | `WindowSizeClass`-basiert; Falz vermeiden |

---

## 9. Microcopy (Beispiele DE/EN)

| Kontext | DE | EN |
|---|---|---|
| Messbereit | „Gerät anlegen – bereit" | "Place device — ready" |
| Stabilisiert | „Stabil – gespeichert" | "Stable — saved" |
| Qualität gut | „Qualität gut (±1,3°)" | "Good quality (±1.3°)" |
| Interferenz | „Magnetische Störung – Metall entfernen" | "Magnetic interference — move away from metal" |
| Nicht kalibriert | „Kalibrierung empfohlen" | "Calibration recommended" |
| GPS ungenau | „GPS ungenau (±25 m)" | "GPS inaccurate (±25 m)" |
| Undo | „Rückgängig" | "Undo" |

Tonal: knapp, sachlich, handlungsorientiert; keine Verniedlichung (Profi-Zielgruppe).

---

## 10. Interaktionsdetails & Gesten

- **Speichern** = große Taste oder Haptik-Doppelimpuls; **Verwerfen** = Wisch nach links ODER ⌫.
- **Notation/Typ** = schnelle Chips statt tiefer Menüs.
- **Hold** = antippen und halten für „Messung manuell einfrieren" (Zwei-Wege-Komplement zur Auto-Stabilisierung).
- **Listen:** Wischen für Schnellaktionen (Duplizieren, Typ ändern), Undo-Snackbar.
- **Kein** Modal-Dialog für Bestätigung beim Speichern (Fehlklicks durch Undo abfangen).

---

## 11. Anforderungs-Rückverfolgung (Auswahl)

| Anforderung (Doc 02) | UI-Umsetzung |
|---|---|
| US-1.4/1.5 Messmodi + Qualität | StabilizationRing, QualityBadge, Modus-Chips |
| US-1.6 Interferenz | InterferenceMeter + Warnbanner mit Kalibrier-Link |
| US-1.7/1.8 Kalibrierung | Geführter Assistent (§5.1) |
| US-1.9 Deklination/Nord | NorthRefToggle + Statuszeile |
| US-1.10 Manuelle Korrektur | ✎ Manuell im Mess-Screen + Detail-Bearbeitung |
| US-2.3 Undo | Papierkorb + Undo-Snackbar + Historie im Detail |
| US-2.11 Suche/Filter | Suchleiste + Filter-Sheets + Presets |
| US-3.x Export | Export-Wizard mit Validierung |
| US-4.x Auswertung | Stereonet-Screen (§5.5) |
| US-5.x Karte/Linework/Tracking | Karten-Screen (§5.6) |
| NFA-8/9 Feldtauglichkeit/A11y | §6, §7 |

---

## 12. Offene UI-Punkte

1. **Instrument-Optik konkretisieren:** endgültige Farb-/Kontrastwerte, Gauge-Stil, Icon-Set.
2. **Peil-Modus-Interaktion** (Anvisieren über Gerätekante) — genaue Gestenführung festlegen.
3. **Diagramm-Interaktion auf Tablet** (Zoom/Pan im Stereonet, Pinch vs. Controls).
4. **Karten-Overlays für Massendaten** (Clustering-Schwellen).
5. **Onboarding-Länge** (max. Schritte) für ungeduldige Profis.
6. **Audio/Haptik-Regler** und Defaults je Zielgruppe.
