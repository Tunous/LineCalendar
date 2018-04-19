package me.thanel.linecalendar.widgetlist

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_widget_list.*
import me.thanel.linecalendar.CalendarAppWidgetProvider
import me.thanel.linecalendar.ConfigureWidgetActivity
import me.thanel.linecalendar.R
import me.thanel.linecalendar.util.hasGrantedCalendarPermission

class WidgetListActivity : AppCompatActivity() {
    private val adapter = WidgetListAdapter(::onWidgetClick)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_list)

        widgetListRecycler.layoutManager = LinearLayoutManager(this)
        widgetListRecycler.adapter = adapter

        grantPermissionButton.setOnClickListener {
            askPermission(Manifest.permission.READ_CALENDAR) {
                updateViewsVisibility(it.isAccepted)

                if (it.isAccepted && intent.getBooleanExtra(EXTRA_ONLY_ASK_PERMISSION, false)) {
                    CalendarAppWidgetProvider.updateAllWidgets(this)
                    CalendarAppWidgetProvider.updateEventList(this)
                    finish()
                }
            }
        }

        updateViewsVisibility(hasGrantedCalendarPermission())

        val appWidgetManager = AppWidgetManager.getInstance(this)
        val widgetProviderComponentName = ComponentName(this, CalendarAppWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetProviderComponentName)
        val widgetInfos = widgetIds.map {
            val options = appWidgetManager.getAppWidgetOptions(it)
            val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            WidgetInfo(it, width, height)
        }
        adapter.submitList(widgetInfos)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CONFIGURE_WIDGET) {
            if (resultCode == Activity.RESULT_OK) {
                finish()
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateViewsVisibility(hasPermission: Boolean) {
        if (hasPermission) {
            widgetListRecycler.visibility = View.VISIBLE
            grantPermissionContainer.visibility = View.GONE
        } else {
            widgetListRecycler.visibility = View.GONE
            grantPermissionContainer.visibility = View.VISIBLE
        }
    }

    private fun onWidgetClick(appWidgetId: Int) {
        startActivityForResult(
            ConfigureWidgetActivity.getIntent(this, appWidgetId),
            REQUEST_CONFIGURE_WIDGET
        )
    }

    companion object {
        private const val REQUEST_CONFIGURE_WIDGET = 1
        private const val EXTRA_ONLY_ASK_PERMISSION = "onlyAskPermission"

        fun getIntent(context: Context, onlyAskPermission: Boolean): Intent =
            Intent(context, WidgetListActivity::class.java)
                .putExtra(EXTRA_ONLY_ASK_PERMISSION, onlyAskPermission)
    }
}
