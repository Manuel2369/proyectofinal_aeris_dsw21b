package com.example.proyecto_final_app_clima_aeris

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import coil.load
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var tvLocation: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvDetails: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivIcon: ImageView
    private lateinit var btnRefresh: ImageButton

    private val apiKey = "YOUR_API_KEY" // 🔑 Reemplaza con tu clave de OpenWeatherMap

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            getLastLocationAndUpdateWeather()
        } else {
            tvStatus.text = "Permiso de ubicación no concedido."
            Toast.makeText(this, "Permiso de ubicación necesario", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLocation = findViewById(R.id.tvLocation)
        tvDescription = findViewById(R.id.tvDescription)
        tvTemp = findViewById(R.id.tvTemp)
        tvDetails = findViewById(R.id.tvDetails)
        tvStatus = findViewById(R.id.tvStatus)
        ivIcon = findViewById(R.id.ivIcon)
        btnRefresh = findViewById(R.id.btnRefresh)

        btnRefresh.setOnClickListener {
            checkPermissionsAndFetch()
        }

        checkPermissionsAndFetch()
    }

    private fun checkPermissionsAndFetch() {
        when {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocationAndUpdateWeather()
            }
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocationAndUpdateWeather()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getLastLocationAndUpdateWeather() {
        tvStatus.text = "Obteniendo ubicación..."
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    tvLocation.text = "Lat: %.4f, Lon: %.4f".format(lat, lon)
                    fetchWeather(lat, lon)
                } else {
                    tvStatus.text = "No se pudo obtener la ubicación. Activa el GPS."
                }
            }.addOnFailureListener { ex ->
                tvStatus.text = "Error al obtener ubicación: ${ex.message}"
            }
        } catch (ex: SecurityException) {
            tvStatus.text = "Permiso de ubicación no concedido."
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        tvStatus.text = "Obteniendo clima..."
        val api = RetrofitClient.instance.create(WeatherApi::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getCurrentWeather(lat, lon, "metric", "es", apiKey)
                withContext(Dispatchers.Main) {
                    updateUI(response)
                    tvStatus.text = ""
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvStatus.text = "Error al obtener clima: ${e.localizedMessage}"
                }
            }
        }
    }

    private fun updateUI(resp: WeatherResponse) {
        tvLocation.text = resp.name
        val desc = resp.weather.firstOrNull()?.description ?: "Sin descripción"
        tvDescription.text = desc.replaceFirstChar { it.uppercase() }
        val temp = resp.main.temp
        tvTemp.text = "%.0f °C".format(temp)
        tvDetails.text = "Humedad: ${resp.main.humidity}%   Viento: ${resp.wind.speed} m/s"

        val iconCode = resp.weather.firstOrNull()?.icon
        if (iconCode != null) {
            val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
            ivIcon.load(iconUrl) {
                crossfade(true)
            }
        } else {
            ivIcon.setImageDrawable(null)
        }
    }
}