package me.thanel.linecalendar.preference

import android.content.Context

class WidgetPreferences(
    context: Context,
    appWidgetId: Int
) : PreferenceContainer(context, "appWidget${appWidgetId}_") {
    private var selectedCalendars: Set<String> by bindPreference(emptySet())

    var selectedCalendarIds: Set<Long>
        get() = selectedCalendars.map { it.toLong() }.toSet()
        set(value) {
            selectedCalendars = value.map { it.toString() }.toSet()
        }

    var name: String by bindPreference("")

    var isHeaderEnabled: Boolean by bindPreference(true)

    var indicatorStyle: IndicatorStyle by bindPreference(IndicatorStyle.Circle)
}
