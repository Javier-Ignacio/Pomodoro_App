package com.example.pomodoro.data

import android.content.ContentValues
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

class PomodoroRepository(context: Context) {
    private val db = SQLiteHelper(context).writableDatabase

    fun addPomodoro() {
        val values = ContentValues()

        val day = SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date()).lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
        values.put("dayOfWeek", day)

        db.insert("pomodoros", null, values)
    }

    fun getStats(): Map<String, Int> {
        val cursor = db.rawQuery("SELECT dayOfWeek, COUNT(*) FROM pomodoros GROUP BY dayOfWeek", null)
        val result = mutableMapOf<String, Int>()
        android.util.Log.d("DB", "Pomodoro insertado: $result")

        while (cursor.moveToNext()) {
            result[cursor.getString(0)] = cursor.getInt(1)
        }

        cursor.close()
        return result
    }

    fun getTotalPomodoros(): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM pomodoros", null)
        var total = 0

        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }

        cursor.close()
        return total
    }

}