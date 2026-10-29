package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.repository.LoginResult
import com.example.mastermartini.data.repository.RegisterResult
import com.example.mastermartini.data.repository.UserRepository
import com.example.mastermartini.data.session.SessionManager
import com.example.mastermartini.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeUserRepository
    private lateinit var session: FakeSessionManager
    private lateinit var viewModel: RegisterViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeUserRepository()
        session = FakeSessionManager()
        viewModel = RegisterViewModel(repository, session)
    }

    @After fun tearDown() = Dispatchers.resetMain()

    private fun fillValidForm(email: String = "ana@empresa.cl") = with(viewModel) {
        onFullNameChange("Ana Pérez")
        onEmailChange(email)
        onPhoneChange("+56 9 1234 5678")
        onPasswordChange("clave123")
        onConfirmPasswordChange("clave123")
    }

    @Test fun submitEmptyForm_touchesEveryFieldShowsErrorsAndDoesNotCallRepository() = runTest(dispatcher) {
        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        listOf(state.fullName, state.email, state.phone, state.password, state.confirmPassword).forEach {
            assertTrue(it.touched)
            assertTrue(it.showError)
        }
        assertEquals("Ingresa tu nombre", state.fullName.error)
        assertEquals("Repite la contraseña", state.confirmPassword.error)
        assertFalse(state.isLoading)
        assertEquals(0, repository.registerCalls.size)
    }

    @Test fun validForm_callsRegisterWithTheEnteredData() = runTest(dispatcher) {
        fillValidForm()
        viewModel.submit()
        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()

        assertEquals(listOf(listOf("Ana Pérez", "ana@empresa.cl", "+56 9 1234 5678", "clave123")), repository.registerCalls)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test fun duplicateEmail_putsTheErrorOnTheEmailFieldAndKeepsTheUserOnTheForm() = runTest(dispatcher) {
        repository.nextResult = RegisterResult.EmailTaken
        fillValidForm()
        viewModel.submit()
        advanceUntilIdle()

        val email = viewModel.uiState.value.email
        assertEquals(RegisterViewModel.EMAIL_TAKEN, email.error)
        assertTrue(email.showError)
        assertNull(session.userId.value)

        viewModel.onEmailChange("otra@empresa.cl")
        assertNull(viewModel.uiState.value.email.error)
    }

    @Test fun success_startsTheSessionAndEmitsTheNavigationEvent() = runTest(dispatcher) {
        fillValidForm()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(AuthEvent.LoggedIn, viewModel.events.first())
        assertEquals(42L, session.userId.value)
    }

    @Test fun changingPassword_revalidatesTheConfirmation() = runTest(dispatcher) {
        viewModel.onPasswordChange("clave123")
        viewModel.onConfirmPasswordChange("clave123")
        assertNull(viewModel.uiState.value.confirmPassword.error)

        viewModel.onPasswordChange("clave1234")
        assertEquals("Las contraseñas no coinciden", viewModel.uiState.value.confirmPassword.error)
    }

    private class FakeUserRepository : UserRepository {
        val registerCalls = mutableListOf<List<String>>()
        var nextResult: RegisterResult? = null

        override suspend fun register(fullName: String, email: String, phone: String, password: String): RegisterResult {
            registerCalls += listOf(fullName, email, phone, password)
            return nextResult ?: RegisterResult.Success(User(42, fullName, email, phone, null))
        }

        override suspend fun login(email: String, password: String) = LoginResult.InvalidCredentials
        override fun observeUser(id: Long): Flow<User?> = emptyFlow()
        override suspend fun updatePhoto(userId: Long, photoUri: String?) = Unit
    }

    private class FakeSessionManager : SessionManager {
        override val userId = MutableStateFlow<Long?>(null)
        override suspend fun startSession(userId: Long) { this.userId.value = userId }
        override suspend fun endSession() { userId.value = null }
    }
}
