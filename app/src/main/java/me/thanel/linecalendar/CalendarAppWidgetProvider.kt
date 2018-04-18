package me.thanel.linecalendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import me.thanel.linecalendar.preference.WidgetPreferences

class CalendarAppWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val prefs = WidgetPreferences(context, appWidgetId)
            prefs.clear()
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_calendar)
            views.setEmptyView(R.id.events_list_view, R.id.empty_view)

            val intent = Intent(context, CalendarWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.events_list_view, intent)

            val itemIntent = Intent(Intent.ACTION_VIEW)
            val pendingIntent = PendingIntent.getActivity(context, 0, itemIntent, 0)
            views.setPendingIntentTemplate(R.id.events_list_view, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
