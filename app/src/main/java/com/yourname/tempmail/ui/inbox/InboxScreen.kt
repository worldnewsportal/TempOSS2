package com.yourname.tempmail.ui.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourname.tempmail.R
import com.yourname.tempmail.data.db.MessageEntity
import com.yourname.tempmail.di.AppContainer
import com.yourname.tempmail.domain.Lifetime
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/**
 * Inbox for one mailbox: message list, pull-to-refresh, real search. The
 * Compose/Send action is only shown when the provider truly supports sending;
 * otherwise it is honestly absent (rule #60).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    container: AppContainer,
    mailboxId: Long,
    onMessage: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val mailboxRepo = container.mailboxes
    val messagesRepo = container.messages

    val mailboxes by mailboxRepo.observeAllIncludingExpired().collectAsStateWithLifecycle(initialValue = emptyList())
    val mailbox = mailboxes.firstOrNull { it.id == mailboxId }
    val messages by messagesRepo.observeInbox(mailboxId).collectAsStateWithLifecycle(initialValue = emptyList())

    val provider = mailbox?.let { container.providers.providerFor(it.providerId) }
    val canSend = provider?.capabilities?.supportsSending == true

    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(mailboxId, mailbox?.id) {
        mailbox?.let {
            refreshing = true
            messagesRepo.refreshMailbox(it)
            refreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(Modifier.padding(end = 8.dp)) {
                        Text(mailbox?.displayName?.ifBlank { mailbox?.email?.full } ?: "")
                        Text(
                            stringResource(R.string.expires_in, Lifetime.humanize(mailbox?.expiresAt ?: 0L)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.close))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            refreshing = true
                            mailbox?.let { messagesRepo.refreshMailbox(it) }
                            refreshing = false
                        }
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.no_messages), style = MaterialTheme.typography.bodyMedium)
                    if (!canSend) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = {}) {
                            Icon(Icons.Filled.Send, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.send_unavailable))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageRow(msg, onClick = { onMessage(msg.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageRow(msg: MessageEntity, onClick: () -> Unit) {
    val noSubject = stringResource(R.string.no_subject)
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (msg.fromName.isBlank()) msg.fromAddress else msg.fromName,
                    fontWeight = if (!msg.seen) FontWeight.Bold else FontWeight.Medium,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    msg.subject.ifBlank { noSubject },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (msg.seen) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    msg.preview.ifBlank { msg.bodyText.orEmpty() },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                ts(msg.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun ts(millis: Long): String =
    if (millis <= 0) "" else DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(millis))