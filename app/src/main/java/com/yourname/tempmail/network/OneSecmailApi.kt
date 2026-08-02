package com.yourname.tempmail.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** 1secmail public REST API. Uses HTTPS only. */
interface OneSecmailApi {
    @GET("?action=getDomainList")
    suspend fun domains(): Response<List<String>>

    @GET("?action=genRandomMailbox")
    suspend fun randomMailbox(@Query("count") count: Int = 1): Response<List<String>>

    @GET("?action=getMessages")
    suspend fun messages(
        @Query("login") login: String,
        @Query("domain") domain: String,
    ): Response<List<OneSecListedMessage>>

    @GET("?action=readMessage")
    suspend fun read(
        @Query("login") login: String,
        @Query("domain") domain: String,
        @Query("id") id: Int,
    ): Response<OneSecMessage>

    @GET("?action=downloadAttachment")
    suspend fun download(
        @Query("login") login: String,
        @Query("domain") domain: String,
        @Query("id") id: Int,
        @Query("file") file: String,
    ): Response<okhttp3.ResponseBody>
}

data class OneSecListedMessage(
    val id: Int = 0,
    val from: String = "",
    val subject: String = "",
    val date: String = "",
)

data class OneSecMessage(
    val id: Int = 0,
    val from: String = "",
    val subject: String = "",
    val date: String = "",
    val body: String = "",
    val textBody: String? = null,
    val htmlBody: String? = null,
    val attachments: List<OneSecAttachment> = emptyList(),
)

data class OneSecAttachment(
    val filename: String = "",
    val contentType: String = "",
    val size: Long = 0,
)