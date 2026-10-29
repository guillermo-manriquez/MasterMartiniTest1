package com.example.mastermartini

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mastermartini.navigation.AppNavHost
import com.example.mastermartini.ui.theme.MasterMartiniTheme
import com.example.mastermartini.util.AppLocale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        // Spanish UI on any device language (same technique AppCompat uses for app locales);
        // must run before anything reads resources
        applyOverrideConfiguration(AppLocale.overrideConfiguration())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MasterMartiniTheme {
                AppNavHost()
            }
        }
    }
}
