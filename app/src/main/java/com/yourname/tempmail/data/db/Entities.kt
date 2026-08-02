package com.yourname.tempmail.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "mailboxes", indices = [Index(value = ["email"], unique = true)])
data class MailboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerId: String,
    val login: String,
    val domain: String,
    val email: String,
    val createdAt: Long,
    val expiresAt: Long,
    val displayName: String = "",
    val favorite: Boolean = false,
    val unreadCount: Int = 0,
    val lastSyncedAt: Long = 0L,
)

@Entity(tableName = "messages", indices = [Index(value = ["mailboxId", "providerRawId"], unique = true)])
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mailboxId: Long,
    val providerRawId: String,
    val fromName: String = "",
    val fromAddress: String = "",
    val to: String = "",
    val cc: String = "",
    val bcc: String = "",
    val subject: String = "",
    val preview: String = "",
    val bodyText: String? = null,
    val bodyHtml: String? = null,
    val date: Long = 0L,
    val seen: Boolean = false,
    val starred: Boolean = false,
    val label: String? = null,
    val messageIdHeader: String? = null,
    val inReplyTo: String? = null,
)

@Entity(tableName = "attachments")
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: Long,
    val filename: String,
    val contentType: String? = null,
    val sizeBytes: Long = 0,
    val url: String? = null,
    val provider: String = "",
)

@Entity(
    tableName = "drafts",
    indices = [Index(value = ["mailboxId"])],
)
data class DraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mailboxId: Long,
    val to: String = "",
    val cc: String = "",
    val bcc: String = "",
    val subject: String = "",
    val body: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val lastHealth: String = "UNKNOWN",
    val lastCheckedAt: Long = 0L,
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String = "",
)

@Entity(tableName = "ad_rewards")
data class AdRewardEntity(
    @PrimaryKey val id: String,
    val grantedAt: Long,
    val source: String = "rewarded",
)

@Entity(
    tableName = "favorites",
    indices = [Index(value = ["messageId"], unique = true)],
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: Long,
    val mailboxId: Long,
)