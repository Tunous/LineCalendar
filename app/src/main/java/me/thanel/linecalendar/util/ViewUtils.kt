package me.thanel.linecalendar.util

import android.support.annotation.ColorInt
import android.support.v4.graphics.drawable.DrawableCompat
import android.widget.ImageView

fun ImageView.tintDrawable(@ColorInt tint: Int) {
    val wrappedDrawable = DrawableCompat.wrap(drawable).mutate()
    DrawableCompat.setTint(wrappedDrawable, tint)
    setImageDrawable(wrappedDrawable)
}
