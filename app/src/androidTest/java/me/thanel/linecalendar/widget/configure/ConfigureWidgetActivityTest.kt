package me.thanel.linecalendar.widget.configure

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
import me.thanel.linecalendar.preference.IndicatorStyle
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
        object : ActivityTestRule<ConfigureWidgetActivity>(ConfigureWidgetActivity::class.java) {
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
    private lateinit var tempPreferences: WidgetPreferences

    @Before
    fun initPreferences() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        preferences = WidgetPreferences(targetContext, 1)
        preferences.clear()
        tempPreferences = WidgetPreferences(targetContext, AppWidgetManager.INVALID_APPWIDGET_ID)
        tempPreferences.clear()
    }

    @Test
    fun uncheckHeaderSwitch_hidesHeaderAndUpdatesPreference() {
        // By default should enable header
        onView(withId(R.id.headerEnabledSwitch)).check(matches(isChecked()))
        onView(withId(R.id.eventsHeader)).check(matches(isDisplayed()))

        // Uncheck header switch
        onView(withId(R.id.headerEnabledSwitch)).perform(click())

        // Verify that the header has been hidden
        onView(withId(R.id.headerEnabledSwitch)).check(matches(isNotChecked()))
        onView(withId(R.id.eventsHeader)).check(matches(not(isDisplayed())))
        // ...and preference updated
        assertThat(tempPreferences.isHeaderEnabled, equalTo(false))
    }

    @Test
    fun clickFinishFab_closesActivityWithOkResult() {
        onView(withId(R.id.finishFab)).perform(click())

        assertThat(activityTestRule.activity.isFinishing, equalTo(true))
        assertThat(activityTestRule.activityResult, hasResultCode(Activity.RESULT_OK))
    }

    @Test
    fun indicatorStyle_updatesViewsAndPreference() {
        // By default should check circle style
        onView(withId(R.id.indicatorStyleNone)).check(matches(not(isChecked())))
        onView(withId(R.id.indicatorStyleCircle)).check(matches(isChecked()))
        onView(withId(R.id.indicatorStyleRoundedRect)).check(matches(not(isChecked())))

        // Click on "none" style
        onView(withId(R.id.indicatorStyleNone)).perform(click())

        // Verify that radio button state has been updated
        onView(withId(R.id.indicatorStyleNone)).check(matches(isChecked()))
        onView(withId(R.id.indicatorStyleCircle)).check(matches(not(isChecked())))
        onView(withId(R.id.indicatorStyleRoundedRect)).check(matches(not(isChecked())))
        // ...and preference modified
        assertThat(tempPreferences.indicatorStyle, equalTo(IndicatorStyle.None))
    }
}
