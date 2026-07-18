package dev.mizzenmast.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import dev.mizzenmast.calculator.ui.theme.CalculatorTheme

private const val PREFS_NAME = "smart_calc"
private const val KEY_ONBOARDED = "onboarded"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                val context = LocalContext.current
                val prefs = remember { context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }
                var showOnboarding by remember { mutableStateOf(!prefs.getBoolean(KEY_ONBOARDED, false)) }

                if (showOnboarding) {
                    OnboardingScreen(onContinue = {
                        prefs.edit { putBoolean(KEY_ONBOARDED, true) }
                        showOnboarding = false
                    })
                } else {
                    App()
                }
            }
        }
    }
}