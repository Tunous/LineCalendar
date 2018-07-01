package me.thanel.linecalendar.preference

enum class DaysToShow(val days: Int) {
    Today(1),
    TodayAndTomorrow(2),
    ThreeDays(3),
    Week(7),
    TwoWeeks(14),
    Month(30),
    TwoMonths(60),
    HalfYear(183),
    Year(356);

    companion object {
        fun fromPosition(position: Int): DaysToShow? = values().find { it.ordinal == position }
    }
}
