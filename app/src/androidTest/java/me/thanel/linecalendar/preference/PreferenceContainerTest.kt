package me.thanel.linecalendar.preference

import android.content.Context
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class PreferenceContainerTest : PreferencesTestBase() {

    @Test
    fun addsPreferencesToDefaults() {
        val prefs = TestPreferences(context, 1)
        assertThat(
            prefs.defaults,
            equalTo(
                mapOf(
                    "booleanPref" to true,
                    "floatPref" to 9f,
                    "intPref" to 5,
                    "longPref" to 2L,
                    "stringPref" to "default",
                    "stringSetPref" to setOf("1")
                )
            )
        )
    }

    @Test
    fun correctlyWritesAndReadsPreferences() {
        val prefs = TestPreferences(context, 1)
        prefs.booleanPref = false
        assertThat(prefs.booleanPref, equalTo(false))
        prefs.floatPref = 99f
        assertThat(prefs.floatPref, equalTo(99f))
        prefs.intPref = 437
        assertThat(prefs.intPref, equalTo(437))
        prefs.longPref = 12L
        assertThat(prefs.longPref, equalTo(12L))
        prefs.stringPref = "string"
        assertThat(prefs.stringPref, equalTo("string"))
        prefs.stringSetPref = setOf("a", "b")
        assertThat(prefs.stringSetPref, equalTo(setOf("a", "b")))
    }

    @Test
    fun clear_removesAllPrefixedPreferences() {
        sharedPreferences.edit()
            .putInt("pref1_pref", 1)
            .putInt("pref2_pref", 2)
            .putInt("otherPref", 3)
            .commit()

        val prefs = TestPreferences(context, 1)
        Assert.assertThat(prefs.clear(), Matchers.equalTo(true))

        Assert.assertThat(sharedPreferences.contains("pref1_pref"), Matchers.equalTo(false))
        Assert.assertThat(sharedPreferences.contains("pref2_pref"), Matchers.equalTo(true))
        Assert.assertThat(sharedPreferences.contains("otherPref"), Matchers.equalTo(true))
    }

    @Test
    fun hasSameSettings_returnsTrueForDefaults() {
        val prefs1 = TestPreferences(context, 1)
        val prefs2 = TestPreferences(context, 2)
        assertThat(prefs1.hasSameSettings(prefs2), equalTo(true))
    }

    @Test
    fun hasSameSettings_returnsFalseForDifferences() {
        val prefs1 = TestPreferences(context, 1)
        val prefs2 = TestPreferences(context, 2)

        prefs1.stringPref = "different"
        assertThat(prefs1.hasSameSettings(prefs2), equalTo(false))
    }

    @Test
    fun copyFrom_setsAllPreferences() {
        val prefs1 = TestPreferences(context, 1).apply {
            booleanPref = false
            floatPref = 11f
            intPref = 8
            longPref = 3L
            stringPref = "modified"
            stringSetPref = setOf("4")
        }
        val prefs2 = TestPreferences(context, 2)
        prefs2.copyFrom(prefs1)
        assertThat(prefs1.hasSameSettings(prefs2), equalTo(true))
    }

    private class TestPreferences(
        context: Context,
        number: Int
    ) : PreferenceContainer<TestPreferences>(context, number) {
        override val identifier: String = "pref"

        var booleanPref by bindPreference("booleanPref", true)

        var floatPref by bindPreference("floatPref", 9f)

        var intPref by bindPreference("intPref", 5)

        var longPref by bindPreference("longPref", 2L)

        var stringPref by bindPreference("stringPref", "default")

        var stringSetPref by bindPreference("stringSetPref", setOf("1"))
    }
}
