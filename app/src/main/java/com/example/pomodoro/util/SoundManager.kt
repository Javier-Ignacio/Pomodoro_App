package com.example.pomodoro.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer

object SoundManager {
    private var player: MediaPlayer? = null

    fun playSound(context: Context, soundRes: Int) {
        stop()

        player = MediaPlayer.create(context, soundRes).apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setOnCompletionListener {
                it.release()
                player = null
            }
            start()
        }
    }

    fun stop() {
        try {
            player?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            player = null
        }
    }
}