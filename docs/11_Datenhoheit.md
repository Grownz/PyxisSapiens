# PyxisSapiens — Datenhoheit / Robustheit (Schritt 7)

**Stand:** Oktober 2026
**Ergebnis:** Verschlüsselte Datenbank (SQLCipher) mit Keystore-Schlüssel und optionaler
Passphrase, automatischer Verschlüsselung/Re-Key, Feld-Historie mit Undo/Redo, Validierung und
vollständiger Löschung.

---

## 1. Verschlüsselung (SQLCipher)

- **Schlüssel:** zufälliger 256-Bit-Masterkey, mit einem **Android-Keystore-AES-Schlüssel**
  (GCM) gewrappt und in `SharedPreferences` abgelegt – transparent, keine Eingabe.
- **Optionale Passphrase:** wenn gesetzt, wird der DB-Schlüssel per **PBKDF2-HMAC-SHA256**
  (100 000, 256 Bit) aus Passphrase + Masterkey abgeleitet.
- **Wiring:** `System.loadLibrary("sqlcipher")` + `SupportOpenHelperFactory(passphrase)`.

## 2. Migration & Re-Key (`DatabaseEncryption`)

Alles über **Room auf beiden Seiten** (keine Low-Level-SQLCipher-API):

- **Erststart:** verschlüsselte DB wird angelegt (Marker-Datei).
- **Bestehende Klartext-DB:** Zeilen werden einmalig in eine neue verschlüsselte DB kopiert
  (Klartext-Datei wird entfernt).
- **Passphrase geändert:** transparentes **Re-Key** (alt → neu); schlägt es fehl, wird weiterhin
  der Schlüssel verwendet, der die Datei tatsächlich öffnet (`resolvePassphrase`).
- Bei fehlgeschlagener Klartext-Migration wird bewusst sauber neu begonnen (dokumentiert).

## 3. Historie / Undo / Redo

- `MeasurementEditManager` (core:data): `add`/`update`/`delete` schreiben **feldweise Historie**
  (`MeasurementHistory`) und pflegen **Undo-/Redo-Stacks** (in-memory, max. 100).
- UI (Messungsliste): **Undo-/Redo-Buttons** und je Datensatz ein Löschen-Button; Antippen öffnet
  die **History-Ansicht** (Feld: alt → neu).
- Der Messkern speichert jetzt über den Edit-Manager.

## 4. Validierung

- `MeasurementValidator` (core:domain) prüft Wertebereiche und Pflichtwerte je Messart vor dem
  Speichern.

## 5. Vollständige Löschung

- `DataEraser` (core:data): leert alle Tabellen und löscht Medien-/Exportverzeichnisse.
- **Mehr → Einstellungen**: „Alle Daten löschen" mit Bestätigungsdialog; dazu das Setzen/Entfernen
  der optionalen Passphrase.

## 6. Verifikation

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug test lint
```

Ergebnis: **BUILD SUCCESSFUL** — Build ✅, alle Unit-Tests ✅, Lint ✅.

## 7. Offen / Hinweise

- **Laufzeitverifikation** von SQLCipher/Keystore/Rekey war auf diesem Rechner **nicht möglich**
  (kein Emulator/Gerät) – verifiziert sind Kompilierung und Lint; die Logik ist defensiv
  (Fallback auf den funktionierenden Schlüssel) gebaut.
- **Medien** existieren noch nicht als eigene Ablage; der Löscher entfernt bereits die
  vorgesehenen Verzeichnisse.
- **Persistente Undo-Stacks** (über App-Neustart) sind bewusst nicht umgesetzt (Historie ist
  persistent).
