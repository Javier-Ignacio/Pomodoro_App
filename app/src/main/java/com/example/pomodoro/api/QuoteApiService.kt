package com.example.pomodoro.api

import com.example.pomodoro.data.MotivationQuote
import retrofit2.Call
import retrofit2.http.GET

interface QuoteApiService {
    @GET("random")
    fun getRandomQuote(): Call<List<MotivationQuote>>
}