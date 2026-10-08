# Changelog

All notable changes to this project are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Compose UI / instrumented tests (require device/emulator)
- Full feature-level i18n extraction (DE/EN)
- Shapefile/GPX import, WMS parameter form
- Complementary gyro filter for heading smoothing

## [0.1.0] - 2026-10

Initial scaffold and vertical slices covering the roadmap R1–R4.

### Added
- **Project scaffold**: Gradle multimodule project (core/feature/app) with version catalog,
  wrapper, GPLv3 license and GitHub Actions CI.
- **Design system**: instrument theme, tokens, JetBrains Mono typography, custom geology icons,
  measurement/app-chrome components and a design gallery.
- **Measurement core**: WMM2025 declination (validated against NOAA test values), vector-based
  averaging with outlier rejection, Fisher κ, quality scoring and a measurement screen.
- **Analysis**: stereonet with Schmidt/Wulff projection, great circles/poles, Kamb and
  Fisher-kernel density, marching-squares contours, Fisher/eigen/Woodcock statistics, fold axis,
  mean plane, β-axis, rose and dip histograms, SVG/PNG export.
- **Export/Import**: CSV, GeoJSON, KML, KMZ, PDF report and encrypted project archive
  (AES-GCM + PBKDF2), validation, CSV/KML import, share sheet via FileProvider.
- **Map/Tracking**: MapLibre map, offline MBTiles rendering (local tile server), linework drawing
  and editing, GPS tracking with a foreground service, GPX export and basemap management.
- **Data sovereignty**: SQLCipher database encryption with Android Keystore, optional passphrase
  with transparent re-key, field-level history with undo/redo, validation and full data erasure.
- **Calibration & data management**: hard/soft-iron + tilt calibration wizard, data types and
  rock-unit catalogue, manual measurement entry/editing, filters in list and map.
- **Accuracy**: raw accelerometer + magnetometer heading fusion with iron correction applied
  before fusion; fallback to the fused rotation vector.
- **Interop**: configurable geological WMS/XYZ raster layer with link-out, optional Macrostrat
  lookup, GeoJSON/KML linework import (no backend).
- **Release polish**: R8 release build with signing placeholder, adaptive vector launcher icon,
  Robolectric tests and an i18n scaffold (DE/EN app chrome).

[Unreleased]: https://github.com/OWNER/PyxisSapiens/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/OWNER/PyxisSapiens/releases/tag/v0.1.0
