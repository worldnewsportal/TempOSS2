package com.yourname.tempmail.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Minimal AES/GCM encryption backed by the Android Keystore.
 *
 * Used only for provider credentials/tokens (mail.tm). We never log plaintext.
 * There is intentionally no fallback to a hardcoded key.
 */
class SecretStore(private val context: Context) {

    private val prefs = context.getSharedPreferences("secret_store", Context.MODE_PRIVATE)
    private val ks = "tempmail_keystore"

    fun put(key: String, value: String) {
        val enc = encrypt(value) ?: return
        prefs.edit().putString(key, base64Encode(enc)).apply()
    }

    fun read(key: String): String? {
        val stored = prefs.getString(key, null) ?: return null
        return decrypt(storedBytes(stored))
    }

    fun remove(key: String) { prefs.edit().remove(key).apply() }

    fun contains(key: String): Boolean = prefs.contains(key)

    private fun encrypt(plain: String): ByteArray? = try {
        val c = Cipher.getInstance(TRANS)
        c.init(Cipher.ENCRYPT_MODE, key())
        val iv = c.iv
        val enc = c.doFinal(plain.toByteArray(Charsets.UTF_8))
        iv + enc
    } catch (e: Exception) { null }

    private fun decrypt(data: ByteArray): String? = try {
        if (data.size < IV_LEN) return null
        val iv = data.copyOfRange(0, IV_LEN)
        val body = data.copyOfRange(IV_LEN, data.size)
        val c = Cipher.getInstance(TRANS)
        c.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        String(c.doFinal(body), Charsets.UTF_8)
    } catch (e: Exception) { null }

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    private fun base64Encode(b: ByteArray) = Base64.encodeToString(b, Base64.NO_WRAP)
    private fun stored(s: String): ByteArray = Base64.decode(s, Base64.NO_WRAP)

    companion object {
        private const val TRANS = "AES/GCM/NoPadding"
        private const val ALIAS = "tempmail_crypto"
        private const val IV_LEN = 12
    }
}