package ru.igels.camerastream02.ui

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import ru.igels.camerastream02.R
import ru.igels.camerastream02.data.PermissionData
import ru.igels.camerastream02.databinding.FragmentRequestPermissionBinding
import ru.igels.camerastream02.domain.logger.iLog
import ru.igels.camerastream02.utilities.Popups


class RequestPermissionFragment() : Fragment(R.layout.fragment_request_permission) {
    val logTag = "PermissionFragment"
    private val permReqId: Int = 1234
    private var currentPermission = 0
    private lateinit var binding: FragmentRequestPermissionBinding

    var callback: (() -> Unit?)? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        iLog(logTag, "$permission")
//        if (isGranted) {
//            iLog(logTag, "Granted ")
//        } else {
//            iLog(logTag, "Denied ")
//        }
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        iLog(logTag, "Check permissions")
//        checkPermissions()
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iLog(logTag, "view created")
        requestPermissions()
    }
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentRequestPermissionBinding.inflate(layoutInflater)
//        return binding.root
//    }

//    @Suppress("DEPRECATION")
//    private fun checkPermissions() {
//        val startPerm = currentPermission
//        for (x in startPerm until PermissionData.appPermission.count()) {
//            iLog(logTag, "Check permission ${PermissionData.appPermission[x]}")
//            currentPermission++
//            if (!PermissionData.hasPermission(PermissionData.appPermission[x])) {
////                ActivityCompat.requestPermissions(this, arrayOf(PermissionData.appPermission[x]), permReqId)
//                requestPermissions(arrayOf(PermissionData.appPermission[x]), permReqId)
//                return
//            }
//        }
////        iLog(logTag, "Check if OREO")
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
////            && !android.provider.Settings.canDrawOverlays(appContext)) {
////            val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
////            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
////            appContext?.startActivity(intent)
//        PermissionData.checkAll()
////        if (callback == null) throw Throwable("No permissions callback")
//        callback?.let { it( ) }
//    }

    private fun requestPermissions() {
//        for (permission in PermissionData.appPermission) {
            requestPermissionLauncher.launch(PermissionData.appPermission)
//        }
    }


    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.isNotEmpty()) iLog(logTag, "Response permissions ${permissions.first()}")
        if (requestCode == permReqId && grantResults.isNotEmpty() && !grantResults.contains(-1)) {
//            processPermission(permissions.first())
        } else {
            Popups.showToast("Permissions not granted by the user.")
        }
//        checkPermissions()
    }
}