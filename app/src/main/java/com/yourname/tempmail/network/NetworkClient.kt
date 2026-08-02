package com.yourname.tempmail.network

import com.yourname.tempmail.domain.ProviderResult
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/**
 * Creates OkHttp + Retrofit instances per provider base-url. HTTPS-only is
 * enforced by OkHttp defaults; every provider base URL is https://.
 */
object NetworkClient {
    fun okHttp(baseUrl: String): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .retryOnConnectionFailure(false)
            .build()
    }

    fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /** Normalize any thrown exception to a ProviderResult.Failure with a kind token. */
    fun <T> toResult(e: Throwable): ProviderResult.Failure {
        val kind = when (e) {
            is SocketTimeoutException -> "err.timeout"
            else -> "err.provider"
        }
        return ProviderResult.Failure(kind, e)
    }
}