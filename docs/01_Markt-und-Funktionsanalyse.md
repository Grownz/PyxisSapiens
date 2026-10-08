# PyxisSapiens — Wettbewerbs- und Funktionsanalyse

**Stand:** Oktober 2026
**Zweck:** Grundlage für die Konzeption einer Android-App für den Geologenkompass mit
Datenverwaltung, -auswertung, -darstellung und -verknüpfung.
**Methodik:** Auswertung der öffentlichen Store-Einträge (Google Play / App Store),
der Herstellerangaben, veröffentlichter Nutzerreviews sowie der wissenschaftlichen
Literatur zur Genauigkeit smartphone-basierter Kompasse.

---

## 1. Marktübersicht

Der Markt teilt sich in drei grobe Kategorien:

| Kategorie | Beispiele | Charakter |
|---|---|---|
| **Integrierte Feldmapping-Suiten** | FieldMove Clino, FieldMove, Rockd | Kompass + Karte + Datenbank + Export, teils professioneller Anspruch |
| **Spezialisierte Mess-/Analyse-Apps** | GeoCompass, Stereonet Mobile, GeoID, Stereonet, listerCompass/Stereoplot (GeologyCompass), GeoKit | Fokus auf Messung und strukturelle Auswertung |
| **Minimale Feld-Notizbücher** | Field Geology, generische Kompass-Apps | Grundfunktionen, wenig Struktur |

Wettbewerber-Kurzprofil:

| App | Anbieter | Downloads | Bewertung | Modell | Plattform |
|---|---|---|---|---|---|
| **FieldMove Clino** | Petroleum Experts (Petex) | 100K+ | 4,4 (≈762–773) | kostenlos + IAP (Sterenet/Symbole) | iOS, Android (nur Smartphone) |
| **GeoCompass** (Geology Clinometer) | Hung-Hsun Lin / Lemon Studio | 50K+ | k. A. in Auswertung | kostenlos + Werbung + IAP | iOS, Android |
| **Field Geology** | devdip it (Dipraj Roy) | 1K+ | keine Review-Übersicht | kostenlos | Android |
| **Rockd** | Macrostrat / Univ. Wisconsin | hoch | 4,1 (429) | kostenlos (Förderung) | iOS, Android |
| **Stereonet Mobile** | Richard Allmendinger | k. A. | 4,5 (13) | kostenpflichtig | iOS |
| **GeoID** | Sang Ho Lee | – | 5,0 (3) | kostenpflichtig | iOS |
| **GeoKit** | GeoKit | – | – | kostenlos | Web/App |
| **listerCompass / Stereoplot** | GeologyCompass | – | – | kommerziell | iOS/Apple |

---

## 2. FieldMove Clino (Petroleum Experts)

Der **funktionale Referenzmaßstab** im professionellen Bereich. Positioniert als
digitaler Kompass-Klinometer für Datenerfassung im Feld, optimiert auf GPS- und
Lagesensoren des Geräts. Android-Version bewusst **nur für Smartphones**, nicht für Tablets.

### 2.1 Funktionsumfang

