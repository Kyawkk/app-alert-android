package com.kyawzinlinn.app.alert.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmFailure
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmPlayer
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmRequest
import com.kyawzinlinn.app.alert.feature.alarm.domain.AlarmStartResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmPlaybackController(
    context: Context,
    private val scope: CoroutineScope,
    private val notifier: AlarmNotifier,
) : AlarmPlayer {

    private val context = context.applicationContext
    private val audioManager: AudioManager? = this.context.getSystemService(AudioManager::class.java)
    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            this.context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            this.context.getSystemService(Vibrator::class.java)
        }

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val prefs = this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private var savedAlarmVolume: Int? = null

    private val lock = Any()
    private var currentPlayer: MediaPlayer? = null
    private var currentJob: Job? = null
    private var focusRequest: AudioFocusRequest? = null
    private var generation = 0

    // losing audio focus must not silence an alarm, so changes are only logged
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        Log.d(TAG, "Audio focus changed ($change); alarm keeps playing")
    }

    init {
        restoreVolumeLeftRaised()
    }

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    override suspend fun start(request: AlarmRequest): AlarmStartResult {
        val myGeneration = synchronized(lock) {
            stopLocked()
            ++generation
        }

        val requested = request.soundUri?.let { Uri.parse(it) }
        val fallback = defaultAlarmUri()
        val volume = request.volumePercent.coerceIn(0, 100) / 100f
        val player = withContext(Dispatchers.IO) {
            requested?.let { prepare(it, volume) } ?: fallback?.let { prepare(it, volume) }
        }
        if (player == null) {
            val reason = if (requested == null && fallback == null) {
                AlarmFailure.NO_SOUND_AVAILABLE
            } else {
                AlarmFailure.SOUND_UNREADABLE
            }
            return AlarmStartResult.Failed(reason)
        }

        synchronized(lock) {
            if (generation != myGeneration) { // stop() or a newer start() won the race
                releaseQuietly(player)
                return AlarmStartResult.Started
            }
            currentPlayer = player
            raiseAlarmVolume(request.volumePercent)
            requestFocus()
            _isPlaying.value = true
            notifier.show()
            currentJob = scope.launch { runPlayback(player, request) }
        }
        return AlarmStartResult.Started
    }

    override fun stop() {
        synchronized(lock) {
            stopLocked()
            generation++
        }
    }

    private suspend fun runPlayback(player: MediaPlayer, request: AlarmRequest) {
        try {
            repeat(request.repeatCount + 1) { index ->
                if (index > 0) delay(request.repeatIntervalSeconds * 1000L)
                player.start()
                if (request.vibrate) startVibration()
                delay(request.durationSeconds * 1000L)
                player.pause()
                vibrator?.cancel()
            }
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Player released while playing") // stop() raced with the loop
        } finally {
            synchronized(lock) {
                if (currentPlayer === player) stopLocked() else releaseQuietly(player)
            }
        }
    }

    private fun prepare(uri: Uri, volume: Float): MediaPlayer? {
        val player = MediaPlayer()
        return try {
            player.setAudioAttributes(audioAttributes)
            player.setDataSource(context, uri)
            player.isLooping = true
            player.setVolume(volume, volume)
            player.setOnErrorListener { failed, what, _ ->
                Log.w(TAG, "MediaPlayer error $what")
                synchronized(lock) { if (currentPlayer === failed) stopLocked() }
                true
            }
            player.prepare()
            player
        } catch (e: Exception) {
            Log.w(TAG, "Cannot prepare sound: ${e.javaClass.simpleName}")
            releaseQuietly(player)
            null
        }
    }

    private fun defaultAlarmUri(): Uri? =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

    private fun stopLocked() {
        currentJob?.cancel()
        currentJob = null
        currentPlayer?.let { releaseQuietly(it) }
        currentPlayer = null
        vibrator?.cancel()
        restoreAlarmVolume()
        abandonFocus()
        if (_isPlaying.value) notifier.hide()
        _isPlaying.value = false
    }

    // 100% in the app means the loudest the device can play, so the alarm stream is raised while ringing
    private fun raiseAlarmVolume(percent: Int) {
        val manager = audioManager ?: return
        if (percent <= 0 || savedAlarmVolume != null) return
        try {
            val max = manager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val before = manager.getStreamVolume(AudioManager.STREAM_ALARM)
            if (before == max) return
            manager.setStreamVolume(AudioManager.STREAM_ALARM, max, 0)
            savedAlarmVolume = before
            prefs.edit().putInt(KEY_SAVED_VOLUME, before).apply()
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot raise alarm volume: ${e.javaClass.simpleName}")
        }
    }

    private fun restoreAlarmVolume() {
        val manager = audioManager ?: return
        val saved = savedAlarmVolume ?: return
        savedAlarmVolume = null
        prefs.edit().remove(KEY_SAVED_VOLUME).apply()
        restoreIfUntouched(manager, saved)
    }

    // covers the process being killed while an alarm was ringing
    private fun restoreVolumeLeftRaised() {
        val manager = audioManager ?: return
        val saved = prefs.getInt(KEY_SAVED_VOLUME, -1)
        if (saved < 0) return
        prefs.edit().remove(KEY_SAVED_VOLUME).apply()
        restoreIfUntouched(manager, saved)
    }

    private fun restoreIfUntouched(manager: AudioManager, saved: Int) {
        try {
            val max = manager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            // if the user changed the volume while the alarm rang, their choice stays
            if (manager.getStreamVolume(AudioManager.STREAM_ALARM) == max) {
                manager.setStreamVolume(AudioManager.STREAM_ALARM, saved, 0)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot restore alarm volume: ${e.javaClass.simpleName}")
        }
    }

    private fun releaseQuietly(player: MediaPlayer) {
        try {
            player.release()
        } catch (e: Exception) {
            Log.w(TAG, "release failed: ${e.javaClass.simpleName}")
        }
    }

    private fun requestFocus() {
        val manager = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(focusListener)
                .build()
            focusRequest = request
            manager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            manager.requestAudioFocus(focusListener, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }

    private fun abandonFocus() {
        val manager = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { manager.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            manager.abandonAudioFocus(focusListener)
        }
    }

    private fun startVibration() {
        val device = vibrator ?: return
        if (!device.hasVibrator()) return
        val pattern = longArrayOf(0, 700, 500)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> device.vibrate(
                VibrationEffect.createWaveform(pattern, 0),
                VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM),
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                @Suppress("DEPRECATION")
                device.vibrate(VibrationEffect.createWaveform(pattern, 0), audioAttributes)
            }
            else -> {
                @Suppress("DEPRECATION")
                device.vibrate(pattern, 0, audioAttributes)
            }
        }
    }

    private companion object {
        const val TAG = "AlarmPlayback"
        const val PREFS = "alarm_playback"
        const val KEY_SAVED_VOLUME = "restore_alarm_volume"
    }
}
