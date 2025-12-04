package com.example.pomodoro.storage

import android.content.Context
import com.example.pomodoro.model.SessionRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object HistoryManager {
    private const val PREFS_NAME = "pomodoro_history"
    private const val KEY_HISTORY = "session_list"
    private val gson = Gson()

    fun saveSession(context: Context, minutes: Int) {
        val list = getHistory(context).toMutableList()
        list.add(SessionRecord(minutes, System.currentTimeMillis()))
        val json = gson.toJson(list)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HISTORY, json)
            .apply()
    }

    fun getHistory(context: Context): List<SessionRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, "[]")
        val type = object : TypeToken<List<SessionRecord>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearHistory(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_HISTORY).apply()
    }
}
