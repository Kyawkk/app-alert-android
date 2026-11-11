# App Alert

An Android app that plays an alarm when a notification arrives from apps you pick, for the times you can't watch your phone. Your notifications stay on the device. Message text is only read to apply your filters and is never stored or logged. The only network use is the update check you start yourself in Settings.

Kotlin, Jetpack Compose, Material 3, minSdk 24.

Website: https://kyawkk.github.io/app-alert-android/

## Features

- Pick which apps trigger the alarm, each with its own sound and optional keyword and category filters
- Custom sound (system ringtone or your own audio file), volume, duration, repeats, vibration
- Quiet hours with weekdays, including windows that cross midnight
- Duplicate detection and a per-app cooldown
- A monitoring status that is only "active" when Android has really bound the listener
- Alert history (outcome only, last 30 days)
- Phone and tablet layouts, with a System / Light / Dark theme switch (Settings > Theme)

## Screenshots

<p>
  <img src="screenshots/phone-home.png" width="19%" alt="Home" />
  <img src="screenshots/phone-apps.png" width="19%" alt="Applications" />
  <img src="screenshots/phone-app-rules.png" width="19%" alt="Per-app sound and filters" />
  <img src="screenshots/phone-history.png" width="19%" alt="History" />
  <img src="screenshots/phone-settings.png" width="19%" alt="Settings" />
</p>

On tablets and landscape the bottom bar becomes a navigation rail and Applications shows a list and detail side by side.

<p>
  <img src="screenshots/tablet-home.png" width="49%" alt="Home on a tablet" />
  <img src="screenshots/tablet-apps.png" width="49%" alt="Applications on a tablet" />
</p>
<p>
  <img src="screenshots/tablet-history.png" width="49%" alt="History on a tablet" />
  <img src="screenshots/tablet-settings.png" width="49%" alt="Settings on a tablet" />
</p>

## Run it

Open the project in a recent Android Studio and run the `app` configuration, ideally on a physical device. Android requires you to turn on Notification access yourself (Settings > Notifications > Device & app notifications). The onboarding opens that screen for you.

```bash
./gradlew testDebugUnitTest          # unit tests
./gradlew connectedDebugAndroidTest  # Room, DataStore and UI tests (needs a device)
./gradlew assembleRelease            # R8-shrunk release build
```

## How it works

```text
NotificationListenerService      builds a NotificationEvent, ignores unselected apps
  -> ProcessIncomingNotificationUseCase
       monitoring on > app selected > app enabled > not a summary/ongoing
       > quiet hours > filters > duplicate > cooldown
  -> AlarmPlaybackController     MediaPlayer + vibration, one alarm at a time
  -> history                     outcome only; a failed write never blocks the alarm
```

- Apps are identified by package name.
- Duplicate and cooldown state is kept in memory. After a restart the worst case is one extra alarm, never a missed one.
- Volume is relative to the device's alarm volume. System volume is never changed.
- Every alarm has a bounded length and can be stopped from the app or its notification.

## Performance

Measured on a Lenovo TB522FU (Android 17) with the `benchmark` build type. Numbers vary by device and Android version, so treat them as a baseline to compare against, not a promise.

| Area | Test | Result |
|---|---|---|
| Startup | Warm start, time to initial display (median of 10) | 35.9 ms |
| UI | Switching tabs, frame CPU time P50 / P95 / P99 | 2.3 / 4.6 / 8.4 ms |
| UI | Scrolling the Applications list, frame CPU time P50 / P95 / P99 | 3.4 / 5.7 / 7.4 ms |
| Alarm logic | Notification from a selected app, median / P99 | 15.5 µs / 302.9 µs |
| Alarm logic | Notification from an ignored app, median / P99 | 1.5 µs / 18.2 µs |
| Size | Release APK | 1.95 MB (dex 1.42 MB, native libs 0.25 MB, resources 0.14 MB) |
| Build | Clean release build, median of 3 | 71.7 s |
| Build | Clean debug build, median of 3 | 17.7 s |
| Build | Debug build with nothing changed, median of 3 | 2.1 s |

Frame times stay well under the 16.7 ms budget for 60 fps. The alarm logic figures are from a JVM test and measure only our code; end-to-end time from notification to sound depends on Android and is measured on the device. Build times were measured on a Mac and depend on the machine.

