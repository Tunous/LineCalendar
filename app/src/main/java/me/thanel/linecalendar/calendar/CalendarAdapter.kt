package me.thanel.linecalendar.calendar

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.item_calendar.view.*
import me.thanel.linecalendar.R
import me.thanel.linecalendar.util.tintDrawable

class CalendarAdapter :
    ListAdapter<CalendarData, CalendarAdapter.ViewHolder>(CalendarData.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val from = LayoutInflater.from(parent.context)
        val view = from.inflate(R.layout.item_calendar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val calendarData = getItem(position)
        holder.calendarIcon.tintDrawable(calendarData.color)
        holder.calendarData = calendarData
        holder.calendarNameView.text = calendarData.name
        holder.calendarCheckBox.isChecked = calendarData.isChecked
    }

    fun getSelectedCalendars(): Set<Long> = (0 until itemCount)
        .map { getItem(it) }
        .filter { it.isChecked }
        .mapTo(mutableSetOf()) { it.id }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val calendarIcon: ImageView = itemView.calendarIcon
        val calendarNameView: TextView = itemView.calendarNameView
        val calendarCheckBox: CheckBox = itemView.calendarCheckBox

        var calendarData: CalendarData? = null

        init {
            itemView.setOnClickListener {
                calendarCheckBox.toggle()
            }
            calendarCheckBox.setOnCheckedChangeListener { _, isChecked ->
                calendarData?.isChecked = isChecked
            }
        }
    }
}
