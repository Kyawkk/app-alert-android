# Contributing to App Alert

Thanks for helping. Bug reports, device-specific findings and pull requests are all welcome.

## Before you start

* Open an issue first for anything larger than a small fix, so we can agree on the approach.
* For bugs, include the **device model, Android version, and whether the screen was off/locked**. Notification and battery behaviour varies a lot by manufacturer.
* Never paste real notification content, phone numbers or message text into issues or logs.

## Setup

1. Open the project in a recent Android Studio.
2. `./gradlew testDebugUnitTest` must pass.
3. Run on a physical device when you touch anything in `core/notifications`, `core/audio` or permissions.

## Architecture rules

* Clean Architecture with feature folders: `presentation -> domain <- data`. Domain code has no Android or Compose imports.
* Each screen has `UiState`, `Action`, `ViewModel`, a stateless `Screen` and a small `Route`. No business logic in composables.
* Reuse components from `core/designsystem`. No raw colors, ad-hoc spacing or one-off buttons in feature code.
* Keep files small and focused. Split a file when it handles more than one responsibility.
* Read-modify-write on stored data must be atomic in the repository.
* Avoid `!!`. Handle failure explicitly; background work must never crash the app.
* Animations stay short and subtle, with no infinite loops.

## Privacy rules (strict)

* Notification title and text are used only in memory to apply filters.
* Never store or log notification content. Log package names and outcomes only.
* No analytics, ads, network calls or `QUERY_ALL_PACKAGES`.

## Tests

* Add unit tests for domain logic (matching, quiet hours, throttling, decisions) using the fakes in `src/test/.../testing/Fakes.kt`.
* Add instrumented tests for Room/DataStore changes and Compose UI tests for screen behaviour.
* Bug fixes should come with a regression test where practical.

## Pull requests

* One focused change per PR with a short description of what and why.
* Include screenshots for UI changes (light and dark, and a large font size if layout changed).
* State which device/Android version you tested on.
* Make sure `./gradlew testDebugUnitTest` passes.

## License

By contributing you agree that your contributions are licensed under the MIT License.
