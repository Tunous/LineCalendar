package me.thanel.linecalendar.preference

import android.support.v4.view.GravityCompat
import android.view.Gravity

enum class Alignment(val gravity: Int) {
    Start(GravityCompat.START),
    Center(Gravity.CENTER_HORIZONTAL),
    End(GravityCompat.END);

    companion object {
        fun fromPosition(position: Int): Alignment? = values().find { it.ordinal == position }
    }
}
