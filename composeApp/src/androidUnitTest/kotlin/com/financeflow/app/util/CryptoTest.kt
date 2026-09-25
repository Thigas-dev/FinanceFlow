package com.financeflow.app.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Confere a implementação em Kotlin puro contra a da JVM. */
class CryptoTest {
    @Test
    fun sha256IgualAJvm() {
        for (size in listOf(0, 3, 55, 56, 63, 64, 65, 200)) {
            val data = ByteArray(size) { (it * 31 + 7).toByte() }
            assertArrayEquals(MessageDigest.getInstance("SHA-256").digest(data), Crypto.sha256(data))
        }
    }

    @Test
    fun pbkdf2IgualAJvm() {
        val salt = ByteArray(16) { it.toByte() }
        val jvm = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec("segredo123".toCharArray(), salt, 1000, 256)).encoded
        assertArrayEquals(jvm, Crypto.pbkdf2Sha256("segredo123".encodeToByteArray(), salt, 1000, 32))
    }

    @Test
    fun hashDeSenha() {
        val salt = Passwords.newSalt()
        val h = Passwords.hash("abc12345", salt)
        assertTrue(Passwords.verify("abc12345", salt, h))
        assertFalse(Passwords.verify("abc12346", salt, h))
        assertEquals(24, salt.length)
    }
}
