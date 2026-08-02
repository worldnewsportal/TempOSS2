package com.yourname.tempmail.ui.inbox

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourname.tempmail.R
import com.yourname.tempmail.di.AppContainer
import com.yourname.tempmail.security.UrlValidator
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/**
 * Message reader. Untrusted HTML is sanitized with jsoup, and any link the user
 * taps is passed through [UrlValidator] (only http/https/mailto/tel), so remote
 * mail HTML can never execute scripts inside the app (rules #46–#48).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    container: AppContainer,
    mailboxId: Long,
    messageId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val mailboxRepo = container.mailboxes
    val messagesRepo = container.messages
    val scope = rememberCoroutineScope()

    val mailboxes by mailboxRepo.observeAllIncludingExpired().collectAsStateWithLifecycle(initialValue = emptyList())
    val mailbox = mailboxes.firstOrNull { it.id == mailboxId }
    val msg by messagesRepo.observeOne(messageId).collectAsStateWithLifecycle(initialValue = null)

    var html by remember(messageId) { mutableStateOf<String?>(null) }
    var loadingBody by remember(messageId) { mutableStateOf(false) }

    LaunchedEffect(mailbox?.id, messageId, msg?.bodyHtml, msg?.bodyText) {
        val current = msg
        if (current != null && current.bodyHtml == null && current.bodyText == null && mailbox != null) {
            loadingBody = true
            val fetched = messagesRepo.fetchFull(mailbox, messageId)
            html = fetched?.bodyHtml?.let(container.htmlSanitizer::sanitize) ?: fetched?.bodyText
            loadingBody = false
        } else {
            html = current?.bodyHtml?.let(container.htmlSanitizer::sanitize) ?: current?.bodyText
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(msg?.subject?.ifBlank { stringResource(R.string.no_subject) } ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.close))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val current = msg
                        if (current != null) {
                            scope.launch { messagesRepo.markStarred(current.id, !current.starred) }
                        }
                    }) {
                        Icon(
                            if (msg?.starred == true) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = stringResource(R.string.mark_read),
                        )
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
                .padding(16.dp),
        ) {
            if (msg != null) {
                Text(
                    if (msg!!.fromName.isBlank()) msg!!.fromAddress else msg!!.fromName,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    msg!!.fromAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(msg!!.date)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                when {
                    loadingBody -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.width(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.loading))
                    }
                    html != null -> SafeHtmlBody(html!!, context)
                    else -> Text(
                        stringResource(R.string.empty),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                Text(stringResource(R.string.empty))
            }
        }
    }
}

@Composable
private fun SafeHtmlBody(html: String, context: Context) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                isVerticalScrollBarEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                        val safe = UrlValidator.safeToOpen(url)
                        if (safe != null) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(safe)))
                        }
                        return true
                    }
                }
                loadData(html, "text/html", "utf-8", null)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp),
    )
}