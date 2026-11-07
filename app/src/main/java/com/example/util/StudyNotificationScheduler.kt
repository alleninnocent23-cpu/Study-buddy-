package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

/**
 * Handles 2-hour recurring study and exam reminder notifications,
 * and welcome email notification simulation for newly onboarded students.
 */
class StudyNotificationScheduler(private val context: Context) {

    companion object {
        const val STUDY_CHANNEL_ID = "study_buddy_reminder_channel"
        const val STUDY_CHANNEL_NAME = "Study & Exam Prep Reminders"
        const val EMAIL_CHANNEL_ID = "study_buddy_email_channel"
        const val EMAIL_CHANNEL_NAME = "StudyBuddy Welcome & System Notes"

        const val ACTION_STUDY_REMINDER = "com.example.ACTION_STUDY_REMINDER"
        const val NOTIFICATION_ID_WELCOME = 4001
        const val NOTIFICATION_ID_STUDY = 4002

        private val STUDY_MESSAGES = listOf(
            "⏰ Time to Study!" to "Your next study milestone is waiting. Let's solve a quick problem or review your notes!",
            "🎯 WAEC & JAMB Prep" to "Consistency is key to scoring 300+ in JAMB and getting straight A's in WAEC. Tap to practice past questions!",
            "🧠 Keep Your Momentum Going" to "Review key STEM concepts and test yourself with a 5-question speed quiz now.",
            "📚 Ready for Your Next Lesson?" to "StudyBuddy is ready to explain any difficult mathematics, physics, or chemistry concept.",
            "⚡ Exam Readiness Check" to "2 hours have passed. Take 10 minutes to review your formulas and diagrams!"
        )
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val studyChannel = NotificationChannel(
                STUDY_CHANNEL_ID,
                STUDY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Periodic reminders every 2 hours to comeback, learn, and prepare for exams"
                enableVibration(true)
                setShowBadge(true)
            }

            val emailChannel = NotificationChannel(
                EMAIL_CHANNEL_ID,
                EMAIL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Welcome email and student onboarding notifications"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(studyChannel)
            notificationManager.createNotificationChannel(emailChannel)
        }
    }

    /**
     * Schedules periodic study notifications every 2 hours.
     */
    fun scheduleBiHourlyStudyReminders() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = ACTION_STUDY_REMINDER
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getBroadcast(context, 100, intent, flags)

        val twoHoursMillis = 2 * 60 * 60 * 1000L
        val triggerAt = System.currentTimeMillis() + twoHoursMillis

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                twoHoursMillis,
                pendingIntent
            )
            Log.i("StudyNotification", "Scheduled bi-hourly study reminders successfully")
        } catch (e: Exception) {
            Log.e("StudyNotification", "Error scheduling alarm", e)
        }
    }

    /**
     * Sends the official "Welcome to StudyBuddy" email notification with additional starter notes.
     */
    fun sendWelcomeEmailNotification(userName: String, userEmail: String) {
        val title = "📧 Welcome to StudyBuddy, $userName!"
        val emailBody = """
            Welcome to StudyBuddy! We're excited to partner with you on your academic journey.
            
            Here are your starter notes:
            • AI Tutor: Ask any complex question in Mathematics, Physics, Chemistry, Biology, and more.
            • Tabular Forms & Diagrams: Request ruled tables, scientific illustrations, and step-by-step working.
            • Voice Study: Hold the mic button to record voice questions and receive spoken explanations.
            • WAEC & JAMB: Practice CBT questions with instant grading and explanations.
            
            Sent to: $userEmail
        """.trimIndent()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getActivity(context, 201, intent, flags)

        val largeIcon = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_study_buddy_avatar)
        } catch (_: Exception) {
            null
        }

        val notification = NotificationCompat.Builder(context, EMAIL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bot)
            .setContentTitle(title)
            .setContentText("Your account ($userEmail) is now active. Tap to read your starter study notes.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(emailBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .apply {
                if (largeIcon != null) setLargeIcon(largeIcon)
            }
            .build()

        notificationManager.notify(NOTIFICATION_ID_WELCOME, notification)
    }

    /**
     * Dispatches an individual study reminder notification.
     */
    fun showStudyReminderNotification() {
        val (title, body) = STUDY_MESSAGES.random()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getActivity(context, 202, intent, flags)

        val largeIcon = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_study_buddy_avatar)
        } catch (_: Exception) {
            null
        }

        val notification = NotificationCompat.Builder(context, STUDY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bot)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .apply {
                if (largeIcon != null) setLargeIcon(largeIcon)
            }
            .build()

        notificationManager.notify(NOTIFICATION_ID_STUDY, notification)
    }
}

/**
 * BroadcastReceiver triggered every 2 hours by AlarmManager to notify the student.
 */
class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduler = StudyNotificationScheduler(context)
        scheduler.showStudyReminderNotification()
    }
}