Benchmark sources live in the `benchmark/` module and `scripts/`. See [benchmark/README.md](benchmark/README.md) for how to run them.

## Architecture

One Gradle module, feature folders, dependencies pointing inward (`presentation -> domain <- data`).

```text
app/            manual DI (AppContainer), navigation
core/           common, designsystem, database, datastore, notifications, audio, permissions, ui
feature/        dashboard, applications, alarm, history, settings, onboarding
                each with domain/, data/, presentation/
```

Each screen has a `UiState`, an `Action`, a `ViewModel`, a stateless `Screen` and a small `Route`. Preferences are in DataStore, history and per-app rules are in Room. Read-modify-write operations are atomic in the repositories.

## Permissions

| Permission | Purpose | Required |
|---|---|---|
| Notification access | See notifications from selected apps | Yes |
| `POST_NOTIFICATIONS` (Android 13+) | Show the Stop notification while ringing | No |
| Do Not Disturb access | Let you allow alarms in DND | No |
| Unrestricted battery use | More reliable on aggressive OEMs | No |
| `VIBRATE` | Vibration | No |
| `INTERNET` | Check GitHub Releases and download an update, only when you tap "Check for updates" | No |
| `REQUEST_INSTALL_PACKAGES` | Hand a downloaded update to the Android installer | No |

No `QUERY_ALL_PACKAGES` and no storage permission (custom sounds use the document picker).

## Updates

Settings > Updates > Check for updates asks the GitHub Releases API for the latest release, downloads its APK, and opens the Android installer. Nothing is checked in the background and nothing about you or your alerts is sent.

Before the installer opens the app verifies that the download matches the SHA-256 in the release's `update.json`, that it is this app (same package name and version code), and that it is signed with the same key as the installed one. Android still asks you to confirm every install and only accepts an update signed with the key of the installed app. If you installed a build signed with a different key (for example a throwaway CI key), uninstall once and install the new release.

Releases published before `update.json` existed are ignored by the updater. Pre-releases are never offered. Google Play restricts `REQUEST_INSTALL_PACKAGES`, so remove the update feature before publishing there.

## Releasing

CI runs unit tests, lint and a release build on every push and pull request. Each push to `main` also publishes a new GitHub Release (`v1.0.<run number>`, marked Latest) with the APK attached. Pushing a version tag builds a signed APK and AAB and publishes a GitHub Release:

```bash
git tag v1.0.0
git push origin v1.0.0
```

Release notes come from the commit messages (`feat`, `fix`, `perf`, `refactor` are listed; `docs`, `ci` and `chore` are not), and every version is also listed in [CHANGELOG.md](CHANGELOG.md). After a release, refresh it with `python3 scripts/changelog.py`; this also updates the copy shown in Settings > What's new. A build can only list versions tagged before it, so the newest release appears in the app one release later, and on the update card before it is installed.

Tags with a suffix like `v1.1.0-beta1` become pre-releases. Without signing secrets the release is signed with a throwaway test key and marked as a pre-release.

To sign with your own key, create a keystore and keep it safe (losing it means you can't update the app):

```bash
keytool -genkeypair -v -keystore release.jks -alias appalert \
  -keyalg RSA -keysize 4096 -validity 10000
base64 -w0 release.jks
```

Add these repository secrets: `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`. In-app updates only work between builds signed with the same key, so set them before relying on updates. Every release also carries an `update.json` (version code, APK name, SHA-256) that the updater reads. Never commit the keystore. Keep each release's `mapping.txt` to decode crash traces.

## Troubleshooting

- **Waiting for Android to connect:** access is on but the service isn't bound. Toggle the notification access off and on, or reopen the app.
- **No sound:** raise the alarm volume and check Do Not Disturb.
- **Stops working after a while on Xiaomi, Huawei, OnePlus or Samsung:** set battery to unrestricted and allow auto-start. See https://dontkillmyapp.com.
- **Custom sound stopped playing:** the file was moved or deleted. The app falls back to the default alarm sound.

## Known limitations

- Playback runs in the app process, not a foreground service.
- UI strings are English only and not yet in `strings.xml`.
- The onboarding step is not restored after process death.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Licensed under [MIT](LICENSE).
