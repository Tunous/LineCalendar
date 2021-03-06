package me.thanel.linecalendar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.RemoteViews
import me.thanel.linecalendar.R
import me.thanel.linecalendar.preference.WidgetPreferences
import me.thanel.linecalendar.receiver.EnvironmentChangedReceiver
import me.thanel.linecalendar.util.hasGrantedCalendarPermission
import me.thanel.linecalendar.util.setViewAsVisible
import me.thanel.linecalendar.widget.configure.ConfigureWidgetActivity
import me.thanel.linecalendar.widget.configure.EventAdapter
import me.thanel.linecalendar.widgetlist.WidgetListActivity
import java.text.SimpleDateFormat
import java.util.*

class CalendarAppWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = createViews(context, appWidgetId)
            setupEventsList(context, appWidgetId, views)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.eventsListView)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val prefs = WidgetPreferences(context, appWidgetId)
            prefs.clear()
        }
    }

    companion object {
        fun getWidgetIds(context: Context): IntArray {
            return AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, CalendarAppWidgetProvider::class.java))
        }

        fun updateEventList(context: Context, vararg appWidgetIds: Int = getWidgetIds(context)) {
            AppWidgetManager.getInstance(context)
                .notifyAppWidgetViewDataChanged(appWidgetIds, R.id.eventsListView)
        }

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, CalendarAppWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getWidgetIds(context))
            }
            context.sendBroadcast(intent)
        }

        fun inflateViews(container: ViewGroup, appWidgetId: Int, eventAdapter: EventAdapter) {
            // Remove all views in case if the widget was previously created
            container.removeAllViews()

            // Create widget views with new settings
            val context = container.context
            val views = createViews(context, appWidgetId)
            val widgetView = views.apply(context.applicationContext, container)
            container.addView(widgetView)

            // Initialize list
            widgetView.findViewById<ListView>(R.id.eventsListView).apply {
                adapter = eventAdapter
                emptyView = widgetView.findViewById(R.id.eventsEmptyView)
            }
        }

        private fun createViews(context: Context, appWidgetId: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_calendar)
            val prefs = WidgetPreferences(context, appWidgetId)

            setupHeader(context, appWidgetId, views, prefs)
            setupEmptyView(context, appWidgetId, views)

            return views
        }

        private fun setupHeader(
            context: Context,
            appWidgetId: Int,
            views: RemoteViews,
            prefs: WidgetPreferences
        ) {
            if (!prefs.isHeaderEnabled) {
                views.setViewVisibility(R.id.eventsHeader, View.GONE)
                return
            }
            views.setViewVisibility(R.id.eventsHeader, View.VISIBLE)

            val today = SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(Date())
            views.setTextViewText(R.id.headerTitleView, today)

            val builder = CalendarContract.CONTENT_URI.buildUpon()
                .appendPath("time")
            ContentUris.appendId(builder, System.currentTimeMillis())
            val calendarIntent = Intent(Intent.ACTION_VIEW)
                .setData(builder.build())
            val calendarPendingIntent =
                PendingIntent.getActivity(context, appWidgetId, calendarIntent, 0)
            views.setOnClickPendingIntent(R.id.headerTitleView, calendarPendingIntent)
            views.setInt(
                R.id.headerTitleViewContainer,
                "setGravity",
                prefs.headerTextAlignment.gravity
            )

            val addIntent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
            val addPendingIntent = PendingIntent.getActivity(context, appWidgetId, addIntent, 0)
            views.setOnClickPendingIntent(R.id.add_event_header_button, addPendingIntent)
            views.setImageViewResource(R.id.add_event_header_button, R.drawable.ic_add)
            views.setViewAsVisible(R.id.add_event_header_button, prefs.showAddEventHeaderButton)

            val refreshIntent = Intent(EnvironmentChangedReceiver.ACTION_REFRESH)
            val refreshPendingIntent =
                PendingIntent.getBroadcast(context, appWidgetId, refreshIntent, 0)
            views.setOnClickPendingIntent(R.id.refresh_header_button, refreshPendingIntent)
            views.setImageViewResource(R.id.refresh_header_button, R.drawable.ic_refresh)
            views.setViewAsVisible(R.id.refresh_header_button, prefs.showRefreshHeaderButton)

            val settingsIntent = ConfigureWidgetActivity.getIntent(context, appWidgetId)
            val settingsPendingIntent =
                PendingIntent.getActivity(context, appWidgetId, settingsIntent, 0)
            views.setOnClickPendingIntent(R.id.settings_header_button, settingsPendingIntent)
            views.setImageViewResource(R.id.settings_header_button, R.drawable.ic_settings)
            views.setViewAsVisible(R.id.settings_header_button, prefs.showSettingsHeaderButton)
        }

        private fun setupEventsList(context: Context, appWidgetId: Int, views: RemoteViews) {
            val intent = Intent(context, CalendarWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.eventsListView, intent)

            val itemIntent = Intent(Intent.ACTION_VIEW)
            val pendingIntent = PendingIntent.getActivity(context, appWidgetId, itemIntent, 0)
            views.setPendingIntentTemplate(R.id.eventsListView, pendingIntent)
        }

        private fun setupEmptyView(context: Context, appWidgetId: Int, views: RemoteViews) {
            views.setEmptyView(R.id.eventsListView, R.id.eventsEmptyView)

            val permissionGranted = context.hasGrantedCalendarPermission()
            val emptyText = if (permissionGranted) R.string.no_events else R.string.grant_permission
            views.setTextViewText(R.id.eventsEmptyView, context.getString(emptyText))

            if (!permissionGranted) {
                val permissionIntent = WidgetListActivity.getIntent(context, true)
                val pendingIntent =
                    PendingIntent.getActivity(context, appWidgetId, permissionIntent, 0)
                views.setOnClickPendingIntent(R.id.eventsEmptyView, pendingIntent)
            }
        }
    }
}
