package com.yourname.tempmail

import com.yourname.tempmail.data.db.MailboxEntity
import com.yourname.tempmail.data.repo.toDomain
import com.yourname.tempmail.data.repo.toEntity
import com.yourname.tempmail.domain.EmailAddress
import com.yourname.tempmail.domain.Mailbox
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperTest {

    @Test
    fun `entity to domain to entity round trips`() {
        val entity = MailboxEntity(
            id = 7,
            providerId = "onesecmail",
            login = "user1",
            domain = "example.com",
            email = "user1@example.com",
            createdAt = 1L,
            expiresAt = 2L,
            displayName = "My box",
            favorite = true,
            unreadCount = 3,
            lastSyncedAt = 4L,
        )
        val domain = entity.toDomain()
        assertEquals(7L, domain.id)
        assertEquals("onesecmail", domain.providerId)
        assertEquals(EmailAddress("user1", "example.com"), domain.email)
        assertEquals("My box", domain.displayName)
        assertEquals(true, domain.favorite)

        val back = domain.toEntity()
        assertEquals(entity, back)
    }

    @Test
    fun `default id maps to zero`() {
        val m = Mailbox(
            providerId = "mailtm",
            email = EmailAddress("a", "example.com"),
            createdAt = 0L,
            expiresAt = 0L,
        )
        val e = m.toEntity()
        assertEquals(0L, e.id)
        assertEquals("a@example.com", e.email)
    }
}