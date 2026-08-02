package com.yourname.tempmail.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourname.tempmail.R
import com.yourname.tempmail.di.AppContainer
import com.yourname.tempmail.domain.Lifetime
import com.yourname.tempmail.domain.Mailbox
import kotlinx.coroutines.flow.Flow

/**
 * Handbox mailbox manager: create/keep many disposable addresses at once
 * (rule #3). Banner ad, when consented & configured, sits in its own slot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    container: AppContainer,
    onOpenMailbox: (Mailbox) -> Unit,
    onCreate: () -> Unit,
    onSettings: () -> Unit,
) {
    val context = LocalContext.current

    val mailboxesFlow: Flow<List<Mailbox>> = remember(container.mailboxes) {
        container.mailboxes.observeActive()
    }
    val mailboxes by mailboxesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val adsEnabled by container.settings.adsEnabled.collectAsStateWithLifecycle(initialValue = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_mailboxes)) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (adsEnabled) {
                BannerSlot(container)
            }
            if (mailboxes.isEmpty()) {
                EmptyMailboxesBox(onCreate = onCreate)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(mailboxes, key = { it.id }) { mailbox ->
                        MailboxCard(
                            mailbox = mailbox,
                            onClick = { onOpenMailbox(mailbox) },
                            onCopy = { copyMailbox(context, mailbox) },
                        )
                    }
                }
            }
        }
    }
}

fun copyMailbox(context: Context, mailbox: Mailbox) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("address", mailbox.email.full))
    Toast.makeText(context, R.string.email_copied, Toast.LENGTH_SHORT).show()
}

@Composable
private fun BannerSlot(container: AppContainer) {
    val activity = LocalContext.current as? android.app.Activity ?: return
    val view = remember(activity) { container.ads.banner.load(activity) }
    if (view != null) {
        AndroidView(
            factory = { view },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp, max = 120.dp),
        )
    }
}

@Composable
private fun EmptyMailboxesBox(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.no_messages), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCreate) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.generate_email))
        }
    }
}

@Composable
private fun MailboxCard(mailbox: Mailbox, onClick: () -> Unit, onCopy: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (mailbox.favorite) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = stringResource(R.string.favorite),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(mailbox.email.full, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.expires_in, remember(mailbox.expiresAt) {
                        Lifetime.humanize(mailbox.expiresAt)
                    }),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onCopy) {
                Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.copy))
            }
        }
    }
}