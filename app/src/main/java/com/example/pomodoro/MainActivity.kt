/*

INTEGRANTES:

- MATÍAS VALENZUELA.
- JAVIER BARROS.

*/


package com.example.pomodoro

import android.util.Log
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pomodoro.data.PomodoroRepository
import com.example.pomodoro.databinding.ActivityMainBinding

import com.example.pomodoro.api.RetrofitClient
import com.example.pomodoro.data.MotivationQuote
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var countDownTimer: CountDownTimer? = null

    private var isBreak = false

    // 3 segundos para pruebas
    private val pomodoroDuration = 5 * 1000L
    private val breakDuration = 5 * 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()

        binding.btnStart.setOnClickListener { startPomodoro() }
        binding.btnStop.setOnClickListener { stopPomodoro() }
        binding.btnStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
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

    // ------------------------------
    //         POMODORO
    // ------------------------------
    private fun startPomodoro() {

        isBreak = false
        countDownTimer?.cancel()

        // Cambiar drawable a modo trabajo
        binding.progressCircular.progressDrawable =
            ContextCompat.getDrawable(this, R.drawable.circle_progress_work)

        countDownTimer = object : CountDownTimer(pomodoroDuration, 1000) {

            override fun onTick(ms: Long) {
                val min = ms / 60000
                val sec = (ms % 60000) / 1000

                binding.tvTimer.text = String.format("%02d:%02d", min, sec)
                val progress = ((pomodoroDuration - ms).toFloat() / pomodoroDuration * 100).toInt()
                binding.progressCircular.progress = progress
            }

            override fun onFinish() {
                binding.tvTimer.text = "00:00"
                binding.progressCircular.progress = 100

                // Guardar en SQLite
                PomodoroRepository(this@MainActivity).addPomodoro()

                showNotification()

                // Empezar descanso
                startBreak()
            }
        }

        countDownTimer?.start()
    }

    // ------------------------------
    //           DESCANSO
    // ------------------------------

    private fun loadMotivationQuote() {
        val call = RetrofitClient.instance.getRandomQuote()

        call.enqueue(object : Callback<List<MotivationQuote>> {
            override fun onResponse(
                call: Call<List<MotivationQuote>>,
                response: Response<List<MotivationQuote>>
            ) {
                if (response.isSuccessful) {
                    val quoteList = response.body()

                    if (!quoteList.isNullOrEmpty()) {
                        val quote = quoteList[0]
                        binding.tvQuote.text = "\"${quote.q}\" \n- ${quote.a}"
                    } else {
                        binding.tvQuote.text = "No se recibió frase"
                    }
                } else {
                    binding.tvQuote.text = "Error en la respuesta de la API"
                }
            }

            override fun onFailure(call: Call<List<MotivationQuote>>, t: Throwable) {
                binding.tvQuote.text = "No se pudo conectar a la API"
            }
        })
    }

    private fun startBreak() {

        isBreak = true
        countDownTimer?.cancel()

        // Cambiar drawable a modo descanso
        binding.progressCircular.progressDrawable =
            ContextCompat.getDrawable(this, R.drawable.circle_progress_break)

        loadMotivationQuote()

        countDownTimer = object : CountDownTimer(breakDuration, 1000) {

            override fun onTick(ms: Long) {
                val min = ms / 60000
                val sec = (ms % 60000) / 1000

                binding.tvTimer.text = "Break: ${String.format("%02d:%02d", min, sec)}"
                val progress = ((breakDuration - ms).toFloat() / breakDuration * 100).toInt()
                binding.progressCircular.progress = progress
            }

            override fun onFinish() {
                binding.tvTimer.text = "Descanso terminado"
                binding.progressCircular.progress = 100
            }
        }

        countDownTimer?.start()
    }

    private fun stopPomodoro() {
        countDownTimer?.cancel()
    }

    private fun showNotification() {
        val channelId = "pomodoro_done"
        val manager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            "Pomodoro",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Pomodoro completado")
            .setContentText("Toma un descanso")

        manager.notify(1001, builder.build())
    }
}