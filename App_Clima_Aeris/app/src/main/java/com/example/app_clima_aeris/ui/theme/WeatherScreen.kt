package com.example.app_clima_aeris.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.app_clima_aeris.data.CurrentWeather
import com.example.app_clima_aeris.data.DailyWeather
import com.example.app_clima_aeris.data.HourlyWeather
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // --- LÓGICA DE GPS ---
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            viewModel.loadWeatherByCoordinates(location.latitude, location.longitude)
                            viewModel.currentCityName.value = "Mi Ubicación"
                        } else {
                            Toast.makeText(context, "Ubicación no encontrada, cargando Madrid.", Toast.LENGTH_SHORT).show()
                            viewModel.loadWeatherByCoordinates(40.4168, -3.7038)
                        }
                    }
            }
        } catch (e: Exception) { }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getCurrentLocation()
        } else {
            viewModel.loadWeatherByCoordinates(40.4168, -3.7038)
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
    // ---------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3A1C71), Color(0xFF632E7B))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // CAMBIO 1: Usamos el icono "Air" (Viento) que viene con Android
                    Icon(
                        imageVector = Icons.Rounded.Air,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "AERIS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "WEATHER", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }

                // Botón de GPS
                IconButton(
                    onClick = { getCurrentLocation() },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .size(40.dp)
                ) {
                    // CAMBIO 2: Usamos "MyLocation" que es el típico icono de mira de GPS
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = "GPS",
                        tint = Color.White
                    )
                }
            }

            // CONTENIDO
            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                is WeatherUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            // BARRA DE BÚSQUEDA
                            var text by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it },
                                placeholder = { Text("Buscar ciudad...", color = Color.White.copy(0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.White
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        if (text.isNotBlank()) {
                                            viewModel.searchCity(text)
                                            focusManager.clearFocus()
                                            text = ""
                                        }
                                    }
                                ),
                                trailingIcon = {
                                    // CAMBIO 3: Icono de Lupa (Search)
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = "Buscar",
                                        tint = Color.White.copy(0.7f)
                                    )
                                }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                        item { CurrentWeatherCard(state.data.current, viewModel.currentCityName.value) }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                        item { HourlyForecast(state.data.hourly) }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                        item { DailyForecast(state.data.daily) }
                    }
                }
                is WeatherUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(current: CurrentWeather, cityName: String) {
    val frostedGlassColor = Color.White.copy(alpha = 0.1f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = frostedGlassColor)
    ) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "AHORA EN", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            Text(text = cityName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            // Aquí usamos Coil para cargar la imagen de OpenWeatherMap.
            // Si la API falla, se vería blanco, pero OpenWeatherMap es muy fiable.
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${current.weather.first().icon}@4x.png",
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            Text(text = "${current.temp.toInt()}°", color = Color.White, fontSize = 90.sp, fontWeight = FontWeight.Thin)
            Text(text = current.weather.first().description.replaceFirstChar { it.uppercase() }, color = Color.White.copy(alpha = 0.9f), fontSize = 20.sp)

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem("HUMEDAD", "${current.humidity}%")
                InfoItem("VIENTO", "${current.wind_speed.toInt()} km/h")
                InfoItem("VISIBILIDAD", "${current.visibility / 1000} km")
            }
        }
    }
}

// ... TUS OTRAS FUNCIONES (HourlyForecast, DailyForecast, etc.) SIGUEN IGUAL ...
// Asegúrate de tenerlas aquí abajo para que no de error.
@Composable
fun HourlyForecast(hourly: List<HourlyWeather>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "HOY", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(hourly.take(24)) { hour ->
                HourlyItem(hour)
            }
        }
    }
}

@Composable
fun HourlyItem(hour: HourlyWeather) {
    val frostedGlassColor = Color.White.copy(alpha = 0.1f)
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = frostedGlassColor)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "12:00", color = Color.White)
            AsyncImage(model = "https://openweathermap.org/img/wn/${hour.weather.first().icon}@2x.png", contentDescription = null, modifier = Modifier.size(40.dp))
            Text(text = "${hour.temp.toInt()}°", color = Color.White)
        }
    }
}

@Composable
fun DailyForecast(daily: List<DailyWeather>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "PRÓXIMOS DÍAS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            daily.take(7).forEach { day ->
                DailyItem(day)
            }
        }
    }
}

@Composable
fun DailyItem(day: DailyWeather) {
    val frostedGlassColor = Color.White.copy(alpha = 0.1f)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = frostedGlassColor)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = "https://openweathermap.org/img/wn/${day.weather.first().icon}@2x.png", contentDescription = null, modifier = Modifier.size(40.dp))
                Text(text = "Día", color = Color.White, modifier = Modifier.padding(start = 16.dp))
            }
            Text(text = "${day.temp.max.toInt()}° / ${day.temp.min.toInt()}°", color = Color.White)
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Divider(color: Color, thickness: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.fillMaxWidth().height(thickness).background(color))
}