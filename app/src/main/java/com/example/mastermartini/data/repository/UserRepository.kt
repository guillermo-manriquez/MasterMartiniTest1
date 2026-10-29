package com.example.mastermartini.data.repository

import com.example.mastermartini.data.local.dao.UserDao
import com.example.mastermartini.data.local.entity.UserEntity
import com.example.mastermartini.model.User
import com.example.mastermartini.util.security.PasswordHasher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

sealed interface RegisterResult {
    data class Success(val user: User) : RegisterResult
    data object EmailTaken : RegisterResult
}

sealed interface LoginResult {
    data class Success(val user: User) : LoginResult

    /** Deliberately does not say whether the email or the password was wrong. */
    data object InvalidCredentials : LoginResult
}

interface UserRepository {
    /** Creates a local account. Input is expected to be validated by the ViewModel. */
    suspend fun register(fullName: String, email: String, phone: String, password: String): RegisterResult

    suspend fun login(email: String, password: String): LoginResult

    fun observeUser(id: Long): Flow<User?>

    suspend fun updatePhoto(userId: Long, photoUri: String?)
}

class RoomUserRepository(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
    // PBKDF2 is deliberately slow (~0.7 s on an emulator), so it must not run on the main thread
    private val hashingDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UserRepository {

    override suspend fun register(fullName: String, email: String, phone: String, password: String): RegisterResult {
        val hashed = withContext(hashingDispatcher) { passwordHasher.hash(password) }
        val entity = UserEntity(
            fullName = fullName.trim(),
            email = email.normalizedEmail(),
            phone = phone.trim(),
            passwordHash = hashed.hash,
            salt = hashed.salt,
        )
        val id = userDao.insert(entity)
        return if (id == -1L) RegisterResult.EmailTaken else RegisterResult.Success(entity.copy(id = id).toModel())
    }

    override suspend fun login(email: String, password: String): LoginResult {
        val user = userDao.findByEmail(email.normalizedEmail())
        val matches = user != null && withContext(hashingDispatcher) {
            passwordHasher.verify(password, user.passwordHash, user.salt)
        }
        return if (user != null && matches) {
            LoginResult.Success(user.toModel())
        } else {
            LoginResult.InvalidCredentials
        }
    }

    override fun observeUser(id: Long): Flow<User?> = userDao.observeById(id).map { it?.toModel() }

    override suspend fun updatePhoto(userId: Long, photoUri: String?) = userDao.updatePhoto(userId, photoUri)
}

/** Emails are stored lowercase so the unique index treats Ana@x.cl and ana@x.cl as the same. */
private fun String.normalizedEmail(): String = trim().lowercase()
