# App Alert release shrinking rules.
# The default Android rules (proguard-android-optimize.txt) are added in build.gradle.kts.
# Room, DataStore, Compose, Navigation and Lifecycle ship their own consumer rules, so most
# of what is needed is already covered. Add only what is proven necessary.

# Readable crash stack traces (upload mapping.txt from the release build to decode them).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Framework components are kept by the manifest (listener service, receiver, activity).
# The listener service is bound by the system by name; keep it explicit as a safety net.
-keep class com.kyawzinlinn.app.alert.core.notifications.AppAlertNotificationListenerService { *; }
-keep class com.kyawzinlinn.app.alert.core.audio.StopAlarmReceiver { *; }

# Room entities and DAOs are accessed by generated code.
-keep class com.kyawzinlinn.app.alert.core.database.** { *; }

# Notification content must never reach logs in release builds: strip debug/verbose logging.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
