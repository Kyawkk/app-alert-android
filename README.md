# App Alert

App Alert plays an alarm or ringtone when a notification arrives from apps you choose (Viber, WhatsApp, Telegram, …). It is built for the moments you can't watch your phone: asleep, away from the desk, or in a meeting you can't check.

* **Platform:** Android 7.0+ (minSdk 24), Kotlin, Jetpack Compose, Material 3
* **Privacy:** everything stays on the device. Message text is read only to apply your filters, is never stored, and never appears in logs.
* **Weight:** no polling, no wake locks, no always-running service. It reacts to system callbacks.

## Features

| Area | What you get |
|---|---|
| Monitoring | Global switch, per-app switch, honest status (never "active" unless Android has really bound the listener) |
| Apps | Searchable list of installed apps, selected apps first, per-app sound and filters |
| Filters | Keywords (title, text or both) and notification categories (messages, calls, email, …) |
| Alarm | Sound (system or your own audio file), volume, duration, repeat count/interval, vibration, cooldown, test button |
| Noise control | Duplicate detection, per-app cooldown, group-summary/ongoing notifications ignored (calls excepted) |
| Schedule | Quiet hours with start/end and weekdays, including windows that cross midnight |
| History | Last 30 days / 500 entries, outcome only (played, failed, skipped), clearable |
| Onboarding | 8-step setup: access, optional Do Not Disturb, first app, sound, test |
| Adaptive UI | Bottom bar on phones, navigation rail on tablets, side-by-side apps + settings on large screens, light/dark |

## Getting started

1. Open the project in a recent Android Studio (it uses AGP 9.x and Gradle 9.x, see `gradle/libs.versions.toml`).
2. Run the `app` configuration on a **physical device** if you can. Notification delivery, battery management and Do Not Disturb behave differently on emulators.
3. Follow the onboarding. Android requires you to enable **Notification access** yourself (Settings → Notifications → Device & app notifications → App Alert). The app can open that screen but can never switch it on.
4. Select an app, then send yourself a notification from it. You should hear the alarm.

```bash
./gradlew testDebugUnitTest          # JVM unit tests
./gradlew connectedDebugAndroidTest  # Room, DataStore and Compose UI tests (device/emulator)
./gradlew assembleRelease            # release build
```

## How it works

```text
Android system
   │  onNotificationPosted
   ▼
AppAlertNotificationListenerService      core/notifications   (thin: builds a NotificationEvent)
   │  1. own package? → ignore   2. package not selected? → ignore (in-memory check, no allocation)
   ▼
ProcessIncomingNotificationUseCase       feature/alarm/domain
   │  ShouldTriggerAlarmUseCase decides, in this order:
   │  monitoring on → app selected → app enabled → real new message → quiet hours
   │  → filters match → not a duplicate → not in cooldown
   ▼
StartAlarmPlaybackUseCase → AlarmPlayer  (AlarmPlaybackController: MediaPlayer + vibration,
   │                                      USAGE_ALARM, one alarm at a time)
   ▼
AlertHistoryRepository                   outcome only, bounded, failures never block the alarm
```

Design decisions worth knowing:

* **Package names identify apps**, never display names. Nothing app-specific is hard-coded.
* **Duplicates:** Android re-posts the same notification key for updates. The same key with the same title/text/time within 10 minutes counts as one event; changed content counts as a new message.
* **Duplicate/cooldown state lives in memory.** After a process restart the worst case is one extra alarm, never a missed one.
* **Volume** is a percentage of the device's alarm volume. The system volume is never changed.
* **Alarm length is always bounded** (duration × repeats) and can be stopped from the app or the "Stop" notification.
* **Reliability:** the listener requests a rebind when disconnected, `MainActivity.onResume` re-checks permissions and rebinds if access is granted but the service is not connected, and the dashboard shows exactly which of those conditions is failing.

## Architecture

Clean Architecture with feature folders in one Gradle module. Dependencies point inward: `presentation → domain ← data`.

```text
app/src/main/java/com/kyawzinlinn/app/alert/
├── AppAlertApplication.kt, MainActivity.kt
├── app/                  AppContainer (manual DI), navigation, ViewModel factory
├── core/
│   ├── common/           TimeProvider, launchCatching
│   ├── designsystem/     theme (colors, type, shapes, spacing), reusable components, preview helpers
│   ├── database/         Room: history + per-app rules
│   ├── datastore/        Preferences keys and safe reads
│   ├── notifications/    NotificationListenerService, listener connection state
│   ├── audio/            playback controller, sound inspector, "Stop" notification
│   ├── permissions/      permission state + settings intents
│   └── ui/               shared composables (app icon, window width, sound pickers, formatting)
└── feature/
    ├── dashboard/  applications/  alarm/  history/  settings/  onboarding/
    └── each: domain/ (models, repository interfaces, use cases) · data/ · presentation/
```

