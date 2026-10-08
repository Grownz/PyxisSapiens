# PyxisSapiens — Design-System (Schritt 2)

**Stand:** Oktober 2026
**Modul:** `core:ui` · **Galerie:** `de.pyxissapiens.core.ui.gallery.DesignGallery`
(app-interne Route „Mehr → Design-Galerie öffnen")

---

## 1. Fundament

- **Theme:** `PyxisTheme(darkTheme = …)` — Dark-Default („Instrument"), Hochkontrast-Light-Variante.
- **Farben:** `PyxisPalette` (Rohwerte) + `ColorScheme`; semantische **Tokens** `PyxisTokens`
  (ok/warn/critical/north/grid, Abstände, Mindest-Touchziel) über `LocalPyxisTokens`.
- **Typografie:** **JetBrains Mono** (SIL OFL, gebündelt) für Display/Headline/Title/Readouts
  (tabellarische, nicht springende Ziffern); Systemschrift für Fließtext.
  Lizenz: `core/ui/licenses/JetBrainsMono-OFL.txt`, Fonts: `core/ui/src/main/res/font/`.
- **Icons:** eigene Geologie-Symbole als `ImageVector` in `PyxisIcons`
  (StrikeDip, Lineation, Foliation, Fault, Joint, Fold, Bearing).

## 2. Komponenten

**Messkern (`core:ui/component`)**
| Komponente | Zweck |
|---|---|
| `NumericReadout` | großer Messwert + Einheit + Label |
| `CompassFace` | Kompassrose mit Skalenstrichen, N/E/S/W und Azimutnadel |
| `ClinometerGauge` | Halbkreis-Neigungsanzeige (0–90°) mit Zeiger |
| `StabilizationRing` | Fortschrittsring der Auto-Stabilisierung |
| `QualityBadge` / `QualityDot` | Qualität (Symbol + Text + Restunsicherheit), nicht nur Farbe |
| `InterferenceMeter` | Live-µT + magnetische Störungswarnung |
| `HoldButton` | große Primäraktion (≥ 72 dp) |
| `NorthRefToggle` / `NotationToggle` | Geo/Magnetisch bzw. Notation (DD/Dip, RHR, Quadrant) |

**App-Chrome (`core:ui/component`)**
| Komponente | Zweck |
|---|---|
| `PyxisPanel` | erhöhte Inhaltsfläche/Card |
| `SectionHeader` | Abschnittstitel + optionale Aktion |
| `MeasurementListItem` | kompakte Feldlisten-Zeile mit Symbol, Werten, Qualität |
| `StatusPill` | kleines Statuslabel (z. B. GPS-Genauigkeit) |
| `EmptyState` | Leerzustand mit Icon |

## 3. Verifikation

- `:app:assembleDebug` ✅ · `testDebugUnitTest` ✅ · `lint` ✅
- Galerie zeigt alle Komponenten und dient als visuelle Referenz (auch `@Preview`).

## 4. Hinweise

- `CompassFace`/`ClinometerGauge` zeichnen Labels via `android.graphics.Paint` (nur Android).
- Farbe wird nie als alleiniger Zustandsträger verwendet (A11y, NFA-9).
