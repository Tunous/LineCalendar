package me.thanel.linecalendar.widget

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import me.thanel.linecalendar.R
import me.thanel.linecalendar.data.event.provider.EventDataProvider
import me.thanel.linecalendar.preference.IndicatorStyle
import me.thanel.linecalendar.preference.WidgetPreferences
import me.thanel.linecalendar.util.ColorMapper
import me.thanel.linecalendar.util.formatEventTimeText
import me.thanel.linecalendar.util.getTintedBitmap

class CalendarRemoteViewsFactory(
    private val context: Context,
    private val preferences: WidgetPreferences,
    private val dataProvider: EventDataProvider
) : RemoteViewsService.RemoteViewsFactory {
    override fun onCreate() = Unit

    override fun getLoadingView(): RemoteViews? = null

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onDataSetChanged() = dataProvider.onDataSetChanged()

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {
        val event = dataProvider.getEvent(position)

        val intent = Intent().apply {
            data = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
        }

        return RemoteViews(context.packageName, R.layout.item_event).apply {
            setTextViewText(R.id.eventTitleView, event.title)
            val timeText = context.formatEventTimeText(event.startTime, event.allDay)
            setTextViewText(R.id.eventTimeView, timeText)
            setOnClickFillInIntent(R.id.eventView, intent)

            val resId = when (preferences.indicatorStyle) {
                IndicatorStyle.None -> null
                IndicatorStyle.Circle -> R.drawable.shape_circle_small
                IndicatorStyle.RoundedRectangle -> R.drawable.shape_rounded_rect_small
            }

            if (resId != null) {
                val circle =
                    context.getTintedBitmap(resId, ColorMapper.getDisplayColor(event.color))
                setImageViewBitmap(R.id.eventColorIcon, circle)
                setViewVisibility(R.id.eventColorIcon, View.VISIBLE)
            } else {
                setViewVisibility(R.id.eventColorIcon, View.GONE)
            }
        }
    }

    override fun getCount(): Int = dataProvider.count

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() = dataProvider.onDestroy()
}
