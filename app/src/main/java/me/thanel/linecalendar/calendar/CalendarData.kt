package me.thanel.linecalendar.calendar

import android.support.v7.util.DiffUtil

data class CalendarData(
    val id: Long,
    val name: String,
    val color: Int,
    var isChecked: Boolean
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CalendarData>() {
            override fun areItemsTheSame(oldItem: CalendarData, newItem: CalendarData): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: CalendarData, newItem: CalendarData): Boolean =
                oldItem == newItem
        }
    }
}
