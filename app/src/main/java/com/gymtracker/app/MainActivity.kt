package com.gymtracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.gymtracker.app.ui.navigation.GymNavGraph
import com.gymtracker.app.ui.theme.GymProgressTrackerTheme
import com.gymtracker.app.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as GymTrackerApplication
        val factory = ViewModelFactory(app.repository)

        setContent {
            val settings by app.repository.getSettings().collectAsState(initial = null)

            // Wait for the first settings read before deciding onboarding vs. home,
            // so we never briefly flash the onboarding flow for returning users.
            if (settings == null) return@setContent

            GymProgressTrackerTheme(themeMode = settings!!.themeMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GymNavGraph(factory = factory, hasCompletedOnboarding = settings!!.hasCompletedOnboarding)
                }
            }
        }
    }
}
