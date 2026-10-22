package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class PomodoroNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "study_buddy_pomodoro_channel"
        const val CHANNEL_NAME = "Study Buddy Pomodoro Alerts"
        const val NOTIFICATION_ID_COMPLETED = 2001
        const val NOTIFICATION_ID_PROGRESS = 2002
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when study sessions or breaks finish"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Trigger notification + vibration + audio alert when an interval finishes.
     */
    fun showSessionFinishedNotification(isWorkSession: Boolean, nextMinutes: Int) {
        val title = if (isWorkSession) {
            "🎉 Study Session Complete!"
        } else {
            "🔔 Break Over — Time to Focus!"
        }

        val message = if (isWorkSession) {
            "Great job focusing! Take a $nextMinutes-minute break to rest your eyes, stretch, and hydrate."
        } else {
            "Your break is finished. Tap to jump back into your $nextMinutes-minute study session!"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "POMODORO")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID_COMPLETED, notification)
        } catch (_: Exception) { }

        // Local Vibration Alert
        triggerVibration()

        // Local Pleasant Tone Alert
        triggerTone()
    }

    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 300, 150, 300), -1)
                }
            }
        } catch (_: Exception) { }
    }

    private fun triggerTone() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 500)
        } catch (_: Exception) { }
    }

    fun cancelAll() {
        try {
            notificationManager.cancel(NOTIFICATION_ID_COMPLETED)
        } catch (_: Exception) { }
    }
}
