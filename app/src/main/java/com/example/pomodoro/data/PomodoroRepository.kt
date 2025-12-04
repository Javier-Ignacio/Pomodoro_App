package com.example.pomodoro.data

import android.content.ContentValues
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

class PomodoroRepository(context: Context) {

    private val db = SQLiteHelper(context).writableDatabase

    fun addPomodoro(durationMinutes: Int) {
        val values = ContentValues()
        val day = SimpleDateFormat("EEEE", Locale("es", "ES"))
            .format(Date())
            .lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        values.put("dayOfWeek", day)
        values.put("date", date)
        values.put("durationMinutes", durationMinutes)
        db.insert("pomodoros", null, values)
    }

    fun getTotalPomodoros(): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM pomodoros", null)
        var total = 0
        if (cursor.moveToFirst()) total = cursor.getInt(0)
        cursor.close()
        return total
    }

    fun getTotalMinutes(): Int {
        val cursor = db.rawQuery("SELECT SUM(durationMinutes) FROM pomodoros", null)
        var total = 0
        if (cursor.moveToFirst()) total = cursor.getInt(0)
        cursor.close()
        return total
    }

    // -------------------------
    // Útiles para gráficos
    // -------------------------

    /**
     * Devuelve un LinkedHashMap ordenado con las últimas 7 fechas (yyyy-MM-dd) -> minutos (0 si no hay)
     * Orden: del más antiguo al más reciente.
     */
    fun getMinutesLast7Days(): Map<String, Int> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        // crear lista de 7 días (hace 6 días ... hoy)
        val dates = mutableListOf<String>()
        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            dates.add(fmt.format(c.time))
        }

        val result = LinkedHashMap<String, Int>()
        val query = "SELECT SUM(durationMinutes) FROM pomodoros WHERE date = ?"

        for (d in dates) {
            val cursor = db.rawQuery(query, arrayOf(d))
            var valMin = 0
            if (cursor.moveToFirst()) valMin = cursor.getInt(0)
            cursor.close()
            result[d] = valMin
        }
        return result
    }

    /**
     * Devuelve un LinkedHashMap con las últimas 7 fechas -> cantidad de pomodoros (0 si no hay)
     * Orden: del más antiguo al más reciente.
     */
    fun getPomodorosLast7Days(): Map<String, Int> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = mutableListOf<String>()
        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            dates.add(fmt.format(c.time))
        }

        val result = LinkedHashMap<String, Int>()
        val query = "SELECT COUNT(*) FROM pomodoros WHERE date = ?"

        for (d in dates) {
            val cursor = db.rawQuery(query, arrayOf(d))
            var cnt = 0
            if (cursor.moveToFirst()) cnt = cursor.getInt(0)
            cursor.close()
            result[d] = cnt
        }
        return result
    }

    /**
     * Devuelve un LinkedHashMap con los últimos 4 *periodos de semana* (label -> minutos totales).
     * Las semanas son ventanas de 7 días terminando hoy. Orden: de la más antigua a la más reciente.
     * Labels sugeridos: "Semana -3", "Semana -2", "Semana -1", "Semana actual" (se pueden mostrar como rango)
     */
    fun getMinutesLast4Weeks(): Map<String, Int> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        // calcular 4 semanas: cada semana es [start, end] con end = hoy - offset
        val result = LinkedHashMap<String, Int>()
        // empezamos 3 semanas atrás (semana 0 = hace 3 semanas, semana 3 = actual)
        for (weekIndex in 3 downTo 0) {
            val endCal = Calendar.getInstance()
            endCal.add(Calendar.WEEK_OF_YEAR, -weekIndex) // si weekIndex=3 -> hace 3 semanas; weekIndex=0 -> actual
            // determinar el inicio de esa semana: restar 6 días
            val startCal = endCal.clone() as Calendar
            startCal.add(Calendar.DAY_OF_YEAR, -6)

            val startStr = fmt.format(startCal.time)
            val endStr = fmt.format(endCal.time)

            val cursor = db.rawQuery(
                "SELECT SUM(durationMinutes) FROM pomodoros WHERE date BETWEEN ? AND ?",
                arrayOf(startStr, endStr)
            )
            var sum = 0
            if (cursor.moveToFirst()) sum = cursor.getInt(0)
            cursor.close()
            val label = "${startStr}\n→\n${endStr}"
            result[label] = sum
        }
        return result
    }
}
