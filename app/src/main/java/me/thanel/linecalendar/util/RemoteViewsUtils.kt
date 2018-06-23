package me.thanel.linecalendar.util

import android.support.annotation.IdRes
import android.view.View
import android.widget.RemoteViews

fun RemoteViews.setViewAsVisible(@IdRes viewId: Int, isVisible: Boolean) {
    setViewVisibility(viewId, if (isVisible) View.VISIBLE else View.GONE)
}
