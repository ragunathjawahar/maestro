package dev.mobile.maestro

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock

class MaestroLocationProvider(context: Context) {
    companion object {
        private const val PROVIDER_NAME = "maestro-location-provider"
    }

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun setLocation(latitude: Double, longitude: Double) {
        register()
        setMockLocation(latitude, longitude)
        unregister()
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
        }

        locationManager.setTestProviderEnabled(PROVIDER_NAME, true)
    }

    private fun setMockLocation(latitude: Double, longitude: Double) {
        val location = Location(PROVIDER_NAME).apply {
            this.latitude = latitude
            this.longitude = longitude
            this.time = System.currentTimeMillis()
            this.accuracy = 1f
            this.altitude = 224.0
            this.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }
        locationManager.setTestProviderLocation(PROVIDER_NAME, location)
    }

    private fun unregister() {
        locationManager.removeTestProvider(PROVIDER_NAME)
    }
}
