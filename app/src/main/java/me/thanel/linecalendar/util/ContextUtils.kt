package me.thanel.linecalendar.util

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.format.DateUtils
import me.thanel.linecalendar.R
import java.text.SimpleDateFormat
import java.util.*

fun Context.hasGrantedCalendarPermission(): Boolean {
    val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
    return result == PermissionChecker.PERMISSION_GRANTED
}

fun createColoredCircleBitmap(context: Context, @ColorInt tint: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, R.drawable.circle_small)!!
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas().apply {
        setBitmap(bitmap)
    }
    val wrappedDrawable = DrawableCompat.wrap(drawable).mutate()
    wrappedDrawable.setBounds(
        0,
        0,
        wrappedDrawable.intrinsicWidth,
        wrappedDrawable.intrinsicHeight
    )
    DrawableCompat.setTint(wrappedDrawable, tint)
    wrappedDrawable.draw(canvas)
    return bitmap
}

fun formatEventTimeText(context: Context, startTime: Long, allDay: Boolean): CharSequence {
    if (allDay) {
        val formatter = SimpleDateFormat("d MMM", Locale.getDefault())
        return formatter.format(Date(startTime))
    }
    return DateUtils.getRelativeDateTimeString(
        context,
        startTime,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.WEEK_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_ABBREV_WEEKDAY
    )
}
