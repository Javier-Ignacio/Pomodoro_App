package com.example.pomodoro.model

data class SessionRecord(
    val durationMinutes: Int,
    val timestamp: Long // System.currentTimeMillis()
)
