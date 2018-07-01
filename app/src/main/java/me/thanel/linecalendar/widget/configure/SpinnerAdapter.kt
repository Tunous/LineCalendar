package me.thanel.linecalendar.widget.configure

import android.content.Context
import android.support.annotation.ArrayRes
import android.support.annotation.StringRes
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import me.thanel.linecalendar.R

class SpinnerAdapter(
    context: Context,
    @ArrayRes arrayResId: Int,
    @StringRes private val titleResId: Int
) : ArrayAdapter<CharSequence>(
    context,
    R.layout.spinner_item_with_title,
    android.R.id.text1,
    context.resources.getTextArray(arrayResId)
) {
    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
        super.getView(position, convertView, parent).apply {
            findViewById<TextView>(R.id.titleTextView).setText(titleResId)
        }
}
