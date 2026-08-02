package com.yourname.tempmail.providers

import com.yourname.tempmail.domain.AttachmentRef
import com.yourname.tempmail.domain.Contact
import com.yourname.tempmail.domain.EmailAddress
import com.yourname.tempmail.domain.EmailMessage
import com.yourname.tempmail.domain.Mailbox
import com.yourname.tempmail.domain.ProviderCapabilities
import com.yourname.tempmail.domain.ProviderName
import com.yourname.tempmail.domain.ProviderResult
import com.yourname.tempmail.network.NetworkClient
import com.yourname.tempmail.network.OneSecAttachment
import com.yourname.tempmail.network.OneSecmailApi
import com.yourname.tempmail.security.HtmlSanitizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 1secmail — free, auth-less temporary e-mail via an HTTPS REST API.
 *
 * Real-time events use its WebSocket. Outbound send is NOT supported by the
 * service, so the app will never advertise sending for this provider (rule #60).
 */
class OneSecmailProvider(
    private val okHttp: OkHttpClient = NetworkClient.okHttp(BASE_URL),
    private val sanitizer: HtmlSanitizer = HtmlSanitizer(),
) : EmailProvider {

    private val client = okHttp.newBuilder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .build()
    private val api: OneSecmailApi = NetworkClient.retrofit(BASE_URL, okHttp).create(OneSecmailApi::class.java)

    override val id: String = "onesecmail"
    override val displayName: String = "1secmail"
    override val capabilities: ProviderCapabilities = ProviderCapabilities.ONESECMAIL

    override suspend fun health(): ProviderHealth {
        val start = System.currentTimeMillis()
        return try {
            val r = api.domains()
            ProviderHealth(
                status = if (r.isSuccessful) ProviderName.UP else ProviderName.DEGRADED,
                latencyMs = System.currentTimeMillis() - start,
            )
        } catch (e: Exception) {
            ProviderHealth(ProviderName.DOWN, message = e.message)
        }
    }

    override suspend fun domains(): ProviderResult<List<String>> {
        return try {
            val r = api.domains()
            if (r.isSuccessful) ProviderResult.Success(r.body() ?: emptyList())
            else ProviderResult.Failure("err.provider")
        } catch (e: Exception) {
            ProviderResult.Failure("err.timeout", e)
        }
    }

    override suspend fun generate(randomCount: Int): ProviderResult<List<EmailAddress>> {
        return try {
            val r = api.randomMailbox(randomCount.coerceIn(1, 20))
            if (!r.isSuccessful) return ProviderResult.Failure("err.provider")
            val addresses = (r.body() ?: emptyList()).mapNotNull { EmailAddress.from(it) }
            if (addresses.isEmpty()) ProviderResult.Failure("err.provider")
            else ProviderResult.Success(addresses)
        } catch (e: Exception) {
            ProviderResult.Failure("err.timeout", e)
        }
    }

    override suspend fun createMailbox(address: EmailAddress): ProviderResult<Unit> {
        // 1secmail accepts any address lazily; there is nothing to explicitly reserve.
        return ProviderResult.Success(Unit)
    }

    override suspend fun listMessages(mailbox: Mailbox): ProviderResult<List<MessageThumb>> {
        return try {
            val r = api.messages(mailbox.email.login, mailbox.email.domain)
            if (!r.isSuccessful) return ProviderResult.Failure("err.provider")
            val list = r.body() ?: emptyList()
            ProviderResult.Success(list.map { m ->
                MessageThumb(
                    providerRawId = m.id.toString(),
                    from = Contact(address = m.from),
                    subject = m.subject,
                    preview = m.subject,
                    date = parseDate(m.date),
                )
            })
        } catch (e: Exception) {
            ProviderResult.Failure("err.timeout", e)
        }
    }

    override suspend fun readMessage(mailbox: Mailbox, messageId: String): ProviderResult<EmailMessage> {
        val id = messageId.toIntOrNull() ?: return ProviderResult.Failure("err.provider")
        return try {
            val r = api.read(mailbox.email.login, mailbox.email.domain, id)
            if (!r.isSuccessful) return ProviderResult.Failure("err.provider")
            val m = r.body() ?: return ProviderResult.Failure("err.provider")
            ProviderResult.Success(
                EmailMessage(
                    providerRawId = m.id.toString(),
                    mailboxId = mailbox.id.toString(),
                    providerId = this.id,
                    from = parseFrom(m.from),
                    subject = m.subject,
                    preview = m.subject,
                    bodyText = m.textBody ?: m.body,
                    bodyHtml = sanitizer.sanitize(m.htmlBody),
                    date = parseDate(m.date),
                    attachments = m.attachments.map { toAttachment(it, m.id.toString()) },
                )
            )
        } catch (e: Exception) {
            ProviderResult.Failure("err.timeout", e)
        }
    }

    override suspend fun markSeen(mailbox: Mailbox, messageId: String, seen: Boolean): ProviderResult<Unit> {
        // 1secmail has no upstream seen flag; the app keeps it locally only.
        return ProviderResult.Success(Unit)
    }

    override suspend fun attachmentBytes(
        mailbox: Mailbox,
        messageId: String,
        attachment: AttachmentRef,
    ): ProviderResult<ByteArray> {
        val id = messageId.toIntOrNull() ?: return ProviderResult.Failure("err.provider")
        return try {
            val r = api.download(mailbox.email.login, mailbox.email.domain, id, attachment.filename)
            if (!r.isSuccessful) return ProviderResult.Failure("err.provider")
            val bytes = r.body()?.bytes() ?: return ProviderResult.Failure("err.provider")
            ProviderResult.Success(bytes)
        } catch (e: Exception) {
            ProviderResult.Failure("err.timeout", e)
        }
    }

    override fun messageEvents(mailbox: Mailbox): Flow<String>? = flow {
        val req = Request.Builder()
            .url("wss://www.1secmail.com/ws/?login=${mailbox.email.login}&domain=${mailbox.email.domain}")
            .build()
        val event = awaitOneWsMessage(client, req)
        if (event.isNotEmpty()) emit(event)
    }.flowOn(Dispatchers.IO)

    private suspend fun awaitOneWsMessage(client: OkHttpClient, request: Request): String =
        suspendCancellableCoroutine { cont ->
            val listener = object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    if (cont.isActive) cont.resume(text)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    if (cont.isActive) cont.resumeWithException(t)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (cont.isActive) cont.resume("")
                }
            }
            val ws = client.newWebSocket(request, listener)
            cont.invokeOnCancellation { ws.cancel() }
        }

    private fun toAttachment(a: OneSecAttachment, messageId: String) = AttachmentRef(
        providerMessageId = messageId,
        filename = a.filename,
        sizeBytes = a.size,
        contentDisposition = null,
        provider = this.id,
    )

    private fun parseFrom(raw: String): Contact {
        val text = raw.trim()
        val angle = text.indexOf('<')
        return if (angle > 0) {
            Contact(name = text.substring(0, angle).trim(), address = text.substring(angle + 1, text.indexOf('>')).trim())
        } else {
            Contact(address = text)
        }
    }

    private fun parseDate(raw: String): Long = try {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .parse(raw)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    companion object {
        private const val BASE_URL = "https://www.1secmail.com/"
    }
}