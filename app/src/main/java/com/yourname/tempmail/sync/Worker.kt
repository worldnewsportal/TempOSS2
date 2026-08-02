package com.yourname.tempmail.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.yourname.tempmail.data.db.TempmailDatabase
import com.yourname.tempmail.data.repo.MailboxRepository
import com.yourname.tempmail.data.repo.MessageRepository
import com.yourname.tempmail.providers.EmailProvider
import com.yourname.tempmail.providers.MailtmProvider
import com.yourname.tempmail.providers.OneSecmailProvider
import com.yourname.tempmail.providers.ProviderManager
import com.yourname.tempmail.providers.ProviderRegistry
import com.yourname.tempmail.security.SecretStore
import kotlinx.coroutines.flow.first

/**
 * Periodic background mail sync.
 *
 * Every 15 minutes it:
 *  1. purges expired mailboxes and their cached data (rule #9),
 *  2. pulls new message summaries for every surviving mailbox (rule #21),
 *  3. shows a real notification per newly arrived batch (rule #20).
 *
 * The graph is built fresh here instead of reaching into a static app instance,
 * so the worker never leaks an Application reference.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext

        val db = TempmailDatabase.get(context)
        val providers = buildProviderManager(context)

        val mailboxesRepo = MailboxRepository(db, providers)
        val messagesRepo = MessageRepository(db, providers)

        runCatching { mailboxesRepo.purgeExpired() }

        val active = mailboxesRepo.observeActive().first()
        if (active.isEmpty()) return Result.success()

        val changed = runCatching { messagesRepo.refreshAll(active) }
            .getOrDefault(0)

        if (changed > 0) {
            val first = active.first()
            NotificationHelper().show(
                context = context,
                mailAddress = first.email.full,
                sender = "TempMail",
                subject = "$changed new message(s)",
                preview = first.displayName,
            )
        }
        return Result.success()
    }

    private fun buildProviderManager(context: Context): ProviderManager {
        val registry = ProviderRegistry(
            providers = listOf<EmailProvider>(
                OneSecmailProvider(),
                MailtmProvider(SecretStore(context)),
            ),
        )
        return ProviderManager(registry)
    }

    companion object {
        const val WORK_NAME = "tempmail_sync"

        /**
         * Periodic worker, run when a network is available. WorkManager handles
         * the 15-minute interval with a flex window (rule #21).
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}