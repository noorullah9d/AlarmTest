package com.example.testapp.receiver

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.testapp.MainActivity
import com.example.testapp.R
import java.util.Date

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "alarm_channel"
        const val ALARM_ACTION = "com.example.testapp.ALARM_ACTION"
        const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("alarm_id", -1)
        val time = intent.getLongExtra("trigger_time", 0L)

        Log.d(TAG, "Alarm triggered. ID: $id, Time: ${Date(time)}")

        Log.d(TAG, "onReceive called with action: ${intent.action}")
        if (intent.action == ALARM_ACTION) {
            Log.d(TAG, "Alarm action received, showing notification")
            showNotification(context, id)
            playAlarmSound(context)
        }
    }

    private fun showNotification(context: Context, alarmId: Int) {
        Log.d(TAG, "showNotification started")
        createNotificationChannel(context)

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alarmId, // Unique again
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
            .setContentTitle("Timer $alarmId Expired")
            .setContentText("Alarm #$alarmId has finished.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(alarmId, builder.build())
            Log.d(TAG, "Notification sent")
        }
    }

    private fun createNotificationChannel(context: Context) {
        Log.d(TAG, "createNotificationChannel started")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Channel"
            val descriptionText = "Channel for alarm notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun playAlarmSound(context: Context) {
        Log.d(TAG, "playAlarmSound started")

        val mediaPlayer = MediaPlayer.create(context, R.raw.azan)
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        mediaPlayer.setOnCompletionListener {
            it.release()
            Log.d(TAG, "Azan playback finished and MediaPlayer released")
        }

        mediaPlayer.start()
        Log.d(TAG, "Azan playback started")
    }
}