Conventions:

* Every screen has `UiState` (immutable), `Action` (sealed), a `ViewModel` exposing `StateFlow`, a stateless `Screen` and a small `Route` that wires the ViewModel. Screens are previewable with sample data and never touch real notification access.
* State is collected with `collectAsStateWithLifecycle()`; storage writes go through `launchCatching`, so a failing write is logged instead of crashing.
* Read-modify-write operations (alarm settings, quiet hours, app rules) are atomic in the repository, so rapid taps cannot overwrite each other.
* Storage: **DataStore** for preferences (switches, sound, volume, quiet hours) and **Room** for structured data (history, per-app rules). History is limited to 30 days and 500 entries.
* Manual constructor injection through `AppContainer` keeps the build free of extra annotation processing (only Room uses KSP). Moving to Hilt later only touches `app/`.

## Design system

Everything visual lives in `core/designsystem`:

* **Color:** semantic Material 3 scheme (light/dark) plus `AppAlertTheme.extendedColors` for success and warning. Composables never use raw colors.
* **Spacing:** the `Spacing` scale (4–48dp) and `Dimens` (48dp minimum touch target, 840dp readable width).
* **Components:** `AppAlertCard`, `AppAlertButton`, `AppAlertSwitchRow`, `AppAlertSlider`, `AppAlertDialog`, `AppAlertEmptyState`, `AppAlertSectionHeader`, `AppAlertPermissionCard`, `AppAlertTopBar`.
* **Motion:** short, subtle and consistent. Tabs fade through; detail screens slide in; status text, app icons, list items and the Test/Stop button cross-fade or animate placement. Durations are short and honour the system animation scale. No infinite animations.
* **Accessibility:** state is always text plus icon, never color alone; switch rows are single `Role.Switch` targets; the monitoring status is a polite live region; fonts scale; previews cover light, dark and large font scales.

## Permissions

| Permission | Why | Required |
|---|---|---|
| Notification access (user-granted in system settings) | Lets the listener see notifications | Yes |
| `POST_NOTIFICATIONS` (Android 13+) | Show the "Stop" notification while ringing; alarms still play without it | No |
| Do Not Disturb access | Lets you allow App Alert in DND settings; never changed silently | No |
| Battery: unrestricted | More reliable monitoring with the screen off on aggressive OEMs | No |
| `VIBRATE` | Vibration with the alarm | No |

The app uses a `<queries>` entry for launcher apps instead of `QUERY_ALL_PACKAGES`, and requests no storage permission (custom sounds use the document picker with a persistable grant).

## Testing

* **Unit (`src/test`)**: matcher, quiet hours (incl. overnight and week wrap), throttle (duplicates, cooldown, bounded memory), trigger decisions, the full notification workflow with fakes, monitoring status, settings clamping, selection/rule use cases.
* **Instrumented (`src/androidTest`)**: Room retention and rules, DataStore persistence across process restart, dashboard and application-list UI behaviour.
* **Manual (required before trusting it)**: test on Android 13–16 and a real device with the screen off, locked, app swiped from recents, after reboot, with Battery Saver and with Do Not Disturb. Emulator results alone do not prove background reliability.

## Troubleshooting

* **"Waiting for Android to connect"** – access is on but the service is not bound. Toggle App Alert's notification access off and on, or reopen the app (it asks Android to rebind).
* **No sound** – raise the phone's *alarm* volume (App Alert's volume is relative to it) and check Do Not Disturb.
* **Stops working after a while on Xiaomi/Huawei/OnePlus/Samsung** – set App Alert to unrestricted battery use and allow auto-start. See <https://dontkillmyapp.com>.
* **Custom sound stopped playing** – the file was moved or deleted; App Alert falls back to the default alarm sound so you are never left without an alert.

## Known limitations

* Playback runs in the app process (kept alive by the system's listener binding) instead of a foreground service. Verify on your target devices.
* UI strings are English-only and not yet in `strings.xml`.
* The onboarding step is not restored after process death.
* Notifications that Android groups into a single summary may only alert for the individual messages the app posts.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for the rules every change follows, and the [LICENSE](LICENSE) (MIT).
