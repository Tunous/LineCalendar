package me.thanel.linecalendar.data.calendar

import android.support.v7.util.DiffUtil

sealed class CalendarListItem {
    data class HeaderItem(val name: String) : CalendarListItem()

    data class CalendarItem(
        val id: Long,
        val name: String,
        val color: Int,
        val accountName: String,
        var isChecked: Boolean
    ) : CalendarListItem()

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CalendarListItem>() {
            override fun areItemsTheSame(
                oldItem: CalendarListItem,
                newItem: CalendarListItem
            ): Boolean =
                (oldItem is HeaderItem && newItem is HeaderItem && oldItem.name == newItem.name) ||
                        (oldItem is CalendarItem && newItem is CalendarItem && oldItem.id == newItem.id)

            override fun areContentsTheSame(
                oldItem: CalendarListItem,
                newItem: CalendarListItem
            ): Boolean =
                oldItem == newItem
        }
    }
}
