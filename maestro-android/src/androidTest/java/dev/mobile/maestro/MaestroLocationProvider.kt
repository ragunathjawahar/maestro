package dev.mobile.maestro

import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.LocationProvider.AVAILABLE
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log

class MaestroLocationProvider(private val context: Context) {
    companion object {
        private const val TAG = "Maestro"
        private const val PROVIDER_NAME = "maestro-driver-service"
    }

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun setLocation(latitude: Double, longitude: Double) {
        register()
        setMockLocation(latitude, longitude)
        //unregister()
    }

    private fun register() {
        val maestroLocationProvider = locationManager.allProviders.find { it == PROVIDER_NAME }
        if (maestroLocationProvider == null) {
            locationManager.addTestProvider(
                /* provider = */ PROVIDER_NAME,
                /* requiresNetwork = */ false,
                /* requiresSatellite = */ false,
                /* requiresCell = */ false,
                /* hasMonetaryCost = */ false,
                /* supportsAltitude = */ false,
                /* supportsSpeed = */ false,
                /* supportsBearing = */ false,
                /* powerUsage = */ Criteria.POWER_LOW,
                /* accuracy = */ Criteria.ACCURACY_FINE,
            )
        } else {
            locationManager.removeTestProvider(PROVIDER_NAME)
        }

        locationManager.setTestProviderEnabled(PROVIDER_NAME, true)
    }

    private fun setMockLocation(latitude: Double, longitude: Double) {
        val handler = Handler(Looper.getMainLooper())

        val fineLocationPermission = context.checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = context.checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        val mockLocationPermission = context.checkCallingOrSelfPermission("ACCESS_MOCK_LOCATION")

        fun postLocation() {
            handler.post {
                val location = getLocation(latitude, longitude)
                locationManager.setTestProviderStatus(
                    /* provider = */ PROVIDER_NAME,
                    /* status = */ AVAILABLE,
                    /* extras = */ null,
                    /* updateTime = */ System.currentTimeMillis()
                )
                locationManager.setTestProviderLocation(PROVIDER_NAME, location)
                Log.d(TAG, "Updating location to ($latitude, $longitude)")

                Log.d(TAG, "fineLocationPermission: ${fineLocationPermission == PackageManager.PERMISSION_GRANTED}")
                Log.d(TAG, "coarseLocationPermission: ${coarseLocationPermission == PackageManager.PERMISSION_GRANTED}")
                Log.d(TAG, "mockLocationPermission: ${mockLocationPermission == PackageManager.PERMISSION_GRANTED}")

                postLocation()
            }
        }

        postLocation()
    }

    private fun getLocation(latitude: Double, longitude: Double): Location {
        return Location(PROVIDER_NAME).apply {
            this.latitude = latitude
            this.longitude = longitude
            this.time = System.currentTimeMillis()
            this.accuracy = 1f
            this.altitude = 224.0
        }.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                this.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            }
        }
    }

    private fun unregister() {
        locationManager.removeTestProvider(PROVIDER_NAME)
    }
}
