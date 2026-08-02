package com.yourname.tempmail.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** mail.tm public REST API (HTTPS only, no key needed; JWT auth after account creation). */
interface MailTmApi {

@GET("domains")
    suspend fun domains(): Response<HydraDomains>

    @POST("accounts")
    suspend fun createAccount(@Body body: MailTmAccountRequest): Response<MailTmAccount>

    @POST("token")
    suspend fun token(@Body body: MailTmTokenRequest): Response<MailTmToken>

    @GET("me")
    suspend fun me(@Header("Authorization") bearer: String): Response<MailTmAccount>

    @GET("messages")
    suspend fun messages(@Header("Authorization") bearer: String): Response<HydraMessages>

    @GET("messages/{id}")
    suspend fun read(@Header("Authorization") bearer: String, @Path("id") id: String): Response<MailTmFullMessage>

    @PATCH("messages/{id}")
    suspend fun markSeen(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: MailTmSeenRequest,
    ): Response<MailTmFullMessage>

    @GET("messages/{id}/attachment/{attachmentId}")
    suspend fun attachment(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Path("attachmentId") attachmentId: String,
    ): Response<okhttp3.ResponseBody>
}

data class HydraDomains(
    @field:com.google.gson.annotations.SerializedName("hydra:member") val domains: List<MailTmDomain> = emptyList(),
)

data class HydraMessages(
    @field:com.google.gson.annotations.SerializedName("hydra:member") val messages: List<MailTmListedMessage> = emptyList(),
)

data class MailTmDomain(
    val domain: String = "",
    val isActive: Boolean = false,
)

data class MailTmAccountRequest(val address: String = "", val password: String = "")
data class MailTmTokenRequest(val address: String = "", val password: String = "")
data class MailTmToken(val token: String = "")
data class MailTmSeenRequest(val seen: Boolean = false)

data class MailTmAccount(
    val id: String = "",
    val address: String = "",
    val isDisabled: Boolean = false,
)

data class MailTmAddress(val name: String? = null, val address: String? = null)

data class MailTmListedMessage(
    val id: String = "",
    val accountId: String = "",
    val msgid: String = "",
    val from: MailTmAddress? = null,
    val to: List<MailTmAddress> = emptyList(),
    val subject: String = "",
    val intro: String = "",
    val seen: Boolean = false,
    val isDeleted: Boolean = false,
    val hasAttachments: Boolean = false,
    val size: Long = 0,
    val downloadUrl: String? = null,
    val createdAt: String = "",
)

data class MailTmFullMessage(
    val id: String = "",
    val accountId: String = "",
    val msgid: String = "",
    val from: MailTmAddress? = null,
    val to: List<MailTmAddress> = emptyList(),
    val cc: List<MailTmAddress> = emptyList(),
    val bcc: List<MailTmAddress> = emptyList(),
    val subject: String = "",
    val intro: String = "",
    val seen: Boolean = false,
    val isDeleted: Boolean = false,
    val hasAttachments: Boolean = false,
    val size: Long = 0,
    val downloadUrl: String? = null,
    val createdAt: String = "",
    val text: String? = null,
    val html: List<String> = emptyList(),
    val attachments: List<MailTmAttachment> = emptyList(),
    val headers: List<Map<String, String>> = emptyList(),
)

data class MailTmAttachment(
    val id: String = "",
    val filename: String = "",
    val contentType: String = "",
    val disposition: String? = null,
    val transferEncoding: String? = null,
    val related: Boolean = false,
    val size: Long = 0,
    val downloadUrl: String? = null,
)