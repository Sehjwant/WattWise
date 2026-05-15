package com.fit5046.wattwise

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val name: String,
    val main: WeatherMain,
    val weather: List<WeatherCondition>,
    val wind: WeatherWind
)

data class WeatherMain(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    val humidity: Int
)

data class WeatherCondition(
    val main: String,
    val description: String
)

data class WeatherWind(
    val speed: Double
)

data class WeatherUiState(
    val isLoading: Boolean   = true,
    val city: String         = "Melbourne",
    val outdoorTempC: Double = 0.0,
    val feelsLikeC: Double   = 0.0,
    val humidity: Int        = 0,
    val condition: String    = "",
    val description: String  = "",
    val windSpeedMs: Double  = 0.0,
    val error: String?       = null
) {
    val energyImpact: String get() = when {
        outdoorTempC > 35 -> "High AC demand expected"
        outdoorTempC > 28 -> "Elevated cooling load"
        outdoorTempC < 10 -> "High heating demand expected"
        outdoorTempC < 15 -> "Elevated heating load"
        else              -> "Moderate energy demand"
    }
}