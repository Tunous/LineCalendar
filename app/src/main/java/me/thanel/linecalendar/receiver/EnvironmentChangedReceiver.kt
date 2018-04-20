package me.thanel.linecalendar.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import me.thanel.linecalendar.CalendarAppWidgetProvider

class EnvironmentChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(
            EnvironmentChangedReceiver::class.java.simpleName,
            "Environment or calendar has been modified, action=${intent.action}"
        )
        when (intent.action) {
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                CalendarAppWidgetProvider.updateAllWidgets(context)
            }
        }
        CalendarAppWidgetProvider.updateEventList(context)
    }
}
