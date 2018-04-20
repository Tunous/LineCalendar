package me.thanel.linecalendar.preference

import android.content.Context
import android.preference.PreferenceManager

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

    companion object {
        private const val KEY_SELECTED_CALENDARS = "selectedCalendars"
        private const val KEY_NAME = "name"
    }
}
