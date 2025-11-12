package com.example.app_clima_aeris.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_clima_aeris.data.WeatherRepository
import com.example.app_clima_aeris.data.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    // Variable para guardar el nombre de la ciudad actual (para la UI)
    var currentCityName = mutableStateOf("Madrid")

    // Función para cargar por Coordenadas (GPS)
    fun loadWeatherByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            val result = repository.getWeather(lat, lon)

            result.onSuccess { weatherResponse ->
                _uiState.value = WeatherUiState.Success(weatherResponse)
            }.onFailure { error ->
                _uiState.value = WeatherUiState.Error(error.message ?: "Error desconocido")
            }
        }
    }

    // Función para buscar por Nombre (Buscador)
    fun searchCity(name: String) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            // 1. Primero buscamos las coordenadas de la ciudad
            val geoResult = repository.getCityCoordinates(name)

            geoResult.onSuccess { location ->
                currentCityName.value = location.name // Actualizamos el nombre en pantalla
                // 2. Con las coordenadas, pedimos el clima
                val weatherResult = repository.getWeather(location.lat, location.lon)

                weatherResult.onSuccess { weatherData ->
                    _uiState.value = WeatherUiState.Success(weatherData)
                }.onFailure {
                    _uiState.value = WeatherUiState.Error("Error al obtener clima")
                }

            }.onFailure {
                _uiState.value = WeatherUiState.Error("Ciudad no encontrada")
            }
        }
    }
}

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val data: WeatherResponse) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}