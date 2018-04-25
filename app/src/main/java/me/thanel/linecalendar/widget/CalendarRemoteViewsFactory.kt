package me.thanel.linecalendar.widget

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.CalendarContract
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import me.thanel.linecalendar.R
import me.thanel.linecalendar.preference.WidgetPreferences
import me.thanel.linecalendar.util.ColorMapper
import me.thanel.linecalendar.util.createColoredCircleBitmap
import me.thanel.linecalendar.util.formatEventTimeText
import me.thanel.linecalendar.util.hasGrantedCalendarPermission

class CalendarRemoteViewsFactory(
    private val context: Context,
    appWidgetId: Int
) : RemoteViewsService.RemoteViewsFactory {
    private val preferences = WidgetPreferences(context, appWidgetId)
    private var cursor: Cursor? = null

    override fun onCreate() {
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return if (cursor?.moveToPosition(position) == true) {
            cursor!!.getLong(EventLoader.PROJECTION_EVENT_ID_INDEX)
        } else {
            position.toLong()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDataSetChanged() {
        cursor?.close()
        cursor = null

        if (!context.hasGrantedCalendarPermission()) {
            return
        }

        val selectedCalendars = preferences.getSelectedCalendars()
        cursor = context.contentResolver.query(
            EventLoader.getUri(),
            EventLoader.PROJECTION,
            EventLoader.getSelection(selectedCalendars),
            EventLoader.getSelectionArgs(selectedCalendars),
            EventLoader.getSortOrder()
        )
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getViewAt(position: Int): RemoteViews? {
        val localCursor = cursor
        if (localCursor?.moveToPosition(position) != true) {
            // TODO: Error
            return null
        }

        val eventId = localCursor.getLong(EventLoader.PROJECTION_EVENT_ID_INDEX)
        val title = localCursor.getString(EventLoader.PROJECTION_TITLE_INDEX) ?: ""
        val color = localCursor.getInt(EventLoader.PROJECTION_DISPLAY_COLOR_INDEX)
        val startTime = localCursor.getLong(EventLoader.PROJECTION_START_TIME_INDEX)
        val allDay = localCursor.getInt(EventLoader.PROJECTION_ALL_DAY_INDEX) != 0

        val intent = Intent().apply {
            data = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        }

        return RemoteViews(context.packageName, R.layout.item_event).apply {
            setTextViewText(R.id.eventTitleView, title)
            val timeText = formatEventTimeText(context, startTime, allDay)
            setTextViewText(R.id.eventTimeView, timeText)
            setOnClickFillInIntent(R.id.eventView, intent)
            val circle = createColoredCircleBitmap(context, ColorMapper.getDisplayColor(color))
            setImageViewBitmap(R.id.eventColorIcon, circle)
        }
    }

    override fun getCount(): Int = cursor?.count ?: 0

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {
        cursor?.close()
        cursor = null
    }
}
