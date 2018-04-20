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
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_configure_widget.*
import me.thanel.linecalendar.R
import me.thanel.linecalendar.calendar.CalendarAdapter
import me.thanel.linecalendar.calendar.CalendarData
import me.thanel.linecalendar.preference.WidgetPreferences

class ConfigureWidgetActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var preferences: WidgetPreferences
    private val adapter = CalendarAdapter()

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

        headerEnabledSwitch.isChecked = preferences.isHeaderEnabled
        headerEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.isHeaderEnabled = isChecked
            updateWidget()
        }

        calendarsRecyclerView.layoutManager = LinearLayoutManager(this)
        calendarsRecyclerView.adapter = adapter

        applySettingsButton.setOnClickListener {
            savePreferences()
            updateWidget()
            setWidgetResult(Activity.RESULT_OK)
            finish()
        }

        askPermission(Manifest.permission.READ_CALENDAR) {
            if (it.isAccepted) {
                supportLoaderManager.initLoader(0, null, this)
            } else {
                finish()
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR
        )
        return CursorLoader(
            this,
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} COLLATE NOCASE ASC"
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (data == null) {
            adapter.submitList(emptyList())
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
        adapter.submitList(calendars)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.submitList(emptyList())
    }

    private fun setWidgetResult(resultCode: Int) {
        setResult(resultCode, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
    }

    private fun updateWidget() {
        CalendarAppWidgetProvider.updateAllWidgets(this)
        CalendarAppWidgetProvider.updateEventList(this, appWidgetId)
    }

    private fun savePreferences() {
        preferences.saveSelectedCalendars(adapter.getSelectedCalendars())
        preferences.saveName(CalendarAppWidgetProvider.getWidgetIds(this).size)
    }

    companion object {
        fun getIntent(context: Context, appWidgetId: Int): Intent =
            Intent(context, ConfigureWidgetActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
    }
}
