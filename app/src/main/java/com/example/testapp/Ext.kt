package com.example.testapp

import android.app.AlarmManager
import android.content.Context
import android.os.Build

fun Context.canScheduleExactAlarms(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true // No permission needed for older versions
    }
}
