package me.thanel.linecalendar.preference

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
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
    fun getName_shouldReturnExistingName_whenNameIsSaved() {
        sharedPreferences.edit()
            .putString("appWidget1_name", "Widget 5")
            .commit()
        val prefs = WidgetPreferences(context, 1)
        assertThat(prefs.getName(8), equalTo("Widget 5"))
    }

    @Test
    fun getName_shouldReturnNameBasedOnNumber_whenNoNameIsSaved() {
        val prefs = WidgetPreferences(context, 1)
        assertThat(prefs.getName(3), equalTo("Widget 3"))
    }

    @Test
    fun saveName_shouldSaveNameBasedOnNumber() {
        val prefs = WidgetPreferences(context, 1)
        assertThat(prefs.saveName(2), equalTo(true))

        assertThat(sharedPreferences.getString("appWidget1_name", null), equalTo("Widget 2"))
    }

    @Test
    fun saveName_shouldNotOverrideExistingName() {
        sharedPreferences.edit()
            .putString("appWidget1_name", "Widget 1")
            .commit()

        val prefs = WidgetPreferences(context, 1)
        assertThat(prefs.saveName(2), equalTo(true))

        assertThat(sharedPreferences.getString("appWidget1_name", null), equalTo("Widget 1"))
    }

    @Test
    fun isHeaderEnabled_shouldReturnSavedPreference() {
        sharedPreferences.edit()
            .putBoolean("appWidget1_headerEnabled", false)
            .commit()
        val prefs = WidgetPreferences(context, 1)
        assertThat(prefs.isHeaderEnabled, equalTo(false))
    }

    @Test
    fun isHeaderEnabled_shouldReturnTrueByDefault() {
        val prefs = WidgetPreferences(context, 1)
        assertThat(prefs.isHeaderEnabled, equalTo(true))
    }

    @Test
    fun setHeaderEnabled_shouldSavePreference() {
        val prefs = WidgetPreferences(context, 1)
        prefs.isHeaderEnabled = false

        assertThat(sharedPreferences.getBoolean("appWidget1_headerEnabled", true), equalTo(false))
    }

    @Test
    fun indicatorStyle_shouldGetStyleByName() {
        sharedPreferences.edit()
            .putString("appWidget1_indicatorStyle", IndicatorStyle.Circle.name)
            .commit()
        val prefs = WidgetPreferences(context, 1)
        assertThat(prefs.indicatorStyle, equalTo(IndicatorStyle.Circle))

        sharedPreferences.edit()
            .putString(
                "appWidget1_indicatorStyle",
                IndicatorStyle.RoundedRectangle.name
            )
            .commit()
        assertThat(prefs.indicatorStyle, equalTo(IndicatorStyle.RoundedRectangle))
    }

    @Test
    fun indicatorStyle_shouldReturnCircleStyleByDefault() {
        val prefs = WidgetPreferences(context, 1)
        assertThat(prefs.indicatorStyle, equalTo(IndicatorStyle.Circle))
    }

    @Test
    fun setIndicatorStyle_shouldSaveStyleByName() {
        val prefs = WidgetPreferences(context, 1)
        prefs.indicatorStyle = IndicatorStyle.RoundedRectangle

        assertThat(
            sharedPreferences.getString(
                "appWidget1_indicatorStyle",
                IndicatorStyle.Circle.name
            ),
            equalTo(IndicatorStyle.RoundedRectangle.name)
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
