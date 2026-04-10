package com.example.bullsandcows

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bullsandcows.ui.screens.MainScreen
import com.example.bullsandcows.ui.theme.BullsAndCowsTheme
import com.example.bullsandcows.viewmodel.GameViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BullsAndCowsTheme {
                val viewModel: GameViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
