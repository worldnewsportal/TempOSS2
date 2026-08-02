package com.yourname.tempmail

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.yourname.tempmail.data.daos.MailboxDao
import com.yourname.tempmail.data.db.MailboxEntity
import com.yourname.tempmail.data.db.TempmailDatabase
import com.yourname.tempmail.data.repo.toDomain
import com.yourname.tempmail.domain.Lifetime
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TempmailDatabaseTest {

    private lateinit var db: TempmailDatabase
    private lateinit var mailboxDao: MailboxDao

    @Before fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, TempmailDatabase::class.java).build()
        mailboxDao = db.mailboxDao()
    }

    @After fun tearDown() { db.close() }

    @Test
    fun `insert then read mailbox`() = runBlocking {
        val now = System.currentTimeMillis()
        val id = mailboxDao.insert(
            MailboxEntity(
                providerId = "onesecmail",
                login = "u1",
                domain = "example.com",
                email = "u1@example.com",
                createdAt = now,
                expiresAt = now + Lifetime.of(168),
            )
        )
        val e = mailboxDao.byId(id)
        assertNotNull(e)
        assertEquals("u1@example.com", e!!.email)
        val domain = e.toDomain()
        assertEquals("u1@example.com", domain.email.full)
    }

    @Test
    fun `unique email constraint aborts duplicate`() = runBlocking {
        val now = System.currentTimeMillis()
        mailboxDao.insert(MailboxEntity(
            providerId = "onesecmail", login = "u", domain = "example.com",
            email = "u@example.com", createdAt = now, expiresAt = now + Lifetime.of(168),
        ))
        Assert.assertThrows(android.database.sqlite.SQLiteConstraintException::class.java) {
            runBlocking {
                mailboxDao.insert(MailboxEntity(
                    providerId = "mailtm", login = "u", domain = "example.com",
                    email = "u@example.com", createdAt = now, expiresAt = now + Lifetime.of(168),
                ))
            }
        }
    }

    @Test
    fun `deleteExpired removes only expired mailboxes`() = runBlocking {
        val now = System.currentTimeMillis()
        mailboxDao.insert(MailboxEntity(
            providerId = "onesecmail", login = "old", domain = "example.com",
            email = "old@example.com", createdAt = now - 1, expiresAt = now - 1,
        ))
        mailboxDao.insert(MailboxEntity(
            providerId = "onesecmail", login = "new", domain = "example.com",
            email = "new@example.com", createdAt = now, expiresAt = now + Lifetime.of(168),
        ))
        mailboxDao.deleteExpired(now)
        assertNull(mailboxDao.findByEmail("old@example.com"))
        assertNotNull(mailboxDao.findByEmail("new@example.com"))
    }
}
