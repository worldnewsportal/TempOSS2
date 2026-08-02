package com.yourname.tempmail.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yourname.tempmail.data.db.AdRewardEntity
import com.yourname.tempmail.data.db.AttachmentEntity
import com.yourname.tempmail.data.db.DraftEntity
import com.yourname.tempmail.data.db.FavoriteEntity
import com.yourname.tempmail.data.db.MailboxEntity
import com.yourname.tempmail.data.db.MessageEntity
import com.yourname.tempmail.data.db.ProviderEntity
import com.yourname.tempmail.data.db.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MailboxDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(m: MailboxEntity): Long

    @Update
    suspend fun update(m: MailboxEntity)

    @Delete
    suspend fun delete(m: MailboxEntity)

    @Query("SELECT * FROM mailboxes WHERE id = :id")
    suspend fun byId(id: Long): MailboxEntity?

    @Query("SELECT * FROM mailboxes ORDER BY favorite DESC, createdAt DESC")
    fun observeAll(): Flow<List<MailboxEntity>>

    @Query("SELECT * FROM mailboxes WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): MailboxEntity?

    @Query("SELECT * FROM mailboxes")
    suspend fun all(): List<MailboxEntity>

    @Query("DELETE FROM mailboxes WHERE expiresAt <= :now")
    suspend fun deleteExpired(now: Long)

    @Query("DELETE FROM mailboxes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM mailboxes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM mailboxes")
    suspend fun deleteAll()
}

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(msgs: List<MessageEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(msg: MessageEntity): Long

    @Update
    suspend fun update(msg: MessageEntity)

    @Query("SELECT * FROM messages WHERE mailboxId = :mailboxId ORDER BY date DESC")
    fun observeInbox(mailboxId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE mailboxId = :mailboxId AND seen = 0 ORDER BY date DESC")
    fun observeUnread(mailboxId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE mailboxId = :mailboxId AND starred = 1 ORDER BY date DESC")
    fun observeStarred(mailboxId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE mailboxId = :mailboxId AND label = :label ORDER BY date DESC")
    fun observeByLabel(mailboxId: Long, label: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun byId(id: Long): MessageEntity?

    @Query("SELECT * FROM messages WHERE id = :id")
    fun observeById(id: Long): Flow<MessageEntity?>

    @Query("SELECT * FROM messages WHERE mailboxId = :mailboxId AND providerRawId = :remoteId")
    suspend fun byRemote(mailboxId: Long, remoteId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE mailboxId = :mailboxId")
    suspend fun allForMailbox(mailboxId: Long): List<MessageEntity>

    @Query(
"SELECT * FROM messages WHERE mailboxId = :mailboxId AND " +
            "(fromName LIKE '%'||:q||'%' OR fromAddress LIKE '%'||:q||'%' " +
            "OR subject LIKE '%'||:q||'%' OR preview LIKE '%'||:q||'%' " +
            "OR bodyText LIKE '%'||:q||'%' OR bodyHtml LIKE '%'||:q||'%') " +
            "ORDER BY date DESC"
    )
    fun search(mailboxId: Long, q: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET seen = :seen WHERE id = :id")
    suspend fun setSeen(id: Long, seen: Boolean)

    @Query("UPDATE messages SET starred = :starred WHERE id = :id")
    suspend fun setStarred(id: Long, starred: Boolean)

    @Query("UPDATE messages SET label = :label WHERE id = :id")
    suspend fun setLabel(id: Long, label: String?)

    @Query("DELETE FROM messages WHERE mailboxId = :mailboxId")
    suspend fun deleteForMailbox(mailboxId: Long)

    @Query("DELETE FROM messages WHERE mailboxId IN (:ids)")
    suspend fun deleteForMailboxIds(ids: List<Long>)

    @Query("DELETE FROM messages")
    suspend fun purgeMessages()

    @Query("DELETE FROM messages WHERE mailboxId = :mailboxId AND id = :id")
    suspend fun deleteOne(mailboxId: Long, id: Long)

    @Query("DELETE FROM messages")
    suspend fun purgeAll()

    @Query("SELECT COUNT(*) FROM messages WHERE mailboxId = :mailboxId AND seen = 0")
    suspend fun unreadCount(mailboxId: Long): Int

    @Query("DELETE FROM messages WHERE mailboxId IN (SELECT id FROM mailboxes WHERE expiresAt <= :now)")
    suspend fun deleteForExpired(now: Long)
}

@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(a: AttachmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<AttachmentEntity>)

    @Query("SELECT * FROM attachments WHERE messageId = :messageId")
    fun observeForMessage(messageId: Long): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE messageId = :messageId")
    suspend fun getForMessage(messageId: Long): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE id = :id")
    suspend fun byId(id: Long): AttachmentEntity?

    @Query("DELETE FROM attachments WHERE messageId IN (SELECT id FROM messages WHERE mailboxId = :mailboxId)")
    suspend fun deleteForMailbox(mailboxId: Long)

    @Query("DELETE FROM attachments WHERE messageId IN (SELECT id FROM messages WHERE mailboxId IN (:ids))")
    suspend fun deleteForMailboxIds(ids: List<Long>)

    @Query("DELETE FROM attachments")
    suspend fun purgeAttachments()
}

@Dao
interface DraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(d: DraftEntity): Long

    @Update
    suspend fun update(d: DraftEntity)

    @Query("SELECT * FROM drafts ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<DraftEntity>>

    @Query("SELECT * FROM drafts WHERE mailboxId = :mailboxId ORDER BY updatedAt DESC")
    fun observeForMailbox(mailboxId: Long): Flow<List<DraftEntity>>

    @Query("SELECT * FROM drafts WHERE id = :id")
    suspend fun byId(id: Long): DraftEntity?

    @Query("SELECT * FROM drafts WHERE mailboxId = :mailboxId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun latestForMailbox(mailboxId: Long): DraftEntity?

    @Delete
    suspend fun delete(d: DraftEntity)

    @Query("DELETE FROM drafts WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ProviderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(p: ProviderEntity)

    @Query("SELECT * FROM providers")
    fun observeAll(): Flow<List<ProviderEntity>>
}

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(e: SettingsEntity)

    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun get(key: String): SettingsEntity?

    @Query("SELECT * FROM settings")
    fun observeAll(): Flow<List<SettingsEntity>>

    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun remove(key: String)
}

@Dao
interface AdRewardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(r: AdRewardEntity)

    @Query("SELECT * FROM ad_rewards WHERE id = :id")
    suspend fun byId(id: String): AdRewardEntity?

    @Query("SELECT * FROM ad_rewards")
    fun observeAll(): Flow<List<AdRewardEntity>>
}

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(f: FavoriteEntity)

    @Query("SELECT messageId FROM favorites WHERE mailboxId = :mailboxId")
    fun observeMessageIds(mailboxId: Long): Flow<List<Long>>

    @Query("DELETE FROM favorites WHERE messageId = :messageId")
    suspend fun deleteByMessage(messageId: Long)

    @Query("SELECT COUNT(*) FROM favorites WHERE mailboxId = :mailboxId")
    suspend fun count(mailboxId: Long): Int
}