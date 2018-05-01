package me.thanel.linecalendar.widget.configure

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import me.thanel.linecalendar.event.provider.DemoEventDataProvider
import me.thanel.linecalendar.preference.WidgetPreferences
import me.thanel.linecalendar.widget.CalendarRemoteViewsFactory

class EventAdapter(
    context: Context,
    preferences: WidgetPreferences
) : BaseAdapter() {
    private val appContext = context.applicationContext
    private val dataProvider = DemoEventDataProvider(appContext)
    private val factory = CalendarRemoteViewsFactory(appContext, preferences, dataProvider)

    override fun getCount(): Int = dataProvider.count

    override fun getItem(position: Int): Any = dataProvider.getEvent(position)

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        factory.getViewAt(position)
            .apply(appContext, parent)
}
