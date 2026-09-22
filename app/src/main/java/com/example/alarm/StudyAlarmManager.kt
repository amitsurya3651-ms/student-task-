package com.example.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.StudentTask

object StudyAlarmManager {
    const val CHANNEL_ID = "student_study_tasks_channel"
    const val CHANNEL_NAME = "Study Tasks & Clock Alerts"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_TASK_SUMMARY = "extra_task_summary"
    const val EXTRA_TASK_SUBJECT = "extra_task_subject"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies students when it is time to study their scheduled task"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun scheduleTaskAlarm(context: Context, task: StudentTask) {
        val now = System.currentTimeMillis()
        if (task.scheduledTimeMillis <= now || task.isCompleted) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
            putExtra(EXTRA_TASK_SUMMARY, task.summary)
            putExtra(EXTRA_TASK_SUBJECT, task.subject)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.scheduledTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    task.scheduledTimeMillis,
                    pendingIntent
                )
            }
            Log.d("StudyAlarmManager", "Scheduled alarm for task ${task.id} at ${task.scheduledTimeMillis}")
        } catch (e: SecurityException) {
            // In Android 12+, exact alarm permission might not be granted by default; fallback to inexact
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.scheduledTimeMillis,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e("StudyAlarmManager", "Failed to schedule alarm", ex)
            }
        } catch (e: Exception) {
            Log.e("StudyAlarmManager", "Error scheduling alarm", e)
        }
    }

    fun cancelTaskAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
