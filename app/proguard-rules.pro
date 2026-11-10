-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class com.kyawzinlinn.app.alert.core.notifications.AppAlertNotificationListenerService { *; }
-keep class com.kyawzinlinn.app.alert.core.audio.StopAlarmReceiver { *; }
-keep class com.kyawzinlinn.app.alert.core.database.** { *; }

-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
