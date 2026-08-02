package com.yourname.tempmail.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourname.tempmail.R
import com.yourname.tempmail.di.AppContainer
import com.yourname.tempmail.ui.TempmailAppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    appViewModel: TempmailAppViewModel,
    onBack: () -> Unit,
) {
    val settings = container.settings

    val theme by settings.theme.collectAsStateWithLifecycle(initialValue = "system")
    val notifications by settings.notifications.collectAsStateWithLifecycle(initialValue = true)
    val notifSound by settings.notifSound.collectAsStateWithLifecycle(initialValue = true)
    val notifVibrate by settings.notifVibrate.collectAsStateWithLifecycle(initialValue = true)
    val adsEnabled by settings.adsEnabled.collectAsStateWithLifecycle(initialValue = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.close))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            ThemeDropdown(
                themeKey = theme,
                onSelect = { appViewModel.setTheme(it) },
            )

            Labeled(
                title = stringResource(R.string.notif_enabled),
                checked = notifications,
                onCheckedChange = { settings.setNotifications(it) },
            )
            Labeled(
                title = stringResource(R.string.notif_sound),
                checked = notifSound,
                enabled = notifications,
                onCheckedChange = { settings.setNotifSound(it) },
            )
            Labeled(
                title = stringResource(R.string.notif_vibrate),
                checked = notifVibrate,
                enabled = notifications,
                onCheckedChange = { settings.setNotifVibrate(it) },
            )
            Labeled(
                title = stringResource(R.string.ads_opt_in_title),
                checked = adsEnabled,
                onCheckedChange = { settings.setAdsEnabled(it) },
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.provider_limits),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.provider_limits_msg),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeDropdown(themeKey: String, onSelect: (String) -> Unit) {
    val options = ThemeEntry.entries
    var expanded by remember { mutableStateOf(false) }
    val selected = options.firstOrNull { it.key == themeKey } ?: ThemeEntry.SYSTEM

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(selected.labelRes),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.theme)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    onClick = {
                        expanded = false
                        onSelect(option.key)
                    },
                )
            }
        }
    }
}

private enum class ThemeEntry(val key: String, val labelRes: Int) {
    SYSTEM(ThemeName.system, R.string.theme_system),
    LIGHT(ThemeName.light, R.string.theme_light),
    DARK(ThemeName.dark, R.string.theme_dark),
    OLED(ThemeName.oled, R.string.theme_oled),
}

object ThemeName {
    const val system = "system"
    const val light = "light"
    const val dark = "dark"
    const val oled = "oled"
}

@Composable
private fun Labeled(
    title: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
        )
    }
}