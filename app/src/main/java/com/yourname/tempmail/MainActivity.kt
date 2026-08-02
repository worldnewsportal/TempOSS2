package com.yourname.tempmail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourname.tempmail.ui.TempmailAppViewModel
import com.yourname.tempmail.ui.TempmailAppViewModelFactory
import com.yourname.tempmail.ui.navigation.TempmailNavHost
import com.yourname.tempmail.ui.settings.SettingsUiState
import com.yourname.tempmail.ui.theme.TempmailTheme
import com.yourname.tempmail.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    private val container by lazy { (application as TempmailApp).container }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: TempmailAppViewModel = viewModel(factory = TempmailAppViewModelFactory(container))
            val themeKey by vm.themeMode.collectAsStateWithLifecycle(initialValue = SettingsUiState.themeDefaultKey)
            TempmailTheme(
                themeMode = ThemeMode.entries.firstOrNull { it.key == themeKey } ?: ThemeMode.SYSTEM,
            ) {
                TempmailNavHost(
                    container = container,
                    appViewModel = vm,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        container.ads.onActivityResume(this)
    }

    override fun onPause() {
        super.onPause()
        container.ads.onActivityPause()
    }

    override fun onDestroy() {
        container.ads.onActivityDestroy()
        super.onDestroy()
    }
}