package com.example.mastermartini.util.security

/** A password hash and the random salt used to produce it. */
data class HashedPassword(val hash: String, val salt: String)

/**
 * Hashes and verifies passwords so they are never stored in plain text.
 * See [Pbkdf2PasswordHasher]; tests can pass a fake.
 */
interface PasswordHasher {
    /** Hashes [password] with a new random salt. */
    fun hash(password: String): HashedPassword

    /** True if [password] produces [hash] when combined with [salt]. */
    fun verify(password: String, hash: String, salt: String): Boolean
}
