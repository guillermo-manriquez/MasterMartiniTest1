package com.example.mastermartini.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.repository.ProductRepository
import com.example.mastermartini.data.repository.UserRepository
import com.example.mastermartini.data.session.SessionManager
import com.example.mastermartini.di.appContainer
import com.example.mastermartini.model.Product
import com.example.mastermartini.model.User
import com.example.mastermartini.util.camera.ProfilePhotoStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val favorites: List<Product> = emptyList(),
)

enum class PhotoEvent { SAVED, NO_CAMERA_APP }

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val userRepository: UserRepository,
    productRepository: ProductRepository,
    private val sessionManager: SessionManager,
    private val photoStore: ProfilePhotoStore,
    // Holds the photo being taken, so the result is not lost if Android kills the app
    // while the camera app is open
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = sessionManager.userId
        .filterNotNull()
        .flatMapLatest { userId ->
            combine(userRepository.observeUser(userId), productRepository.observeFavorites(userId)) { user, favorites ->
                ProfileUiState(isLoading = false, user = user, favorites = favorites)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    private val _loggedOut = Channel<Unit>(Channel.BUFFERED)
    val loggedOut: Flow<Unit> = _loggedOut.receiveAsFlow()

    private val _photoEvents = Channel<PhotoEvent>(Channel.BUFFERED)
    val photoEvents: Flow<PhotoEvent> = _photoEvents.receiveAsFlow()

    /** Creates the file the camera app writes into. Call only once the camera permission is granted. */
    fun preparePhotoCapture(): Uri? {
        val userId = uiState.value.user?.id ?: return null
        return photoStore.newPhotoUri(userId).also { savedStateHandle[PENDING_PHOTO] = it.toString() }
    }

    /** Saves the new photo on the user and deletes the old one; a cancelled capture leaves the current photo. */
    fun onPhotoCaptured(success: Boolean) {
        val pending = savedStateHandle.remove<String>(PENDING_PHOTO) ?: return
        viewModelScope.launch {
            val userId = sessionManager.userId.first()
            if (!success || userId == null) {
                photoStore.delete(pending)
                return@launch
            }
            val previous = userRepository.observeUser(userId).first()?.photoUri
            userRepository.updatePhoto(userId, pending)
            previous?.let { photoStore.delete(it) }
            _photoEvents.send(PhotoEvent.SAVED)
        }
    }

    fun onCameraUnavailable() {
        val pending = savedStateHandle.remove<String>(PENDING_PHOTO)
        viewModelScope.launch {
            pending?.let { photoStore.delete(it) }
            _photoEvents.send(PhotoEvent.NO_CAMERA_APP)
        }
    }

    /** Clears the saved session; the account, favorites and quotes stay in the local database. */
    fun logout() {
        viewModelScope.launch {
            sessionManager.endSession()
            _loggedOut.send(Unit)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                ProfileViewModel(
                    container.userRepository,
                    container.productRepository,
                    container.sessionManager,
                    container.profilePhotoStore,
                    createSavedStateHandle(),
                )
            }
        }

        private const val PENDING_PHOTO = "pending_photo_uri"
    }
}
