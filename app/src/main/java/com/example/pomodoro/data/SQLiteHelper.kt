package com.example.pomodoro.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper(context: Context)
    : SQLiteOpenHelper(context, "pomodoro.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE pomodoros (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dayOfWeek TEXT NOT NULL,
                date TEXT NOT NULL,
                durationMinutes INTEGER NOT NULL
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS pomodoros")
        onCreate(db)
    }
}
