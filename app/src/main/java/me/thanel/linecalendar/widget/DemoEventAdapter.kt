package me.thanel.linecalendar.widget

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.item_event.view.*
import me.thanel.linecalendar.R
import me.thanel.linecalendar.preference.WidgetPreferences
import me.thanel.linecalendar.util.formatEventTimeText
import me.thanel.linecalendar.util.getTintedBitmap
import java.util.concurrent.TimeUnit

class DemoEventAdapter(
    context: Context,
    private val preferences: WidgetPreferences
) : ArrayAdapter<DemoEventAdapter.DemoEvent>(context, 0, createDemoEvents(context)) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false).apply {
                tag = ViewHolder(this)
            }
        onBindViewHolder(view.tag as ViewHolder, position)
        return view
    }

    private fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = getItem(position)
        val resId = when (preferences.indicatorStyle) {
            WidgetPreferences.IndicatorStyle.Circle -> R.drawable.shape_circle_small
            WidgetPreferences.IndicatorStyle.RoundedRectangle -> R.drawable.shape_rounded_rect_small
        }
        with(holder) {
            val indicator = getTintedBitmap(eventColorIcon.context, resId, event.color)
            eventColorIcon.setImageBitmap(indicator)
            eventTitleView.text = event.title
            eventTimeView.text =
                    formatEventTimeText(eventTimeView.context, event.startTime, event.allDay)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventColorIcon: ImageView = itemView.eventColorIcon
        val eventTitleView: TextView = itemView.eventTitleView
        val eventTimeView: TextView = itemView.eventTimeView
    }

    data class DemoEvent(
        val title: String,
        val color: Int,
        val startTime: Long,
        val allDay: Boolean
    )

    companion object {
        private fun createDemoEvents(context: Context): List<DemoEvent> {
            val titles = context.resources.getStringArray(R.array.demo_event_titles)
            var titleIndex = 0
            fun getTitle() = titles.getOrElse(titleIndex++) { "Unnamed" }

            val now = System.currentTimeMillis()
            return listOf(
                DemoEvent(getTitle(), Color.YELLOW, now - TimeUnit.DAYS.toMillis(1), false),
                DemoEvent(getTitle(), Color.WHITE, now - TimeUnit.MINUTES.toMillis(10), false),
                DemoEvent(getTitle(), Color.BLUE, now + TimeUnit.MINUTES.toMillis(37), false),
                DemoEvent(getTitle(), Color.RED, now + TimeUnit.HOURS.toMillis(8), false),
                DemoEvent(getTitle(), Color.GREEN, now + TimeUnit.DAYS.toMillis(5), true)
            )
        }
    }
}
