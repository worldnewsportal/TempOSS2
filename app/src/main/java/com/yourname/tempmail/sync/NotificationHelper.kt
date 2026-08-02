package com.yourname.tempmail.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.yourname.tempmail.MainActivity

/** Real notification for a newly-arrived message (rule #20). */
class NotificationHelper {

    fun show(
        context: Context,
        mailAddress: String,
        sender: String,
        subject: String,
        preview: String?,
    ): Int {
        createChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notifId = (mailAddress.hashCode() xor subject.hashCode() and 0x7fffffff)
        val title = subject.ifBlank { sender }
        val content = preview ?: sender

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_action_email)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setAutoCancel(true)
            .setContentIntent(pending)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, builder.build())
        return notifId
    }

    private fun createChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Incoming mail",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = "New temporary e-mail notifications" }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "new_mail"
    }
}