package me.thanel.linecalendar.widgetlist

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_widget_list.*
import me.thanel.linecalendar.CalendarAppWidgetProvider
import me.thanel.linecalendar.ConfigureWidgetActivity
import me.thanel.linecalendar.R

class WidgetListActivity : AppCompatActivity() {
    private val adapter = WidgetListAdapter(::onWidgetClick)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_list)

        widgetListRecycler.layoutManager = LinearLayoutManager(this)
        widgetListRecycler.adapter = adapter

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

    private fun onWidgetClick(appWidgetId: Int) {
        val intent = Intent(this, ConfigureWidgetActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        startActivityForResult(
            intent,
            REQUEST_CONFIGURE_WIDGET
        )
    }

    companion object {
        private const val REQUEST_CONFIGURE_WIDGET = 1
    }
}
