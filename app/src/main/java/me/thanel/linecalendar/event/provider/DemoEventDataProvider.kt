package me.thanel.linecalendar.event.provider

import android.content.Context
import android.graphics.Color
import me.thanel.linecalendar.R
import me.thanel.linecalendar.event.EventData
import java.util.concurrent.TimeUnit

class DemoEventDataProvider(context: Context) : EventDataProvider {
    private val events: List<EventData>

    override val count: Int
        get() = events.size

    init {
        val titles = context.resources.getStringArray(R.array.demo_event_titles)
        var titleIndex = 0
        fun getTitle() = titles.getOrElse(titleIndex++) { "Unnamed" }

        val now = System.currentTimeMillis()
        events = listOf(
            EventData(0, getTitle(), Color.YELLOW, now - TimeUnit.DAYS.toMillis(1), false),
            EventData(0, getTitle(), Color.WHITE, now - TimeUnit.MINUTES.toMillis(10), false),
            EventData(0, getTitle(), Color.BLUE, now + TimeUnit.MINUTES.toMillis(37), false),
            EventData(0, getTitle(), Color.RED, now + TimeUnit.HOURS.toMillis(8), false),
            EventData(0, getTitle(), Color.GREEN, now + TimeUnit.DAYS.toMillis(5), true)
        )
    }

    override fun onDataSetChanged() = Unit

    override fun getEvent(position: Int) = events[position]

    override fun onDestroy() = Unit
}
