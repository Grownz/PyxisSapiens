# PyxisSapiens — Scaffold-Status (Schritt 1)

**Stand:** Oktober 2026
**Ergebnis:** Lauffähiges Gradle-Multimodul-Android-Gerüst; `assembleDebug`, `testDebugUnitTest`
und `lint` laufen erfolgreich durch.

---

## 1. Verifizierte Toolchain

| Komponente | Version / Wert |
|---|---|
| Gradle | **9.8.1** (via Wrapper, eingecheckt) |
| Android Gradle Plugin | **9.4.1** |
| Kotlin (KGP) | **2.3.21** |
| KSP | **2.3.12** |
| Jetpack Compose BOM | **2026.09.00** |
| Room | 2.8.5 |
| Hilt / hilt-navigation-compose | 2.60.1 / 1.4.0 |
| SQLCipher (sqlcipher-android) | 4.19.1 |
| MapLibre Native | 13.6.1 |
| kotlinx.serialization | 1.11.0 |
| compileSdk / targetSdk / minSdk | **37 / 37 / 26** |
| Build-Tools | 36.0.0 |
| JDK (Build) | Android Studio JBR **25** |
| applicationId / Namespace | `de.pyxissapiens` |

## 2. Wichtige Erkenntnis: AGP 9 „Built-in Kotlin"

AGP 9 bringt die Kotlin-Unterstützung integriert mit und **verbietet** das separate
`org.jetbrains.kotlin.android`-Plugin. Umgesetzt:

- Kein `kotlin-android`-Plugin mehr; Kotlin wird über AGP kompiliert.
- KGP/KSP werden im Root-`build.gradle.kts` per `buildscript`-Classpath auf die
  Projektversionen angehoben (AGP pinnt transitiv KGP 2.2.10).
- `jvmTarget` folgt automatisch `compileOptions.targetCompatibility` (Java 17).
- KMP + `com.android.library` im selben Modul ist unter AGP 9 nicht erlaubt → die
  portablen Module sind deshalb als **reine Kotlin-JVM-Module** (Android-frei) angelegt.

## 3. Modulstruktur (alle angelegt)

**Portabel / Android-frei (Kotlin JVM):** `core:common`, `core:ports`, `core:domain`,
`core:geology`, `core:wmm`

**Android-Core:** `core:ui` (Compose-Design-System), `core:database` (Room + KSP + Hilt +
SQLCipher-Abhängigkeit), `core:data` (Repositories), `core:sensors`, `core:location`,
`core:media`, `core:export`

**Features:** `feature:compass`, `projects`, `measurements`, `media`, `export`,
`stereonet`, `map`, `settings`

**App:** `app` (Hilt `@HiltAndroidApp`, Compose-Navigation mit Bottom-Navigation
Messen/Daten/Karte/Auswertung/Mehr, Instrument-Theme)

**Infrastruktur:** `.github/workflows/ci.yml`, `LICENSE` (GPLv3), `.gitignore`,
Versionskatalog (`gradle/libs.versions.toml`), Gradle-Wrapper.

## 4. Bereits funktionsfähig (nicht nur Platzhalter)

- **GeoMath** (`core:geology`): Fläche↔Pol, Linear↔Vektor, Strike/Dip-Direction-Konvertierung,
  Winkelberechnung – mit Unit-Tests (Round-Trips, Vertikalebene, Orthogonalität).
- **Room-Schema** mit Entitäten für Projekt/Site/Messung/**History**, DAOs, `PyxisDatabase`
  (KSP-generiert), Hilt-Provider.
- **Repositories** mit Entity↔Domain-Mapping.
- **SensorPort** (`core:sensors`): `SensorManager`-basierter `callbackFlow` für Accel/Gyro/Mag.
- **Export-Encoder**: CSV und GeoJSON (serialisierungsbasiert).
- **Compose-Theme** mit Instrument-Farbtokens (Dark-Default + Hochkontrast-Light) und
  `NumericReadout`-Komponente.
- **Kompilierte Native-Libs** in der APK: `libmaplibre.so`, `libsqlcipher.so`.

## 5. Bewusst als TODO markiert (noch nicht verdrahtet)

- SQLCipher-`SupportOpenHelperFactory` (DB aktuell unverschlüsselt, Abhängigkeit vorhanden)
- `LocationPort`-Implementierung (Stub ohne Fix-Quelle)
- Alle Feature-Screens sind Platzhalter
- WMM-Berechnung (Stub mit neutralem Feld)
- Kalibrierung/Auto-Stabilisierung/Messpipeline (Epic 1)
- Kein `build-logic`-Convention-Plugin (Module explizit konfiguriert; späterer Refactor)

## 6. Build lokal ausführen

Die System-Java-Installation ist Java 8 — nicht verwendbar. Build-JDK ist das JBR aus
Android Studio:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = 'C:\Users\grownz\AppData\Local\Android\Sdk'   # alternativ local.properties

.\gradlew.bat :app:assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lint
```

**Verifizierte Ergebnisse:** `assembleDebug` ✅ · `testDebugUnitTest` ✅ · `lint` ✅
(APK: `app/build/outputs/apk/debug/app-debug.apk`, ~74 MB Debug, inkl. Native-Libs).

## 7. Nächste Schritte

- **Schritt 2:** Design-System in `core:ui` implementieren (Tokens + Kernkomponenten:
  `CompassFace`, `ClinometerGauge`, `StabilizationRing`, `QualityBadge`, `InterferenceMeter`).
- **Schritt 3:** Messkern vertikal durchstechen (`core:geology`/`wmm`/Messpipeline + Unit-Tests
  + erste Mess-UI).
- **Schritt 4:** DB-/DAO-Detailentwurf, WMM-Einbindung, Export-Formate oder Stereonet-Mathematik.
