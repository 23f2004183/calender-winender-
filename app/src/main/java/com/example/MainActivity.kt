package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.ui.GoalCalendarApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CalendarViewModel

class MainActivity : ComponentActivity() {
    
    // Instantiate our unified state-controller ViewModel using the custom Room Repository Factory
    private val viewModel: CalendarViewModel by viewModels {
        CalendarViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup full bleed edge-to-edge content presentation
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Set padding content contextually, but inner panels manage safety grids with custom scroll targets
                    GoalCalendarApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
