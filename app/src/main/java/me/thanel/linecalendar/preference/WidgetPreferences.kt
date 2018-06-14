package me.thanel.linecalendar.preference

import android.content.Context
import android.support.v4.util.ObjectsCompat

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

    fun setFrom(other: WidgetPreferences) {
        selectedCalendars = other.selectedCalendars
        name = other.name
        isHeaderEnabled = other.isHeaderEnabled
        indicatorStyle = other.indicatorStyle
    }

    // TODO: Test
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WidgetPreferences) return false
        return ObjectsCompat.equals(selectedCalendarIds, other.selectedCalendarIds) &&
                ObjectsCompat.equals(name, other.name) &&
                ObjectsCompat.equals(isHeaderEnabled, other.isHeaderEnabled) &&
                ObjectsCompat.equals(indicatorStyle, other.indicatorStyle)
    }

    // TODO: Test
    override fun hashCode(): Int = ObjectsCompat.hash(
        selectedCalendarIds,
        name,
        isHeaderEnabled,
        indicatorStyle
    )

}
