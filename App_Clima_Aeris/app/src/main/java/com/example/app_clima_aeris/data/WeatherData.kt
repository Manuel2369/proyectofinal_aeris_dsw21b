package com.example.app_clima_aeris.data

data class WeatherResponse(
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>
)

data class CurrentWeather(
    val temp: Double,
    val humidity: Int,
    val wind_speed: Double,
    val visibility: Int,
    val weather: List<WeatherDescription>
)

data class HourlyWeather(
    val dt: Long,
    val temp: Double,
    val weather: List<WeatherDescription>
)

data class DailyWeather(
    val dt: Long,
    val temp: DailyTemp,
    val weather: List<WeatherDescription>
)

data class DailyTemp(
    val min: Double,
    val max: Double
)

data class WeatherDescription(
    val main: String,
    val description: String,
    val icon: String
)
data class GeoLocation(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String
)