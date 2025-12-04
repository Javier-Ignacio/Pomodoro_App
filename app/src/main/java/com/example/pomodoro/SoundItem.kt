package com.example.pomodoro

data class SoundItem(
    val id: Int,
    val name: String,
    val res: Int    // recurso del sonido (R.raw.xxx)
)