package me.thanel.linecalendar.widgetlist

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_widget_list.*
import me.thanel.linecalendar.R
import me.thanel.linecalendar.preference.WidgetPreferences
import me.thanel.linecalendar.util.hasGrantedCalendarPermission
import me.thanel.linecalendar.widget.CalendarAppWidgetProvider
import me.thanel.linecalendar.widget.configure.ConfigureWidgetActivity

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

        val widgetIds = CalendarAppWidgetProvider.getWidgetIds(this)
        val numWidgets = widgetIds.size
        val widgetInfos = widgetIds.map {
            WidgetInfo(it, WidgetPreferences(this, it).getName(numWidgets))
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
