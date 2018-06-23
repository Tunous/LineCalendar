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

                if (it.isAccepted) {
                    CalendarAppWidgetProvider.updateAllWidgets(this)
                    CalendarAppWidgetProvider.updateEventList(this)

                    if (intent.getBooleanExtra(EXTRA_ONLY_ASK_PERMISSION, false)) {
                        finish()
                    }
                }
            }
        }

        val widgetIds = CalendarAppWidgetProvider.getWidgetIds(this)
        if (widgetIds.isEmpty()) {
            emptyInfoView.visibility = View.VISIBLE
            widgetListRecycler.visibility = View.GONE
        } else {
            emptyInfoView.visibility = View.GONE
            widgetListRecycler.visibility = View.VISIBLE
            val widgetInfos = widgetIds.map {
                WidgetInfo(it, WidgetPreferences(this, it).name)
            }
            adapter.submitList(widgetInfos)
        }

        updateViewsVisibility(hasGrantedCalendarPermission())
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
            widgetListRecycler.visibility = if (adapter.itemCount > 0) View.VISIBLE else View.GONE
            grantPermissionButton.visibility = View.GONE
            emptyInfoView.visibility = if (adapter.itemCount > 0) View.GONE else View.VISIBLE
        } else {
            widgetListRecycler.visibility = View.GONE
            grantPermissionButton.visibility = View.VISIBLE
            emptyInfoView.visibility = View.GONE
        }

        val hintTextResId = when {
            !hasPermission -> R.string.hint_grant_permission
            adapter.itemCount == 0 -> R.string.hint_no_widgets
            else -> R.string.hint_configure_widget
        }
        hintView.setText(hintTextResId)
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
