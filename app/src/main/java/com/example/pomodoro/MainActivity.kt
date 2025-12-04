package com.example.pomodoro

import android.Manifest
import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pomodoro.data.PomodoroRepository
import com.example.pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var countDownTimer: CountDownTimer? = null

    private var isBreak = false

    private val pomodoroDuration = 5 * 1000L // Tiemmpo duración pomodoro
    private val breakDuration = 3 * 1000L    // Tiempo duración break

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()

        // init UI
        binding.tvStatus.text = "Listo"

        binding.btnStart.setOnClickListener { startPomodoro() }
        binding.btnStop.setOnClickListener { stopPomodoro() }
        binding.btnStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        binding.fabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun playSelectedSound() {
        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val soundRes = prefs.getInt("selected_sound", R.raw.notificacion)
        com.example.pomodoro.util.SoundManager.playSound(this, soundRes)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }

    private fun animateIndicator(from: Int, to: Int) {
        // usaremos ObjectAnimator sobre la propiedad "progress" del CircularProgressIndicator
        val animator = ObjectAnimator.ofInt(binding.progressCircular, "progress", from, to)
        animator.duration = 600
        animator.start()
    }

    private fun startPomodoro() {
        isBreak = false
        countDownTimer?.cancel()

        binding.progressCircular.setIndicatorColor(ContextCompat.getColor(this, R.color.primary))
        binding.tvStatus.text = "Trabajo"

        // playSelectedSound()

        countDownTimer = object : CountDownTimer(pomodoroDuration + 100, 1000) { // ← extra 100 ms
            override fun onTick(ms: Long) {
                val safeMs = if (ms < 1000) 0 else ms   // ← evita valores negativos

                val min = safeMs / 60000
                val sec = (safeMs % 60000) / 1000
                binding.tvTimer.text = String.format("%02d:%02d", min, sec)

                val progress = ((pomodoroDuration - safeMs).toFloat() / pomodoroDuration * 100).toInt()
                animateIndicator(binding.progressCircular.progress, progress)
            }

            override fun onFinish() {
                // 1️⃣ --- MOSTRAR 00:00 SIEMPRE ---
                binding.tvTimer.text = "00:00"
                animateIndicator(binding.progressCircular.progress, 100)

                // 2️⃣ --- PAUSA BREVE PARA QUE SE VEA 00:00 ---
                Handler(Looper.getMainLooper()).postDelayed({
                    playSelectedSound()
                    PomodoroRepository(this@MainActivity).addPomodoro()
                    showNotification()
                    startBreak()
                }, 800)  // 0.8 segundos
            }
        }.apply { start() }
    }

    private fun startBreak() {
        isBreak = true
        countDownTimer?.cancel()

        binding.progressCircular.setIndicatorColor(ContextCompat.getColor(this, R.color.accent))
        binding.tvStatus.text = "Descanso"

        loadMotivationQuote()

        countDownTimer = object : CountDownTimer(breakDuration + 100, 1000) {
            override fun onTick(ms: Long) {
                val safeMs = if (ms < 1000) 0 else ms

                val min = safeMs / 60000
                val sec = (safeMs % 60000) / 1000
                binding.tvTimer.text = "Descanso: ${String.format("%02d:%02d", min, sec)}"

                val progress = ((breakDuration - safeMs).toFloat() / breakDuration * 100).toInt()
                animateIndicator(binding.progressCircular.progress, progress)
            }

            override fun onFinish() {
                binding.tvTimer.text = "Descanso terminado"
                animateIndicator(binding.progressCircular.progress, 100)
                binding.tvStatus.text = "Listo"
            }
        }.apply { start() }
    }

    private fun stopPomodoro() {
        countDownTimer?.cancel()
        binding.tvStatus.text = "Pausado"
    }

    private fun showNotification() {
        val channelId = "pomodoro_done"
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(channelId, "Pomodoro", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)

        val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Pomodoro completado")
            .setContentText("Toma un descanso")

        manager.notify(1001, builder.build())
    }

    // ---------- API Quote (ZenQuotes) ----------
    private fun loadMotivationQuote() {
        // tu implementación con Retrofit (ya funcional)
        binding.tvQuote.text = "Cargando..."
        com.example.pomodoro.api.RetrofitClient.instance.getRandomQuote()
            .enqueue(object : retrofit2.Callback<List<com.example.pomodoro.data.MotivationQuote>> {
                override fun onResponse(
                    call: retrofit2.Call<List<com.example.pomodoro.data.MotivationQuote>>,
                    response: retrofit2.Response<List<com.example.pomodoro.data.MotivationQuote>>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body()
                        if (!list.isNullOrEmpty()) {
                            val q = list[0]
                            binding.tvQuote.text = "\"${q.q}\" \n— ${q.a}"
                        } else {
                            binding.tvQuote.text = "No hay frase."
                        }
                    } else {
                        binding.tvQuote.text = "Error al obtener frase."
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<com.example.pomodoro.data.MotivationQuote>>, t: Throwable) {
                    binding.tvQuote.text = "Frase offline."
                }
            })
    }
}