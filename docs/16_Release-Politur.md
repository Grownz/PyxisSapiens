# PyxisSapiens — Release-Politur & Verifikation (Schritt 12)

**Stand:** Oktober 2026
**Ergebnis:** Release-Build mit R8, adaptives App-Icon, Robolectric-Tests für die Datenschicht und
i18n-Grundgerüst (DE/EN).

---

## 1. Release-Konfiguration

- `release` mit **R8**: `isMinifyEnabled = true`, `isShrinkResources = true`.
- **Signing:** Debug-Signing als Platzhalter, damit `assembleRelease` ohne Keystore läuft; ein
  echter Keystore wird über `keystore.properties` (storeFile/storePassword/keyAlias/keyPassword)
  automatisch verwendet, sofern vorhanden.
- **ProGuard-Regeln** (`app/proguard-rules.pro`): Keeps für **kotlinx.serialization**,
  **SQLCipher** (`net.zetetic`/`net.sqlcipher`) und **MapLibre**.
- Ergebnis: `app-release.apk` (~56,5 MB) und `app-debug.apk` (~75 MB).

## 2. App-Icon

- **Adaptives Launcher-Icon** (Vektor, Instrument-Stil: Kompassrose) mit **Round-** und
  **Monochrome**-Variante (`mipmap-anydpi-v26/ic_launcher[_round].xml`,
  `drawable/ic_launcher_foreground.xml`, Farbe `ic_launcher_background`).
- Im Manifest als `android:icon`/`android:roundIcon` gesetzt.

## 3. Tests

- **Robolectric-Unit-Tests** (`core:data`, laufen ohne Gerät, `@Config(sdk=[33])`):
  - `MeasurementEditManager`: add → undo → redo → delete → undo;
  - `update` schreibt **Feld-Historie**;
  - Repository-Mapping (GeoPoint) Round-Trip;
  - `GeoJsonGeom` Round-Trip; Linework-Repository Round-Trip.
- Zusätzlich bestehen alle Pure-Tests (Geologie, Stereonet, Messpipeline, WMM, Kalibrierung,
  Export/Import).

## 4. i18n-Grundgerüst

- App-Chrome-Strings (Navigation, „Mehr"-Aktionen) ausgelagert nach
  `values/strings.xml` (**Englisch als Default**) und `values-de/strings.xml` (**Deutsch**);
  `PyxisApp` nutzt `stringResource(...)`.
- Feature-interne Texte sind weiterhin deutsch – schrittweise Auslagerung als Folgeschritt.

## 5. Verifikation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug test lint assembleRelease
```

Ergebnis: **BUILD SUCCESSFUL** — Debug ✅, alle Unit-Tests (inkl. Robolectric) ✅, Lint ✅,
**Release (R8)** ✅.

## 6. Offen / Hinweise

- **Signing** ist ein Platzhalter (Debug); für eine Veröffentlichung echten Keystore via
  `keystore.properties` hinterlegen.
- **Feature-Strings** sind noch nicht vollständig in Ressourcen ausgelagert.
- **Compose-UI-/Instrumented-Tests** benötigen ein Gerät/Emulator und sind als Folgeschritt
  vorgesehen.
- **`keystore.properties`** ist per `.gitignore` ausgeschlossen.
