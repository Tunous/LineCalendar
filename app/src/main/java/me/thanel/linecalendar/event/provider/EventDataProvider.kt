package me.thanel.linecalendar.event.provider

import me.thanel.linecalendar.event.EventData

interface EventDataProvider {
    val count: Int
    fun onDataSetChanged()
    fun onDestroy()
    fun getEvent(position: Int): EventData
}
