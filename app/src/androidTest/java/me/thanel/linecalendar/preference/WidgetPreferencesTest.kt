package me.thanel.linecalendar.preference

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class WidgetPreferencesTest {
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setupTests() {
        context = InstrumentationRegistry.getTargetContext()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    @After
    fun clearPreferences() {
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun getSelectedCalendars_shouldReturnCalendarsForWidget() {
        sharedPreferences.edit()
            .putStringSet("appWidget1_selectedCalendars", setOf("5", "8"))
            .putStringSet("appWidget2_selectedCalendars", setOf("4"))
            .commit()
        val prefs = WidgetPreferences(context, 1)
        assertThat(prefs.getSelectedCalendars(), containsInAnyOrder(5L, 8L))
    }

    @Test
    fun getSelectedCalendars_shouldReturnEmptySetIfNothingIsSaved() {
        val prefs = WidgetPreferences(context, 1)
        assertThat(prefs.getSelectedCalendars(), empty())
    }

    @Test
    fun saveSelectedCalendars_shouldSavePreference() {
        val prefs = WidgetPreferences(context, 1)
        assertThat(prefs.saveSelectedCalendars(setOf(1, 3)), equalTo(true))

        assertThat(
            sharedPreferences.getStringSet("appWidget1_selectedCalendars", emptySet()),
            containsInAnyOrder("1", "3")
        )
    }

    @Test
    fun clear_shouldRemoveWidgetPreferences() {
        sharedPreferences.edit()
            .putInt("appWidget1_pref", 1)
            .putInt("appWidget2_pref", 2)
            .putInt("otherPref", 3)
            .commit()

        val prefs = WidgetPreferences(context, 1)
        assertThat(prefs.clear(), equalTo(true))

        assertThat(sharedPreferences.contains("appWidget1_pref"), equalTo(false))
        assertThat(sharedPreferences.contains("appWidget2_pref"), equalTo(true))
        assertThat(sharedPreferences.contains("otherPref"), equalTo(true))
    }
}
