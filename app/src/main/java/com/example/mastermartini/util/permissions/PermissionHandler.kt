package com.example.mastermartini.util.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/** What the screen should do after asking for a permission. */
enum class PermissionResult {
    /** Use the feature. */
    GRANTED,

    /** The user said no but can still be asked: explain why and offer to retry. */
    SHOW_RATIONALE,

    /** Android will not show the dialog again: only the app settings can enable it. */
    PERMANENTLY_DENIED,
}

/**
 * Shared runtime-permission flow for location (T14) and camera (T17).
 *
 * [request] answers right away if the permission is already granted or a rationale should be
 * shown first; otherwise it opens the system dialog. [launchSystemDialog] skips the rationale
 * check (used by the rationale dialog's "Reintentar" button).
 */
class PermissionRequester internal constructor(
    private val check: () -> PermissionResult?,
    private val launch: () -> Unit,
    private val deliver: (PermissionResult) -> Unit,
) {
    fun request() {
        val immediate = check()
        if (immediate != null) deliver(immediate) else launch()
    }

    fun launchSystemDialog() = launch()
}

/**
 * @param permissions requested together.
 * @param grantedWhen decides whether the result counts as granted; by default any one of
 *   them is enough (e.g. approximate location without precise location).
 */
@Composable
fun rememberPermissionRequester(
    permissions: List<String>,
    grantedWhen: (Map<String, Boolean>) -> Boolean = { results -> results.values.any { it } },
    onResult: (PermissionResult) -> Unit,
): PermissionRequester {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnResult by rememberUpdatedState(onResult)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        currentOnResult(
            when {
                grantedWhen(results) -> PermissionResult.GRANTED
                activity.shouldShowRationale(permissions) -> PermissionResult.SHOW_RATIONALE
                // Denied and Android won't ask again (second denial, or "Don't allow" remembered)
                else -> PermissionResult.PERMANENTLY_DENIED
            }
        )
    }

    return remember(launcher, permissions) {
        PermissionRequester(
            check = {
                val current = permissions.associateWith { context.hasPermission(it) }
                when {
                    grantedWhen(current) -> PermissionResult.GRANTED
                    activity.shouldShowRationale(permissions) -> PermissionResult.SHOW_RATIONALE
                    else -> null
                }
            },
            launch = { launcher.launch(permissions.toTypedArray()) },
            deliver = { currentOnResult(it) },
        )
    }
}

fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun Activity?.shouldShowRationale(permissions: List<String>): Boolean =
    this != null && permissions.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it) }

/** Opens this app's page in the system settings, where a permanently denied permission can be enabled. */
fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
