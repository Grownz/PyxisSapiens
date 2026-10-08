# Contributing to PyxisSapiens

Thanks for your interest in improving PyxisSapiens! This project is open source under the
**GNU GPLv3** and welcomes contributions of all kinds: code, geology domain expertise, translations,
documentation, icons and testing.

## Ways to contribute

- **Bug reports & feature requests** – use the GitHub issue templates.
- **Code** – bug fixes, features from the roadmap, performance, tests.
- **Geology / measurement correctness** – algorithms, conventions, field workflows.
- **Translations** – German/English (and more) strings.
- **Documentation** – `docs/` and the README.

## Development setup

Requirements:

- **JDK 17+** for building. Android Studio's bundled JBR (JDK 25) works well.
- **Android SDK** with **Platform API 37** and **Build-Tools 36.0.0**.
- **Gradle** is provided by the wrapper (`./gradlew`), no local install needed.

On Windows, point `JAVA_HOME` at a modern JDK (the system Java 8 is not sufficient):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

Common tasks:

```powershell
.\gradlew.bat assembleDebug        # build the debug APK
.\gradlew.bat test                 # all unit tests (incl. Robolectric)
.\gradlew.bat lint                 # Android Lint
.\gradlew.bat assembleRelease      # R8 release build
```

Do **not** commit `local.properties`, `keystore.properties`, keystores or any secrets.

## Project layout

- `core/*` – reusable libraries. `core/common`, `core/ports`, `core/domain`, `core/geology`,
  `core/wmm` are **Android-free** (pure Kotlin) and portable.
- `core/database`, `core/data`, `core/sensors`, `core/location`, `core/media`, `core/export`,
  `core/ui` – Android modules.
- `feature/*` – UI features (compass, measurements, stereonet, map, export, settings).
- `app` – application shell, navigation, DI wiring.
- `docs/` – analysis, requirements, architecture, UI concept and per-step notes.

## Coding guidelines

- **Kotlin**, **Jetpack Compose**, **Hilt**, **Room**, **kotlinx.serialization**.
- Keep the **domain layer Android-free** (KMP-ready) – platform access goes through ports.
- Prefer **vector/geology correctness over shortcuts**; add unit tests for math and pipelines.
- Follow the existing formatting (4-space indent, trailing commas) and naming.
- Every change should build with `assembleDebug test lint` green.

## Tests

- Pure logic (geology, stereonet, WMM, calibration, measurement pipeline, export) → `kotlin.test`/JUnit.
- Android data layer → **Robolectric** unit tests.
- When adding measurement/analysis code, add or extend tests.

## Commit & pull requests

- Use clear, conventional commit messages (`feat:`, `fix:`, `docs:`, `test:`, `chore:`).
- Keep PRs focused; describe **what** and **why**, and reference issues.
- Update `CHANGELOG.md` and `docs/` when behaviour changes.
- By contributing you agree that your contributions are licensed under **GPLv3**.

## Code of conduct

Participation is governed by our [Code of Conduct](CODE_OF_CONDUCT.md).
