package me.thanel.linecalendar.event

data class EventData(
    val id: Long,
    val title: String,
    val color: Int,
    val startTime: Long,
    val allDay: Boolean
)
