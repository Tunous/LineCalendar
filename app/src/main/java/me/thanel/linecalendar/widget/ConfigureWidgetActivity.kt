package me.thanel.linecalendar.widget

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_configure_widget.*
import kotlinx.android.synthetic.main.view_events_header.*
import kotlinx.android.synthetic.main.widget_calendar.*
import me.thanel.linecalendar.R
import me.thanel.linecalendar.calendar.CalendarAdapter
import me.thanel.linecalendar.calendar.CalendarData
import me.thanel.linecalendar.preference.WidgetPreferences
import java.text.SimpleDateFormat
import java.util.*

class ConfigureWidgetActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var preferences: WidgetPreferences
    private lateinit var eventAdapter: EventAdapter
    private val calendarAdapter = CalendarAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure_widget)

        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        setWidgetResult(Activity.RESULT_CANCELED)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        preferences = WidgetPreferences(this, appWidgetId)

        eventAdapter = EventAdapter(this, preferences)
        eventsListView.adapter = eventAdapter
        eventsListView.emptyView = eventsEmptyView

        eventsHeader.visibility = if (preferences.isHeaderEnabled) View.VISIBLE else View.GONE
        headerTitleView.text = SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(Date())

        headerEnabledSwitch.isChecked = preferences.isHeaderEnabled
        headerEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.isHeaderEnabled = isChecked
            eventsHeader.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        calendarsRecyclerView.layoutManager = LinearLayoutManager(this)
        calendarsRecyclerView.adapter = calendarAdapter
        calendarsRecyclerView.isNestedScrollingEnabled = false

        finishFab.setOnClickListener {
            setWidgetResult(Activity.RESULT_OK)
            finish()
        }

        indicatorStyleSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.indicator_styles,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        indicatorStyleSpinner.setSelection(preferences.indicatorStyle.ordinal)
        indicatorStyleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val style =
                    WidgetPreferences.IndicatorStyle.values().find { it.ordinal == position }
                preferences.indicatorStyle = style ?: WidgetPreferences.IndicatorStyle.Circle
                eventAdapter.notifyDataSetChanged()
            }
        }

        askPermission(Manifest.permission.READ_CALENDAR) {
            if (it.isAccepted) {
                supportLoaderManager.initLoader(LOADER_ID_CALENDARS, null, this)
                supportLoaderManager.initLoader(LOADER_ID_EVENTS, null, this)
            } else {
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        savePreferences()
        updateWidget()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return when (id) {
            LOADER_ID_CALENDARS -> {
                val projection = arrayOf(
                    CalendarContract.Calendars._ID,
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Calendars.CALENDAR_COLOR
                )
                CursorLoader(
                    this,
                    CalendarContract.Calendars.CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} COLLATE NOCASE ASC"
                )
            }
            LOADER_ID_EVENTS -> {
                val selectedCalendars = preferences.getSelectedCalendars()
                CursorLoader(
                    this,
                    EventLoader.getUri(),
                    EventLoader.PROJECTION,
                    EventLoader.getSelection(selectedCalendars),
                    EventLoader.getSelectionArgs(selectedCalendars),
                    EventLoader.getSortOrder()
                )
            }
            else -> throw IllegalArgumentException("Unknown loader id: $id")
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        when (loader.id) {
            LOADER_ID_CALENDARS -> {
                if (data == null) {
                    calendarAdapter.submitList(emptyList())
                    return
                }

                val selectedCalendars = preferences.getSelectedCalendars()
                val calendars = mutableListOf<CalendarData>()
                if (data.moveToFirst()) {
                    do {
                        val id = data.getLong(0)
                        calendars.add(
                            CalendarData(
                                id,
                                data.getString(1),
                                data.getInt(2),
                                selectedCalendars.contains(id) || selectedCalendars.isEmpty()
                            )
                        )
                    } while (data.moveToNext())
                }
                calendarAdapter.submitList(calendars)
            }
            LOADER_ID_EVENTS -> {
                eventAdapter.swapCursor(data)
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        when (loader.id) {
            LOADER_ID_CALENDARS -> calendarAdapter.submitList(emptyList())
            LOADER_ID_EVENTS -> eventAdapter.swapCursor(null)
        }
    }

    private fun setWidgetResult(resultCode: Int) {
        setResult(resultCode, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
    }

    private fun updateWidget() {
        CalendarAppWidgetProvider.updateAllWidgets(this)
        CalendarAppWidgetProvider.updateEventList(this, appWidgetId)
    }

    private fun savePreferences() {
        preferences.saveSelectedCalendars(calendarAdapter.getSelectedCalendars())
        preferences.saveName(CalendarAppWidgetProvider.getWidgetIds(this).size)
    }

    companion object {
        private const val LOADER_ID_CALENDARS = 0
        private const val LOADER_ID_EVENTS = 1

        fun getIntent(context: Context, appWidgetId: Int): Intent =
            Intent(context, ConfigureWidgetActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
    }
}
