package me.thanel.linecalendar.receiver

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import me.thanel.linecalendar.CalendarAppWidgetProvider

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CalendarObserverService : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(
            CalendarObserverService::class.java.simpleName,
            "Calendar has been modified."
        )
        CalendarAppWidgetProvider.updateAllWidgets(this)
        CalendarAppWidgetProvider.updateEventList(this)
        return false
    }
}
