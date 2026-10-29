package com.example.mastermartini.util.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class PasswordHasherTest {

    // Low iteration count keeps the tests fast; the algorithm is the same.
    private val hasher = Pbkdf2PasswordHasher(iterations = 1_000)

    private fun base64(bytes: ByteArray) = Base64.getEncoder().encodeToString(bytes)

    @Test fun hash_neverContainsThePlainPassword() {
        val result = hasher.hash("clave123")
        assertFalse(result.hash.contains("clave123"))
        assertEquals(32, Base64.getDecoder().decode(result.hash).size) // 256-bit key
        assertEquals(16, Base64.getDecoder().decode(result.salt).size) // 128-bit salt
    }

    @Test fun samePasswordAndSalt_verifies() {
        val result = hasher.hash("clave123")
        assertTrue(hasher.verify("clave123", result.hash, result.salt))
    }

    @Test fun samePassword_getsDifferentSaltAndHash() {
        val first = hasher.hash("clave123")
        val second = hasher.hash("clave123")
        assertNotEquals(first.salt, second.salt)
        assertNotEquals(first.hash, second.hash)
    }

    @Test fun wrongPassword_failsVerification() {
        val result = hasher.hash("clave123")
        assertFalse(hasher.verify("clave124", result.hash, result.salt))
        assertFalse(hasher.verify("Clave123", result.hash, result.salt))
        assertFalse(hasher.verify("", result.hash, result.salt))
    }

    @Test fun otherUsersSalt_failsVerification() {
        val result = hasher.hash("clave123")
        val otherSalt = hasher.hash("clave123").salt
        assertFalse(hasher.verify("clave123", result.hash, otherSalt))
    }

    @Test fun differentIterationCount_failsVerification() {
        val result = hasher.hash("clave123")
        assertFalse(Pbkdf2PasswordHasher(iterations = 2_000).verify("clave123", result.hash, result.salt))
    }

    @Test fun corruptStoredValues_failVerificationWithoutCrashing() {
        val result = hasher.hash("clave123")
        assertFalse(hasher.verify("clave123", "not base64!", result.salt))
        assertFalse(hasher.verify("clave123", result.hash, "%%%"))
    }

    /** Known-answer test: RFC 7914 §11 PBKDF2-HMAC-SHA256 vector (P="passwd", S="salt", c=1), first 32 bytes. */
    @Test fun matchesRfc7914TestVector() {
        val expected = "55ac046e56e3089fec1691c22544b605f94185216dde0465e68b9d57c20dacbc"
            .chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val salt = base64("salt".toByteArray())
        assertTrue(Pbkdf2PasswordHasher(iterations = 1).verify("passwd", base64(expected), salt))
    }
}
