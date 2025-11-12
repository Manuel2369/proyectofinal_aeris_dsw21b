package com.example.app_clima_aeris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.app_clima_aeris.ui.WeatherScreen
import com.example.app_clima_aeris.ui.theme.App_Clima_AerisTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App_Clima_AerisTheme {
                WeatherScreen()
            }
        }
    }
}