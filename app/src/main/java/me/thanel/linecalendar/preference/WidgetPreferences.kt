package me.thanel.linecalendar.preference

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import android.support.annotation.IdRes
import me.thanel.linecalendar.R

class WidgetPreferences(context: Context, appWidgetId: Int) {
    private val keyPrefix = "appWidget${appWidgetId}_"
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getSelectedCalendars(): Set<Long> =
        preferences.getStringSet(getWidgetKey(KEY_SELECTED_CALENDARS), emptySet())
            .mapTo(mutableSetOf()) { it.toLong() }

    fun saveSelectedCalendars(selectedCalendars: Set<Long>): Boolean {
        return preferences.edit()
            .putStringSet(
                getWidgetKey(KEY_SELECTED_CALENDARS),
                selectedCalendars.mapTo(mutableSetOf()) { it.toString() }
            )
            .commit()
    }

    fun getName(numWidgets: Int): String =
        preferences.getString(getWidgetKey(KEY_NAME), "Widget $numWidgets")

    fun saveName(numWidgets: Int): Boolean {
        return if (!preferences.contains(getWidgetKey(KEY_NAME))) {
            preferences.edit()
                .putString(getWidgetKey(KEY_NAME), "Widget $numWidgets")
                .commit()
        } else {
            true
        }
    }

    var isHeaderEnabled: Boolean
        get() = preferences.getBoolean(getWidgetKey(KEY_HEADER_ENABLED), true)
        @SuppressLint("ApplySharedPref")
        set(value) {
            preferences.edit()
                .putBoolean(getWidgetKey(KEY_HEADER_ENABLED), value)
                .commit()
        }

    var indicatorStyle: IndicatorStyle
        get() = IndicatorStyle.valueOf(
            preferences.getString(
                getWidgetKey(KEY_INDICATOR_STYLE),
                IndicatorStyle.Circle.name
            )
        )
        @SuppressLint("ApplySharedPref")
        set(value) {
            preferences.edit()
                .putString(getWidgetKey(KEY_INDICATOR_STYLE), value.name)
                .commit()
        }

    fun clear(): Boolean {
        val editor = preferences.edit()
        for ((key, _) in preferences.all) {
            if (key.startsWith(keyPrefix)) {
                editor.remove(key)
            }
        }
        return editor.commit()
    }

    private fun getWidgetKey(key: String) = "$keyPrefix$key"

    enum class IndicatorStyle(@IdRes val id: Int) {
        None(R.id.indicator_style_none),
        Circle(R.id.indicator_style_circle),
        RoundedRectangle(R.id.indicator_style_rounded_rect);

        companion object {
            fun fromId(@IdRes id: Int): IndicatorStyle? = values().find { it.id == id }
        }
    }

    companion object {
        private const val KEY_SELECTED_CALENDARS = "selectedCalendars"
        private const val KEY_NAME = "name"
        private const val KEY_HEADER_ENABLED = "headerEnabled"
        private const val KEY_INDICATOR_STYLE = "indicatorStyle"
    }
}
