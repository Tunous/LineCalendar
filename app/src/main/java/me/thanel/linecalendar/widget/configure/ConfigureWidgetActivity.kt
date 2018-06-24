package me.thanel.linecalendar.widget.configure

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_configure_widget.*
import kotlinx.android.synthetic.main.content_configure_widget.*
import me.thanel.linecalendar.R
import me.thanel.linecalendar.calendar.CalendarListItem
import me.thanel.linecalendar.calendar.CalendarLoader
import me.thanel.linecalendar.preference.IndicatorStyle
import me.thanel.linecalendar.preference.WidgetPreferences
import me.thanel.linecalendar.widget.CalendarAppWidgetProvider

class ConfigureWidgetActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var preferences: WidgetPreferences
    private lateinit var tempPreferences: WidgetPreferences
    private lateinit var eventAdapter: EventAdapter
    private val calendarAdapter = CalendarAdapter(::onCalendarCheckedChange)

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
        if (preferences.name.isBlank()) {
            preferences.name = "Widget " + CalendarAppWidgetProvider.getWidgetIds(this).size
        }
        tempPreferences = WidgetPreferences(this, AppWidgetManager.INVALID_APPWIDGET_ID)
        tempPreferences.copyFrom(preferences)

        eventAdapter = EventAdapter(this, tempPreferences)

        updateWidgetPreview()
        setupSettingsViews()

        resetFab.setOnClickListener {
            tempPreferences.copyFrom(preferences)
            setupSettingsViews()
        }

        finishFab.setOnClickListener {
            savePreferences()
            setWidgetResult(Activity.RESULT_OK)
            finish()
        }

        askPermission(Manifest.permission.READ_CALENDAR) {
            if (it.isAccepted) {
                supportLoaderManager.initLoader(LOADER_ID_CALENDARS, null, this)
            } else {
                setWidgetResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun updateWidgetPreview() {
        CalendarAppWidgetProvider.inflateViews(
            previewHolder,
            AppWidgetManager.INVALID_APPWIDGET_ID,
            eventAdapter
        )
    }

    private fun setupSettingsViews() {
        setupNameSettings()
        setupHeaderSettings()
        setupCalendarsSettings()
        setupIndicatorSettings()
        updateResetButtonVisibility()
    }

    private fun updateResetButtonVisibility() {
        if (preferences.hasSameSettings(tempPreferences)) {
            resetFab.hide()
        } else {
            resetFab.show()
        }
    }

    private fun setupNameSettings() {
        widgetNameInputView.setText(tempPreferences.name, TextView.BufferType.EDITABLE)
        widgetNameInputView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                tempPreferences.name = s?.toString() ?: "Unnamed"
                updateResetButtonVisibility()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun setupHeaderSettings() {
        headerEnabledSwitch.isChecked = tempPreferences.isHeaderEnabled
        headerEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            tempPreferences.isHeaderEnabled = isChecked
            updateHeaderButtonsEnabledState()
            updateResetButtonVisibility()
            updateWidgetPreview()
        }
        addEventHeaderButtonToggle.isChecked = tempPreferences.showAddEventHeaderButton
        addEventHeaderButtonToggle.setOnCheckedChangeListener { _, isChecked ->
            tempPreferences.showAddEventHeaderButton = isChecked
            updateResetButtonVisibility()
            updateWidgetPreview()
        }
        refreshHeaderButtonToggle.isChecked = tempPreferences.showRefreshHeaderButton
        refreshHeaderButtonToggle.setOnCheckedChangeListener { _, isChecked ->
            tempPreferences.showRefreshHeaderButton = isChecked
            updateResetButtonVisibility()
            updateWidgetPreview()
        }
        settingsHeaderButtonToggle.isChecked = tempPreferences.showSettingsHeaderButton
        settingsHeaderButtonToggle.setOnCheckedChangeListener { _, isChecked ->
            tempPreferences.showSettingsHeaderButton = isChecked
            updateResetButtonVisibility()
            updateWidgetPreview()
        }
        updateHeaderButtonsEnabledState()
    }

    private fun updateHeaderButtonsEnabledState() {
        val headerEnabled = tempPreferences.isHeaderEnabled
        addEventHeaderButtonToggle.isEnabled = headerEnabled
        refreshHeaderButtonToggle.isEnabled = headerEnabled
        settingsHeaderButtonToggle.isEnabled = headerEnabled
    }

    private fun setupCalendarsSettings() {
        calendarsRecyclerView.layoutManager = LinearLayoutManager(this)
        calendarsRecyclerView.isNestedScrollingEnabled = false
        calendarsRecyclerView.adapter = calendarAdapter
        calendarAdapter.setSelectedCalendars(tempPreferences.selectedCalendarIds)
        calendarAdapter.notifyDataSetChanged()
    }

    private fun setupIndicatorSettings() {
        indicatorStyleRadioGroup.check(tempPreferences.indicatorStyle.id)
        indicatorStyleRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            tempPreferences.indicatorStyle = IndicatorStyle.fromId(checkedId)!!
            updateResetButtonVisibility()
            eventAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return when (id) {
            LOADER_ID_CALENDARS -> {
                CursorLoader(
                    this,
                    CalendarLoader.getUri(),
                    CalendarLoader.PROJECTION,
                    null,
                    null,
                    CalendarLoader.getSortOrder()
                )
            }
            else -> throw IllegalArgumentException("Unknown loader id: $id")
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (loader.id == LOADER_ID_CALENDARS) {
            if (data == null) {
                calendarAdapter.submitList(emptyList())
                return
            }

            val selectedCalendars = tempPreferences.selectedCalendarIds
            val adapterData = mutableListOf<CalendarListItem>()
            var previousAccount: String? = null
            if (data.moveToFirst()) {
                do {
                    val id = data.getLong(CalendarLoader.PROJECTION_ID_INDEX)
                    val accountName = data.getString(CalendarLoader.PROJECTION_ACCOUNT_NAME_INDEX)

                    if (previousAccount != accountName) {
                        previousAccount = accountName
                        adapterData.add(CalendarListItem.HeaderItem(accountName))
                    }

                    adapterData.add(
                        CalendarListItem.CalendarItem(
                            id,
                            data.getString(CalendarLoader.PROJECTION_CALENDAR_DISPLAY_NAME_INDEX),
                            data.getInt(CalendarLoader.PROJECTION_CALENDAR_COLOR_INDEX),
                            accountName,
                            selectedCalendars.contains(id) || selectedCalendars.isEmpty()
                        )
                    )
                } while (data.moveToNext())
            }
            calendarAdapter.submitList(adapterData)
        }
    }

    private fun onCalendarCheckedChange() {
        tempPreferences.selectedCalendarIds = calendarAdapter.getSelectedCalendars()
        updateResetButtonVisibility()
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        when (loader.id) {
            LOADER_ID_CALENDARS -> calendarAdapter.submitList(emptyList())
        }
    }

    private fun setWidgetResult(resultCode: Int) {
        setResult(resultCode, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
    }

    private fun savePreferences() {
        preferences.copyFrom(tempPreferences)
        CalendarAppWidgetProvider.updateAllWidgets(this)
        CalendarAppWidgetProvider.updateEventList(this, appWidgetId)
    }

    companion object {
        private const val LOADER_ID_CALENDARS = 0

        fun getIntent(context: Context, appWidgetId: Int): Intent =
            Intent(context, ConfigureWidgetActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
    }
}
