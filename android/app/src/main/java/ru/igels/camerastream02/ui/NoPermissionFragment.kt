package ru.igels.camerastream02.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import ru.igels.camerastream02.R
import ru.igels.camerastream02.data.PermissionData
import ru.igels.camerastream02.databinding.FragmentNoPermissionBinding

class NoPermissionFragment : Fragment(R.layout.fragment_no_permission) {
    private lateinit var binding: FragmentNoPermissionBinding
    private val logTag = "NoPermissionFragment"
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        PermissionData.checkAll()
    }

    private val requestSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        PermissionData.checkAll()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoPermissionBinding.inflate(layoutInflater)
        requestPermissionLauncher.launch(PermissionData.appPermission)
        binding.btRequest.setOnClickListener {
            requestPermissionLauncher.launch(PermissionData.appPermission)
        }

        binding.btOpenSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context?.packageName, null)
            intent.data = uri
            requestSettingsLauncher.launch(intent)
        }
        return binding.root
    }
}