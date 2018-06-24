package me.thanel.linecalendar.data.event.provider

import me.thanel.linecalendar.data.event.EventData

interface EventDataProvider {
    val count: Int
    fun onDataSetChanged()
    fun onDestroy()
    fun getEvent(position: Int): EventData
}
