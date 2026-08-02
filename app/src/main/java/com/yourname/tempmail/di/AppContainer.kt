package com.yourname.tempmail.di

import android.content.Context
import com.yourname.tempmail.ads.AdsManager
import com.yourname.tempmail.data.db.TempmailDatabase
import com.yourname.tempmail.data.prefs.SettingsRepository
import com.yourname.tempmail.data.repo.MailboxRepository
import com.yourname.tempmail.data.repo.MessageRepository
import com.yourname.tempmail.providers.EmailProvider
import com.yourname.tempmail.providers.MailtmProvider
import com.yourname.tempmail.providers.OneSecmailProvider
import com.yourname.tempmail.providers.ProviderManager
import com.yourname.tempmail.providers.ProviderRegistry
import com.yourname.tempmail.security.HtmlSanitizer
import com.yourname.tempmail.security.SecretStore
import com.yourname.tempmail.sync.NotificationHelper

/**
 * Simple (non-Hilt) dependency container. Keeps the whole graph in one place.
 */
class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext

    val database: TempmailDatabase by lazy { TempmailDatabase.get(appContext) }
    val settings: SettingsRepository by lazy { SettingsRepository(appContext) }

    private val secretStore by lazy { SecretStore(appContext) }

    val providers: ProviderManager by lazy {
        val registry = ProviderRegistry(
            providers = listOf<EmailProvider>(
                OneSecmailProvider(),
                MailtmProvider(secretStore),
            ),
        )
        ProviderManager(registry)
    }

    val mailboxes: MailboxRepository by lazy { MailboxRepository(database, providers) }
    val messages: MessageRepository by lazy { MessageRepository(database, providers) }
    val notifications: NotificationHelper by lazy { NotificationHelper() }

    val htmlSanitizer: HtmlSanitizer by lazy { HtmlSanitizer() }

    val ads: AdsManager by lazy { AdsManager(appContext, database, settings) }
}