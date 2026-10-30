package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PetroLogRepository
import com.example.data.local.PetroLogDatabase
import com.example.ui.PetroLogViewModel
import com.example.ui.PetroLogViewModelFactory
import com.example.ui.screens.PetroLogAppScreen
import com.example.ui.theme.PetroLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.WHITE,
                Color.LTGRAY
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.WHITE,
                Color.LTGRAY
            )
        )

        val database = PetroLogDatabase.getInstance(applicationContext)
        val repository = PetroLogRepository(database.petroLogDao())
        val viewModel = ViewModelProvider(
            this,
            PetroLogViewModelFactory(repository)
        )[PetroLogViewModel::class.java]

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            PetroLogTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PetroLogAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
