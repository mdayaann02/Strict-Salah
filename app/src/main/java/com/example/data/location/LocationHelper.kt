package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class GpsLocationResult(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val isExactGps: Boolean
)

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Bulletproof GPS detection trying multiple complementary methods:
     * 1. Google Play Services FusedLocationProvider (High Accuracy Current Location)
     * 2. FusedLocationProvider Last Known Location
     * 3. Native Android LocationManager (GPS Provider)
     * 4. Native Android LocationManager (Network Provider)
     * Followed by robust asynchronous Geocoding with reliable fallbacks.
     */
    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocation(context: Context): GpsLocationResult? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext null
        }

        var acquiredLocation: Location? = null

        // 1. Attempt Fused Location Provider with 8s timeout
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            acquiredLocation = withTimeoutOrNull(8000) {
                suspendCancellableCoroutine { continuation ->
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { loc ->
                            continuation.resume(loc)
                        }
                        .addOnFailureListener {
                            continuation.resume(null)
                        }
                    continuation.invokeOnCancellation {
                        cts.cancel()
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to next method
        }

        // 2. If null, try Last Known Location from Fused
        if (acquiredLocation == null) {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                acquiredLocation = suspendCancellableCoroutine { cont ->
                    fusedClient.lastLocation
                        .addOnSuccessListener { loc -> cont.resume(loc) }
                        .addOnFailureListener { cont.resume(null) }
                }
            } catch (e: Exception) {
                // Fallback
            }
        }

        // 3. If still null, try Android Native LocationManager
        if (acquiredLocation == null) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (locationManager != null) {
                    val gpsLoc = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    } else null

                    val netLoc = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    } else null

                    acquiredLocation = gpsLoc ?: netLoc

                    // If no cached location, request single update via native API
                    if (acquiredLocation == null) {
                        acquiredLocation = withTimeoutOrNull(5000) {
                            suspendCancellableCoroutine { cont ->
                                val listener = object : LocationListener {
                                    override fun onLocationChanged(location: Location) {
                                        locationManager.removeUpdates(this)
                                        if (cont.isActive) cont.resume(location)
                                    }
                                    @Deprecated("Deprecated in Java")
                                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                                    override fun onProviderEnabled(provider: String) {}
                                    override fun onProviderDisabled(provider: String) {}
                                }
                                try {
                                    val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                        LocationManager.GPS_PROVIDER
                                    } else LocationManager.NETWORK_PROVIDER
                                    locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                                    cont.invokeOnCancellation {
                                        locationManager.removeUpdates(listener)
                                    }
                                } catch (e: Exception) {
                                    cont.resume(null)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        if (acquiredLocation == null) {
            return@withContext null
        }

        val lat = acquiredLocation.latitude
        val lng = acquiredLocation.longitude
        val cityName = reverseGeocode(context, lat, lng)

        return@withContext GpsLocationResult(
            cityName = cityName,
            latitude = lat,
            longitude = lng,
            isExactGps = true
        )
    }

    suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = suspendCancellableCoroutine<List<android.location.Address>?> { cont ->
                    geocoder.getFromLocation(lat, lng, 1) { addrs ->
                        cont.resume(addrs)
                    }
                }
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Detected Location"
                    val country = addr.countryName ?: ""
                    return@withContext if (country.isNotBlank()) "$locality, $country" else locality
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Detected Location"
                    val country = addr.countryName ?: ""
                    return@withContext if (country.isNotBlank()) "$locality, $country" else locality
                }
            }
        } catch (e: Exception) {
            // Geocoder unavailable (offline)
        }

        return@withContext String.format(Locale.US, "GPS (%.4f° N, %.4f° E)", lat, lng)
    }
}
