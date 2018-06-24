package me.thanel.linecalendar.widget.configure

import android.content.res.ColorStateList
import android.support.v4.widget.CompoundButtonCompat
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import kotlinx.android.synthetic.main.item_calendar.view.*
import kotlinx.android.synthetic.main.item_calendar_account_header.view.*
import me.thanel.linecalendar.R
import me.thanel.linecalendar.calendar.CalendarListItem
import me.thanel.linecalendar.util.ColorMapper

class CalendarAdapter(private val onCheckedChangeListener: () -> Unit) :
    ListAdapter<CalendarListItem, CalendarAdapter.ViewHolder>(CalendarListItem.DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is CalendarListItem.CalendarItem -> VIEW_TYPE_ITEM
        is CalendarListItem.HeaderItem -> VIEW_TYPE_HEADER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val from = LayoutInflater.from(parent.context)
        val layoutResId = when (viewType) {
            VIEW_TYPE_ITEM -> R.layout.item_calendar
            VIEW_TYPE_HEADER -> R.layout.item_calendar_account_header
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
        val view = from.inflate(layoutResId, parent, false)
        return when (viewType) {
            VIEW_TYPE_ITEM -> ViewHolder.CalendarItemViewHolder(view, onCheckedChangeListener)
            VIEW_TYPE_HEADER -> ViewHolder.HeaderItemViewHolder(view)
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val calendarData = getItem(position)
        when (holder) {
            is ViewHolder.CalendarItemViewHolder -> {
                val calendarItem = calendarData as CalendarListItem.CalendarItem
                holder.item = calendarItem
                holder.calendarCheckBox.text = calendarItem.name
                holder.calendarCheckBox.isChecked = calendarItem.isChecked
                CompoundButtonCompat.setButtonTintList(
                    holder.calendarCheckBox,
                    ColorStateList.valueOf(ColorMapper.getDisplayColor(calendarItem.color))
                )
            }
            is ViewHolder.HeaderItemViewHolder -> {
                val headerItem = calendarData as CalendarListItem.HeaderItem
                holder.nameView.text = headerItem.name
            }
        }
    }

    fun getSelectedCalendars(): Set<Long> = (0 until itemCount)
        .map { getItem(it) }
        .filterIsInstance<CalendarListItem.CalendarItem>()
        .filter { it.isChecked }
        .mapTo(mutableSetOf()) { it.id }

    fun setSelectedCalendars(selectedIds: Set<Long>) {
        for (i in 0 until itemCount) {
            val calendarData = getItem(i)
            if (calendarData is CalendarListItem.CalendarItem) {
                calendarData.isChecked = calendarData.id in selectedIds
            }
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class HeaderItemViewHolder(itemView: View) : ViewHolder(itemView) {
            val nameView: TextView = itemView.accountNameView
        }

        class CalendarItemViewHolder(
            itemView: View,
            private val onCheckedChangeListener: () -> Unit
        ) : ViewHolder(itemView) {
            val calendarCheckBox: CheckBox = itemView.calendarCheckBox

            var item: CalendarListItem.CalendarItem? = null

            init {
                calendarCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    item?.isChecked = isChecked
                    onCheckedChangeListener.invoke()
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_HEADER = 2
    }
}