**Messung / Kompass**
- Digitaler Kompass-Klinometer für **Flächen (planar)** und **Lineare (linear)**
- Nutzung als klassischer Handpeilkompass
- Automatische Positionsbestimmung via GPS, **manuelle Positionsüberschreibung** (Override)
- Nutzerorientierte Erfassung großer Messmengen („10× schneller als klassisch")

**Struktur / Auswertung**
- **Stereonet** (Equal-Area/Equal-Angle) mit Basis-Statistik → in der Bezahlversion
- Erweiterte **Symbolbibliothek** für Flächen/Lineare → in der Bezahlversion
- Nutzerdefinierte **Stratigraphie-/Gesteinseinheiten-Liste**

**Datenerfassung / -verwaltung**
- **Lokalitäten** (Localities)
- Digitales **Notizbuch** (Textnotizen), **georeferenzierte Fotos**, Skizzen
- Foto-Annotation (v. a. iOS)
- **Projekte** mit Bearbeitung

**Karte / Feldarbeit**
- **Online-Karten** (Google Maps / Mapbox), **Offline-Basiskarten** (importierte georeferenzierte MBTiles/GeoTIFF)
- Linework-Zeichnen (Kontakte, Störungen, Aufschluss-Polygone) → **in Clino nur iOS**; auf Android nur in FieldMove (Tablet)

**Export / Interop**
- Export als **MOVE**, **CSV**, **KMZ** (Google Earth)
- Nativer Projekt-Archiv-Export/Import (`.fm.zip`, `.fm`-Ordner)
- Teilen über Android-Share-Sheet (Drive, Gmail, …)

### 2.2 Stärken (laut Nutzerfeedback)

- Ersetzt Zettel/Karte/Notizbuch und reduziert die mitgeführte Ausrüstung
- Sehr schnelle Erfassung vieler Messungen → statistisch belastbare Datensätze
- **„It's now my primary mapping tool for everything"** – Messungen, Störungen, Slickensides, Notizen, georeferenzierte Bilder, Kartenannotation
- Kompletter Export inkl. symbologische Darstellung in **Google Earth** (S/D-Symbole + Metadaten + Stratigraphie)
- **„geology into the 21st century"**, „easier to use in the field than a Brunton compass"
- Funktioniert offline mit eigenen Basiskarten

### 2.3 Schwächen / Schmerzpunkte (reviews)

- **Abstürze:** beim Aufnehmen eines neuen Fotos, beim Eingeben eines zweiten Kommentars, beim Anlegen einer Lokalität per GPS; in neueren iOS-Versionen teils häufiger
- **GPS-/Datenintegrität:** gemappte Punkte teilten eine einzige falsche „Hero"-Position → räumliche Referenz unbrauchbar; Export korrupt; GPS-Erfassung langsam, verliert Updates ohne Warnung
- **Kein Undo / keine Wiederherstellung:** versehentlich gelöschte Datenpunkte sind unwiederbringlich verloren
- **Genauigkeit:** Flächenmessung fehlerhaft bei **vertikaler Gerätehaltung** (Element um 90° in der Karte verdreht); Abweichung zur Brunton in Tests
- **Fehlende manuelle Eingriffsmöglichkeiten:** keine manuelle Korrektur/Eingabe von Dip/Azimut, kein manuell gesetzter Messpunkt
- **Karte:** keine gefilterte Ansicht nach Einheiten/Lithologien (anders als im Stereonet); Basiskarten-Qualität verbesserungswürdig
- **Datenpflege:** Notizen landeten nach „Locality Reset" am falschen Punkt und waren **in der App nicht editierbar** (nur Position verschiebbar)
- **Plattform:** kein Tablet-Support unter Android (und Clino sperrt Tablets grundsätzlich); **Linework auf Android fehlt**
- **Wunsch:** GPS-**Tracking**/Begehdaten („walking the contacts"), Undo
- **Akku:** hoher Verbrauch
- **Transparenz:** „Data isn't encrypted", „Data can't be deleted", kostenpflichtige Kernfeatures

---

## 3. GeoCompass — Geology Clinometer (Lemon Studio)

Leichtgewichtiger, moderner Kompass-Klinometer mit **starker Foto-/Geo-Metadaten-
Funktionalität und neuem KI-Assistenten**. Deutlich schlanker und weniger integriert als FieldMove Clino.

### 3.1 Funktionsumfang

- Messung von **Flächen- und Linearen-Strukturen** (Strike/Dip, Dip direction, Azimuth, Plunge, Trend)
- **Mehrere Koordinatensysteme:** WGS84, **UTM**, **MGRS** (weitere geplant)
- Speichern umfangreicher Datensätze: Strike, Dip, Azimuth, Dip direction, Plunge, Trend, Koordinaten, Höhe, Adresse
- **Fotos, Videos und Textnotizen** an Messungen
- **Metadaten auf Fotos:** Datum, Uhrzeit, Koordinaten, **Wetterbedingung**
- **Karten- und Listenansicht** der Messungen, Detailansicht
- **Projekte** zur Gruppierung
- **Export** aller Daten (inkl. Fotos/Videos)
- **KI-Assistent** zur schnellen Erhebung von Survey-Informationen
- Kostenlos mit **Werbung** und **In-App-Käufen**; Gratisversion limitiert Anzahl Projekte/Messungen

### 3.2 Stärken

- Sehr einfache Bedienung (Gerät ausrichten → Wert wird angezeigt → „Save")
- Für **Flächen und Lineare** gleichermaßen, viele Messgrößen pro Datensatz
- Ungewöhnlich starker **Geo-/Umgebungs-Metadaten-Umfang** (Wetter, Adresse, Höhe)
- **Foto-/Video-Anreicherung** mit eingebrannten Infos – gut für Dokumentation und Berichte
- **UTM/MGRS** für GIS-/Ingenieur-Workflows
- Projektorganisation, Map+Liste, Export
- Aktive Weiterentwicklung (KI-Assistent)

### 3.3 Schwächen / Risiken

- **Kein Stereonet / keine strukturelle Auswertung** – Daten müssen extern weiterverarbeitet werden
- **Werbung + Freemium-Limits** (Anzahl Projekte/Messungen) — wiederkehrende Nutzerbeschwerden bei vergleichbaren Apps
- **Datenschutz:** gibt App-Aktivität, App-/Performance-Infos und Geräte-IDs an Dritte weiter; „Data isn't encrypted" → für professionelle/behördliche Nutzung kritisch (kein sauberer Offline-/No-Tracking-Modus beschrieben)
- Wetter-/Metadaten hängen von Online-Diensten ab
- Kein Linework/Kartierung wie FieldMove, kein Offline-Basiskarten-Management im Clino-Umfang
- Weniger „Profi-Tool"-Charakter, keine Mächtigkeits-/Qualitätsindikatoren dokumentiert

---

## 4. Field Geology (devdip it)

Kleinste der drei Apps und **streng genommen kein Kompass**, sondern ein minimaler
**digitaler Feld-Datenlogger**. Fokus laut Beschreibung auf Vermeidung von Papier.

### 4.1 Funktionsumfang

- Automatische Zeitstempelung
- Automatische Geo-Verortung offline und online (online bevorzugt für Genauigkeit)
- Speicherung auf dem Gerät
- „Reset"-Funktion mit einem Knopf
- **Als Zukunft versprochen:** PDF je Aufschluss, Excel-Export aller Daten, nachträgliche Bearbeitung

### 4.2 Bewertung

- **Stärke:** sehr einfach, leichtgewichtig, offline-Ortsbestimmung, kein Login
- **Schwächen:**
  - **Kein Kompass-Klinometer**, keine Neigungs-/Streichenmessung
  - **Keine Kartendarstellung**, **kein Stereonet**, keine Strukturanalyse
  - **Kein Export** (Stand der Beschreibung) – Daten faktisch im Gerät eingeschlossen
  - Keine Projekt-/Lithologie-/Symbolverwaltung
  - Sehr kleiner Funktionsumfang, ein Einzelentwickler-Projekt, **keine belastbare Bewertungsbasis** (1K+ Downloads, keine Review-Übersicht)
- **Einordnung:** Für PyxisSapiens nicht als Konkurrent, sondern als Negativbeispiel für
  zu geringen Funktionsumfang und fehlende Datenportabilität.

---

## 5. Angrenzende Lösungen (wichtige Ideengeber)

- **Rockd (Macrostrat):** kostenlos, offline-fähig, >140 geologische Karten, Dashboard zur
  Lokation, Check-in-Feed, **Kompass für Strike/Dip & Trend/Plunge**, Verknüpfung mit
  Macrostrat-Einheiten und Paleobiology Database, **crowd-sourcing/Rückfluss in Datenbanken**,
  Paläogeografie-Karten, Elevation. → Vorbild für **Datenverknüpfung** und Community/Export.
  Schwäche: wiederkehrende Sync-/Auth-/Blank-Screen-Bugs (Android 14), Abhängigkeit von Online-Backend.
- **Stereonet Mobile (R. Allmendinger):** Referenz für **professionelle Strukturanalyse**:
  Kompass per iOS-Orientierung, **AR-Sichtverfahren** für Flächen, manuelle Eingabe per
  Drag/Tippen, beliebige Datentypen (bedding, cleavage, slickensides …), Rotationen,
  Poles↔Planes, Winkelberechnungen, **einfache Hangstabilität**, Mean vectors, zylindrische
  Best-Fits, **Rose-Diagramme, Konturen**, freie 3D-Ansicht, Karten-/Satellitenverortung,
  Live-Stereonet, **Kalibrierung für Kamera-Bump** (nicht-planare Rückseite). → Vorbild für **Auswertung**.
- **GeoID:** stereografische Projektion, **Instabilitätsanalyse**, drahtloses Teilen,
  Notationstypen, Mittelwertbildung, Projektionswahl. → Vorbild für **Projekt-/Notations-/Statistikoptionen**.
- **GeoKit (Web/App):** Ein-Schritt-Messung mit Auto-Lock, tilt-kompensierte Fusion,
  automatische **WMM-Deklination**, Stereonet + Rose + Dip-Histogramm, **KML/GeoJSON-Export**, offline.
- **listerCompass / Stereoplot (GeologyCompass):** Messen am Aufschluss, separate Analyse-App für
  Projektion/Konturierung → **Vorbild für modulare Trennung Messung ↔ Auswertung**.
- **Generische Kompasse (MBCompass, NorthPin, „Accurate Compass"):** wichtige UX-Muster –
  **Interferenzerkennung**, **µT-Anzeige**, **Kalibrier-Anleitung (Figur-8)**, „true north vs magnetic north",
  Genauigkeits-Indikator. → Vorbild für **Vertrauens-/Qualitäts-Feedback**.

---

## 6. Nutzerfeedback — Was gelobt wird (positive Muster)

1. **Geschwindigkeit & Datenmenge:** Viele Messungen schnell erfassbar → statistisch valide Datensätze; „10× schneller".
2. **Ausrüstung reduzieren:** Ein Gerät ersetzt Zettel, Karte, Notizbuch, Kamera, GPS.
3. **Alles an einem Ort:** Messung + Foto + Notiz + Position + Symbol **verknüpft** und **exportierbar**.
4. **Offline-Fähigkeit** ist ein entscheidendes Kaufargument für echte Feldarbeit.
5. **Interoperabilität:** **KMZ → Google Earth**, CSV → Excel/GIS, MOVE → Modellierung.
6. **Sofort-Auswertung im Feld:** Stereonet direkt beim Aufschluss.
7. **„Professionelles Werkzeug"-Look** gegenüber „Spielzeug" wird ausdrücklich geschätzt.
8. **Im Feld einfacher als ein Brunton** – für Studierende/Einsteiger großer Vorteil.

---

## 7. Nutzerfeedback — Was kritisiert wird (negative Muster, priorisiert)

| # | Schmerzpunkt | Häufigkeit | Betroffene Apps | Konsequenz für PyxisSapiens |
|---|---|---|---|---|
| 1 | **Abstürze** (Foto, Kommentar, Lokalität) | hoch | Clino, Rockd | Robustheit als Kernanforderung; Kameraintegration, Edge-Cases |
| 2 | **Datenintegrität / GPS-Fehler** (falsche, geteilte Position; korrupter Export) | hoch | Clino | Position verifizieren, Plausibilitätsprüfung, Export-Validierung |
| 3 | **Kein Undo / keine Bearbeitung** | hoch | Clino, GeoID | Vollständiges Bearbeiten + Undo/History unabdingbar |
| 4 | **Genauigkeit / Kalibrierung / Deklination** | hoch | alle | Kalibrier-Workflow, Genauigkeitsindikator, WMM-Deklination, Mittelung |
| 5 | **Keine manuelle Korrektur/Eingabe** von Dip/Azimut/Punkt | mittel-hoch | Clino, GeoID (nur ein Format) | Manuell + Gerät + Nachbearbeitung, Notationen konvertierbar |
| 6 | **Karten-/Linework-Lücken** (kein Android-Linework, kein Tablet, schwache Basiskarten, keine Einheiten-Filter) | mittel-hoch | Clino | Android-Parität, Filter, anständige Basiskarten, Tablet-Support |
| 7 | **UX/Organisation** (Projekte, Notizen unklar; Lernkurve) | mittel | Clino | Klare Informationsarchitektur |
| 8 | **Kosten/Freemium/Werbung** | mittel | Clino, GeoCompass, generische | Faire Preis-/Lizenzpolitik, no-ads |
| 9 | **Datenschutz / Portabilität** („can't be deleted", unverschlüsselt, Tracking) | mittel, wachsend | Clino, GeoCompass | Lokal-first, verschlüsselte Exporte, vollständige Löschbarkeit |
| 10 | **Performance/Akku/GPS-Latenz** | mittel | Clino, Rockd | Effizientes Sensor-/GPS-Handling |
| 11 | **Abhängigkeit von Online-Backend** (Sync/Auth-Bugs) | mittel | Rockd | Offline-First-Architektur, optionaler Sync |

---

## 8. Technisch-wissenschaftliche Randbedingungen (Genauigkeit)

Diese Punkte sind **entscheidend** für „beste, genaueste" App und aus der Literatur belegt:

- **Azimut/Kompassfehler dominieren:** Novakova & Pavlis (2017) fanden azimutbezogene
  Fehler bis **90°** durch Hard-/Soft-Iron-Effekte und Bewegung; **Dip** ist dagegen deutlich
  **genauer** (wenige Grad). ISPRS (2020): Pitch/Roll < 2°, Azimut in Bewegung/magnetisch
  gestört teils > 30°.
- **Hard-/Soft-Iron-Kalibrierung** ist bei Smartphones praktisch immer nötig
  (fest verbaute magnetisierte Bauteile). Einfache 4-Parameter-Kalibrierung (Offsets + Feldstärke)
  hilft bereits (NXP AN4246).
- **Redundanz & Mittelung** statt Einzelmessung: Allmendinger et al. (2017) erfassen bis zu
  **40 Einzelmessungen** (10 Hz über ~4 s) und verwerfen Werte mit >3°-Abweichung bzw. bei
  Bewegungsüberschreitung; Fehler von Dip und Strike **getrennt über Pole** bewerten.
- **Deklination** muss über das **World Magnetic Model (WMM)** aus Position berechnet und
  angewendet werden – idealerweise offline und explizit sichtbar (magnetisch vs. geografisch).
- **Magnetische Interferenz** muss dem Nutzer **in Echtzeit** angezeigt werden (µT-Wert,
  Genauigkeitsstufe, Figur-8-Kalibrierhinweis).
- **Geräteabhängigkeit:** Sensorqualität variiert stark; Profi-Apps brauchen einen Modus,
  in dem der Nutzer **Sensorwerte deaktiviert** und manuell eingibt.
- **Geometrische Besonderheit:** nicht-planare Geräterückseite (Kamerabump) verfälscht das
  Aufliegen → **Kalibrierung der Auflagefläche** (vgl. Stereonet Mobile).
- **Vertical-Orientation-Bug**: Messung von Flächen bei senkrechter Gerätehaltung ist fehleranfällig
  (bei Clino dokumentiert) → sauberes tilt-kompensiertes Rechnen (Sensorfusion) erforderlich.
- **Stereonet-Konventionen:** Equal-Area (Schmidt) für Statistik/Dichte-Konturierung,
  Equal-Angle (Wulff) für Formtreue; Lower-Hemisphere-Standard; RHR-Strike vs. Dip-Direction
  konvertierbar. Vollständige Strukturanalyse umfasst: Pole/Großkreise, Kamb-Dichte,
  Eigenvektoren (S1/S2/S3, Woodcock K/C), **Fisher-Statistik** (κ, R̄, α95), **Girdle/Fold-Axis**,
  Mean Plane, Zwei-Ebenen-Intersektion (β-Achse), Rose-Diagramm, Dip-Histogramm.

---

## 9. Funktions-Gap-Matrix

Legende: ● vorhanden · ◐ eingeschränkt/optional · ○ fehlt

| Funktion | FieldMove Clino | GeoCompass | Field Geology | PyxisSapiens-Chance |
|---|---|---|---|---|
| Kompass-Klinometer (Fläche + Linear) | ● | ● | ○ | Referenzniveau + Genauigkeit toppen |
| Manuelle Eingabe/Korrektur Strike/Dip | ◐ (nur Gerät) | ◐ | ○ | **● differenzieren** |
| Stabilitäts-/Genauigkeitsindikator | ○ | ◐ | ○ | **● differenzieren** |
| WMM-Deklination | ● | ◐ | ○ | ● explizit & offline |
| Hard-/Soft-Iron-Kalibrierung | ◐ (Device) | ◐ | ○ | **● geführt + Auflageflächen-Kalibrierung** |
| Mittelung/Redundanz (Auto-n) | ○ | ◐ | ○ | **● differenzieren** |
| AR-Sichtverfahren | ○ | ○ | ○ | ● (vgl. Stereonet Mobile) |
| Projekte/Probenverwaltung | ● | ● (limitiert) | ○ | ● + volle Bearbeitung/History |
| Lokalitäten/Aufschlüsse | ● | ◐ | ◐ | ● + Undo |
| Fotos (georeferenziert) | ● | ● (+Video) | ◐ | ● stabil & annotierbar |
| Textnotizen | ● | ● | ● | ● |
| Skizzen/Annotation | ◐ (iOS) | ◐ | ○ | **● plattformgleich** |
| Stratigraphie/Einheiten | ● | ○ | ○ | **● differenzieren** |
| Symbolbibliothek | ● (IAP) | ○ | ○ | ● |
| Online-/Offline-Basiskarten | ● | ● (online) | ◐ | ● + Offline-Cache |
| Linework (Kontakte/Störungen) | ◐ (iOS-only) | ○ | ○ | **● Android-Parität** |
| GPS-Tracking/Begehung | ○ | ○ | ○ | **● differenzieren** |
| Kartendarstellung + Liste | ● | ● | ○ | ● |
| Filter nach Einheit/Datentyp | ○ | ◐ | ○ | **● differenzieren** |
| Stereonet (Equal Area/Angle) | ● (IAP) | ○ | ○ | **● vollwertig, inkl. frei** |
| Konturen/Kamb/Eigenwerte | ◐ | ○ | ○ | **● Pro-Niveau** |
| Fisher-Statistik/α95 | ◐ | ○ | ○ | **●** |
| Rose-Diagramm/Dip-Histogramm | ○ | ○ | ○ | **●** |
| Fold-Axis/Girdle/β-Achse | ○ | ○ | ○ | **●** |
| Hangstabilitätsanalyse | ○ | ○ | ○ | ● (vgl. GeoID/Stereonet Mobile) |
| Rotationen/Poles↔Planes | ◐ | ○ | ○ | ● |
| Manueller Positions-Override | ● | ◐ | ○ | ● |
| Koordinatensysteme (WGS84/UTM/MGRS) | ◐ (WGS84) | ● | ◐ | ● (+ weitere) |
| Wetter-/Umfeld-Metadaten | ○ | ● | ○ | ◐ optional |
| Export CSV | ● | ● | ○ | ● |
| Export KMZ | ● | ◐ | ○ | ● |
| Export MOVE / GIS (GeoJSON/KML) | ● | ◐ | ○ | **● + GeoJSON/Shapefile** |
| Import von Fremddaten | ◐ | ○ | ○ | **● (CSV/KML/Shapefile)** |
| Projektaustausch/Archiv | ● | ◐ | ○ | ● verschlüsselt |
| KI-Assistent | ○ | ● | ○ | ◐ optional |
| Offline-First / kein Zwangs-Backend | ● | ◐ | ● | **●** |
| Datenschutz (lokal, löschbar, verschlüsselt) | ◐ (löschbar? nein) | ◐ | ● | **● differenzieren** |
| Tablet-Support | ○ (gesperrt) | ● | ● | ● |
| Undo/History | ○ | ○ | ○ | **● differenzieren** |

---

## 10. Ableitungen für PyxisSapiens (Blueprint)

### 10.1 Differenzierungsstrategie („beste, genaueste, umfangreichste")

Die drei untersuchten Apps lassen klar drei Lücken:
1. **Vertrauen in die Messung** (Kalibrierung, Genauigkeitsfeedback, Mittelung, manuelle Korrektur).
2. **Datenhoheit** (Offline-First, Undo, vollständige Bearbeitung, verschlüsselt, löschbar).
3. **Auswertungstiefe** (vollwertiges Stereonet & Statistik statt Bezahl-Feature oder Fehlanzeige).

### 10.2 Funktionssäulen (entlang der vier geforderten Achsen)

**A. Messung (Kompass-Klinometer) — Genauigkeits-Kern**
- Sensorfusion (Accelerometer + Gyro + Magnetometer), tilt-kompensiert, vertikalfest
- Geführte **Figur-8-Kalibrierung** + **Auflageflächen-/Bump-Kalibrierung**
- **Echtzeit-Genauigkeits-/Stabilitätsindikator** + µT-Interferenzwarnung
- **Auto-Mittelung** über n Werte, Verwerfen bei Bewegung/Abweichung > Schwelle
- **WMM-Deklination** offline, explizite Anzeige magnetisch vs. geografisch
- Manuelle Eingabe & Nachkorrektur; Notationen konvertierbar (RHR-Strike ↔ Dip-Direction)
- Optional AR-**Sichtverfahren**

**B. Datenverwaltung**
- Projekte → Aufschlüsse/Lokalitäten → Messungen; frei definierbare Datentypen (bedding, cleavage, slickensides …)
- Vollständige CRUD-Bearbeitung + **Undo/History**
- Fotos, Videos, Notizen, Skizzen, Symbolik
- Stratigraphie-/Einheitenkatalog mit GIS-/International-Stratigraphie-Bezug
- Lokal-first, **verschlüsselte** Projekte, optionaler Sync, echte Löschbarkeit

**C. Auswertung**
- Vollwertiges **Stereonet**: Equal-Area (Schmidt) + Equal-Angle (Wulff), Lower Hemisphere
- Pole/Großkreise, **Dichte-Konturen (Kamb)**, **Fisher-Statistik (κ, R̄, α95)**, Eigenvektoren (S1/S2/S3, Woodcock K/C)
- Girdle/Fold-Axis (π), Mean Plane, β-Achse, Winkelberechnungen, Rotationen, Poles↔Planes
- **Rose-Diagramme**, Dip-Histogramm, Set-Analyse
- Einfache **Hang-/Kinematik-Stabilität**

**D. Darstellung & Verknüpfung**
- Karte (online + **offline gecacht**) mit Messpunkten, Einheiten, Linework
- **Filter** nach Einheit/Datentyp/Alter; Listenansicht
- **Linework** (Kontakte, Störungen, Aufschlusspolygone) auf **allen** Plattformen inkl. Tablet
- **GPS-Tracking** (Begehung von Kontakten)
- **Export/Import:** CSV, KMZ/KML, **GeoJSON/Shapefile**, MOVE, Projektarchiv
- **Verknüpfung:** Geologische Karten/Webdienste, Stratigraphie-Datenbanken (z. B. Macrostrat-Ideen),
  optional Community/Teilen

### 10.3 Nicht-funktionale Anforderungen (direkt aus dem Feedback)
- **Stabilität** (keine Abstürze bei Foto/Kommentar/GPS) – höchste Priorität
- **Datenintegrität** (Positionsvalidierung, Exportprüfung, Undo, autosave)
- **Performance/Akku** (effizientes Sensor-/GPS-Sampling)
- **Tablet-Support** und einheitliche Feature-Parität
- **Fairen Monetarisierung** (kostenlos/optionaler Pro-Umfang, keine Werbung, kein Zwang)
- **Datenschutz** (lokal, verschlüsselt, löschbar, kein Tracking)

### 10.4 Kurzfazit
PyxisSapiens kann sich gegenüber den drei untersuchten Apps durch **Mess-Genauigkeit &
-darstellung**, **Auswertungstiefe** und **Datenhoheit/Offline-First mit voller Android-/
Tablet-Parität** klar absetzen. Die größten vermeidbaren Fehler sind genau die, die in
Reviews am häufigsten genannt werden: Abstürze, Datenverlust ohne Undo, falsche/gekoppelte
GPS-Position, fehlende manuelle Eingriffe, Bezahlschranken vor Kernfunktionen und
mangelnde Portabilität/Transparenz.

---

## 11. Quellen (Auswahl)

- Google Play: FieldMove Clino (`com.mve.fieldmove.clino`), GeoCompass (`com.wix.lemonstudio01.geocompass3.prod`),
  Field Geology (`com.devdip.fieldgeology`), Rockd (`org.macrostrat.rockd`)
- Apple App Store: FieldMove Clino, Geology Clinometer: GeoCompass, FieldMove, Stereonet Mobile, GeoID
- Petroleum Experts: Digital Field Mapping / FieldMove Brochure & User Guide (2020)
- Lemon Studio: GeoCompass Produktseite
- Rockd / Macrostrat Projektseite
- GeologyCompass: listerCompass, Stereoplot
- GeoKit: Strike & Dip Compass
- Novakova, L. & Pavlis, T. L. (2017): Assessment of the precision of smart phones and tablets
  for measurement of planar orientations. *Journal of Structural Geology* 97, 93–103.
- Allmendinger, R. W. et al. (2017): Structural data collection with mobile devices.
  *Journal of Structural Geology* 102, 98–112.
- ISPRS Annals V-1-2020: Multi-sensor fusion strategies for smartphone rotation parameters.
- NXP Application Note AN4246: Calibrating an eCompass (Hard/Soft-Iron).
- Seequent/RockWare/GeoStereonet: Stereonet-Konventionen (Schmidt/Wulff, Kamb, Fisher).
