package me.thanel.linecalendar.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViewsService
import me.thanel.linecalendar.data.event.provider.CursorEventDataProvider
import me.thanel.linecalendar.preference.WidgetPreferences

class CalendarWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val preferences = WidgetPreferences(applicationContext, appWidgetId)
        val dataProvider = CursorEventDataProvider(applicationContext, preferences)
        return CalendarRemoteViewsFactory(applicationContext, preferences, dataProvider)
    }
}
