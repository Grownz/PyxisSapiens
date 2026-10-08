# PyxisSapiens — Stereonet / Auswertung (Schritt 4)

**Stand:** Oktober 2026
**Ergebnis:** Vollständiger, getesteter Auswertungs-Rechenkern in `core:geology` plus interaktive
Stereonet-UI mit Dichte-Konturen (Kamb **und** Fisher-Kernel) und SVG/PNG-Export.

---

## 1. Projektion & Geometrie (`core:geology/stereo/Stereonet.kt`)

- **Schmidt (flächen-treu)** als Standard, **Wulff** umschaltbar; Lower Hemisphere.
- `project` / `unproject` (auto-Aufwärts→Abwärts-Normierung), `greatCircle` (Sampling),
  `plotPlane`/`plotLine`, `poleTrendPlunge`, Strike-Azimut.
- Round-Trip-Test: project→unproject reproduziert Trend/Plunge für beide Projektionen.

## 2. Statistik (`core:geology/stereo/Statistics.kt`)

- **Fisher**: Mittelrichtung, R̄, **κ** (overflow-sichere Bisektion), **α95**.
- **Orientierungstensor** (1/N·Σ v·vᵀ) und **Eigenzerlegung** (Jacobi-Rotation, absteigend sortiert).
- **Woodcock K/C** (Cluster vs. Girdle).
- **Fold-Achse** (kleinster Eigenvektor), **Mean Plane**, **Zwei-Ebenen-Intersektion (β-Achse)**.
- **Rose-Diagramm** (Azimut-Histogramm) und **Dip-Histogramm**.

## 3. Dichte (`core:geology/stereo/Density.kt`)

- **Beide Verfahren:** **Kamb** (Zählkreis mit Vollmer-Kerneln **raw / linear / exponential**)
  und **Fisher-Kernel** (isotrop, κ-Bandbreite).
- Raster über den Disk, normiert auf das Maximum; **Marching-Squares-Konturen** (`contours`).

## 4. Auswertungs-UI (`feature:stereonet`, Hilt-VM)

- Interaktiver Plot: Dichte-Heatmap (geclippt), Konturlinien, Großkreise, Pole (Kreuze),
  Lineare (Punkte), Außenkreis + N/E/S/W.
- Steuerung: Projektion, Datensatz (Flächen-Pole/Lineare), Verfahren (Kamb/Fisher),
  Kern (raw/linear/exponential), Zählwinkel α, κ, Kontur-Level; Elemente ein-/ausblenden.
- Statistikpanel (n, R̄, κ, α95, S1/S2/S3, Woodcock K/C, Fold-Achse, Mittlere Fläche).
- **Export**: **SVG** (Vektordokument) und **PNG** (via `CanvasDrawScope` → `ImageBitmap` →
  Bitmap) nach `getExternalFilesDir/exports/`.
- Selektor „Auswertung" in der Bottom-Navigation.

## 5. Weitere Änderungen

- `:feature:stereonet` erhält Hilt/KSP, Compose + `core:data`-Anbindung.
- Die Bottom-Navigation zeigt jetzt echte Mess- (Messen) und Auswertungsinhalte.

## 6. Verifikation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug test lint
```

Ergebnis: **BUILD SUCCESSFUL** — Build ✅, alle Unit-Tests inkl. 17 Stereonet-/Geologie-Tests ✅,
Lint ✅.

## 7. Offen / bewusst zurückgestellt

- **Datentyp-/Einheiten-Filter** im Plot (nach `typeId`/`unitId`) noch nicht umgesetzt.
- **Konturlinien** sind Segmente (keine zusammenhängenden Pfade) – für Anzeige/Export ausreichend.
- **PDF-Einbettung** der Diagramme kommt mit der Export-Pipeline (Epic 3).
- Export speichert in den App-Speicher; Teilen über das Android-Share-Sheet folgt mit dem Export-Wizard.
