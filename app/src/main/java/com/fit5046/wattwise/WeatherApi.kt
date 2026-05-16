package com.fit5046.wattwise

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q")     city:   String,    // passed dynamically from ViewModel
        @Query("appid") apiKey: String,
        @Query("units") units:  String = "metric"
    ): WeatherResponse
}