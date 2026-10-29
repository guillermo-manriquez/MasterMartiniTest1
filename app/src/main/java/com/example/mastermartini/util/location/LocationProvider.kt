package com.example.mastermartini.util.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.location.LocationManagerCompat
import com.example.mastermartini.util.permissions.hasPermission
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/** Outcome of turning the device position into a delivery address. */
sealed interface LocationLookup {
    /** [fromGeocoder] is false when only coordinates were available ("lat, lng"). */
    data class Found(
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val fromGeocoder: Boolean,
    ) : LocationLookup

    /** Location services are switched off in the device settings. */
    data object LocationDisabled : LocationLookup

    /** No permission at the moment of the lookup (e.g. revoked in the background). */
    data object NoPermission : LocationLookup

    /** No fix within the timeout, or the provider failed. */
    data object Unavailable : LocationLookup
}

interface LocationProvider {
    suspend fun currentAddress(): LocationLookup
}

/**
 * Fused location (Google Play services) + Geocoder. If the geocoder returns nothing
 * (common offline or on emulators), the coordinates are used as the address text so
 * the user can still edit it.
 */
class AndroidLocationProvider(private val context: Context) : LocationProvider {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(LocationManager::class.java)

    override suspend fun currentAddress(): LocationLookup {
        val hasFine = context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarse = context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (!hasFine && !hasCoarse) return LocationLookup.NoPermission
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) return LocationLookup.LocationDisabled

        // Approximate-only permission is accepted; it just gives a less precise fix
        val priority = if (hasFine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
        val location = withTimeoutOrNull(TIMEOUT_MS) { currentLocation(priority) } ?: return LocationLookup.Unavailable

        val street = reverseGeocode(location.latitude, location.longitude)
        return LocationLookup.Found(
            address = street ?: String.format(Locale.US, "%.5f, %.5f", location.latitude, location.longitude),
            latitude = location.latitude,
            longitude = location.longitude,
            fromGeocoder = street != null,
        )
    }

    @SuppressLint("MissingPermission") // checked in currentAddress()
    private suspend fun currentLocation(priority: Int): Location? = suspendCancellableCoroutine { continuation ->
        val cancellation = CancellationTokenSource()
        continuation.invokeOnCancellation { cancellation.cancel() }
        try {
            fusedClient.getCurrentLocation(priority, cancellation.token)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
        } catch (e: SecurityException) {
            continuation.resume(null)
        }
    }

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.forLanguageTag("es-CL"))
        val address = withTimeoutOrNull(TIMEOUT_MS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) = continuation.resume(addresses.firstOrNull())
                        override fun onError(errorMessage: String?) = continuation.resume(null)
                    })
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    runCatching { geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull() }.getOrNull()
                }
            }
        }
        return address?.toDeliveryText()
    }

    /** "Av. Providencia 1234, Providencia" when the parts exist, else the first address line. */
    private fun Address.toDeliveryText(): String? {
        val street = listOfNotNull(thoroughfare, subThoroughfare).joinToString(" ").ifBlank { null }
        return when {
            street != null && locality != null -> "$street, $locality"
            street != null -> street
            else -> getAddressLine(0)?.takeIf { it.isNotBlank() }
        }
    }

    private companion object {
        const val TIMEOUT_MS = 10_000L
    }
}
