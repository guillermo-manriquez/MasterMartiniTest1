package com.example.mastermartini.ui.screens.profile

import android.Manifest
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.mastermartini.R
import com.example.mastermartini.model.User
import com.example.mastermartini.ui.components.EmptyState
import com.example.mastermartini.ui.components.MasterMartiniWordmark
import com.example.mastermartini.ui.components.PermissionRationaleDialog
import com.example.mastermartini.ui.components.ProductCard
import com.example.mastermartini.util.permissions.PermissionResult
import com.example.mastermartini.util.permissions.openAppSettings
import com.example.mastermartini.util.permissions.rememberPermissionRequester
import com.example.mastermartini.viewmodel.PhotoEvent
import com.example.mastermartini.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onProductClick: (Long) -> Unit,
    onGoToCatalog: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    var showCameraRationale by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val showMessage: (Int, Int?, () -> Unit) -> Unit = { messageRes, actionRes, onAction ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = context.getString(messageRes),
                actionLabel = actionRes?.let(context::getString),
                duration = if (actionRes != null) SnackbarDuration.Long else SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) onAction()
        }
    }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        viewModel.onPhotoCaptured(saved)
    }
    val openCamera: () -> Unit = {
        viewModel.preparePhotoCapture()?.let { uri ->
            try {
                takePicture.launch(uri)
            } catch (_: ActivityNotFoundException) {
                viewModel.onCameraUnavailable()
            }
        }
    }

    // Camera permission: same flow as location in the quote (T14)
    val cameraPermission = rememberPermissionRequester(listOf(Manifest.permission.CAMERA)) { result ->
        when (result) {
            PermissionResult.GRANTED -> openCamera()
            PermissionResult.SHOW_RATIONALE -> showCameraRationale = true
            PermissionResult.PERMANENTLY_DENIED ->
                showMessage(R.string.camera_permanently_denied, R.string.action_open_settings) { context.openAppSettings() }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.loggedOut.collect { onLoggedOut() }
    }
    LaunchedEffect(viewModel) {
        viewModel.photoEvents.collect { event ->
            when (event) {
                PhotoEvent.SAVED -> showMessage(R.string.profile_photo_saved, null) {}
                PhotoEvent.NO_CAMERA_APP -> showMessage(R.string.camera_no_app, null) {}
            }
        }
    }

    if (showCameraRationale) {
        PermissionRationaleDialog(
            icon = Icons.Outlined.CameraAlt,
            title = stringResource(R.string.camera_rationale_title),
            message = stringResource(R.string.camera_rationale_message),
            onAllow = {
                showCameraRationale = false
                cameraPermission.launchSystemDialog()
            },
            onDismiss = { showCameraRationale = false },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                windowInsets = WindowInsets(0),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        val user = state.user
        if (state.isLoading || user == null) {
            Box(Modifier.padding(padding).fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileHeader(user, onTakePhoto = cameraPermission::request)

            SectionTitle(stringResource(R.string.profile_account))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoRow(Icons.Outlined.Email, user.email)
                    InfoRow(Icons.Outlined.Phone, user.phone)
                }
            }

            SectionTitle(stringResource(R.string.profile_favorites))
            if (state.favorites.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = stringResource(R.string.profile_favorites_empty_title),
                    message = stringResource(R.string.profile_favorites_empty_message),
                    actionLabel = stringResource(R.string.quote_go_catalog),
                    onAction = onGoToCatalog,
                )
            } else {
                state.favorites.forEach { product ->
                    ProductCard(product = product, onClick = { onProductClick(product.id) })
                }
            }

            SectionTitle(stringResource(R.string.profile_about))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MasterMartiniWordmark()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text(
                            stringResource(R.string.disclaimer),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { confirmLogout = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                Text(stringResource(R.string.profile_logout), Modifier.padding(start = 8.dp))
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            icon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
            title = { Text(stringResource(R.string.profile_logout_confirm_title)) },
            text = { Text(stringResource(R.string.profile_logout_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    viewModel.logout()
                }) { Text(stringResource(R.string.profile_logout)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

/** Camera photo (or the user's initials until there is one) with a button to take a new one, plus name. */
@Composable
private fun ProfileHeader(user: User, onTakePhoto: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            Crossfade(targetState = user.photoUri, label = "profilePhoto") { photoUri ->
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = stringResource(R.string.cd_profile_photo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Text(
                            text = user.initials(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            FilledIconButton(
                onClick = onTakePhoto,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp),
            ) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = stringResource(R.string.cd_take_profile_photo))
            }
        }
        Text(
            user.fullName,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun User.initials(): String =
    fullName.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
