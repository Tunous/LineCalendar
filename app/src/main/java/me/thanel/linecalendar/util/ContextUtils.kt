package me.thanel.linecalendar.util

import android.Manifest
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker

fun Context.hasGrantedCalendarPermission(): Boolean {
    val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
    return result == PermissionChecker.PERMISSION_GRANTED
}
