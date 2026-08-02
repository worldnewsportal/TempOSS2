package com.yourname.tempmail.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yourname.tempmail.data.daos.AdRewardDao
import com.yourname.tempmail.data.daos.AttachmentDao
import com.yourname.tempmail.data.daos.DraftDao
import com.yourname.tempmail.data.daos.FavoriteDao
import com.yourname.tempmail.data.daos.MailboxDao
import com.yourname.tempmail.data.daos.MessageDao
import com.yourname.tempmail.data.daos.ProviderDao
import com.yourname.tempmail.data.daos.SettingsDao

@Database(
    entities = [
        MailboxEntity::class,
        MessageEntity::class,
        AttachmentEntity::class,
        DraftEntity::class,
        ProviderEntity::class,
        SettingsEntity::class,
        AdRewardEntity::class,
        FavoriteEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class TempmailDatabase : RoomDatabase() {
    abstract fun mailboxDao(): MailboxDao
    abstract fun messageDao(): MessageDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun draftDao(): DraftDao
    abstract fun providerDao(): ProviderDao
    abstract fun settingsDao(): SettingsDao
    abstract fun adRewardDao(): AdRewardDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile private var INSTANCE: TempmailDatabase? = null

        fun get(context: Context): TempmailDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TempmailDatabase::class.java,
                    "tempmail.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}