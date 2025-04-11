package com.example.testapp

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.testapp.receiver.AlarmReceiver
import com.example.testapp.ui.theme.TestAppTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val state = rememberTimePickerState()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimePicker(state = state)

        Button(onClick = {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, state.hour)
                set(Calendar.MINUTE, state.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DATE, 1)
            }
            setAlarm(context, calendar, 1001)
        }, modifier = Modifier.padding(16.dp)) {
            Text("Set Timer 1")
        }

        Button(onClick = {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, state.hour)
                set(Calendar.MINUTE, state.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DATE, 1)
            }
            setAlarm(context, calendar, 1002)
        }, modifier = Modifier.padding(16.dp)) {
            Text("Set Timer 2")
        }
    }
}

private fun setAlarm(context: Context, calendar: Calendar, id: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        requestNotificationPermission(context as Activity)
    }

    if (context.canScheduleExactAlarms()) {
        scheduleAlarm(context, calendar.timeInMillis, id)
    } else {
        Toast.makeText(context, "Permission needed to schedule exact alarms", Toast.LENGTH_LONG).show()
        // Optional: redirect user to settings to enable permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
        }
    }
}

/*private fun scheduleAlarm(context: Context, seconds: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        action = AlarmReceiver.ALARM_ACTION
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE  or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val calendar: Calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        add(Calendar.SECOND, seconds.toInt())
    }

    alarmManager.set(
        AlarmManager.RTC_WAKEUP,
//        calendar.timeInMillis,
        seconds,
        pendingIntent
    )

    Toast.makeText(context, "Timer set for $seconds seconds", Toast.LENGTH_SHORT).show()
}*/
private fun scheduleAlarm(context: Context, triggerAtMillis: Long, requestCode: Int) {
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        action = AlarmReceiver.ALARM_ACTION
        putExtra("alarm_id", requestCode)
        putExtra("trigger_time", triggerAtMillis)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode, // 👈 Important: Unique request code for each alarm
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerAtMillis,
        pendingIntent
    )

    Toast.makeText(context, "Alarm set for request code: $requestCode", Toast.LENGTH_SHORT).show()
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun requestNotificationPermission(activity: Activity) {
    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
    }
}

@Preview(showBackground = true)
@Composable
fun TimerScreenPreview() {
    TestAppTheme {
        TimerScreen()
    }
}