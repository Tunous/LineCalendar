package me.thanel.linecalendar.widgetlist

import android.support.v7.util.DiffUtil

data class WidgetInfo(val id: Int, val width: Int, val height: Int) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WidgetInfo>() {
            override fun areItemsTheSame(oldItem: WidgetInfo, newItem: WidgetInfo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: WidgetInfo, newItem: WidgetInfo): Boolean {
                return oldItem == newItem
            }
        }
    }
}
