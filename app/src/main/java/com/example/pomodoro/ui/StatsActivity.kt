package com.example.pomodoro

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pomodoro.data.PomodoroRepository
import com.example.pomodoro.databinding.ActivityStatsBinding
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private lateinit var repo: PomodoroRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = PomodoroRepository(this)

        loadSummary()
        setupMinutesLast7DaysChart()
        setupPomodorosLast7DaysChart()
        setupMinutesLast4WeeksChart()
    }

    private fun loadSummary() {
        val totalPom = repo.getTotalPomodoros()
        val totalMin = repo.getTotalMinutes()
        binding.tvTotalPomodoros.text = totalPom.toString()
        binding.tvTotalMinutes.text = "$totalMin min"
    }

    // ------------------------------
    // A) Minutos por día (últimos 7 días)
    // ------------------------------
    private fun setupMinutesLast7DaysChart() {
        val map = repo.getMinutesLast7Days() // ordered map yyyy-MM-dd -> minutos
        val labels = ArrayList<String>()
        val entries = ArrayList<Entry>()

        val inputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFmt = SimpleDateFormat("EEE", Locale("es", "ES")) // etiqueta tipo "lun", "mar", etc.

        var index = 0f
        for ((dateStr, minutes) in map) {
            // convertir label corto en español
            val d = inputFmt.parse(dateStr)
            val label = if (d != null) dayFmt.format(d).lowercase().replace(".", "") else dateStr
            labels.add(label)
            entries.add(Entry(index, minutes.toFloat()))
            index += 1f
        }

        val dataSet = LineDataSet(entries, "Minutos")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.setCircleColors(ColorTemplate.MATERIAL_COLORS.toList())
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.valueTextSize = 10f

        val lineData = LineData(dataSet)
        binding.lineChartMinutesDay.data = lineData

        // XAxis labels
        val xAxis = binding.lineChartMinutesDay.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        binding.lineChartMinutesDay.axisRight.isEnabled = false
        binding.lineChartMinutesDay.description = Description().apply { text = "" }
        binding.lineChartMinutesDay.animateX(700)
        binding.lineChartMinutesDay.invalidate()
    }

    // ------------------------------
    // B) Pomodoros por día (últimos 7 días)
    // ------------------------------
    private fun setupPomodorosLast7DaysChart() {
        val map = repo.getPomodorosLast7Days() // ordered map yyyy-MM-dd -> count
        val labels = ArrayList<String>()
        val entries = ArrayList<Entry>()

        val inputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFmt = SimpleDateFormat("EEE", Locale("es", "ES"))

        var index = 0f
        for ((dateStr, count) in map) {
            val d = inputFmt.parse(dateStr)
            val label = if (d != null) dayFmt.format(d).lowercase().replace(".", "") else dateStr
            labels.add(label)
            entries.add(Entry(index, count.toFloat()))
            index += 1f
        }

        val dataSet = LineDataSet(entries, "Sesiones")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.setCircleColors(ColorTemplate.COLORFUL_COLORS.toList())
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.valueTextSize = 10f

        val lineData = LineData(dataSet)
        binding.lineChartPomodorosDay.data = lineData

        val xAxis = binding.lineChartPomodorosDay.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        binding.lineChartPomodorosDay.axisRight.isEnabled = false
        binding.lineChartPomodorosDay.description = Description().apply { text = "" }
        binding.lineChartPomodorosDay.animateX(700)
        binding.lineChartPomodorosDay.invalidate()
    }

    // ------------------------------
    // C) Minutos por semana (últimas 4 semanas)
    // ------------------------------
    private fun setupMinutesLast4WeeksChart() {
        val map = repo.getMinutesLast4Weeks() // label range -> minutes
        val labels = ArrayList<String>()
        val entries = ArrayList<Entry>()

        var index = 0f
        for ((label, minutes) in map) {
            // label ya es "yyyy-MM-dd -> yyyy-MM-dd"
            labels.add(shortenWeekLabel(label))
            entries.add(Entry(index, minutes.toFloat()))
            index += 1f
        }

        val dataSet = LineDataSet(entries, "Minutos semana")
        dataSet.colors = ColorTemplate.PASTEL_COLORS.toList()
        dataSet.setCircleColors(ColorTemplate.PASTEL_COLORS.toList())
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.valueTextSize = 10f

        val lineData = LineData(dataSet)
        binding.lineChartMinutesWeek.data = lineData

        val xAxis = binding.lineChartMinutesWeek.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        binding.lineChartMinutesWeek.axisRight.isEnabled = false
        binding.lineChartMinutesWeek.description = Description().apply { text = "" }
        binding.lineChartMinutesWeek.animateX(800)
        binding.lineChartMinutesWeek.invalidate()
    }

    private fun shortenWeekLabel(label: String): String {
        // label es "yyyy-MM-dd\n→\nyyyy-MM-dd" según repo
        val parts = label.split("\n")
        return if (parts.size >= 3) {
            val start = parts[0]
            val end = parts[2]
            val fmtIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fmtOut = SimpleDateFormat("dd/MM", Locale.getDefault())
            val sDate = fmtIn.parse(start)
            val eDate = fmtIn.parse(end)
            if (sDate != null && eDate != null) {
                "${fmtOut.format(sDate)}-${fmtOut.format(eDate)}"
            } else {
                "$start - $end"
            }
        } else label
    }
}
