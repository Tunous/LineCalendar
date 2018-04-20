package me.thanel.linecalendar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import me.thanel.linecalendar.R
import me.thanel.linecalendar.preference.WidgetPreferences
import me.thanel.linecalendar.util.hasGrantedCalendarPermission
import me.thanel.linecalendar.widgetlist.WidgetListActivity

class CalendarAppWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_calendar
            )

            val permissionGranted = context.hasGrantedCalendarPermission()
            val emptyText = if (permissionGranted) R.string.no_events else R.string.grant_permission
            views.setEmptyView(
                R.id.events_list_view,
                R.id.empty_view
            )
            views.setTextViewText(R.id.empty_view, context.getString(emptyText))
            if (!permissionGranted) {
                val permissionIntent = WidgetListActivity.getIntent(context, true)
                val pendingIntent =
                    PendingIntent.getActivity(context, appWidgetId, permissionIntent, 0)
                views.setOnClickPendingIntent(R.id.empty_view, pendingIntent)
            }

            val intent = Intent(context, CalendarWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.events_list_view, intent)

            val itemIntent = Intent(Intent.ACTION_VIEW)
            val pendingIntent = PendingIntent.getActivity(context, appWidgetId, itemIntent, 0)
            views.setPendingIntentTemplate(R.id.events_list_view, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val prefs = WidgetPreferences(context, appWidgetId)
            prefs.clear()
        }
    }

    companion object {
        private fun getWidgetIds(context: Context): IntArray {
            return AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, CalendarAppWidgetProvider::class.java))
        }

        fun updateEventList(context: Context, vararg appWidgetIds: Int = getWidgetIds(context)) {
            AppWidgetManager.getInstance(context)
                .notifyAppWidgetViewDataChanged(appWidgetIds, R.id.events_list_view)
        }

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, CalendarAppWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getWidgetIds(context))
            }
            context.sendBroadcast(intent)
        }
    }
}
