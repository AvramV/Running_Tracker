package com.avramhorseman.runningtracker.other

import android.content.Context
import android.icu.util.Calendar
import com.avramhorseman.runningtracker.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(val runs: List<Run>, context: Context, layoutId: Int): MarkerView(context, layoutId) {


    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)

        if(e == null){
            return
        }

        val runId = e.x.toInt()
        val run = runs[runId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }

        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDateMV.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedKMH}km/h"
        tvAvgSpeedMV.text = avgSpeed

        val distanceInKM = "${run.distanceInMeters / 1000f}km"
        tvDistanceMV.text = distanceInKM

        tvDurationMV.text = TrackingUtility.getFormattedTime(run.timeInMillis)

        val caloriesBurned = "${run.caloriesBurned}kcal"
        tvCaloriesBurnedMV.text = caloriesBurned

    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

}