package com.example.pomodoro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.adapter.SoundAdapter
import com.example.pomodoro.databinding.ActivitySettingsBinding
import com.example.pomodoro.SettingsActivity
import com.example.pomodoro.util.SoundManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    // lista de sonidos (id, nombre, recurso)
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
        val selected = prefs.getInt("selected_sound", R.raw.notificacion)

        val adapter = SoundAdapter(this, sounds, selected) { item ->
            prefs.edit().putInt("selected_sound", item.res).apply()
        }

        binding.recyclerSounds.layoutManager = LinearLayoutManager(this)
        binding.recyclerSounds.adapter = adapter

        binding.btnSaveSound.setOnClickListener {
            // ya guardamos al seleccionar; aquí podemos cerrar
            SoundManager.stop()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.stop()
    }
}