# Contributing

Bug reports and pull requests are welcome. For anything bigger than a small fix, open an issue first.

For bugs, include the device model, Android version and whether the screen was off or locked. Never paste real notification content or personal data.

## Setup

Open the project in Android Studio. `./gradlew testDebugUnitTest` must pass. Test on a physical device if you change anything in `core/notifications`, `core/audio` or permissions.

## Code rules

- `presentation -> domain <- data`. Domain code has no Android or Compose imports.
- No business logic in composables. Each screen has `UiState`, `Action`, `ViewModel`, a stateless `Screen` and a `Route`.
- Use the components and tokens in `core/designsystem` instead of raw colors or one-off styles.
- Read-modify-write on stored data goes through the repository so it stays atomic.
- No `!!`. Background work must not crash the app.
- Never store or log notification title or text. Log package names and outcomes only.
- No analytics, ads or network calls.

## Tests

Add unit tests for domain logic using the fakes in `src/test`, and instrumented tests for Room, DataStore and UI changes. Add a regression test with bug fixes where practical.

## Pull requests

Keep them focused, say what you tested on, and add screenshots (light and dark) for UI changes. By contributing you agree to license your work under the MIT License.
