package ru.igels.camerastream02.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import ru.igels.camerastream02.appmessagebus.AppMessageBus
import ru.igels.camerastream02.domain.models.APPMSG_TYPE
import ru.igels.camerastream02.domain.models.AppMessage
import ru.igels.camerastream02.domain.models.AppPermissions

class PermissionData private constructor(var context: Context) {
    private val logTag = "PermissionData"
    private var permissions = AppPermissions()

    private val appPermission = arrayOf(
        Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: PermissionData? = null

        val storagePermission: Boolean
            get() = checkIsInit().permissions.storagePermission
        val cameraPermission: Boolean
            get() = checkIsInit().permissions.cameraPermission
        val locationPermission: Boolean
            get() = checkIsInit().permissions.locationPermission
        val permissionsForStart: Boolean
            get() = checkIsInit().permissions.cameraPermission && checkIsInit().permissions.storagePermission
        val allPermissions: AppPermissions
            get() = checkIsInit().permissions
        val appPermission: Array<String>
            get() = checkIsInit().appPermission

        private fun checkIsInit(): PermissionData {
            if (instance == null) throw Exception("init() PermissionController first!")
            return instance!!
        }

        fun init(context: Context): PermissionData {
            if (instance == null) instance = PermissionData(context)
            return instance!!
        }

        fun checkAll() = checkIsInit().checkAll()
    }

    init {
        checkAll()
    }

    fun checkAll() {
        val newPerm = AppPermissions(
            cameraPermission = hasPermission(Manifest.permission.CAMERA),
            microphonePermission = hasPermission(Manifest.permission.RECORD_AUDIO),
            storagePermission = hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            locationPermission = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        )
        if (permissions == newPerm) return
        permissions = newPerm
        AppMessageBus.publish(AppMessage(APPMSG_TYPE.PERMISSION_UPDATED, permissions))
    }

    private fun hasPermission(permission: String?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permission != null) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    protected fun finalize() {
        Log.i(logTag, "Finalize PermissionData")
    }
}