package me.thanel.linecalendar.event.provider

import me.thanel.linecalendar.event.EventData

interface EventDataProvider {
    fun onDataSetChanged()
    fun getEvent(position: Int): EventData
}
