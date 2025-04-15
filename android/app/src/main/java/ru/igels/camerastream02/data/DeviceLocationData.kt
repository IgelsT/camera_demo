package ru.igels.camerastream02.data

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import ru.igels.camerastream02.appmessagebus.AppMessageBus
import ru.igels.camerastream02.domain.logger.eLog
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.domain.models.APPMSG_TYPE
import ru.igels.camerastream02.domain.models.AppMessage
import ru.igels.camerastream02.domain.models.DeviceLocationModel
import ru.igels.camerastream02.utilities.Utils


class DeviceLocationData private constructor(context: Context) : LocationListener {
    val logTag = "DeviceLocationData"
    private val locationManager: LocationManager
    private val locationHandler: Handler = Utils.getThread("DeviceLocationThread")
    private val GPSMINTIME = 30 * 1000L
    private val GPSMINDIST = 5f
    private var location = DeviceLocationModel()

    companion object {
        private var instance: DeviceLocationData? = null

        fun init(context: Context): DeviceLocationData {
            if (instance == null) instance = DeviceLocationData(context)
            return instance!!
        }


        private fun checkIsInit(): DeviceLocationData {
            if (instance == null) throw Exception("init() DeviceLocationData first!")
            return instance!!
        }

        fun getLocation(): Location? = checkIsInit().location.location
    }

    init {
        iLog(logTag, "init")
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestLocation()
    }

    private fun requestLocation() {
        val currentLocation = location.copy()
        currentLocation.permissions = PermissionData.locationPermission
        if (currentLocation.permissions) {
            try {
                currentLocation.gpsProviderEnabled =
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                currentLocation.networkProviderEnabled =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                currentLocation.location =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (currentLocation.location == null)
                    currentLocation.location =
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (currentLocation.location == null) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, GPSMINTIME, GPSMINDIST, this
                    )
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, GPSMINTIME, GPSMINDIST, this
                    )
                }
            } catch (e: SecurityException) {
                eLog(logTag, "no location permission")
            }
        }
        sendLocationToBus(currentLocation)
        locationHandler.postDelayed({ requestLocation() }, GPSMINTIME)
    }

    private fun sendLocationToBus(loc: DeviceLocationModel) {
        if (location != loc && location.location?.longitude != loc.location?.longitude
            && location.location?.latitude != loc.location?.latitude
        ) {
            AppMessageBus.publish(AppMessage(APPMSG_TYPE.LOCATION_UPDATED, location))
            location = loc
        }
    }

    override fun onLocationChanged(location: Location) {
//        iLog(logTag, "location $location")
        locationManager.removeUpdates(this)
        sendLocationToBus(this.location.copy(location = location))
    }

    override fun onProviderEnabled(provider: String) {
        iLog(logTag, "onProviderEnabled $provider")
    }

    override fun onProviderDisabled(provider: String) {
        iLog(logTag, "onProviderDisabled $provider")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        iLog(logTag, "status changed $provider $status $extras")
    }

    protected fun finalize() {
        Utils.closeThread(locationHandler)
        iLog(logTag, "Finalize class")
    }
}