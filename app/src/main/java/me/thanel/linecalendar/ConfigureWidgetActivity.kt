package me.thanel.linecalendar

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.RemoteViews
import com.github.florent37.runtimepermission.kotlin.askPermission

class ConfigureWidgetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure_widget)

        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        setResult(
            RESULT_CANCELED,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        )

        askPermission(Manifest.permission.READ_CALENDAR) {
            val appWidgetManager = AppWidgetManager.getInstance(this@ConfigureWidgetActivity)
            val views = RemoteViews(packageName, R.layout.widget_calendar)
            views.setEmptyView(R.id.events_list_view, R.id.empty_view)

            val intent =
                Intent(this@ConfigureWidgetActivity, CalendarWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                }
            views.setRemoteAdapter(R.id.events_list_view, intent)

            val itemIntent = Intent(Intent.ACTION_VIEW)
            val pendingIntent =
                PendingIntent.getActivity(this@ConfigureWidgetActivity, 0, itemIntent, 0)
            views.setPendingIntentTemplate(R.id.events_list_view, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)

            // Permission granted
            setResult(
                RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            )
            finish()
        }.onDeclined {
            TODO("Permission declined")
        }
    }
}
