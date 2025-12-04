package com.example.pomodoro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.adapter.SoundAdapter
import com.example.pomodoro.databinding.ActivitySettingsBinding
import com.example.pomodoro.util.SoundManager
import com.google.android.material.slider.Slider

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val sounds = listOf(
        SoundItem(1, "Alarm", R.raw.alarm),
        SoundItem(2, "Clock", R.raw.clock),
        SoundItem(3, "Classic", R.raw.notificacion),
        SoundItem(4, "Ring", R.raw.ring_bell),
        SoundItem(5, "Whatsapp", R.raw.whatsapp_ring)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("config", MODE_PRIVATE)

        // ---------------------------
        // Configuración de sonido
        // ---------------------------
        val selected = prefs.getInt("selected_sound", R.raw.notificacion)
        val adapter = SoundAdapter(this, sounds, selected) { item ->
            prefs.edit().putInt("selected_sound", item.res).apply()
        }
        binding.recyclerSounds.layoutManager = LinearLayoutManager(this)
        binding.recyclerSounds.adapter = adapter

        // ---------------------------
        // Configuración de tiempos
        // ---------------------------
        binding.sliderPomodoro.value = prefs.getInt("time_pomodoro", 25).toFloat()
        binding.sliderShortBreak.value = prefs.getInt("time_short_break", 5).toFloat()

        binding.tvPomodoroValue.text = "${binding.sliderPomodoro.value.toInt()} min"
        binding.tvShortBreakValue.text = "${binding.sliderShortBreak.value.toInt()} min"

        binding.sliderPomodoro.addOnChangeListener { _: Slider, value: Float, _: Boolean ->
            binding.tvPomodoroValue.text = "${value.toInt()} min"
        }
        binding.sliderShortBreak.addOnChangeListener { _: Slider, value: Float, _: Boolean ->
            binding.tvShortBreakValue.text = "${value.toInt()} min"
        }

        // ---------------------------
        // Guardar configuraciones
        // ---------------------------
        binding.btnSaveSound.setOnClickListener {
            prefs.edit()
                .putInt("time_pomodoro", binding.sliderPomodoro.value.toInt())
                .putInt("time_short_break", binding.sliderShortBreak.value.toInt())
                .apply()

            SoundManager.stop()
            setResult(RESULT_OK) // <- indica a MainActivity recargar tiempos
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.stop()
    }
}
