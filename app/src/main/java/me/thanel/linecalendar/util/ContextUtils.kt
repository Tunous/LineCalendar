package me.thanel.linecalendar.util

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v4.graphics.drawable.DrawableCompat
import android.text.format.DateUtils

fun Context.hasGrantedCalendarPermission(): Boolean {
    val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
    return result == PermissionChecker.PERMISSION_GRANTED
}

fun Context.getTintedBitmap(@DrawableRes drawableRes: Int, @ColorInt tint: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(this, drawableRes)!!
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
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

fun Context.formatEventTimeText(startTime: Long, allDay: Boolean): CharSequence {
    if (allDay) {
        return DateUtils.getRelativeTimeSpanString(
            startTime,
            System.currentTimeMillis(),
            DateUtils.DAY_IN_MILLIS,
            DateUtils.FORMAT_NO_YEAR
        )
    }
    return DateUtils.getRelativeDateTimeString(
        this,
        startTime,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.WEEK_IN_MILLIS,
        if (allDay) DateUtils.FORMAT_SHOW_WEEKDAY
        else DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_ABBREV_WEEKDAY
    )
}
