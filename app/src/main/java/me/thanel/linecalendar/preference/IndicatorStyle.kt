package me.thanel.linecalendar.preference

import android.support.annotation.IdRes
import me.thanel.linecalendar.R

enum class IndicatorStyle(@IdRes val id: Int) {
    None(R.id.indicatorStyleNone),
    Circle(R.id.indicatorStyleCircle),
    RoundedRectangle(R.id.indicatorStyleRoundedRect);

    companion object {
        fun fromId(@IdRes id: Int): IndicatorStyle? = values().find { it.id == id }
    }
}
