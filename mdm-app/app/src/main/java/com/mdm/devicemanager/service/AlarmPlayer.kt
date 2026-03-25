package com.mdm.devicemanager.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class AlarmPlayer private constructor(private val context: Context) {

    companion object {
        private const val TAG = "AlarmPlayer"

        @Volatile
        private var instance: AlarmPlayer? = null

        fun getInstance(context: Context): AlarmPlayer {
            return instance ?: synchronized(this) {
                instance ?: AlarmPlayer(context.applicationContext).also { instance = it }
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    fun startAlarm() {
        if (isPlaying) {
            Log.w(TAG, "Alarm already playing")
            return
        }

        try {
            // Set volume to max
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            // Play alarm ringtone
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, alarmUri)
                isLooping = true
                prepare()
                start()
            }

            // Start vibration
            startVibration()

            isPlaying = true
            Log.i(TAG, "Alarm started at max volume")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start alarm", e)
            stopAlarm()
        }
    }

    fun stopAlarm() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null

            stopVibration()

            isPlaying = false
            Log.i(TAG, "Alarm stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping alarm", e)
            mediaPlayer = null
            isPlaying = false
        }
    }

    private fun startVibration() {
        val vibrator = getVibrator()
        val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, 0)
        }
    }

    private fun stopVibration() {
        getVibrator().cancel()
    }

    private fun getVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}
