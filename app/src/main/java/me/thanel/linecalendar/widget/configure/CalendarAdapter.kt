package me.thanel.linecalendar.widget.configure

import android.content.res.ColorStateList
import android.support.v4.widget.CompoundButtonCompat
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import kotlinx.android.synthetic.main.item_calendar.view.*
import me.thanel.linecalendar.R
import me.thanel.linecalendar.calendar.CalendarData
import me.thanel.linecalendar.util.ColorMapper

class CalendarAdapter(private val onCheckedChangeListener: () -> Unit) :
    ListAdapter<CalendarData, CalendarAdapter.ViewHolder>(CalendarData.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val from = LayoutInflater.from(parent.context)
        val view = from.inflate(R.layout.item_calendar, parent, false)
        return ViewHolder(view, onCheckedChangeListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val calendarData = getItem(position)
        holder.calendarData = calendarData
        holder.calendarCheckBox.text = calendarData.name
        holder.calendarCheckBox.isChecked = calendarData.isChecked
        CompoundButtonCompat.setButtonTintList(
            holder.calendarCheckBox,
            ColorStateList.valueOf(ColorMapper.getDisplayColor(calendarData.color))
        )
    }

    fun getSelectedCalendars(): Set<Long> = (0 until itemCount)
        .map { getItem(it) }
        .filter { it.isChecked }
        .mapTo(mutableSetOf()) { it.id }

    fun setSelectedCalendars(selectedIds: Set<Long>) {
        for (i in 0 until itemCount) {
            val calendarData = getItem(i)
            calendarData.isChecked = calendarData.id in selectedIds
        }
    }

    class ViewHolder(
        itemView: View,
        private val onCheckedChangeListener: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        val calendarCheckBox: CheckBox = itemView.calendarCheckBox

        var calendarData: CalendarData? = null

        init {
            calendarCheckBox.setOnCheckedChangeListener { _, isChecked ->
                calendarData?.isChecked = isChecked
                onCheckedChangeListener.invoke()
            }
        }
    }
}
