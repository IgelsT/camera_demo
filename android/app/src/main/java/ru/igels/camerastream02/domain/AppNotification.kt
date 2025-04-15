package ru.igels.camerastream02.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import ru.igels.camerastream02.MainApp
import ru.igels.camerastream02.R

class AppNotification(var context: Context) {

    fun makeNotification(): Notification {
        val notifyPendingIntent = PendingIntent.getActivity(
            context, 0, MainApp.getInstance().mainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        var notification = buildForegroundNotification(notifyPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId =
                createNotificationChannel("camStreamService", "Camera streamer")
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
            notification = notificationBuilder.setOngoing(true)
                .setContentIntent(notifyPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        }
        return notification
    }

    private fun buildForegroundNotification(intent: PendingIntent): Notification {
        val b = NotificationCompat.Builder(context);
        b.setOngoing(true)
            .setContentIntent(intent)
            .setContentTitle("Streamer")
            .setSmallIcon(R.mipmap.ic_launcher)
        return (b.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        channelId: String,
        channelName: String
    ): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}