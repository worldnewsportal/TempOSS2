package com.yourname.tempmail.ui.create

import androidx.compose.foundation.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yourname.tempmail.R
import com.yourname.tempmail.di.AppContainer
import com.yourname.tempmail.domain.EmailAddress
import com.yourname.tempmail.domain.ProviderResult
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val DEFAULT_PROVIDER = "onesecmail"

/**
 * "Change / Generate an email": pick a username & domain (or fully random) and
 * create a new mailbox instantly. No cooldown is enforced anywhere (rule #10).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateScreen(
    container: AppContainer,
    onDone: () -> Unit,
) {
    var customLogin by rememberSaveable { mutableStateOf("") }
    var selectedDomain by rememberSaveable { mutableStateOf("") }
    var domains by remember { mutableStateOf<List<String>>(emptyList()) }
    var loadingDomains by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        when (val r = container.providers.domainsFor(DEFAULT_PROVIDER)) {
            is ProviderResult.Success -> {
                domains = r.data
                selectedDomain = r.data.firstOrNull() ?: ""
            }
            is ProviderResult.Failure -> error = r.reason
        }
        loadingDomains = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.generate_email)) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.close))
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
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.choose_domain_hint, "1secmail"),
                style = MaterialTheme.typography.bodyMedium,
            )

            if (loadingDomains) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.width(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.loading))
                }
            } else {
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    domains.forEach { d ->
                        FilledTonalButton(onClick = { selectedDomain = d }) {
                            Text(d)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = customLogin,
                onValueChange = { customLogin = it },
                label = { Text(stringResource(R.string.custom_username)) },
                placeholder = { Text(randomLogin(6)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        val login = customLogin.trim().ifBlank { randomLogin(10) }
                        val address = EmailAddress(login, selectedDomain)
                        val result = container.mailboxes.create(
                            providerId = DEFAULT_PROVIDER,
                            address = address,
                        )
                        busy = false
                        result.onFailure { error = it.message ?: "create_failed" }
                            .onSuccess { onDone() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy && selectedDomain.isNotBlank(),
            ) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.width(20.dp))
                } else {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.generate_email))
                }
            }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.generate_failed, it),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun randomLogin(length: Int): String {
    val chars = ('a'..'z') + ('0'..'9')
    return List(length) { chars[Random.nextInt(chars.size)] }.joinToString("")
}