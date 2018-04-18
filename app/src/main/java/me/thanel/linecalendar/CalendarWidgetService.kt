package me.thanel.linecalendar

import android.content.Intent
import android.widget.RemoteViewsService

class CalendarWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return CalendarRemoteViewsFactory(applicationContext)
    }
}
