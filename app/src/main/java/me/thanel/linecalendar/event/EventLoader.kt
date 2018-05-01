package me.thanel.linecalendar.event

import android.content.ContentUris
import android.net.Uri
import android.provider.CalendarContract
import java.util.*

object EventLoader {
    val PROJECTION = arrayOf(
        CalendarContract.Instances._ID,
        CalendarContract.Instances.EVENT_ID,
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.DISPLAY_COLOR,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.ALL_DAY
    )

    const val PROJECTION_ID_INDEX = 0
    const val PROJECTION_EVENT_ID_INDEX = 1
    const val PROJECTION_TITLE_INDEX = 2
    const val PROJECTION_DISPLAY_COLOR_INDEX = 3
    const val PROJECTION_START_TIME_INDEX = 4
    const val PROJECTION_ALL_DAY_INDEX = 5

    fun getUri(): Uri {
        val startTime = Calendar.getInstance()
        val startMillis = startTime.timeInMillis
        val endTime = Calendar.getInstance().apply {
            add(Calendar.DATE, 60)
        }
        val endMillis = endTime.timeInMillis

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)
        return builder.build()
    }

    fun getSelection(selectedCalendars: Set<Long>): String {
        return selectedCalendars.joinToString(
            separator = ", ",
            prefix = "${CalendarContract.Events.CALENDAR_ID} IN (",
            postfix = ")"
        ) { "?" }
    }

    fun getSelectionArgs(selectedCalendars: Set<Long>) = selectedCalendars
        .map { it.toString() }
        .toTypedArray()

    fun getSortOrder() = "${CalendarContract.Instances.BEGIN} ASC"
}
