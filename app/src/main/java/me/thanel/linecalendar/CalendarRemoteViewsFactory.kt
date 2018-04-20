package me.thanel.linecalendar

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.CalendarContract
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.format.DateUtils
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import me.thanel.linecalendar.preference.WidgetPreferences
import me.thanel.linecalendar.util.hasGrantedCalendarPermission
import java.text.SimpleDateFormat
import java.util.*

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
            cursor!!.getLong(PROJECTION_EVENT_ID_INDEX)
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

        val startTime = Calendar.getInstance()
        val startMillis = startTime.timeInMillis
        val endTime = Calendar.getInstance().apply {
            add(Calendar.DATE, 60)
        }
        val endMillis = endTime.timeInMillis

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        val selectedCalendars = preferences.getSelectedCalendars()
        val calendarIds = selectedCalendars.map { it.toString() }.toTypedArray()
        val selection = selectedCalendars.joinToString(
            separator = ", ",
            prefix = "${CalendarContract.Events.CALENDAR_ID} IN (",
            postfix = ")"
        ) { "?" }

        cursor = context.contentResolver.query(
            builder.build(),
            EVENT_PROJECTION,
            selection,
            calendarIds,
            "${CalendarContract.Instances.BEGIN} ASC"
        )
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getViewAt(position: Int): RemoteViews? {
        var eventId = 0L
        var title = ""
        var color = 0xFFF
        var startTime = 0L
        var allDay = false
        val localCursor = cursor
        if (localCursor?.moveToPosition(position) == true) {
            eventId = localCursor.getLong(PROJECTION_EVENT_ID_INDEX)
            title = localCursor.getString(PROJECTION_TITLE_INDEX) ?: ""
            color = localCursor.getInt(PROJECTION_DISPLAY_COLOR_INDEX)
            startTime = localCursor.getLong(PROJECTION_START_TIME_INDEX)
            allDay = localCursor.getInt(PROJECTION_ALL_DAY_INDEX) != 0
        }

        val timeText = if (allDay) {
            val formatter = SimpleDateFormat("d MMM", Locale.getDefault())
            formatter.format(Date(startTime))
        } else {
            DateUtils.getRelativeDateTimeString(
                context,
                startTime,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_ABBREV_WEEKDAY
            )
        }

        val intent = Intent().apply {
            data = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        }

        return RemoteViews(context.packageName, R.layout.item_event).apply {
            setTextViewText(R.id.event_title_view, title)
            setTextViewText(R.id.event_time_view, timeText)
            setOnClickFillInIntent(R.id.event_view, intent)

            val circle = createColoredCircle(color)
            setImageViewBitmap(R.id.event_color_icon, circle)
        }
    }

    private fun createColoredCircle(@ColorInt color: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, R.drawable.circle_small)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas().apply {
            setBitmap(bitmap)
        }
        val wrappedDrawable = DrawableCompat.wrap(drawable).mutate()
        wrappedDrawable.setBounds(
            0,
            0,
            wrappedDrawable.intrinsicWidth,
            wrappedDrawable.intrinsicHeight
        )
        DrawableCompat.setTint(wrappedDrawable, color)
        wrappedDrawable.draw(canvas)
        return bitmap
    }

    override fun getCount(): Int = cursor?.count ?: 0

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {
        cursor?.close()
        cursor = null
    }


    companion object {
        private val EVENT_PROJECTION = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DISPLAY_COLOR,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.ALL_DAY
        )
        private const val PROJECTION_EVENT_ID_INDEX = 0
        private const val PROJECTION_TITLE_INDEX = 1
        private const val PROJECTION_DISPLAY_COLOR_INDEX = 2
        private const val PROJECTION_START_TIME_INDEX = 3
        private const val PROJECTION_ALL_DAY_INDEX = 4
    }
}
