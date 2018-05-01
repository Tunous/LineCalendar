package me.thanel.linecalendar.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.ActivityResultMatchers.hasResultCode
import android.support.test.espresso.matcher.ViewMatchers.assertThat
import android.support.test.espresso.matcher.ViewMatchers.isChecked
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.isNotChecked
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.rule.GrantPermissionRule
import android.support.test.runner.AndroidJUnit4
import me.thanel.linecalendar.R
import me.thanel.linecalendar.preference.WidgetPreferences
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ConfigureWidgetActivityTest {

    @Suppress("MemberVisibilityCanBePrivate")
    @get:Rule
    val activityTestRule =
        object : ActivityTestRule<ConfigureWidgetActivity>(
            ConfigureWidgetActivity::class.java,
            false,
            false
        ) {
            override fun getActivityIntent(): Intent {
                val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
                return Intent(targetContext, ConfigureWidgetActivity::class.java)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 1)
            }
        }

    @Suppress("unused")
    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.READ_CALENDAR)

    private lateinit var preferences: WidgetPreferences

    @Before
    fun initPreferences() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        preferences = WidgetPreferences(targetContext, 1)
        preferences.clear()
    }

    @Test
    fun uncheckHeaderSwitch_hidesHeaderAndUpdatesPreference() {
        activityTestRule.launchActivity(null)

        val headerEnabledSwitch = onView(withId(R.id.headerEnabledSwitch))
        val eventsHeader = onView(withId(R.id.eventsHeader))

        headerEnabledSwitch.check(matches(isChecked()))
        eventsHeader.check(matches(isDisplayed()))

        // Uncheck header switch
        headerEnabledSwitch.perform(click())

        // Check that the header has been hidden
        headerEnabledSwitch.check(matches(isNotChecked()))
        eventsHeader.check(matches(not(isDisplayed())))
        // ...and preference updated
        assertThat(preferences.isHeaderEnabled, equalTo(false))
    }

    @Test
    fun checkHeaderSwitch_showsHeaderAndUpdatesPreference() {
        preferences.isHeaderEnabled = false

        activityTestRule.launchActivity(null)

        val headerEnabledSwitch = onView(withId(R.id.headerEnabledSwitch))
        val eventsHeader = onView(withId(R.id.eventsHeader))

        headerEnabledSwitch.check(matches(isNotChecked()))
        eventsHeader.check(matches(not(isDisplayed())))

        // Check header switch
        headerEnabledSwitch.perform(click())

        // Check that the header has been shown
        headerEnabledSwitch.check(matches(isChecked()))
        eventsHeader.check(matches(isDisplayed()))
        // ...and preference updated
        assertThat(preferences.isHeaderEnabled, equalTo(true))
    }

    @Test
    fun clickFinishFab_closesActivityWithOkResult() {
        activityTestRule.launchActivity(null)

        onView(withId(R.id.finishFab)).perform(click())

        assertThat(activityTestRule.activity.isFinishing, equalTo(true))
        assertThat(activityTestRule.activityResult, hasResultCode(Activity.RESULT_OK))
    }
}
