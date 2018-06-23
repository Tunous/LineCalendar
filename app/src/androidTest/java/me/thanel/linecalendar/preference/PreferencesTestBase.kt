package me.thanel.linecalendar.preference

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import org.junit.After
import org.junit.Before

abstract class PreferencesTestBase {
    protected lateinit var context: Context
        private set
    protected lateinit var sharedPreferences: SharedPreferences
        private set

    @Before
    fun setupTests() {
        context = InstrumentationRegistry.getTargetContext()
        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)
    }

    @After
    fun clearPreferences() {
        sharedPreferences.edit().clear().commit()
    }
}
