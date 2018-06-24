package me.thanel.linecalendar.data.calendar

import android.net.Uri
import android.provider.CalendarContract

object CalendarLoader {
    val PROJECTION = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.CALENDAR_COLOR,
        CalendarContract.Calendars.ACCOUNT_NAME
    )

    const val PROJECTION_ID_INDEX = 0
    const val PROJECTION_CALENDAR_DISPLAY_NAME_INDEX = 1
    const val PROJECTION_CALENDAR_COLOR_INDEX = 2
    const val PROJECTION_ACCOUNT_NAME_INDEX = 3

    fun getUri(): Uri = CalendarContract.Calendars.CONTENT_URI

    fun getSortOrder(): String = "${CalendarContract.Calendars.ACCOUNT_NAME} COLLATE NOCASE ASC, " +
            "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} COLLATE NOCASE ASC"
}
