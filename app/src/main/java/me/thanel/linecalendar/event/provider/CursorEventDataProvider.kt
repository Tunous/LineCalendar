package me.thanel.linecalendar.event.provider

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import me.thanel.linecalendar.event.EventData
import me.thanel.linecalendar.event.EventLoader
import me.thanel.linecalendar.preference.WidgetPreferences
import me.thanel.linecalendar.util.hasGrantedCalendarPermission

class CursorEventDataProvider(
    private val context: Context,
    private val preferences: WidgetPreferences
) : EventDataProvider {
    private var cursor: Cursor? = null

    override val count: Int
        get() = cursor?.count ?: 0

    override fun onDataSetChanged() {
        onDestroy()

        if (!context.hasGrantedCalendarPermission()) {
            return
        }

        val selectedCalendars = preferences.selectedCalendarIds
        cursor = context.contentResolver.query(
            EventLoader.getUri(),
            EventLoader.PROJECTION,
            EventLoader.getSelection(selectedCalendars),
            EventLoader.getSelectionArgs(selectedCalendars),
            EventLoader.getSortOrder()
        )
    }

    override fun getEvent(position: Int): EventData {
        val localCursor = cursor
        if (localCursor?.moveToPosition(position) != true) {
            // TODO: Error
            return EventData(0, "Error", Color.RED, System.currentTimeMillis(), false)
        }

        return EventData(
            localCursor.getLong(EventLoader.PROJECTION_EVENT_ID_INDEX),
            localCursor.getString(EventLoader.PROJECTION_TITLE_INDEX) ?: "",
            localCursor.getInt(EventLoader.PROJECTION_DISPLAY_COLOR_INDEX),
            localCursor.getLong(EventLoader.PROJECTION_START_TIME_INDEX),
            localCursor.getInt(EventLoader.PROJECTION_ALL_DAY_INDEX) != 0
        )
    }

    override fun onDestroy() {
        cursor?.close()
        cursor = null
    }
}
