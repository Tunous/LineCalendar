package me.thanel.linecalendar.preference

import android.content.Context

class WidgetPreferences(
    context: Context,
    appWidgetId: Int
) : PreferenceContainer<WidgetPreferences>(context, appWidgetId) {
    override val identifier: String = "appWidget"

    private var selectedCalendars: Set<String> by bindPreference("selectedCalendars", emptySet())

    var selectedCalendarIds: Set<Long>
        get() = selectedCalendars.map { it.toLong() }.toSet()
        set(value) {
            selectedCalendars = value.map { it.toString() }.toSet()
        }

    var name: String by bindPreference("name", "")

    var isHeaderEnabled: Boolean by bindPreference("isHeaderEnabled", true)

    var indicatorStyle: IndicatorStyle by bindPreference("indicatorStyle", IndicatorStyle.Circle)

    var showAddEventHeaderButton: Boolean by bindPreference("showAddEventHeaderButton", true)

    var showRefreshHeaderButton: Boolean by bindPreference("showRefreshHeaderButton", true)

    var showSettingsHeaderButton: Boolean by bindPreference("showSettingsHeaderButton", true)

}
