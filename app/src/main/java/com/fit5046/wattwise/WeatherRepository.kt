package com.fit5046.wattwise

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.fit5046.wattwise.BuildConfig

class WeatherRepository {

    companion object {
        val API_KEY = BuildConfig.WEATHER_API_KEY  // replace with your key
        const val BASE_URL = "https://api.openweathermap.org/"
    }

    private val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    suspend fun getWeather(city: String): WeatherUiState {
        return try {
            val response = api.getCurrentWeather(
                city   = city,
                apiKey = API_KEY
            )
            WeatherUiState(
                isLoading    = false,
                city         = response.name,
                outdoorTempC = response.main.temp,
                feelsLikeC   = response.main.feelsLike,
                humidity     = response.main.humidity,
                condition    = response.weather.firstOrNull()?.main ?: "",
                description  = response.weather.firstOrNull()?.description ?: "",
                windSpeedMs  = response.wind.speed
            )
        } catch (e: Exception) {
            WeatherUiState(
                isLoading = false,
                error     = "Weather unavailable: ${e.localizedMessage}"
            )
        }
    }
}