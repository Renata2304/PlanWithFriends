package com.example.planwithfriends.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.planwithfriends.R
import com.example.planwithfriends.PlanWithFriendsApplication
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.core.content.edit

class EventNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val prefs = appContext.getSharedPreferences("notifs_log", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result {
        try {
            val app = applicationContext as PlanWithFriendsApplication
            val eventsRepository = app.container.eventsRepository
            val groupsRepository = app.container.groupsRepository

            val events = eventsRepository.getAllEventsOnce()
            val groups = groupsRepository.getAllGroups().first()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val now = LocalDateTime.now()

            for (event in events) {
                try {
                    val eventDateTimeStr = "${event.date} ${event.time}"
                    val eventTime = LocalDateTime.parse(eventDateTimeStr, formatter)

                    val minutesUntilEvent = ChronoUnit.MINUTES.between(now, eventTime)

                    val keySoon = "notif_soon_${event.id}"
                    val keyTomorrow = "notif_tomorrow_${event.id}"

                    if (minutesUntilEvent in 45..60) {
                        if (!prefs.getBoolean(keySoon, false)) {
                            val title = applicationContext.getString(R.string.notif_title_soon)
                            val message = applicationContext.getString(R.string.notif_msg_soon, event.title)

                            showNotification(
                                notificationId = event.id.hashCode(),
                                title = title,
                                message = message
                            )
                            prefs.edit { putBoolean(keySoon, true) }
                        }
                    } else if (minutesUntilEvent in 1425..1440) {
                        if (!prefs.getBoolean(keyTomorrow, false)) {
                            val title = applicationContext.getString(R.string.notif_title_tomorrow)

                            val message = if (event.groupId != null) {
                                val groupName = groups.find { it.id == event.groupId }?.name ?: "Necunoscut"
                                applicationContext.getString(R.string.notif_msg_tomorrow_group, event.title, groupName)
                            } else {
                                applicationContext.getString(R.string.notif_msg_tomorrow_no_group, event.title)
                            }

                            showNotification(
                                notificationId = event.id.hashCode() + 1,
                                title = title,
                                message = message
                            )
                            prefs.edit { putBoolean(keyTomorrow, true) }
                        }
                    }

                } catch (e: Exception) {
                    Log.e("Worker", "Eroare la parsarea datei pt evenimentul ${event.title}")
                }
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun showNotification(notificationId: Int, title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelName = applicationContext.getString(R.string.notif_channel_name)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "event_channel",
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "event_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}