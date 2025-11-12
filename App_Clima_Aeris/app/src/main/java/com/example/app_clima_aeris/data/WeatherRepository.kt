package com.example.app_clima_aeris.data


import com.example.app_clima_aeris.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService
) {
    suspend fun getWeather(lat: Double, lon: Double): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        try {
            // Usa la variable generada por Gradle
            val response = apiService.getWeatherData(lat, lon, BuildConfig.AERIS_API_KEY)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red. Verifica tu internet.", e))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.message}", e))
        }
    }
    // NUEVA FUNCIÓN
    suspend fun getCityCoordinates(cityName: String): Result<GeoLocation> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCoordinates(cityName, 1, BuildConfig.AERIS_API_KEY)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                // Devolvemos el primer resultado encontrado
                Result.success(response.body()!!.first())
            } else {
                Result.failure(Exception("Ciudad no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}