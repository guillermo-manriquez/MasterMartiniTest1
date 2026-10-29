package com.example.mastermartini.util.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PBKDF2-HMAC-SHA256 with a random 16-byte salt per password (needs API 26+, hence minSdk 26).
 * Hash and salt are stored as Base64 strings. Uses only java/javax APIs, so it also
 * runs in plain JUnit tests.
 */
class Pbkdf2PasswordHasher(
    private val iterations: Int = DEFAULT_ITERATIONS,
    private val random: SecureRandom = SecureRandom(),
) : PasswordHasher {

    override fun hash(password: String): HashedPassword {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        return HashedPassword(hash = encode(derive(password, salt)), salt = encode(salt))
    }

    override fun verify(password: String, hash: String, salt: String): Boolean {
        val expected = runCatching { decode(hash) }.getOrNull() ?: return false
        val saltBytes = runCatching { decode(salt) }.getOrNull() ?: return false
        // Constant-time comparison so timing does not reveal how many bytes matched
        return MessageDigest.isEqual(derive(password, saltBytes), expected)
    }

    private fun derive(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    private fun decode(text: String): ByteArray = Base64.getDecoder().decode(text)

    companion object {
        const val DEFAULT_ITERATIONS = 120_000
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val SALT_BYTES = 16
        private const val KEY_BITS = 256
    }
}
