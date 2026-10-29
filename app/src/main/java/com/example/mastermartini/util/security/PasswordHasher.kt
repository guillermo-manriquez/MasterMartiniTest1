package com.example.mastermartini.util.security

/** A password hash and the random salt used to produce it. */
data class HashedPassword(val hash: String, val salt: String)

/**
 * Hashes and verifies passwords so they are never stored in plain text.
 * The PBKDF2 implementation is added in T7.
 */
interface PasswordHasher {
    /** Hashes [password] with a new random salt. */
    fun hash(password: String): HashedPassword

    /** True if [password] produces [hash] when combined with [salt]. */
    fun verify(password: String, hash: String, salt: String): Boolean
}
