package me.thanel.linecalendar.widgetlist

import android.appwidget.AppWidgetManager
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.item_widget.view.*
import me.thanel.linecalendar.R

class WidgetListAdapter(
    private val onItemClick: (Int) -> Unit
) : ListAdapter<WidgetInfo, WidgetListAdapter.ViewHolder>(WidgetInfo.DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_widget, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val widgetInfo = getItem(position)
        holder.appWidgetId = widgetInfo.id
        holder.widgetItemNameView.text = holder.itemView.context.getString(
            R.string.widget_info,
            widgetInfo.id,
            widgetInfo.width,
            widgetInfo.height
        )
    }

    class ViewHolder(
        itemView: View,
        onItemClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        val widgetItemNameView: TextView = itemView.widgetItemNameView

        var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

        init {
            itemView.setOnClickListener {
                onItemClick(appWidgetId)
            }
        }
    }
}
