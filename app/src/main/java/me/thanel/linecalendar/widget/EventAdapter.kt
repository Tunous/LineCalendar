package me.thanel.linecalendar.widget

import android.content.Context
import android.database.Cursor
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.item_event.view.*
import me.thanel.linecalendar.R
import me.thanel.linecalendar.util.createColoredCircleBitmap
import me.thanel.linecalendar.util.formatEventTimeText

class EventAdapter(context: Context) : CursorAdapter(context, null, 0) {
    override fun newView(context: Context, cursor: Cursor?, parent: ViewGroup): View {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_event, parent, false)
        view.tag = ViewHolder(view)
        return view
    }

    override fun bindView(view: View, context: Context, cursor: Cursor?) {
        var title = ""
        var color = 0xFFF
        var startTime = 0L
        var allDay = false
        if (cursor != null) {
            title = cursor.getString(EventLoader.PROJECTION_TITLE_INDEX) ?: ""
            color = cursor.getInt(EventLoader.PROJECTION_DISPLAY_COLOR_INDEX)
            startTime = cursor.getLong(EventLoader.PROJECTION_START_TIME_INDEX)
            allDay = cursor.getInt(EventLoader.PROJECTION_ALL_DAY_INDEX) != 0
        }

        val holder = view.tag as ViewHolder
        holder.eventColorIcon.setImageBitmap(createColoredCircleBitmap(context, color))
        holder.eventTitleView.text = title
        holder.eventTimeView.text = formatEventTimeText(context, startTime, allDay)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventColorIcon: ImageView = itemView.eventColorIcon
        val eventTitleView: TextView = itemView.eventTitleView
        val eventTimeView: TextView = itemView.eventTimeView
    }
}
