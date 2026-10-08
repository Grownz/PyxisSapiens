# PyxisSapiens — GitHub-Setup / Migration

**Stand:** Oktober 2026
**Ziel:** Dieses lokale Git-Repository (`main`) als öffentliches GitHub-Repository
**Grownz/PyxisSapiens** veröffentlichen.

> Die Platzhalter (`OWNER`) sind bereits durch **Grownz** ersetzt; Branch ist `main`, Remote wird
> unten gesetzt.

> Das Repository ist bereits vorbereitet: Branch `main`, `.gitignore`/`.gitattributes`,
> GPLv3-Lizenz, CI-Workflow, Community-Dateien. Es fehlt nur noch das Anlegen des Remote und der Push.

---

## 1. Repository auf GitHub anlegen

**Variante A – Weboberfläche:** Auf https://github.com/new ein neues, **leeres** Repository
`PyxisSapiens` erstellen (Public, **ohne** README/`.gitignore`/Lizenz, da bereits vorhanden).

**Variante B – GitHub CLI** (falls installiert und angemeldet):

```powershell
gh repo create PyxisSapiens --public --source . --remote origin --push
```

## 2. Remote setzen und pushen (falls nicht via `gh`)

```powershell
```powershell
git remote add origin https://github.com/Grownz/PyxisSapiens.git
git push -u origin main
```

## 3. Platzhalter

Die `OWNER`-Platzhalter wurden bereits durch **Grownz** ersetzt in:

- `README.md` – Badges
- `CHANGELOG.md` – Vergleichs-/Release-Links
- `.github/ISSUE_TEMPLATE/config.yml` – Dokumentations-/Security-Links
- `.github/CODEOWNERS` – `@Grownz`

Schnellprüfung (ignoriert das Wort „CODEOWNERS"):

```powershell
git grep -n "github.com/OWNER"
```

## 4. CI und Schutz (optional, empfohlen)

- Der Workflow `.github/workflows/ci.yml` läuft bei Push/PR auf `main`:
  `assembleDebug`, `test` (inkl. Robolectric) und `lint`.
- **Branch-Schutz `main` (konfiguriert):**
  - Änderungen nur über **Pull Requests**;
  - erforderlicher Status-Check: **`build`**;
  - erforderliche Approvals: **0** (Single-Maintainer);
  - `enforce_admins` aus (Notfall-Bypass möglich).
- Optional: Dependabot ist aktiv (`.github/dependabot.yml`) und öffnet wöchentliche Updates.

## 5. Secrets / Signing

- **Kein Secret nötig** für die CI (Build/Test/Lint).
- **Keystores/`keystore.properties` werden nicht committet** (in `.gitignore`); `local.properties`
  ebenso. Für signierte Release-Artefakte in CI später **GitHub Actions Secrets** verwenden
  (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) und den Workflow erweitern.

## 6. Verifizieren

- Actions-Tab: der „CI“-Workflow ist grün.
- Badges im README zeigen den Status (nach Schritt 3 korrekt).
- `git status` ist sauber; keine Secrets/`build`/`.gradle` getrackt.

## 7. Hinweise

- **Zeilenenden** werden über `.gitattributes` normalisiert (`gradlew` bleibt LF für Linux-CI).
- **Lizenz:** GPLv3 (`LICENSE`) – Beiträge fallen unter dieselbe Lizenz.
- **F-Droid** (optional): Metadaten können später ergänzt werden (`metadata/` bzw. `fdroidserver`);
  die App ist quelloffen, werbefrei und ohne Tracking.
