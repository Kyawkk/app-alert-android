# Changelog

Generated from commit messages by `scripts/changelog.py`. Only features, fixes, performance work and refactors are listed.

## 1.0.22 - 2026-11-12

No user-facing changes.

## 1.0.21 - 2026-11-12

### Added

- **update:** check and install updates from GitHub Releases
- **release:** generate release notes and changelog from commits
- **changelog:** add changelog screen with timeline and tablet layout
- **changelog:** add previews for changelog and update UI
- **applications:** multi-keyword chip input and animated tablet detail pane

### Fixed

- **alarm:** play at the device's maximum volume when set to 100%
- **applications:** keep list order stable on toggle and animate the edit button

### Performance

- **changelog:** lazy lists, drawn timeline and fewer recompositions
- **applications:** animate keyword chips and apply filter edits instantly

## 1.0.20 - 2026-11-11

No user-facing changes.

## 1.0.19 - 2026-11-11

No user-facing changes.

## 1.0.18 - 2026-11-11

No user-facing changes.

## 1.0.17 - 2026-11-11

### Fixed

- **build:** stop repackaging kotlin and androidx classes in release
- **site:** cache-bust css and js on each deploy

## 1.0.15 - 2026-11-11

### Added

- **site:** redesign landing page

## 1.0.14 - 2026-11-11

### Added

- **site:** redesign landing page

## 1.0.13 - 2026-11-11

### Added

- **site:** add landing page and Pages deploy workflow

## 1.0.12 - 2026-11-11

### Added

- **ci:** add ProGuard rules and release pipeline
- initial release of App Alert
- **ui:** animate bottom bar and unify screen headers

### Fixed

- repair AppNavHost declaration broken by comment cleanup
- **ci:** create nightly release before uploading the APK
- add untracked source files, fonts and licenses

### Changed

- **ui:** use shared components and add screenshots to readme
