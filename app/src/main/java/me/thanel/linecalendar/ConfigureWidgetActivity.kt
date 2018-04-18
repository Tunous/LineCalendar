package me.thanel.linecalendar

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.CalendarContract
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_configure_widget.*
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

        calendarsRecyclerView.layoutManager = LinearLayoutManager(this)
        calendarsRecyclerView.adapter = adapter

        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        preferences = WidgetPreferences(this, appWidgetId)

        setWidgetResult(Activity.RESULT_CANCELED)

        askPermission(Manifest.permission.READ_CALENDAR) {
            supportLoaderManager.initLoader(0, null, this)
        }.onDeclined {
            TODO("Permission declined")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.configure_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.apply -> {
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    preferences.saveSelectedCalendars(adapter.getSelectedCalendars())
                    updateWidget()
                    setWidgetResult(Activity.RESULT_OK)
                }
                finish()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
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
        val appWidgetManager = AppWidgetManager.getInstance(this)
        CalendarAppWidgetProvider.updateWidget(this, appWidgetManager, appWidgetId)
    }
}
