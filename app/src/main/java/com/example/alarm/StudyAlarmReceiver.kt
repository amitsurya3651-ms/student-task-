package com.example.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class StudyAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(StudyAlarmManager.EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(StudyAlarmManager.EXTRA_TASK_TITLE) ?: "Study Session"
        val summary = intent.getStringExtra(StudyAlarmManager.EXTRA_TASK_SUMMARY) ?: "Time to focus on your studies!"
        val subject = intent.getStringExtra(StudyAlarmManager.EXTRA_TASK_SUBJECT) ?: "General"

        // Ensure channel exists
        StudyAlarmManager.createNotificationChannel(context)

        // Haptic buzz
        triggerVibration(context)

        // Build Intent to open App
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(StudyAlarmManager.EXTRA_TASK_ID, taskId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, StudyAlarmManager.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Study Time: $title")
            .setContentText(summary)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("Study Time: $title ($subject)")
                    .bigText("Summary / Study Notes:\n$summary")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(taskId.toInt(), notification)
    }

    private fun triggerVibration(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(500)
                }
            }
        } catch (_: Exception) {
            // Best effort
        }
    }
}
