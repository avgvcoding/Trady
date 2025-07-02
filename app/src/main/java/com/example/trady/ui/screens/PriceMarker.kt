package com.example.trady.ui.screens

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.example.trady.R

/** Simple marker that shows the date + price when the user taps the chart */
class PriceMarker(
    context: Context,
    layoutRes: Int,
    private val dateLabels: List<String>
) : MarkerView(context, layoutRes) {

    private val tv: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e ?: return
        val idx = e.x.toInt().coerceIn(dateLabels.indices)
        tv.text = "${dateLabels[idx]}\n$${e.y}"
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF =
        MPPointF((-width / 2).toFloat(), (-height - 12).toFloat())
}
