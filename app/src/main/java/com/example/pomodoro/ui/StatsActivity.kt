package com.example.pomodoro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.pomodoro.data.PomodoroRepository
import com.example.pomodoro.databinding.ActivityStatsBinding
import java.text.SimpleDateFormat
import java.util.*



class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repo = PomodoroRepository(this)

        // 1. Obtener todos los pomodoros agrupados por día
        val stats = repo.getStats()

        // 2. Obtener el día actual
        val todayRaw = SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date())
        val today = todayRaw.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")

        // 3. Extraer la cantidad de pomodoros para HOY
        val totalToday = stats[today] ?: 0   // si no hay registro, es 0

        // 4. Progreso (cada pomodoro vale 10%)
        val progressToday = (totalToday * 10).coerceAtMost(100)

        // 5. Mostrar datos en pantalla
        val totalPomodoros = repo.getTotalPomodoros()
        binding.tvTotal.text = totalPomodoros.toString()
        binding.progressToday.progress = progressToday

        // Frase motivadora
        binding.tvMotivation.text = fraseMotivadoraDelDia()

        // === LOGS DE DEPURACIÓN ===
        Log.d("STATS", "Stats completo: $stats")
        Log.d("STATS", "Hoy es: $today")
        Log.d("STATS", "Pomodoros hoy: $totalToday")
        Log.d("STATS", "Progreso asignado: $progressToday")

        Log.d("DEBUG_DAY", "Hoy (formateado) es: $today")
    }

    private fun fraseMotivadoraDelDia(): String {
        return listOf(
            "¡Hoy avanzaste más que ayer!",
            "Sigue así, vas increíble 👏",
            "Tu esfuerzo te está acercando al éxito.",
            "Cada pomodoro cuenta, ¡bien hecho!"
        ).random()
    }
}