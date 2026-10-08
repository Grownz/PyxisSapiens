# PyxisSapiens — Genauigkeit: Roh-Mag-Heading (Schritt 10)

**Stand:** Oktober 2026
**Ergebnis:** Der Heading wird aus einer **Accel+Mag-Fusion** berechnet, mit angewandter
**Hard-/Soft-Iron-Korrektur** vor der Fusion; Fallback auf den Rotationsvektor ohne Magnetometer.

---

## 1. Fusion

- `AndroidOrientationPort` registriert **Accelerometer** und **Magnetometer** und berechnet die
  Orientierung mit `SensorManager.getRotationMatrix(R, null, accel, magCorr)`.
- **Kalibrierung vor der Fusion:** die Roh-Magnetometerwerte werden zuerst mit dem
  Hard-/Soft-Iron-Fit korrigiert (`applyCalibration`), erst dann fusioniert – dadurch wirkt sich
  die Kalibrierung direkt auf den **Azimut** aus (nicht nur auf die Feldstärke).
- **Konvention unverändert:** die Spalten von `R` sind die Geräteachsen im Welt-ENU
  (X=Ost, Y=Nord, Z=Up) → die Messpipeline bleibt unverändert.
- **Fallback:** ohne Magnetometer wird weiterhin `TYPE_ROTATION_VECTOR` genutzt.

## 2. Kalibrier-Provider

- Neuer Port `MagCalibrationProvider` (+ `MagCalibration`) in `core:ports`.
- `CalibrationRepository` (core:data) implementiert den Provider; Hilt-Binding in
  `CalibrationModule`. Der Sensor-Layer hängt nur vom Port ab (keine Data-Abhängigkeit).

## 3. Auswirkung

- Der `CompassViewModel` wendet weiterhin die **Tilt-Korrektur** auf die Frames an; die
  **Magnetometer-Kalibrierung** fließt nun zusätzlich in die Fusion ein.
- Interferenzanzeige nutzt die kalibrierte Feldstärke (bereits in Schritt 8).

## 4. Verifikation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug test lint
```

Ergebnis: **BUILD SUCCESSFUL** — Build ✅, alle Unit-Tests ✅, Lint ✅.

## 5. Offen / Hinweise

- **Laufzeitverifikation** der Fusion war hier nicht möglich (kein Emulator/Gerät).
- **Gyroskop** wird in dieser Fusion nicht verwendet (Stabilität über Mittelung/Ausreißerfilter);
  ein Komplementärfilter (Gyro-Stabilisierung des Headings) ist der nächste mögliche
  Genauigkeitsschritt.
- **Sensor-Deaktivierung/manueller Modus** (docs/03 §4) bleibt als Option für Geräte mit
  schlechtem Magnetometer vorgesehen.
