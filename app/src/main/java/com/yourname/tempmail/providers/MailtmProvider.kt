package com.yourname.tempmail.providers

import com.yourname.tempmail.domain.AttachmentRef
import com.yourname.tempmail.domain.Contact
import com.yourname.tempmail.domain.EmailAddress
import com.yourname.tempmail.domain.EmailMessage
import com.yourname.tempmail.domain.Mailbox
import com.yourname.tempmail.domain.ProviderCapabilities
import com.yourname.tempmail.domain.ProviderName
import com.yourname.tempmail.domain.ProviderResult
import com.yourname.tempmail.network.MailTmAccountRequest
import com.yourname.tempmail.network.MailTmAddress
import com.yourname.tempmail.network.MailTmApi
import com.yourname.tempmail.network.MailTmSeenRequest
import com.yourname.tempmail.network.MailTmTokenRequest
import com.yourname.tempmail.network.NetworkClient
import com.yourname.tempmail.security.HtmlSanitizer
import com.yourname.tempmail.security.SecretStore
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

/**
 * mail.tm — free temporary mail via HTTPS REST + per-account JWT stored in the
 * Keystore-backed [SecretStore]. Outbound sending is NOT possible on the public
 * API, so sending is never advertised (rule #60).
 */
class MailtmProvider(
    private val secretStore: SecretStore,
    private val sanitizer: HtmlSanitizer = HtmlSanitizer(),
) : EmailProvider {

    private val api: MailTmApi = NetworkClient.retrofit(BASE_URL, NetworkClient.okHttp(BASE_URL)).create(MailTmApi::class.java)

    override val id: String = "mailtm"
    override val displayName: String = "mail.tm"
    override val capabilities: ProviderCapabilities = ProviderCapabilities.MAIL_TM

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
            if (r.isSuccessful) {
                val list = (r.body()?.domains ?: emptyList()).filter { it.isActive }.map { it.domain }
                if (list.isEmpty()) ProviderResult.Failure("NO_DOMAIN") else ProviderResult.Success(list)
            } else {
                ProviderResult.Failure("err.provider")
            }
        } catch (e: Exception) {
            ProviderResult.Failure("err.timeout", e)
        }
    }

    override suspend fun generate(randomCount: Int): ProviderResult<List<EmailAddress>> {
        val dl = when (val d = domains()) {
            is ProviderResult.Success -> d.data
            is ProviderResult.Failure -> return d
        }
        if (randomCount == 0) return ProviderResult.Success(emptyList())
        val addresses = (0 until randomCount).map { i ->
            val login = "u" + Random.nextLong(1_000_000L, 9_999_999_999L) + Random.nextInt(10, 999)
            EmailAddress(login.toString(), dl[i % dl.size])
        }
        return ProviderResult.Success(addresses)
    }

    override suspend fun createMailbox(address: EmailAddress): ProviderResult<Unit> {
        val existingToken = secretStore.read(tokenKey(address))
        if (existingToken.isNotBlank()) {
            val me = api.me("Bearer $existingToken")
            if (me.isSuccessful) return ProviderResult.Success(Unit)
        }
        return try {
            val password = "tmp-" + Random.nextLong(1_000_000L, Long.MAX_VALUE) + "-" + Random.nextInt(100, 999)
            val created = api.createAccount(MailTmAccountRequest(address.full, password))
            if (!created.isSuccessful) return ProviderResult.Failure("ADDRESS_TAKEN")
            val tokenResp = api.token(MailTmTokenRequest(address.full, password))
            val token = tokenResp.body()?.token
            if (token.isNullOrBlank()) return ProviderResult.Failure("NO_TOKEN")
            secretStore.put(pwdKey(address), password)
            secretStore.put(tokenKey(address), token)
            ProviderResult.Success(Unit)
        } catch (e: Exception) {
            ProviderResult.Failure("err.timeout", e)
        }
    }

    override suspend fun listMessages(mailbox: Mailbox): ProviderResult<List<MessageThumb>> {
        val token = tokenFor(mailbox.email)
            ?: return ProviderResult.Failure("err.no_auth")
        return try {
            val r = api.messages("Bearer $token")
            if (!r.isSuccessful) return ProviderResult.Failure("err.provider")
            val list = r.body()?.messages ?: emptyList()
            ProviderResult.Success(list.map { m ->
                MessageThumb(
                    providerRawId = m.id,
                    from = m.from?.let { toContact(it) } ?: Contact(),
                    subject = m.subject,
                    preview = m.intro,
                    date = parseDate(m.createdAt),
                    hasAttachments = m.hasAttachments,
                )
            })
        } catch (e: Exception) {
            ProviderResult.Failure("err.timeout", e)
        }
    }

    override suspend fun readMessage(mailbox: Mailbox, messageId: String): ProviderResult<EmailMessage> {
        val token = tokenFor(mailbox.email)
            ?: return ProviderResult.Failure("err.no_auth")
        return try {
            val r = api.read("Bearer $token", messageId)
            if (!r.isSuccessful) return ProviderResult.Failure("err.provider")
            val m = r.body() ?: return ProviderResult.Failure("err.provider")
            ProviderResult.Success(
                EmailMessage(
                    providerRawId = m.id,
                    mailboxId = mailbox.id.toString(),
                    providerId = id,
                    from = m.from?.let { toContact(it) } ?: Contact(),
                    to = m.to.map { toContact(it) },
                    cc = m.cc.map { toContact(it) },
                    bcc = m.bcc.map { toContact(it) },
                    subject = m.subject,
                    preview = m.intro,
                    bodyText = m.text,
                    bodyHtml = m.html?.firstOrNull()?.let { sanitizer.sanitize(it) },
                    date = parseDate(m.createdAt),
                    seen = m.seen,
                    attachments = m.attachments.map { a ->
                        AttachmentRef(
                            providerMessageId = m.id,
                            filename = a.filename,
                            sizeBytes = a.size,
                            contentDisposition = a.disposition,
                            url = a.downloadUrl,
                            provider = id,
                        )
                    },
                )
            )
        } catch (e: Exception) {
            ProviderResult.Failure("err.timeout", e)
        }
    }

    override suspend fun markSeen(mailbox: Mailbox, messageId: String, seen: Boolean): ProviderResult<Unit> {
        val token = tokenFor(mailbox.email)
            ?: return ProviderResult.Failure("err.no_auth")
        return try {
            val r = api.markSeen("Bearer $token", messageId, MailTmSeenRequest(seen))
            if (r.isSuccessful) ProviderResult.Success(Unit) else ProviderResult.Failure("err.provider")
        } catch (e: Exception) {
            ProviderResult.Failure("err.timeout", e)
        }
    }

    override suspend fun attachmentBytes(
        mailbox: Mailbox,
        messageId: String,
        attachment: AttachmentRef,
    ): ProviderResult<ByteArray> {
        val token = tokenFor(mailbox.email)
            ?: return ProviderResult.Failure("err.no_auth")
        val attachmentId = attachment.url?.substringAfterLast("/")
            ?: return ProviderResult.Failure("err.provider")
        return try {
            val r = api.attachment("Bearer $token", messageId, attachmentId)
            if (r.isSuccessful) {
                val bytes = r.body()?.bytes() ?: return ProviderResult.Failure("err.provider")
                ProviderResult.Success(bytes)
            } else {
                ProviderResult.Failure("err.provider")
            }
        } catch (e: Exception) {
            ProviderResult.Failure("err.timeout", e)
        }
    }

    override fun messageEvents(mailbox: Mailbox): Flow<String>? = null

    private fun tokenFor(address: EmailAddress): String? =
        secretStore.read(tokenKey(address)).takeIf { it.isNotBlank() }

    private fun tokenKey(address: EmailAddress) = "mailtm.token.${address.full}"
    private fun pwdKey(address: EmailAddress) = "mailtm.pwd.${address.full}"

    private fun toContact(a: MailTmAddress): Contact = Contact(a.name.orEmpty(), a.address.orEmpty())

    private fun parseDate(raw: String): Long = try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(raw)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    companion object {
        private const val BASE_URL = "https://api.mail.tm/"
    }
}